package org.immregistries.lonestar.cdsi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Produces reviewable CDSi release suggestions; it never changes LoneStar XML.
 */
public final class CdsiSuggestionTool {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private CdsiSuggestionTool() {
    }

    public static void main(String[] args) throws Exception {
        Arguments arguments = Arguments.parse(args);
        Path root = repositoryRoot();
        Path diff = arguments.reference
                .resolve("supporting-data/diffs/" + arguments.from + "-to-" + arguments.to + ".json");
        require(Files.isRegularFile(diff), "Missing CDSi diff " + diff);
        require(Files
                .isDirectory(arguments.reference.resolve("supporting-data/versions/" + arguments.to + "/normalized")),
                "Missing normalized CDSi " + arguments.to + " data");

        Map<String, Mapping> mappings = loadMappings();
        JsonObject diffDocument = JsonParser.parseString(new String(Files.readAllBytes(diff), StandardCharsets.UTF_8))
                .getAsJsonObject();
        List<Suggestion> suggestions = new ArrayList<Suggestion>();
        for (JsonElement element : diffDocument.getAsJsonArray("changes")) {
            suggestions.add(classify(element.getAsJsonObject(), mappings, root));
        }
        Collections.sort(suggestions);
        Report report = new Report(arguments.from, arguments.to, suggestions);
        Path outputDirectory = root.resolve("target/cdsi-suggestions");
        Files.createDirectories(outputDirectory);
        Files.write(outputDirectory.resolve(arguments.from + "-to-" + arguments.to + ".json"),
                GSON.toJson(report).getBytes(StandardCharsets.UTF_8));
        Files.write(outputDirectory.resolve(arguments.from + "-to-" + arguments.to + ".md"),
                report.markdown().getBytes(StandardCharsets.UTF_8));
        System.out.println("Wrote " + suggestions.size() + " suggestions to " + outputDirectory);
    }

    private static Suggestion classify(JsonObject change, Map<String, Mapping> mappings, Path root) throws Exception {
        JsonObject identifier = change.getAsJsonObject("identifier");
        String antigen = value(identifier, "antigen");
        String series = value(identifier, "series");
        String dose = value(identifier, "dose");
        String category = value(change, "category");
        String field = value(change, "field");
        Mapping mapping = mappings.get(key(antigen, series));
        String kind = "unmapped";
        String reason = "No checked-in LoneStar mapping exists for this CDSi series.";
        String loneStarPath = null;
        String currentValue = null;
        String suggestedValue = compact(change.get("new_value"));

        if (mapping != null) {
            String scheduleName = mapping.doses.get(dose);
            if (isDirect(category, field) && scheduleName != null) {
                String attribute = loneStarAttribute(category, field);
                String element = "maxAge".equals(field) ? "finished" : "valid";
                try {
                    LoneStarValue current = loneStarValue(root, mapping.file, scheduleName, element, attribute);
                    kind = "direct";
                    reason = "Mapped standard-series " + category + " change with one LoneStar timing attribute.";
                    loneStarPath = "lonestar-core/src/main/schedules/" + mapping.file + "#"
                            + scheduleName + "/" + current.element + "/@" + attribute;
                    currentValue = current.value;
                    suggestedValue = scalar(change.get("new_value"));
                } catch (IllegalArgumentException exception) {
                    kind = "review-required";
                    reason = "Mapped CDSi dose has no matching LoneStar node: " + exception.getMessage();
                }
            } else {
                kind = "review-required";
                reason = scheduleName == null ? "Mapped series has no unambiguous LoneStar dose path."
                        : "Mapped series uses a CDSi feature outside the initial age/previous-dose interval scope.";
            }
        }
        return new Suggestion(kind, antigen, series, dose, category, field, loneStarPath, currentValue,
                suggestedValue, reason, value(change, "change_type"));
    }

    private static boolean isDirect(String category, String field) {
        return ("age".equals(category) && ("absMinAge".equals(field) || "minAge".equals(field)
                || "earliestRecAge".equals(field) || "maxAge".equals(field)))
                || ("interval".equals(category) && ("absMinInt".equals(field) || "minInt".equals(field)
                        || "earliestRecInt".equals(field)));
    }

    private static String loneStarAttribute(String category, String field) {
        if ("maxAge".equals(field))
            return "age";
        return "interval".equals(category) ? "interval" : "age";
    }

