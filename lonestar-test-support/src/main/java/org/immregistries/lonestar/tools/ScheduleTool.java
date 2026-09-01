package org.immregistries.lonestar.tools;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class ScheduleTool {
    private static final Set<String> TERMINAL_TARGETS = new HashSet<String>(
            Arrays.asList("COMPLETE", "INVALID", "CONTRA", "FINISHED"));
    private static final String DEFAULT_SCHEDULE_NAME = "TCH-2018-08-24";

    private ScheduleTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !Arrays.asList("split", "validate", "assemble", "render", "generate")
                .contains(args[0])) {
            throw new IllegalArgumentException("Usage: ScheduleTool <split|validate|assemble|render|generate>");
        }

        PathsConfig paths = PathsConfig.fromWorkingDirectory();
        String action = args[0];
        if ("split".equals(action)) {
            split(paths.runtimeSchedule, paths.seriesDirectory);
            return;
        }

        List<SeriesDocument> series = loadAndValidate(paths.seriesDirectory);
        if ("validate".equals(action)) {
            System.out.println("Validated " + series.size() + " schedule series.");
            return;
        }
        if ("assemble".equals(action) || "generate".equals(action)) {
            assemble(series, paths.runtimeSchedule);
        }
        if ("render".equals(action) || "generate".equals(action)) {
            render(series, paths.webScheduleDirectory);
        }
    }

    private static void split(Path runtimeSchedule, Path seriesDirectory) throws Exception {
        Document document = parse(runtimeSchedule);
        Element root = document.getDocumentElement();
        require("schedule".equals(root.getTagName()), "Expected <schedule> root in " + runtimeSchedule);
        Files.createDirectories(seriesDirectory);
        int index = 0;
        for (Element forecast : children(root, "forecast")) {
            index++;
            Document seriesDocument = newDocument();
            seriesDocument.appendChild(seriesDocument.importNode(forecast, true));
            String fileName = String.format("%02d-%s.xml", index, fileStem(attribute(forecast, "seriesName")));
            writeXml(seriesDocument, seriesDirectory.resolve(fileName));
        }
        System.out.println("Split " + index + " schedule series into " + seriesDirectory);
    }

    private static List<SeriesDocument> loadAndValidate(Path seriesDirectory) throws Exception {
        require(Files.isDirectory(seriesDirectory), "Missing schedule source directory " + seriesDirectory);
        List<Path> paths = new ArrayList<Path>();
        try (java.util.stream.Stream<Path> files = Files.list(seriesDirectory)) {
            files.filter(path -> path.getFileName().toString().endsWith(".xml")).forEach(paths::add);
        }
        Collections.sort(paths);
        require(!paths.isEmpty(), "No per-series schedule XML files found in " + seriesDirectory);

        List<SeriesDocument> series = new ArrayList<SeriesDocument>();
        Set<String> seriesNames = new HashSet<String>();
        for (Path path : paths) {
            Document document = parse(path);
            Element forecast = document.getDocumentElement();
            require("forecast".equals(forecast.getTagName()), "Expected <forecast> root in " + path);
            String seriesName = attribute(forecast, "seriesName");
            require(!seriesName.isEmpty(), "Missing forecast seriesName in " + path);
            require(seriesNames.add(seriesName), "Duplicate forecast seriesName " + seriesName);
            validateSeries(forecast, path);
            series.add(new SeriesDocument(path, document, forecast));
        }
        return series;
    }

    private static void validateSeries(Element forecast, Path path) {
        Set<String> vaccineNames = new HashSet<String>();
        for (Element vaccine : children(forecast, "vaccine")) {
            String vaccineName = attribute(vaccine, "vaccineName");
            require(!vaccineName.isEmpty(), "Missing vaccineName in " + path);
            require(vaccineNames.add(vaccineName.toUpperCase(Locale.ROOT)),
                    "Duplicate vaccine " + vaccineName + " in " + path);
            require(!attribute(vaccine, "vaccineIds").isEmpty(),
                    "Missing vaccineIds for " + vaccineName + " in " + path);
        }

        Set<String> targets = new HashSet<String>();
        Set<String> positions = new HashSet<String>();
        for (Element schedule : children(forecast, "schedule")) {
            String scheduleName = attribute(schedule, "scheduleName");
            require(!scheduleName.isEmpty(), "Missing scheduleName in " + path);
            require(targets.add(scheduleName), "Duplicate schedule " + scheduleName + " in " + path);
            List<Element> positionsForSchedule = children(schedule, "pos");
            if (positionsForSchedule.size() != 1) {
                System.err.println("Warning: Schedule " + scheduleName + " must have exactly one <pos> in "
                        + path);
                continue;
            }
            Element position = positionsForSchedule.get(0);
            String row = attribute(position, "row");
            String column = attribute(position, "column");
            if (row.isEmpty() || column.isEmpty()) {
                System.err.println("Warning: Schedule " + scheduleName + " has an incomplete <pos> in "
                        + path);
                continue;
            }
            if (!positions.add(row + ":" + column)) {
                System.err.println("Warning: Duplicate schedule position " + row + ":" + column
                        + " in " + path);
            }
        }
        for (Element decisionLogic : children(forecast, "decisionLogic")) {
            String name = attribute(decisionLogic, "name");
            require(!name.isEmpty(), "Unnamed decisionLogic in " + path);
            targets.add(name);
        }

        validateVaccineReferences(forecast, vaccineNames, path);
        for (Element indicate : descendants(forecast, "indicate")) {
            String target = attribute(indicate, "schedule");
            require(TERMINAL_TARGETS.contains(target.toUpperCase(Locale.ROOT)) || targets.contains(target),
                    "Unknown indicate target " + target + " in " + path);
        }
    }

    private static void validateVaccineReferences(Element forecast, Set<String> vaccineNames,
            Path path) {
        String[] referenceAttributes = { "vaccineName", "previousVaccineName", "historyOfVaccineName",
                "hasHad", "ifGiven", "against" };
        for (String referenceAttribute : referenceAttributes) {
            NodeList nodes = forecast.getElementsByTagName("*");
            for (int index = 0; index < nodes.getLength(); index++) {
                Element node = (Element) nodes.item(index);
                String reference = attribute(node, referenceAttribute);
                if (!reference.isEmpty()) {
                    require(vaccineNames.contains(reference.toUpperCase(Locale.ROOT)),
                            "Unknown vaccine " + reference + " in " + path);
                }
            }
        }
    }

    private static void assemble(List<SeriesDocument> series, Path runtimeSchedule) throws Exception {
        String scheduleName = DEFAULT_SCHEDULE_NAME;
        if (Files.exists(runtimeSchedule)) {
            scheduleName = attribute(parse(runtimeSchedule).getDocumentElement(), "scheduleName");
        }
        Document assembled = newDocument();
        Element root = assembled.createElement("schedule");
        root.setAttribute("scheduleName", scheduleName);
        assembled.appendChild(root);
        for (SeriesDocument source : series) {
            root.appendChild(assembled.importNode(source.forecast, true));
        }
        writeXml(assembled, runtimeSchedule);
        System.out.println("Assembled " + series.size() + " schedule series into " + runtimeSchedule);
    }

    private static void render(List<SeriesDocument> series, Path webScheduleDirectory) throws IOException {
        Files.createDirectories(webScheduleDirectory);
        for (SeriesDocument source : series) {
            String stem = fileStem(attribute(source.forecast, "seriesName"));
            Files.write(webScheduleDirectory.resolve(stem + ".svg"), renderSvg(source.forecast)
                    .getBytes(StandardCharsets.UTF_8));
            Files.write(webScheduleDirectory.resolve(stem + ".html"), renderHtml(source.forecast, stem)
                    .getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("Rendered " + series.size() + " schedule views into " + webScheduleDirectory);
    }

    private static String renderHtml(Element forecast, String stem) {
        String seriesName = attribute(forecast, "seriesName");
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\"><head><meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>").append(escape(seriesName)).append(" schedule</title>\n");
        html.append(
                "<style>body{font-family:system-ui,sans-serif;margin:2rem;max-width:1100px}table{border-collapse:collapse;width:100%;margin:1rem 0}th,td{border:1px solid #bbb;padding:.45rem;text-align:left;vertical-align:top}th{background:#e8f0ef}img{max-width:100%;border:1px solid #bbb}code{white-space:pre-wrap}</style></head><body>\n");
        html.append("<h1>").append(escape(seriesName)).append(" schedule</h1>\n");
        html.append("<img src=\"").append(stem).append(".svg\" alt=\"").append(escape(seriesName))
                .append(" transition graph\">\n");
        html.append("<h2>Vaccine groups</h2><table><thead><tr><th>Name</th><th>CVX IDs</th></tr></thead><tbody>\n");
        for (Element vaccine : children(forecast, "vaccine")) {
            html.append("<tr><td>").append(escape(attribute(vaccine, "vaccineName"))).append("</td><td>")
                    .append(escape(attribute(vaccine, "vaccineIds"))).append("</td></tr>\n");
        }
        html.append(
                "</tbody></table><h2>Schedule nodes</h2><table><thead><tr><th>Node</th><th>Dose</th><th>Label</th><th>Timing</th><th>Transitions</th></tr></thead><tbody>\n");
        for (Element schedule : children(forecast, "schedule")) {
            html.append("<tr><td>").append(escape(attribute(schedule, "scheduleName"))).append("</td><td>")
                    .append(escape(attribute(schedule, "dose"))).append("</td><td>")
                    .append(escape(attribute(schedule, "label"))).append("</td><td>")
                    .append(escape(timing(schedule))).append("</td><td>")
                    .append(escape(transitions(schedule))).append("</td></tr>\n");
        }
        html.append("</tbody></table>");
        List<Element> decisionLogic = children(forecast, "decisionLogic");
        if (!decisionLogic.isEmpty()) {
            html.append("<h2>Decision logic</h2><ul>");
            for (Element logic : decisionLogic) {
                html.append("<li>").append(escape(attribute(logic, "name"))).append("</li>");
            }
            html.append("</ul>");
        }
        html.append("</body></html>\n");
        return html.toString();
    }

    private static String renderSvg(Element forecast) {
        List<Element> schedules = children(forecast, "schedule");
        Map<String, Element> byName = new HashMap<String, Element>();
        int maxRow = 1;
        int maxColumn = 1;
        for (Element schedule : schedules) {
            byName.put(attribute(schedule, "scheduleName"), schedule);
            maxRow = Math.max(maxRow, positionValue(schedule, "row", fallbackIndex(schedule)));
            maxColumn = Math.max(maxColumn, positionValue(schedule, "column", 1));
        }
        int width = 80 + maxColumn * 220;
        int height = 90 + maxRow * 130;
        StringBuilder svg = new StringBuilder();
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(width)
                .append("\" height=\"").append(height).append("\" viewBox=\"0 0 ").append(width)
                .append(' ').append(height)
                .append("\"><defs><marker id=\"arrow\" markerWidth=\"8\" markerHeight=\"8\" refX=\"7\" refY=\"4\" orient=\"auto\"><path d=\"M0,0 L8,4 L0,8 z\" fill=\"#41635d\"/></marker></defs>");
        svg.append(
                "<rect width=\"100%\" height=\"100%\" fill=\"#f7faf9\"/><text x=\"20\" y=\"30\" font-family=\"sans-serif\" font-size=\"20\" fill=\"#173d37\">")
                .append(escape(attribute(forecast, "seriesName"))).append(" transitions</text>");
        for (Element schedule : schedules) {
            for (Element indicate : children(schedule, "indicate")) {
                Element target = byName.get(attribute(indicate, "schedule"));
                if (target != null) {
                    int[] from = center(schedule);
                    int[] to = center(target);
                    svg.append("<line x1=\"").append(from[0]).append("\" y1=\"").append(from[1])
                            .append("\" x2=\"").append(to[0]).append("\" y2=\"").append(to[1])
                            .append("\" stroke=\"#41635d\" stroke-width=\"2\" marker-end=\"url(#arrow)\"/>");
                }
            }
        }
        for (Element schedule : schedules) {
            int[] center = center(schedule);
            svg.append("<rect x=\"").append(center[0] - 85).append("\" y=\"").append(center[1] - 25)
                    .append("\" width=\"170\" height=\"50\" rx=\"4\" fill=\"#dceeea\" stroke=\"#41635d\"/>");
            svg.append("<text x=\"").append(center[0]).append("\" y=\"").append(center[1] - 3)
                    .append("\" text-anchor=\"middle\" font-family=\"sans-serif\" font-size=\"14\" fill=\"#173d37\">")
                    .append(escape(attribute(schedule, "scheduleName"))).append("</text>");
            svg.append("<text x=\"").append(center[0]).append("\" y=\"").append(center[1] + 15)
                    .append("\" text-anchor=\"middle\" font-family=\"sans-serif\" font-size=\"12\" fill=\"#173d37\">")
                    .append(escape(attribute(schedule, "label"))).append("</text>");
        }
        return svg.append("</svg>\n").toString();
    }

    private static int[] center(Element schedule) {
        return new int[] { 120 + (positionValue(schedule, "column", 1) - 1) * 220,
                80 + (positionValue(schedule, "row", fallbackIndex(schedule)) - 1) * 130 };
    }

    private static int positionValue(Element schedule, String attributeName, int fallback) {
        List<Element> positions = children(schedule, "pos");
        if (positions.size() != 1) {
            return fallback;
        }
        String value = attribute(positions.get(0), attributeName);
        return value.isEmpty() ? fallback : Integer.parseInt(value);
    }

    private static int fallbackIndex(Element schedule) {
        NodeList nodes = schedule.getParentNode().getChildNodes();
        int index = 0;
        for (int nodeIndex = 0; nodeIndex < nodes.getLength(); nodeIndex++) {
            Node node = nodes.item(nodeIndex);
            if (node.getNodeType() == Node.ELEMENT_NODE && "schedule".equals(node.getNodeName())) {
                index++;
                if (node == schedule) {
                    return index;
                }
            }
        }
        return 1;
    }

    private static String timing(Element schedule) {
        List<String> values = new ArrayList<String>();
        for (String name : Arrays.asList("valid", "early", "due", "overdue", "finished")) {
            List<Element> nodes = children(schedule, name);
            if (!nodes.isEmpty()) {
                Element node = nodes.get(0);
                values.add(name + "=" + attribute(node, "age") + "; " + attribute(node, "interval"));
            }
        }
        return join(values, " | ");
    }

    private static String transitions(Element schedule) {
        List<String> values = new ArrayList<String>();
        for (Element indicate : children(schedule, "indicate")) {
            values.add(attribute(indicate, "vaccineName") + " -> " + attribute(indicate, "schedule"));
        }
        return join(values, " | ");
    }

    private static List<Element> children(Element parent, String tagName) {
        List<Element> elements = new ArrayList<Element>();
        NodeList nodes = parent.getChildNodes();
        for (int index = 0; index < nodes.getLength(); index++) {
            Node node = nodes.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE && tagName.equals(node.getNodeName())) {
                elements.add((Element) node);
            }
        }
        return elements;
    }

    private static List<Element> descendants(Element parent, String tagName) {
        List<Element> elements = new ArrayList<Element>();
        NodeList nodes = parent.getElementsByTagName(tagName);
        for (int index = 0; index < nodes.getLength(); index++) {
            elements.add((Element) nodes.item(index));
        }
        return elements;
    }

    private static Document parse(Path path) throws Exception {
        return builder().parse(path.toFile());
    }

    private static Document newDocument() throws ParserConfigurationException {
        return builder().newDocument();
    }

    private static DocumentBuilder builder() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory.newDocumentBuilder();
    }

    private static void writeXml(Document document, Path path) throws IOException, TransformerException {
        Files.createDirectories(path.getParent());
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        Files.write(path, writer.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String attribute(Element element, String name) {
        return element.hasAttribute(name) ? element.getAttribute(name).trim() : "";
    }

    private static String fileStem(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private static String join(List<String> values, String separator) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append(separator);
            }
            result.append(value);
        }
        return result.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static final class SeriesDocument {
        private final Path path;
        private final Document document;
        private final Element forecast;

        private SeriesDocument(Path path, Document document, Element forecast) {
            this.path = path;
            this.document = document;
            this.forecast = forecast;
        }
    }

    private static final class PathsConfig {
        private final Path runtimeSchedule;
        private final Path seriesDirectory;
        private final Path webScheduleDirectory;

        private PathsConfig(Path root) {
            runtimeSchedule = root.resolve("lonestar-core/src/main/resources/ForecastSchedule.xml");
            seriesDirectory = root.resolve("lonestar-core/src/main/schedules");
            webScheduleDirectory = root.resolve("lonestar-web/src/main/webapp/schedules");
        }

        private static PathsConfig fromWorkingDirectory() {
            Path directory = Paths.get("").toAbsolutePath();
            if (!Files.isDirectory(directory.resolve("lonestar-core"))) {
                directory = directory.getParent();
            }
            require(directory != null && Files.isDirectory(directory.resolve("lonestar-core")),
                    "Run the schedule tool from the repository root or a direct module directory");
            return new PathsConfig(directory);
        }
    }
}