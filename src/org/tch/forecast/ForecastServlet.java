package org.tch.forecast;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastManagerInterface;
import org.tch.forecast.core.api.impl.ForecastHandlerCore;
import org.tch.forecast.core.api.impl.ForecastOptions;
import org.tch.forecast.core.model.Immunization;
import org.tch.forecast.core.model.PatientRecordDataBean;
import org.tch.forecast.core.server.ForecastReportPrinter;
import org.tch.forecast.model.VaccineModel;
import org.tch.forecast.support.VaccineForecastManager;
import org.tch.forecast.validator.DataSourceUnavailableException;
import org.tch.forecast.validator.db.DatabasePool;

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

  private static ForecastHandlerCore forecastHandlerCore = null;

  @Override
  public void init() throws ServletException {

    super.init();
  }

  private static Map<String, Integer> cvxToVaccineIdMap = null;

  public static VaccineForecastManagerInterface forecastManager = null;

  private void initCvxCodes() throws ServletException {
    if (forecastHandlerCore == null) {
      forecastManager = new VaccineForecastManager();
      forecastHandlerCore = new ForecastHandlerCore(forecastManager);
    }

    if (cvxToVaccineIdMap == null) {

      try {
        Connection conn = DatabasePool.getConnection();
        try {
          cvxToVaccineIdMap = new HashMap<String, Integer>();
          PreparedStatement pstmt = conn.prepareStatement("SELECT cvx_code, vaccine_id FROM vaccine_cvx");
          ResultSet rset = pstmt.executeQuery();
          while (rset.next()) {
            cvxToVaccineIdMap.put(rset.getString(1), rset.getInt(2));
          }
          rset.close();
          pstmt.close();

          rset.close();
          pstmt.close();

        } catch (Exception e) {
          throw new ServletException("Unable to query for CVX codes", e);
        } finally {
          DatabasePool.close(conn);
        }
      } catch (DataSourceUnavailableException e) {
        cvxToVaccineIdMap = null;
        throw new ServletException("Unable to connect to database", e);
      }
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

    Map traceMap = new HashMap();
    List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
    String forecasterScheduleName = "";
    try {
      forecasterScheduleName = forecastHandlerCore.forecast(doseList, patient, imms, forecastDate, traceMap,
          resultList, forecastOptions);
    } catch (Exception e) {
      throw new ServletException("Unable to forecast", e);
    }

    List<ImmunizationForecastDataBean> resultListOriginal = new ArrayList<ImmunizationForecastDataBean>(resultList);
    ForecastHandlerCore.sort(resultListOriginal);

    List<ImmunizationForecastDataBean> forecastListDueToday = new ArrayList<ImmunizationForecastDataBean>();
    traceMap.remove(ImmunizationForecastDataBean.PERTUSSIS);
    for (Iterator<ImmunizationForecastDataBean> it = resultList.iterator(); it.hasNext();) {
      ImmunizationForecastDataBean forecastExamine = it.next();
      if (forecastExamine.getForecastName().equals("MMR")) {
        traceMap.remove(ImmunizationForecastDataBean.MEASLES);
        traceMap.remove(ImmunizationForecastDataBean.MUMPS);
        traceMap.remove(ImmunizationForecastDataBean.RUBELLA);
      }
      if (forecastExamine.getForecastName().equals("DTaP") || forecastExamine.getForecastName().equals("Tdap")
          || forecastExamine.getForecastName().equals("Td")) {
        traceMap.remove(ImmunizationForecastDataBean.DIPHTHERIA);
      }
      if (!forecastDate.isLessThan(new DateTime(forecastExamine.getDue(dueUseEarly)))) {
        if (!forecastDate.isLessThan(new DateTime(forecastExamine.getFinished()))) {
          TraceList traceList = (TraceList) traceMap.get(forecastExamine.getForecastName());
          if (traceList != null) {
            DateTime dt = new DateTime(forecastExamine.getFinished());
            traceList.setStatusDescription("Too late to complete. Next dose was expected before "
                + dt.toString("M/D/Y") + ".");
          }
        } else {
          traceMap.remove(forecastExamine.getForecastName());
          forecastListDueToday.add(forecastExamine);
        }
        it.remove();
      } else {
        traceMap.remove(forecastExamine.getForecastName());
      }
    }
    ForecastHandlerCore.sort(forecastListDueToday);
    ForecastHandlerCore.sort(resultList);

    for (Iterator it = traceMap.keySet().iterator(); it.hasNext();) {
      String key = (String) it.next();
      TraceList traceList = (TraceList) traceMap.get(key);
      if (traceList.getStatusDescription().equals("")) {
        traceList.setStatusDescription("Vaccination series complete.");
      }
    }

    ForecastReportPrinter forecastReportPrinter = new ForecastReportPrinter(forecastManager);
    if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_HTML)) {
      resp.setContentType("text/html");
      PrintWriter out = new PrintWriter(resp.getOutputStream());

      forecastReportPrinter.printHTMLVersionOfForecast(traceMap, resultList, imms, forecasterScheduleName,
          resultListOriginal, forecastListDueToday, forecastDate, dueUseEarly, doseList, out);
      out.close();

    } else if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_TEXT)) {
      resp.setContentType("text/plain");
      PrintWriter out = new PrintWriter(resp.getOutputStream());
      forecastReportPrinter.printTextVersionOfForecast(traceMap, resultList, imms, forecasterScheduleName,
          forecastListDueToday, forecastDate, dueUseEarly, doseList, out);
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
        if (cvxToVaccineIdMap.containsKey(vaccineCvx)) {
          vaccineId = cvxToVaccineIdMap.get(vaccineCvx);
        } else {
          vaccineId = cvxToVaccineIdMap.get("0" + vaccineCvx);
        }
        if (vaccineId == 0) {
          throw new ServletException("CVX code '" + vaccineCvx + "' is not recognized in parameter named 'vaccineCvx"
              + n + "'");
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