    private static LoneStarValue loneStarValue(Path root, String file, String scheduleName, String element,
            String attribute)
            throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(root.resolve("lonestar-core/src/main/schedules").resolve(file).toFile());
        NodeList schedules = document.getDocumentElement().getElementsByTagName("schedule");
        for (int index = 0; index < schedules.getLength(); index++) {
            Element schedule = (Element) schedules.item(index);
            if (scheduleName.equals(schedule.getAttribute("scheduleName"))) {
                NodeList elements = schedule.getElementsByTagName(element);
                if (elements.getLength() == 0)
                    throw new IllegalArgumentException("Missing " + element + " in " + file);
                return new LoneStarValue(element, ((Element) elements.item(0)).getAttribute(attribute));
            }
        }
        throw new IllegalArgumentException("Missing schedule " + scheduleName + " in " + file);
    }

    private static Map<String, Mapping> loadMappings() throws Exception {
        java.io.InputStream input = CdsiSuggestionTool.class.getClassLoader()
                .getResourceAsStream("cdsi/lonestar-cdsi-map.json");
        require(input != null, "Missing cdsi/lonestar-cdsi-map.json");
        JsonArray values = JsonParser.parseString(new String(readAll(input), StandardCharsets.UTF_8))
                .getAsJsonObject().getAsJsonArray("mappings");
        Map<String, Mapping> mappings = new HashMap<String, Mapping>();
        for (JsonElement element : values) {
            JsonObject value = element.getAsJsonObject();
            Mapping mapping = new Mapping(value(value, "scheduleFile"));
            for (Map.Entry<String, JsonElement> dose : value.getAsJsonObject("doses").entrySet()) {
                mapping.doses.put(dose.getKey(), dose.getValue().getAsString());
            }
            mappings.put(key(value(value, "cdsiAntigen"), value(value, "cdsiSeries")), mapping);
        }
        return mappings;
    }

    private static byte[] readAll(java.io.InputStream input) throws java.io.IOException {
        try {
            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } finally {
            input.close();
        }
    }

    private static Path repositoryRoot() {
        Path root = Paths.get("").toAbsolutePath();
        if (!Files.isDirectory(root.resolve("lonestar-core")))
            root = root.getParent();
        require(root != null && Files.isDirectory(root.resolve("lonestar-core")),
                "Run from the repository root or module");
        return root;
    }

    private static String key(String antigen, String series) {
        return antigen + "\n" + series;
    }

    private static String value(JsonObject object, String name) {
        JsonElement value = object.get(name);
        return value == null || value.isJsonNull() ? "" : value.getAsString();
    }

    private static String scalar(JsonElement value) {
        return value == null || value.isJsonNull() || value.isJsonObject() || value.isJsonArray() ? compact(value)
                : value.getAsString();
    }

    private static String compact(JsonElement value) {
        return value == null || value.isJsonNull() ? "" : value.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition)
            throw new IllegalArgumentException(message);
    }

    private static final class Arguments {
        private Path reference;
        private String from;
        private String to;

        static Arguments parse(String[] args) {
            Arguments result = new Arguments();
            for (int index = 0; index < args.length; index += 2) {
                if (index + 1 >= args.length)
                    throw new IllegalArgumentException("Arguments require values");
                if ("--cdsi-reference".equals(args[index]))
                    result.reference = Paths.get(args[index + 1]);
                else if ("--from".equals(args[index]))
                    result.from = args[index + 1];
                else if ("--to".equals(args[index]))
                    result.to = args[index + 1];
                else
                    throw new IllegalArgumentException("Unknown argument " + args[index]);
            }
            require(result.reference != null && result.from != null && result.to != null,
                    "Usage: --cdsi-reference <path> --from <version> --to <version>");
            return result;
        }
    }

    private static final class Mapping {
        private final String file;
        private final Map<String, String> doses = new HashMap<String, String>();

        Mapping(String file) {
            this.file = file;
        }
    }

    private static final class LoneStarValue {
        private final String element;
        private final String value;

        LoneStarValue(String element, String value) {
            this.element = element;
            this.value = value;
        }
    }

    private static final class Suggestion implements Comparable<Suggestion> {
        private final String classification, antigen, series, dose, category, field, loneStarPath, currentValue,
                suggestedValue, reason, changeType;

        Suggestion(String classification, String antigen, String series, String dose, String category, String field,
                String loneStarPath, String currentValue, String suggestedValue, String reason, String changeType) {
            this.classification = classification;
            this.antigen = antigen;
            this.series = series;
            this.dose = dose;
            this.category = category;
            this.field = field;
            this.loneStarPath = loneStarPath;
            this.currentValue = currentValue;
            this.suggestedValue = suggestedValue;
            this.reason = reason;
            this.changeType = changeType;
        }

        public int compareTo(Suggestion other) {
            return classification.compareTo(other.classification);
        }
    }

    private static final class Report {
        private final String from, to;
        private final List<Suggestion> suggestions;

        Report(String from, String to, List<Suggestion> suggestions) {
            this.from = from;
            this.to = to;
            this.suggestions = suggestions;
        }

        String markdown() {
            StringBuilder result = new StringBuilder("# CDSi-to-LoneStar Suggestions " + from + " to " + to + "\n\n");
            for (String classification : new String[] { "direct", "review-required", "unmapped" }) {
                result.append("## ").append(classification).append("\n\n");
                for (Suggestion item : suggestions)
                    if (classification.equals(item.classification))
                        result.append("- **")
                                .append(item.antigen).append("**: ").append(item.category).append("/")
                                .append(item.field)
                                .append(" - ").append(item.reason)
                                .append(item.loneStarPath == null ? ""
                                        : " `" + item.loneStarPath
                                                + "` current=`" + item.currentValue + "` suggested=`"
                                                + item.suggestedValue + "`")
                                .append("\n");
                result.append("\n");
            }
            return result.toString();
        }
    }
}