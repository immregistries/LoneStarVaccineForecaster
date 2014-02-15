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
  private static final String PARAM_VACCINE_DATE = "vaccineDate";
  private static final String PARAM_PATIENT_SEX = "patientSex";
  private static final String PARAM_PATIENT_DOB = "patientDob";
  private static final String PARAM_RESULT_FORMAT = "resultFormat";
  private static final String PARAM_EVAL_SCHEDULE = "evalSchedule";
  private static final String PARAM_EVAL_DATE = "evalDate";
  private static final String PARAM_FLU_SEASON_START = "fluSeasonStart";
  private static final String PARAM_FLU_SEASON_DUE = "fluSeasonDue";
  private static final String PARAM_FLU_SEASON_OVERDUE = "fluSeasonOverdue";
  private static final String PARAM_FLU_SEASON_END = "fluSeasonEnd";
  private static final String PARAM_DUE_USE_EARLY = "dueUseEarly";
  private static final String PARAM_ASSUME_DTAP_SERIES_COMPLETE_AT_AGE = "assumeDtapSeriesCompleteAtAge";
  private static final String PARAM_IGNORE_FOUR_DAY_GRACE = "ignoreFourDayGrace";

  public static final String RESULT_FORMAT_TEXT = "text";
  public static final String RESULT_FORMAT_HTML = "html";
  public static final String RESULT_FORMAT_COMPACT = "compact";

  private static ForecastHandlerCore forecastHandlerCore = null;

  @Override
  public void init() throws ServletException {

    super.init();
  }

  public static VaccineForecastManagerInterface forecastManager = null;

  private void initCvxCodes() throws ServletException {
    if (forecastHandlerCore == null) {
      try {
        forecastManager = new VaccineForecastManager();
      } catch (Exception e) {
        throw new ServletException("Unable to initialize forecaster", e);
      }
      forecastHandlerCore = new ForecastHandlerCore(forecastManager);

    }

  }

  protected List<VaccinationDoseDataBean> doseList = null;
  protected PatientRecordDataBean patient = null;
  protected List<ImmunizationInterface> imms = null;
  protected DateTime forecastDate = null;
  protected ForecastOptions forecastOptions = new ForecastOptions();
  protected boolean dueUseEarly = false;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    readRequest(req);
    String resultFormat = req.getParameter(PARAM_RESULT_FORMAT);
    if (resultFormat == null || resultFormat.equals("")) {
      throw new ServletException("Parameter 'resultFormat' is required. ");
    }

    List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
    String forecasterScheduleName = "";
    try {
      Map<String, List<Trace>> traceMap = new HashMap<String, List<Trace>>();
      forecasterScheduleName = forecastHandlerCore.forecast(doseList, patient, imms, forecastDate, traceMap,
          resultList, forecastOptions);
    } catch (Exception e) {
      throw new ServletException("Unable to forecast", e);
    }

    ForecastHandlerCore.sort(resultList);

    ForecastReportPrinter forecastReportPrinter = new ForecastReportPrinter(forecastManager);
    if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_HTML)) {
      resp.setContentType("text/html");
      PrintWriter out = new PrintWriter(resp.getOutputStream());

      forecastReportPrinter.printHTMLVersionOfForecast(resultList, imms, forecasterScheduleName, forecastDate,
          dueUseEarly, doseList, out);
      out.close();

    } else if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_TEXT)) {
      resp.setContentType("text/plain");
      PrintWriter out = new PrintWriter(resp.getOutputStream());
      forecastReportPrinter.printTextVersionOfForecast(resultList, imms, forecasterScheduleName, forecastDate,
          dueUseEarly, doseList, out);
      out.close();
    } else if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_COMPACT)) {
      resp.setContentType("text/plain");
      PrintWriter out = new PrintWriter(resp.getOutputStream());
      forecastReportPrinter.printNarrowTextVersionOfForecast(resultList, imms, forecasterScheduleName, forecastDate,
          dueUseEarly, doseList, out);
      out.close();
    } else {
      throw new ServletException("Unrecognized result format '" + resultFormat + "'");
    }
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

  protected void readRequest(HttpServletRequest req) throws ServletException {

    Map<String, CvxCode> cvxToVaccineIdMap = null;
    try {
      cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
    } catch (Exception e) {
      throw new ServletException("Unable to initialize CVX mapping", e);
    }
    initCvxCodes();
    doseList = new ArrayList<VaccinationDoseDataBean>();
    patient = new PatientRecordDataBean();
    imms = new ArrayList<ImmunizationInterface>();

    String evalDateString = req.getParameter(PARAM_EVAL_DATE);
    if (evalDateString != null && evalDateString.length() != 8) {
      throw new ServletException("Parameter 'evalDate' is optional, but if sent must be in YYYYMMDD format. ");
    }
    forecastDate = new DateTime(evalDateString == null ? "today" : evalDateString);
    String evalSchedule = req.getParameter(PARAM_EVAL_SCHEDULE);
    if (evalSchedule == null) {
      evalSchedule = "";
    }
    String patientDobString = req.getParameter(PARAM_PATIENT_DOB);
    if (patientDobString == null || patientDobString.length() != 8) {
      throw new ServletException("Parameter 'patientDob' is required and must be in YYYYMMDD format. ");
    }
    DateTime patientDob = new DateTime(patientDobString);
    patient.setDob(patientDob);
    String patientSex = req.getParameter(PARAM_PATIENT_SEX);
    if (patientSex == null || (!patientSex.equalsIgnoreCase("M") && !patientSex.equalsIgnoreCase("F"))) {
      throw new ServletException("Parameter 'patientSex' is required and must have a value of 'M' or 'F'. ");
    }
    patient.setSex(patientSex.toUpperCase());
    int n = 1;
    while (req.getParameter(PARAM_VACCINE_DATE + n) != null) {
      String vaccineDateString = req.getParameter(PARAM_VACCINE_DATE + n);
      if (vaccineDateString.length() != 8) {
        throw new ServletException("Parameter 'vaccineDate" + n + "' must be in YYYYMMDD format.");
      }
      String vaccineCvx = req.getParameter(PARAM_VACCINE_CVX + n);
      String vaccineMvx = req.getParameter(PARAM_VACCINE_MVX + n);
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
      imms.add(imm);
      n++;
    }

    forecastOptions.setFluSeasonDue(readTimePeriod(req, PARAM_FLU_SEASON_DUE));
    forecastOptions.setFluSeasonEnd(readTimePeriod(req, PARAM_FLU_SEASON_END));
    forecastOptions.setFluSeasonOverdue(readTimePeriod(req, PARAM_FLU_SEASON_OVERDUE));
    forecastOptions.setFluSeasonStart(readTimePeriod(req, PARAM_FLU_SEASON_START));
    forecastOptions.setIgnoreFourDayGrace(readBoolean(req, PARAM_IGNORE_FOUR_DAY_GRACE));

    dueUseEarly = readBoolean(req, PARAM_DUE_USE_EARLY);
    TimePeriod assumeDtapSeriesCompleteAtAge = readTimePeriod(req, PARAM_ASSUME_DTAP_SERIES_COMPLETE_AT_AGE);

    if (assumeDtapSeriesCompleteAtAge != null) {
      DateTime assumptionAge = assumeDtapSeriesCompleteAtAge.getDateTimeFrom(patientDob);
      if (forecastDate.isGreaterThanOrEquals(assumptionAge)) {
        DateTime assumptionDate = new DateTime(forecastDate);
        assumptionDate.addDays(1);
        Immunization imm = new Immunization();
        imm.setDateOfShot(assumptionDate.getDate());
        imm.setVaccineId(Immunization.ASSUME_DTAP_SERIES_COMPLETE);
        imm.setLabel("Assuming DTaP Series Complete");
        imms.add(imm);
      }
    }
  }

}
