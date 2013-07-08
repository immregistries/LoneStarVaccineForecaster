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
import org.tch.forecast.core.SoftwareVersion;
import org.tch.forecast.core.TimePeriod;
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.api.impl.ForecastHandlerCore;
import org.tch.forecast.core.api.impl.ForecastOptions;
import org.tch.forecast.core.model.Immunization;
import org.tch.forecast.core.model.PatientRecordDataBean;
import org.tch.forecast.support.VaccineForecastManager;
import org.tch.forecast.validator.DataSourceUnavailableException;
import org.tch.forecast.validator.db.DatabasePool;

public class ForecastServlet extends HttpServlet {
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

  public static final String RESULT_FORMAT_TEXT = "text";
  public static final String RESULT_FORMAT_HTML = "html";

  private static ForecastHandlerCore forecastHandlerCore = null;

  @Override
  public void init() throws ServletException {

    super.init();
  }

  private static Map<String, Integer> cvxToVaccineIdMap = null;

  private void initCvxCodes() throws ServletException {
    if (forecastHandlerCore == null) {
      forecastHandlerCore = new ForecastHandlerCore(new VaccineForecastManager());
    }

    if (cvxToVaccineIdMap == null) {
      String url;

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

    if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_HTML)) {
      resp.setContentType("text/html");
    } else if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_TEXT)) {
      resp.setContentType("text/plain");
      PrintWriter out = new PrintWriter(resp.getOutputStream());

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

      out.println("TCH Immunization Forecaster");
      out.println();
      out.println("VACCINATIONS RECOMMENDED " + new DateTime(forecastDate.getDate()).toString("M/D/Y"));

      List<ImmunizationForecastDataBean> forecastList = forecastListDueToday;
      boolean vaccinesDueToday = false;
      for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();) {
        ImmunizationForecastDataBean forecast = it.next();
        String statusDescription;
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue(dueUseEarly));
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());
        DateTime today = new DateTime(forecastDate.getDate());
        if (today.isLessThan(dueDate)) {
          statusDescription = "";
        } else if (today.isLessThan(overdueDate)) {
          statusDescription = "due";
        } else if (today.isLessThan(finishedDate)) {
          statusDescription = "overdue";
        } else {
          continue;
        }
        vaccinesDueToday = true;

        String forecastDose = forecast.getDose();
        out.print("Forecasting " + forecast.getForecastLabel());
        out.print(" dose " + forecastDose);
        out.print(" due " + dueDate.toString("M/D/Y"));
        out.print(" valid " + validDate.toString("M/D/Y"));
        out.print(" overdue " + overdueDate.toString("M/D/Y"));
        out.print(" finished " + finishedDate.toString("M/D/Y"));
        out.println(" status " + statusDescription);

      }
      out.println();
      out.println("VACCCINATIONS RECOMMENDED AFTER " + new DateTime(forecastDate.getDate()).toString("M/D/Y"));

      forecastList = resultList;
      for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();) {
        ImmunizationForecastDataBean forecast = it.next();
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue(dueUseEarly));
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());
        String forecastDose = forecast.getDose();
        out.print("Forecasting " + forecast.getForecastLabel());
        out.print(" dose " + forecastDose);
        out.print(" due " + dueDate.toString("M/D/Y"));
        out.print(" valid " + validDate.toString("M/D/Y"));
        out.print(" overdue " + overdueDate.toString("M/D/Y"));
        out.println(" finished " + finishedDate.toString("M/D/Y"));
      }
      out.println();
      out.println("VACCINATIONS COMPLETED OR NOT RECOMMENDED");

      for (Iterator it = traceMap.keySet().iterator(); it.hasNext();) {
        String key = (String) it.next();
        TraceList traceList = (TraceList) traceMap.get(key);
        out.println("Forecasting " + traceList.getForecastLabel() + " complete");
      }

      out.println();
      out.println("IMMUNIZATION HISTORY");
      for (ImmunizationInterface imm : imms) {
        out.print("Vaccine " + imm.getCvx() + " (TCH " + imm.getVaccineId() + ")");
        out.println(" given " + new DateTime(imm.getDateOfShot()).toString("M/D/Y"));
      }
      out.println();
      out.println("Forecast generated " + new DateTime().toString("M/D/Y") + " according to schedule "
          + forecasterScheduleName + " using version " + SoftwareVersion.VERSION + " of the TCH Forecaster.");

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
    patient.setDob(new DateTime(patientDobString));
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
      imms.add(imm);
      n++;
    }

    forecastOptions.setFluSeasonDue(readTimePeriod(req, PARAM_FLU_SEASON_DUE));
    forecastOptions.setFluSeasonEnd(readTimePeriod(req, PARAM_FLU_SEASON_END));
    forecastOptions.setFluSeasonOverdue(readTimePeriod(req, PARAM_FLU_SEASON_OVERDUE));
    forecastOptions.setFluSeasonStart(readTimePeriod(req, PARAM_FLU_SEASON_START));

    dueUseEarly = readBoolean(req, PARAM_DUE_USE_EARLY);

  }
}
