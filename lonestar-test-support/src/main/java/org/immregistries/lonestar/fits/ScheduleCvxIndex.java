package org.immregistries.lonestar.fits;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Offline CVX and forecast-series index derived from the bundled schedule. */
final class ScheduleCvxIndex {
    private static ScheduleCvxIndex instance;

    private final Map<String, Integer> vaccineIds = new HashMap<String, Integer>();
    private final Map<String, List<String>> forecastNames = new HashMap<String, List<String>>();

    static synchronized ScheduleCvxIndex getInstance() throws Exception {
        if (instance == null) {
            instance = new ScheduleCvxIndex();
        }
        return instance;
    }

    private ScheduleCvxIndex() throws Exception {
        InputStream input = ScheduleCvxIndex.class.getClassLoader().getResourceAsStream("ForecastSchedule.xml");
        if (input == null) {
            throw new IllegalStateException("ForecastSchedule.xml is not available on the classpath");
        }
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
        NodeList forecasts = document.getDocumentElement().getChildNodes();
        for (int index = 0; index < forecasts.getLength(); index++) {
            Node node = forecasts.item(index);
            if (node.getNodeType() != Node.ELEMENT_NODE || !"forecast".equals(node.getNodeName())) {
                continue;
            }
            Element forecast = (Element) node;
            String forecastName = forecast.getAttribute("seriesName");
            NodeList children = forecast.getChildNodes();
            for (int childIndex = 0; childIndex < children.getLength(); childIndex++) {
                Node child = children.item(childIndex);
                if (child.getNodeType() != Node.ELEMENT_NODE || !"vaccine".equals(child.getNodeName())) {
                    continue;
                }
                String[] ids = ((Element) child).getAttribute("vaccineIds").split(",");
                for (String id : ids) {
                    String cvx = normalize(id);
                    if (cvx != null) {
                        vaccineIds.put(cvx, Integer.valueOf(cvx));
                        List<String> names = forecastNames.get(cvx);
                        if (names == null) {
                            names = new ArrayList<String>();
                            forecastNames.put(cvx, names);
                        }
                        if (!names.contains(forecastName)) {
                            names.add(forecastName);
                        }
                    }
                }
            }
        }
    }

    int vaccineId(String cvx) {
        Integer vaccineId = vaccineIds.get(normalize(cvx));
        if (vaccineId == null) {
            throw new IllegalArgumentException("Unsupported FITS CVX " + cvx);
        }
        return vaccineId.intValue();
    }

    boolean matchesForecast(String cvx, String forecastName) {
        List<String> names = forecastNames.get(normalize(cvx));
        return names != null && names.contains(forecastName);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (!normalized.matches("\\d+")) {
            return null;
        }
        return String.valueOf(Integer.parseInt(normalized));
    }
}