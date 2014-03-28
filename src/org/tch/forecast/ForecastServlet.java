package org.tch.forecast;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.TimePeriod;
import org.tch.forecast.core.Trace;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastManagerInterface;
import org.tch.forecast.core.api.impl.CvxCode;
import org.tch.forecast.core.api.impl.ForecastHandler;
import org.tch.forecast.core.api.impl.ForecastHandlerCore;
import org.tch.forecast.core.api.impl.ForecastOptions;
import org.tch.forecast.core.api.impl.VaccineForecastManager;
import org.tch.forecast.core.model.Immunization;
import org.tch.forecast.core.model.PatientRecordDataBean;
import org.tch.forecast.core.server.ForecastReportPrinter;

public class ForecastServlet extends HttpServlet
{
  private static final String PARAM_VACCINE_MVX = "vaccineMvx";
  private static final String PARAM_VACCINE_CVX = "vaccineCvx";
  private static final String PARAM_VACCINE_CONDITION_CODE = "vaccineConditionCode";
  private static final String PARAM_VACCINE_DATE = "vaccineDate";
  private static final String PARAM_PATIENT_SEX = "patientSex";
  private static final String PARAM_PATIENT_DOB = "patientDob";
  private static final String PARAM_RESULT_FORMAT = "resultFormat";
  private static final String PARAM_EVAL_DATE = "evalDate";
  private static final String PARAM_FLU_SEASON_START = "fluSeasonStart";
  private static final String PARAM_FLU_SEASON_DUE = "fluSeasonDue";
  private static final String PARAM_FLU_SEASON_OVERDUE = "fluSeasonOverdue";
  private static final String PARAM_FLU_SEASON_END = "fluSeasonEnd";
  private static final String PARAM_DUE_USE_EARLY = "dueUseEarly";
  private static final String PARAM_ASSUME_DTAP_SERIES_COMPLETE_AT_AGE = "assumeDtapSeriesCompleteAtAge";
  private static final String PARAM_ASSUME_HEPA_SERIES_COMPLETE_AT_AGE = "assumeHepASeriesCompleteAtAge";
  private static final String PARAM_ASSUME_HEPB_SERIES_COMPLETE_AT_AGE = "assumeHepBSeriesCompleteAtAge";
  private static final String PARAM_ASSUME_MMR_SERIES_COMPLETE_AT_AGE = "assumeMMRSeriesCompleteAtAge";
  private static final String PARAM_ASSUME_VAR_SERIES_COMPLETE_AT_AGE = "assumeVarSeriesCompleteAtAge";
  private static final String PARAM_IGNORE_FOUR_DAY_GRACE = "ignoreFourDayGrace";
  private static final String PARAM_SCHEDULE_NAME = "scheduleName";

  public static final String RESULT_FORMAT_TEXT = "text";
  public static final String RESULT_FORMAT_HTML = "html";
  public static final String RESULT_FORMAT_COMPACT = "compact";

  private static final String CONDITION_CODE_SUB_POTENT = "S";
  private static final String CONDITION_CODE_FORCE_VALID = "F";

  protected static final String SCHEDULE_NAME_DEFAULT = "default";

  @Override
  public void init() throws ServletException {

    super.init();
  }

  private static Map<String, VaccineForecastManager> forecastManagerMap = new HashMap<String, VaccineForecastManager>();
  private static Map<String, ForecastHandlerCore> forecastHandlerCoreMap = new HashMap<String, ForecastHandlerCore>();

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

  protected static class ForecastInput
  {
    protected List<VaccinationDoseDataBean> doseList = null;
    protected PatientRecordDataBean patient = null;
    protected List<ImmunizationInterface> imms = null;
    protected DateTime forecastDate = null;
    protected ForecastOptions forecastOptions = new ForecastOptions();
    protected boolean dueUseEarly = false;

  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      ForecastInput forecastInput = new ForecastInput();
      readRequest(req, forecastInput);
      String resultFormat = req.getParameter(PARAM_RESULT_FORMAT);
      if (resultFormat == null || resultFormat.equals("")) {
        throw new ServletException("Parameter 'resultFormat' is required. ");
      }

