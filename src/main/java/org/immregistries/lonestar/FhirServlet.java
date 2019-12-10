package org.immregistries.lonestar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.DomUtils;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.TimePeriod;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.api.impl.CvxCode;
import org.immregistries.lonestar.core.api.impl.ForecastHandler;
import org.immregistries.lonestar.core.api.impl.ForecastHandlerCore;
import org.immregistries.lonestar.core.api.impl.ForecastOptions;
import org.immregistries.lonestar.core.api.impl.VaccineForecastManager;
import org.immregistries.lonestar.core.model.Immunization;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;
import org.immregistries.lonestar.core.server.ForecastReportPrinter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

public class FhirServlet extends HttpServlet {

  protected static final String SCHEDULE_NAME_DEFAULT = "default";

  @Override
  public void init() throws ServletException {

    super.init();
  }

  private static Map<String, VaccineForecastManager> forecastManagerMap =
      new HashMap<String, VaccineForecastManager>();
  private static Map<String, ForecastHandlerCore> forecastHandlerCoreMap =
      new HashMap<String, ForecastHandlerCore>();

  protected VaccineForecastManager forecastManager = null;
  protected ForecastHandlerCore forecastHandlerCore = null;

  protected void initSchedule(String scheduleName) throws ServletException {
    forecastHandlerCore = forecastHandlerCoreMap.get(scheduleName);
    forecastManager = forecastManagerMap.get(scheduleName);
    if (forecastHandlerCore == null) {
      try {
        if (scheduleName.equals(SCHEDULE_NAME_DEFAULT)) {
          forecastManager = new VaccineForecastManager();
        } else {
          forecastManager = new VaccineForecastManager(scheduleName + ".xml");
        }
        forecastManagerMap.put(scheduleName, forecastManager);
      } catch (Exception e) {
        throw new ServletException("Unable to initialize forecaster", e);
      }
      forecastHandlerCore = new ForecastHandlerCore(forecastManager);
      forecastHandlerCoreMap.put(scheduleName, forecastHandlerCore);
    }

  }

  protected static class ForecastInput {
    protected List<VaccinationDoseDataBean> doseList = null;
    protected PatientRecordDataBean patient = null;
    protected List<ImmunizationInterface> imms = null;
    protected DateTime forecastDate = null;
    protected ForecastOptions forecastOptions = new ForecastOptions();
    protected boolean dueUseEarly = false;
    protected boolean json = false;

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    out.println("<html><head><title>Lone Star Vaccine Forecaster FHIR Server</title></head><body>");
    out.println("<h1>FHIR Server</h1>");
    out.println(
        "<p>This is the end point for the DSTU2 FHIR end-point. This is experimental and may change at any time. </p>");
    out.println("</body></html>");
    out.close();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      ForecastInput forecastInput = new ForecastInput();
      readRequest(req, forecastInput);
      respond(resp, forecastInput);
    } catch (Exception e) {
      handleException(resp, e);
    }
  }

  protected void respond(HttpServletResponse resp, ForecastInput forecastInput)
      throws ServletException, IOException {
    initSchedule(SCHEDULE_NAME_DEFAULT);
    List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
    String forecasterScheduleName = "";
    try {
      Map<String, List<Trace>> traceMap = new HashMap<String, List<Trace>>();
      forecasterScheduleName = forecastHandlerCore.forecast(forecastInput.doseList,
          forecastInput.patient, forecastInput.imms, forecastInput.forecastDate, traceMap,
          resultList, forecastInput.forecastOptions);
    } catch (Exception e) {
      throw new ServletException("Unable to forecast", e);
    }

    ForecastHandlerCore.sort(resultList);

    ForecastReportPrinter forecastReportPrinter = new ForecastReportPrinter(forecastManager);

    if (forecastInput.json) {
      resp.setContentType("application/xml");
      PrintWriter out = new PrintWriter(resp.getOutputStream());
      forecastReportPrinter.printFhirVersionOfForecastXML(resultList, forecastInput.imms,
          forecasterScheduleName, forecastInput.forecastDate, forecastInput.doseList, out,
          forecastInput.patient);
      out.close();
    } else {
      resp.setContentType("application/fhir+json;charset=utf-8");
      PrintWriter out = new PrintWriter(resp.getOutputStream());
      forecastReportPrinter.printFhirVersionOfForecastJSON(resultList, forecastInput.imms,
          forecasterScheduleName, forecastInput.forecastDate, forecastInput.doseList, out,
          forecastInput.patient);
      out.close();
    }
  }

  protected void handleException(HttpServletResponse resp, Exception e) throws IOException {
    resp.setContentType("text/html");
    resp.setStatus(500);
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("  <head>");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <h1>Oops...</h1>");
    out.println(
        "    <p>The Lone Star Vaccine Forecaster encountered a problem and was unable to return a forecast result. </p>");
    out.println("    <h2>Technical Details</h2>");
    out.println("    <pre>");
    e.printStackTrace(out);
    out.println("    </pre>");
    out.println("  </body>");
    out.println("</html>");
    out.close();
  }

  public TimePeriod readTimePeriod(HttpServletRequest req, String key) {
    String value = req.getParameter(key);
    return value == null || value.equals("") ? null : new TimePeriod(value);
  }

  public boolean readBoolean(HttpServletRequest req, String key) {
    String value = req.getParameter(key);
    if (value == null || value.equals("")) {
      return false;
    }
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes")
        || value.equalsIgnoreCase("t") || value.equalsIgnoreCase("1")
        || value.equalsIgnoreCase("y")) {
      return true;
    }
    return false;
  }

  private void readResource(Node node, ForecastInput forecastInput,
      Map<String, CvxCode> cvxToVaccineIdMap) {
    Node immNode = null;
    NodeList l = node.getChildNodes();
    for (int i = 0, icount = l.getLength(); i < icount; i++) {
      Node n = l.item(i);
      String name = n.getNodeName();
      if (name.equals("Immunization")) {
        immNode = n;
        break;
      }
    }
    if (immNode != null) {
      DateTime dateTime = null;
      String vaccineCvx = null;

      l = immNode.getChildNodes();
      int vaccCount = 0;
      for (int i = 0, icount = l.getLength(); i < icount; i++) {
        Node n = l.item(i);
        String name = n.getNodeName();
        if (name.equals("date")) {
          String dateValue = DomUtils.getAttributeValue(n, "value");
          dateTime = new DateTime(dateValue);
        } else if (name.equals("vaccineType")) {
          Node codingNode = null;
          {
            NodeList l2 = n.getChildNodes();
            for (int i2 = 0, i2count = l2.getLength(); i2 < i2count; i2++) {
              Node n2 = l2.item(i2);
              name = n2.getNodeName();
              if (name.equals("coding")) {
                codingNode = n2;
              }
            }
          }
          if (codingNode != null) {
            NodeList l2 = codingNode.getChildNodes();
            for (int i2 = 0, i2count = l2.getLength(); i2 < i2count; i2++) {
              Node n2 = l2.item(i2);
              name = n2.getNodeName();
              if (name.equals("code")) {
                vaccineCvx = DomUtils.getAttributeValue(n2, "value");
              }
            }
          }
          if (vaccineCvx != null && dateTime != null) {
            int vaccineId = 0;
            {
              if (!cvxToVaccineIdMap.containsKey(vaccineCvx)
                  && !cvxToVaccineIdMap.containsKey("0" + vaccineCvx)) {
                throw new IllegalArgumentException("CVX code '" + vaccineCvx
                    + "' is not recognized in parameter named 'vaccineCvx" + n + "'");
              }
              CvxCode cvxCode = null;
              if (cvxToVaccineIdMap.containsKey(vaccineCvx)) {
                cvxCode = cvxToVaccineIdMap.get(vaccineCvx);
              } else {
                cvxCode = cvxToVaccineIdMap.get("0" + vaccineCvx);
              }

              if (cvxCode == null) {
                throw new IllegalArgumentException("CVX code '" + vaccineCvx
                    + "' is not recognized in parameter named 'vaccineCvx" + n + "'");
              } else {
                vaccineId = cvxCode.getVaccineId();
              }
            }
            vaccCount++;
            Immunization imm = new Immunization();
            imm.setCvx(vaccineCvx);
            imm.setDateOfShot(dateTime.getDate());
            imm.setVaccineId(vaccineId);
            imm.setVaccinationId("" + vaccCount);
            forecastInput.imms.add(imm);
          }
        }
      }
    }
  }

  private void readParameter(Node node, ForecastInput forecastInput,
      Map<String, CvxCode> cvxToVaccineIdMap) {
    NodeList l = node.getChildNodes();
    String nameValue = "";
    String dateValue = "";
    String codeValue = "";

    for (int i = 0, icount = l.getLength(); i < icount; i++) {
      Node n = l.item(i);
      String name = n.getNodeName();
      if (name.equals("name")) {
        nameValue = DomUtils.getAttributeValue(n, "value");
      } else if (name.equals("valueDate")) {
        dateValue = DomUtils.getAttributeValue(n, "value");
      } else if (name.equals("valueCode")) {
        codeValue = DomUtils.getAttributeValue(n, "value");
      } else if (name.equals("resource")) {
        readResource(n, forecastInput, cvxToVaccineIdMap);
      }
    }
    if (nameValue.equals("assessmentDate")) {
      forecastInput.forecastDate = new DateTime(dateValue);
    } else if (nameValue.equals("gender")) {
      forecastInput.patient.setSex(codeValue.equalsIgnoreCase("male") ? "M" : "F");
    } else if (nameValue.equals("birthDate")) {
      forecastInput.patient.setDob(new DateTime(dateValue));
    }
  }

  protected void readRequest(HttpServletRequest req, ForecastInput forecastInput)
      throws ServletException, IOException {

    InputStream in = req.getInputStream();
    try {
      readRequestXML(forecastInput, in);
    } catch (Exception e) {
      throw new ServletException("Unable to read XML", e);
    }

  }


  protected void readRequestJSON(ForecastInput forecastInput, InputStream in)
      throws ParserConfigurationException, SAXException, IOException, Exception {
    forecastInput.doseList = new ArrayList<VaccinationDoseDataBean>();
    forecastInput.patient = new PatientRecordDataBean();
    forecastInput.imms = new ArrayList<ImmunizationInterface>();
    forecastInput.forecastDate = new DateTime();
    forecastInput.doseList = new ArrayList<VaccinationDoseDataBean>();

    Map<String, CvxCode> cvxToVaccineIdMap = null;
    try {
      cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
    } catch (Exception e) {
      throw new ServletException("Unable to initialize CVX mapping", e);
    }

    InputStreamReader input = new InputStreamReader(in, "UTF-8");
    JsonReader reader = new JsonReader(input);
    handleObject(reader, forecastInput, "", cvxToVaccineIdMap);
    // handleArray(reader, forecastInput, null);
    input.close();

    // remove later
    forecastInput.patient.setDob(new DateTime());
  }

  private static void handleArray(JsonReader reader, ForecastInput forecastInput, String indent,
      Map<String, CvxCode> cvxToVaccineIdMap) throws IOException {
    reader.beginArray();
    String name = null;
    String indentCurrent = indent;
    while (true) {
      JsonToken token = reader.peek();
      if (token.equals(JsonToken.END_ARRAY)) {
        reader.endArray();
        break;
      } else if (token.equals(JsonToken.BEGIN_OBJECT)) {
        handleObject(reader, forecastInput, indentCurrent, cvxToVaccineIdMap);
      } else if (token.equals(JsonToken.BEGIN_ARRAY)) {
        handleArray(reader, forecastInput, indentCurrent, cvxToVaccineIdMap);
      } else {
        name = handleNonArrayToken(reader, token, forecastInput, name, indentCurrent,
            cvxToVaccineIdMap);
        if (indent.equals("")) {
          indentCurrent = name;
        } else {
          indentCurrent = indent + "." + name;
        }
      }
    }
  }

  /**
   * Handle an Object. Consume the first token which is BEGIN_OBJECT. Within the
   * Object there could be array or non array tokens. We write handler methods for
   * both. Note the peek() method. It is used to find out the type of the next
   * token without actually consuming it.
   *
   * @param reader
   * @throws IOException
   */
  private static void handleObject(JsonReader reader, ForecastInput forecastInput, String indent,
      Map<String, CvxCode> cvxToVaccineIdMap) throws IOException {
    reader.beginObject();
    String name = null;
    String indentCurrent = indent;
    try {
      while (reader.hasNext()) {
        JsonToken token = reader.peek();
        if (token.equals(JsonToken.BEGIN_ARRAY)) {
          handleArray(reader, forecastInput, indentCurrent, cvxToVaccineIdMap);
        } else if (token.equals(JsonToken.BEGIN_OBJECT)) {
          handleObject(reader, forecastInput, indentCurrent, cvxToVaccineIdMap);
        } else if (token.equals(JsonToken.END_OBJECT)) {
          return;
        } else {
          name = handleNonArrayToken(reader, token, forecastInput, name, indentCurrent,
              cvxToVaccineIdMap);
          if (indent.equals("")) {
            indentCurrent = name;
          } else {
            indentCurrent = indent + "." + name;
          }
        }
      }
    } finally {
      reader.endObject();
    }

  }

  private static final String ID = "id";
  private static final String RESOURCE_TYPE = "parameter.resource.resourceType";
  private static final String IMMUNIZATION = "Immunization";
  private static final String ASSESSMENT_DATE = "parameter.valueDate";
  private static final String GENDER = "parameter.resource.gender";
  private static final String BIRTH_DATE = "parameter.resource.birthDate";
  private static final String VACCINATION_DATE = "parameter.resource.occurrenceDateTime";
  private static final String VACCINATION_CODE = "parameter.resource.vaccineCode.coding.code";

  private static String handleNonArrayToken(JsonReader reader, JsonToken token,
      ForecastInput forecastInput, String name, String indent,
      Map<String, CvxCode> cvxToVaccineIdMap) throws IOException {
    if (token.equals(JsonToken.NAME)) {
      name = reader.nextName();
    } else if (token.equals(JsonToken.STRING)) {
      String s = reader.nextString();
      if (indent.equals(ID)) {
        forecastInput.patient.setId(s);
      } else if (indent.equals(RESOURCE_TYPE) && s.equals(IMMUNIZATION)) {
        System.out.println("--> found imm!");
        Immunization imm = new Immunization();
        forecastInput.imms.add(imm);
        imm.setVaccinationId("" + forecastInput.imms.size());
      } else if (indent.equals(ASSESSMENT_DATE)) {
        forecastInput.forecastDate = parseDate(s);
      } else if (indent.equals(BIRTH_DATE)) {
        forecastInput.patient.setDob(parseDate(s));
      } else if (indent.equals(GENDER)) {
        if (s.length() > 1) {
          forecastInput.patient.setSex(s.substring(0, 1).toUpperCase());
        }
      } else if (indent.equals(VACCINATION_DATE)) {
        if (forecastInput.imms.size() > 0) {
          Immunization imm = (Immunization) forecastInput.imms.get(forecastInput.imms.size() - 1);
          imm.setDateOfShot(parseDate(s).getDate());
        }

      } else if (indent.equals(VACCINATION_CODE)) {
        if (forecastInput.imms.size() > 0) {
          Immunization imm = (Immunization) forecastInput.imms.get(forecastInput.imms.size() - 1);
          String vaccineCvx = s;
          imm.setCvx(vaccineCvx);
          CvxCode cvxCode = null;
          if (cvxToVaccineIdMap.containsKey(vaccineCvx)) {
            cvxCode = cvxToVaccineIdMap.get(vaccineCvx);
          } else {
            cvxCode = cvxToVaccineIdMap.get("0" + vaccineCvx);
          }
          if (cvxCode != null) {
            imm.setVaccineId(cvxCode.getVaccineId());
          }
        }
      }
    } else {
      reader.skipValue();
    }
    return name;
  }

  private static DateTime parseDate(String s) {
    Date d = null;
    if (s != null) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      try {
        d = sdf.parse(s);
      } catch (ParseException pe) {
        // ignore
      }
    }
    if (d == null) {
      return new DateTime();
    }
    return new DateTime(d);
  }

  protected void readRequestXML(ForecastInput forecastInput, InputStream in)
      throws ParserConfigurationException, SAXException, IOException, Exception {
    Map<String, CvxCode> cvxToVaccineIdMap = null;
    try {
      cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
    } catch (Exception e) {
      throw new ServletException("Unable to initialize CVX mapping", e);
    }

    forecastInput.doseList = new ArrayList<VaccinationDoseDataBean>();
    forecastInput.patient = new PatientRecordDataBean();
    forecastInput.imms = new ArrayList<ImmunizationInterface>();
    forecastInput.forecastDate = new DateTime();

    DocumentBuilderFactory factory;
    DocumentBuilder builder;
    factory = DocumentBuilderFactory.newInstance();
    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);
    factory.setNamespaceAware(true);
    builder = factory.newDocumentBuilder();
    Document node = builder.parse(in);

    NodeList dl = node.getChildNodes();
    for (int di = 0, dicount = dl.getLength(); di < dicount; di++) {
      Node dNode = dl.item(di);
      String name = dNode.getNodeName();
      if (name.equals("Parameters")) {
        NodeList l = dNode.getChildNodes();
        for (int i = 0, icount = l.getLength(); i < icount; i++) {
          Node n = l.item(i);
          name = n.getNodeName();
          if (name.equals("parameter")) {
            readParameter(n, forecastInput, cvxToVaccineIdMap);
          }
        }
      }
    }
  }

  public void setAssumeParam(HttpServletRequest req, ForecastInput forecastInput,
      DateTime patientDob, String paramName, String label, int vaccineId) {
    TimePeriod assumeSeriesCompleteAtAge = readTimePeriod(req, paramName);

    if (assumeSeriesCompleteAtAge != null) {
      DateTime assumptionAge = assumeSeriesCompleteAtAge.getDateTimeFrom(patientDob);
      if (forecastInput.forecastDate.isGreaterThanOrEquals(assumptionAge)) {
        DateTime assumptionDate = new DateTime(assumptionAge);
        Immunization imm = new Immunization();
        imm.setDateOfShot(assumptionDate.getDate());
        imm.setVaccineId(vaccineId);
        imm.setLabel(label);
        imm.setAssumption(true);
        forecastInput.imms.add(imm);
      }
    }
  }

}