      List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
      String forecasterScheduleName = "";
      try {
        Map<String, List<Trace>> traceMap = new HashMap<String, List<Trace>>();
        forecasterScheduleName = forecastHandlerCore.forecast(forecastInput.doseList, forecastInput.patient,
            forecastInput.imms, forecastInput.forecastDate, traceMap, resultList, forecastInput.forecastOptions);
      } catch (Exception e) {
        throw new ServletException("Unable to forecast", e);
      }

      ForecastHandlerCore.sort(resultList);

      ForecastReportPrinter forecastReportPrinter = new ForecastReportPrinter(forecastManager);
      if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_HTML)) {
        resp.setContentType("text/html");
        PrintWriter out = new PrintWriter(resp.getOutputStream());

        forecastReportPrinter.printHTMLVersionOfForecast(resultList, forecastInput.imms, forecasterScheduleName,
            forecastInput.forecastDate, forecastInput.doseList, out);
        out.close();

      } else if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_TEXT)) {
        resp.setContentType("text/plain");
        PrintWriter out = new PrintWriter(resp.getOutputStream());
        forecastReportPrinter.printTextVersionOfForecast(resultList, forecastInput.imms, forecasterScheduleName,
            forecastInput.forecastDate, forecastInput.doseList, out);
        out.close();
      } else if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_COMPACT)) {
        resp.setContentType("text/plain");
        PrintWriter out = new PrintWriter(resp.getOutputStream());
        forecastReportPrinter.printNarrowTextVersionOfForecast(resultList, forecastInput.imms, forecasterScheduleName,
            forecastInput.forecastDate, forecastInput.doseList, out, true, forecastInput.patient.getDob());
        out.close();
      } else {
        throw new ServletException("Unrecognized result format '" + resultFormat + "'");
      }
    } catch (Exception e) {
      handleException(resp, e);
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
    out.println("    <p>The TCH Forecaster encountered a problem and was unable to return a forecast result. </p>");
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
    if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("t")
        || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("y")) {
      return true;
    }
    return false;
  }

  protected void readRequest(HttpServletRequest req, ForecastInput forecastInput) throws ServletException {

    Map<String, CvxCode> cvxToVaccineIdMap = null;
    try {
      cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
    } catch (Exception e) {
      throw new ServletException("Unable to initialize CVX mapping", e);
    }
    String scheduleName = req.getParameter(PARAM_SCHEDULE_NAME);
    if (scheduleName == null || scheduleName.equals("")) {
      scheduleName = SCHEDULE_NAME_DEFAULT;
    }
    initSchedule(scheduleName);
    forecastInput.doseList = new ArrayList<VaccinationDoseDataBean>();
    forecastInput.patient = new PatientRecordDataBean();
    forecastInput.imms = new ArrayList<ImmunizationInterface>();

    String evalDateString = req.getParameter(PARAM_EVAL_DATE);
    if (evalDateString != null && evalDateString.length() != 8) {
      throw new ServletException("Parameter 'evalDate' is optional, but if sent must be in YYYYMMDD format. ");
    }
    forecastInput.forecastDate = new DateTime(evalDateString == null ? "today" : evalDateString);
    String patientDobString = req.getParameter(PARAM_PATIENT_DOB);
    if (patientDobString == null || patientDobString.length() != 8) {
      throw new ServletException("Parameter 'patientDob' is required and must be in YYYYMMDD format. ");
    }
    DateTime patientDob = new DateTime(patientDobString);
    forecastInput.patient.setDob(patientDob);
    String patientSex = req.getParameter(PARAM_PATIENT_SEX);
    if (patientSex == null || (!patientSex.equalsIgnoreCase("M") && !patientSex.equalsIgnoreCase("F"))) {
      throw new ServletException("Parameter 'patientSex' is required and must have a value of 'M' or 'F'. ");
    }
    forecastInput.patient.setSex(patientSex.toUpperCase());
    int n = 1;
    while (req.getParameter(PARAM_VACCINE_DATE + n) != null) {
      String vaccineDateString = req.getParameter(PARAM_VACCINE_DATE + n);
      if (vaccineDateString.length() != 8) {
        throw new ServletException("Parameter 'vaccineDate" + n + "' must be in YYYYMMDD format.");
      }
      String vaccineCvx = req.getParameter(PARAM_VACCINE_CVX + n);
      String vaccineMvx = req.getParameter(PARAM_VACCINE_MVX + n);
      String vaccineConditionCode = req.getParameter(PARAM_VACCINE_CONDITION_CODE + n);
      int vaccineId = 0;
      if (vaccineCvx == null) {
        throw new ServletException("Parameter 'vaccineCvx" + n + "' is required.");
      } else {
        if (!cvxToVaccineIdMap.containsKey(vaccineCvx) && !cvxToVaccineIdMap.containsKey("0" + vaccineCvx)) {
          throw new ServletException("CVX code '" + vaccineCvx + "' is not recognized in parameter named 'vaccineCvx"
              + n + "'");
        }
        CvxCode cvxCode = null;
        if (cvxToVaccineIdMap.containsKey(vaccineCvx)) {
          cvxCode = cvxToVaccineIdMap.get(vaccineCvx);
        } else {
          cvxCode = cvxToVaccineIdMap.get("0" + vaccineCvx);
        }

        if (cvxCode == null) {
          throw new ServletException("CVX code '" + vaccineCvx + "' is not recognized in parameter named 'vaccineCvx"
              + n + "'");
        } else {
          vaccineId = cvxCode.getVaccineId();
        }
      }
      Immunization imm = new Immunization();
      imm.setCvx(vaccineCvx);
      imm.setDateOfShot(new DateTime(vaccineDateString).getDate());
      imm.setVaccineId(vaccineId);
      imm.setVaccinationId("" + n);
      if (vaccineConditionCode != null) {
        if (vaccineConditionCode.equals(CONDITION_CODE_SUB_POTENT)) {
          imm.setSubPotent(true);
        } else if (vaccineConditionCode.equals(CONDITION_CODE_FORCE_VALID)) {
          imm.setForceValid(true);
        }
      }
      forecastInput.imms.add(imm);
      n++;
    }

    forecastInput.forecastOptions.setFluSeasonDue(readTimePeriod(req, PARAM_FLU_SEASON_DUE));
    forecastInput.forecastOptions.setFluSeasonEnd(readTimePeriod(req, PARAM_FLU_SEASON_END));
    forecastInput.forecastOptions.setFluSeasonOverdue(readTimePeriod(req, PARAM_FLU_SEASON_OVERDUE));
    forecastInput.forecastOptions.setFluSeasonStart(readTimePeriod(req, PARAM_FLU_SEASON_START));
    forecastInput.forecastOptions.setIgnoreFourDayGrace(readBoolean(req, PARAM_IGNORE_FOUR_DAY_GRACE));

    forecastInput.dueUseEarly = readBoolean(req, PARAM_DUE_USE_EARLY);
    forecastInput.forecastOptions.setUseEarlyDue(forecastInput.dueUseEarly);
    forecastInput.forecastOptions.setUseEarlyOverdue(forecastInput.dueUseEarly);

    setAssumeParam(req, forecastInput, patientDob, PARAM_ASSUME_DTAP_SERIES_COMPLETE_AT_AGE,
        "Assuming DTaP series completed in childhood", Immunization.ASSUME_DTAP_SERIES_COMPLETE);
    setAssumeParam(req, forecastInput, patientDob, PARAM_ASSUME_HEPA_SERIES_COMPLETE_AT_AGE,
        "Assuming Hep A series completed in childhood", Immunization.ASSUME_HEPA_COMPLETE);
    setAssumeParam(req, forecastInput, patientDob, PARAM_ASSUME_HEPB_SERIES_COMPLETE_AT_AGE,
        "Assuming Hep B series completed in childhood", Immunization.ASSUME_HEPB_COMPLETE);
    setAssumeParam(req, forecastInput, patientDob, PARAM_ASSUME_MMR_SERIES_COMPLETE_AT_AGE,
        "Assuming MMR series completed in childhood", Immunization.ASSUME_MMR_COMPLETE);
    setAssumeParam(req, forecastInput, patientDob, PARAM_ASSUME_VAR_SERIES_COMPLETE_AT_AGE,
        "Assuming Varicella series completed in childhood", Immunization.ASSUME_VAR_COMPLETE);
  }

  public void setAssumeParam(HttpServletRequest req, ForecastInput forecastInput, DateTime patientDob,
      String paramName, String label, int vaccineId) {
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
