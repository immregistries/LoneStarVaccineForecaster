package org.tch.forecast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import javax.servlet.http.HttpSession;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.Trace;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Contraindicate;
import org.tch.forecast.core.VaccineForecastDataBean.Indicate;
import org.tch.forecast.core.VaccineForecastDataBean.NamedVaccine;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.VaccineForecastDataBean.Transition;
import org.tch.forecast.core.logic.ActionStep;
import org.tch.forecast.core.logic.ActionStepFactory;
import org.tch.forecast.core.logic.ChooseIndicatorStep;
import org.tch.forecast.core.logic.DataStore;
import org.tch.forecast.core.logic.EndStep;
import org.tch.forecast.core.logic.Event;
import org.tch.forecast.core.logic.LookForDoseStep;
import org.tch.forecast.core.logic.SetupStep;
import org.tch.forecast.core.logic.StartStep;
import org.tch.forecast.core.model.Immunization;
import org.tch.forecast.core.model.PatientRecordDataBean;
import org.tch.forecast.support.VaccineForecastManager;
import org.tch.forecast.validator.db.DatabasePool;

public class StepServlet extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    HttpSession session = req.getSession(true);
    try {
      out.println("<html>");
      out.println("  <head>");
      out.println("    <title>Step Through Forecaster</title>");
      out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"step.css\" />");
      out.println("  </head>");
      out.println("  <body>");
      String userName = req.getParameter("userName");
      String caseId = req.getParameter("caseId");
      String nextActionName = req.getParameter("nextActionName");
      if (nextActionName == null) {
        nextActionName = StartStep.NAME;
      }
      if (caseId == null) {
        caseId = "";
      }
      ActionStep actionStep = ActionStepFactory.get(nextActionName);
      DataStore dataStore = (DataStore) session.getAttribute("dataStore");

      @SuppressWarnings("unchecked")
      Map<String, String> vaccineNames = (Map<String, String>) session.getAttribute("vaccineNames");

      if (nextActionName.equals(StartStep.NAME)) {

        Connection conn = DatabasePool.getConnection();
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        out.println("<h1>Step Through Forecaster</h1>");
        out.println("<form action=\"step\" method=\"GET\">");
        out.println("<table>");
        out.println("<tr>");
        out.println("<td>Test Case</td>");
        out.println("<td><select name=\"caseId\">");
        String sql = "select tg.group_label, tc.case_id, tc.case_label, tc.case_description, ts.status_label \n"
            + "from test_case tc, test_group tg, test_status ts \n" + "where tc.group_code = tg.group_code \n"
            + "  and tc.status_code = ts.status_code \n" + "order by tg.group_code, tc.case_id";
        pstmt = conn.prepareStatement(sql);
        rset = pstmt.executeQuery();
        while (rset.next()) {
          if (rset.getString(2).equals(caseId)) {
            out.println("<option value=\"" + rset.getString(2) + "\" selected=\"true\">" + rset.getString(1) + ": "
                + rset.getString(3) + "</option>");
          } else {
            out.println("<option value=\"" + rset.getString(2) + "\">" + rset.getString(1) + ": " + rset.getString(3)
                + "</option>");
          }
        }
        out.println("</select></td>");
        out.println("</tr>");
        sql = "SELECT line_code, line_label FROM forecast_line";
        pstmt = conn.prepareStatement(sql);
        rset = pstmt.executeQuery();
        out.println("<tr>");
        out.println("<td>Forecast Line</td>");
        out.println("<td><select name=\"lineCode\">");
        while (rset.next()) {
          out.println("<option value=\"" + rset.getString(1) + "\">" + rset.getString(2) + "</option>");
        }
        out.println("</select></td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<input type=\"hidden\" value=\"" + userName + "\" name=\"userName\"/>");
        out.println("<input type=\"hidden\" value=\"" + caseId + "\" name=\"caseId\"/>");
        out.println("<input type=\"submit\" value=\"" + SetupStep.NAME + "\" name=\"nextActionName\"/>");
        out.println("</form>");
        pstmt.close();
        conn.close();
      } else if (nextActionName.equals(EndStep.NAME)) {
        out.println("<h1>Finished</h1>");
      } else {
        if (nextActionName.equals(SetupStep.NAME)) {
          dataStore = new DataStore(new VaccineForecastManager());
          session.setAttribute("dataStore", dataStore);

          String lineCode = req.getParameter("lineCode");
          lineCode = convertLineCode(lineCode);
          dataStore.setForecastCode(lineCode);

          Connection conn = DatabasePool.getConnection();
          PreparedStatement pstmt = null;
          ResultSet rset = null;

          String sql = "SELECT tv.cvx_code, cvx.cvx_label, date_format(admin_date, '%m/%d/%Y'), mvx_code, cvx.vaccine_id \n"
              + "FROM test_vaccine tv, vaccine_cvx cvx \n"
              + "WHERE tv.cvx_code = cvx.cvx_code \n"
              + "  AND tv.case_id = ? \n" + "ORDER BY admin_date \n";
          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, caseId);
          rset = pstmt.executeQuery();
          List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();

          while (rset.next()) {
            Immunization imm = new Immunization();
            imm.setDateOfShot(new DateTime(rset.getString(3)).getDate());
            imm.setVaccineId(rset.getInt(5));
            imm.setLabel(rset.getString(2));
            imm.setCvx(rset.getString(1));
            imm.setMvx(rset.getString(4));
            imms.add(imm);
          }
          rset.close();
          pstmt.close();

          sql = "SELECT tc.case_label, tc.case_description, tc.case_source, tc.group_code, tc.patient_first, \n"
              + "tc.patient_last, date_format(tc.patient_dob, '%m/%d/%Y'), tc.patient_sex, tc.status_code, ts.status_label, \n"
              + "tc.forecast_date " + "FROM test_case tc, test_status ts\n" + "WHERE tc.case_id =" + caseId + " \n"
              + "  AND tc.status_code = ts.status_code\n";
          pstmt = conn.prepareStatement(sql);
          rset = pstmt.executeQuery();
          PatientRecordDataBean patient = new PatientRecordDataBean();
          Date forecastDate = null;
          if (rset.next()) {
            patient.setSex(rset.getString(8));
            patient.setDob(new DateTime(rset.getString(7)));
            forecastDate = rset.getDate(11);
          }

          dataStore.setPatient(patient);
          dataStore.setVaccinations(imms);
          dataStore.setForecastDate(forecastDate);
          List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
          List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
          StringBuffer traceBuffer = new StringBuffer();
          Map<String, List<Trace>> traces = new HashMap<String, List<Trace>>();
          dataStore.setResultList(resultList);
          dataStore.setDoseList(doseList);
          dataStore.setTraceBuffer(traceBuffer);
          dataStore.setTraces(traces);

          rset.close();
          pstmt.close();

          vaccineNames = new HashMap<String, String>();
          sql = "SELECT cvx.vaccine_id, cvx.cvx_label \n" + "FROM vaccine_cvx cvx \n";
          pstmt = conn.prepareStatement(sql);
          rset = pstmt.executeQuery();
          while (rset.next()) {
            vaccineNames.put(rset.getString(1), rset.getString(2));
          }
          session.setAttribute("vaccineNames", vaccineNames);

          pstmt.close();
          conn.close();

        }
        StringBuffer detailLog = new StringBuffer();
        dataStore.setDetailLog(detailLog);
        String previousActionName = nextActionName;
        nextActionName = actionStep.doAction(dataStore);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String imageName = previousActionName + "-" + nextActionName + ".png";
        out.println("<table class=\"layout\">");
        out.println("  <tr class=\"layout\">");
        out.println("    <td valign=\"top\" class=\"layout\" width=\"667\">");
        out.println("      <a href=\"step?nextActionName=" + URLEncoder.encode(nextActionName, "UTF-8") + "&userName="
            + URLEncoder.encode(userName, "UTF-8") + "&caseId=" + caseId + "\"><img src=\"img/" + imageName
            + "\"/ width=\"662\" height=\"240\" class=\"stepimg\"></a>");
        out.println("    <br/>");

        if (dataStore.getSchedule() != null && dataStore.getForecast() != null) {
          printSchedules(out, nextActionName, dataStore);
        }

        out.println("    </td>");
        out.println("    <td valign=\"top\" class=\"layout\">");
        if (dataStore.getResultList() != null && dataStore.getResultList().size() > 0) {
          List<ImmunizationForecastDataBean> resultList = dataStore.getResultList();
          out.println("<table width=\"500\">");
          out.println("  <tr>");
          out.println("    <th class=\"smallHeader\"\">Forecast</th>");
          out.println("    <th class=\"smallHeader\"\">Dose</th>");
          out.println("    <th class=\"smallHeader\"\">Valid</th>");
          out.println("    <th class=\"smallHeader\"\">Due</th>");
          out.println("    <th class=\"smallHeader\"\">Overdue</th>");
          out.println("    <th class=\"smallHeader\"\">Finished</th>");
          out.println("    <th class=\"smallHeader\"\">Comment</th>");
          out.println("  </tr>");
          for (ImmunizationForecastDataBean result : resultList) {
            out.println("  <tr>");
            out.println("    <td class=\"insideValue\">" + safe(result.getForecastLabel()) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(result.getDose()) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(sdf.format(result.getValid())) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(sdf.format(result.getDue())) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(sdf.format(result.getOverdue())) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(sdf.format(result.getFinished())) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(result.getComment()) + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");
        }

        if (dataStore.getDoseList() != null) {
          boolean found = false;
          for (VaccinationDoseDataBean dose : dataStore.getDoseList()) {
            if (dose.getStatusCode().equals(VaccinationDoseDataBean.STATUS_MISSED)) {
              if (!found) {
                found = true;
                out.println("<table>");
                out.println("  <tr>");
                out.println("    <th class=\"smallHeader\"\">Missed Dose</th>");
                out.println("    <th class=\"smallHeader\"\">Date</th>");
                out.println("  </tr>");
              }
              String vaccineName = vaccineNames.get("" + dose.getVaccineId());
              if (vaccineName == null) {
                vaccineName = "" + dose.getVaccineId();
              }
              out.println("    <td class=\"insideValue\">" + vaccineName + "</td>");
              out.println("    <td class=\"insideValue\">" + safe(dose.getAdminDate()) + "</td>");
            }
          }
          if (found) {
            out.println("</table>");
          }
        }

        Schedule schedule = dataStore.getSchedule();
        printSchedule(out, dataStore, schedule, vaccineNames);
        out.println("<h4>Additional Information</h4>");

        out.println("<table>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Forecast Code</th>");
        out.println("  <td>" + safe(dataStore.getForecastCode()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Sex</th>");
        out.println("    <td>" + dataStore.getPatient().getSex() + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Before Previous Event Date</th>");
        out.println("    <td>" + safe(dataStore.getBeforePreviousEventDate()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Due Reason</th>");
        out.println("    <td>" + safe(dataStore.getDueReason()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Has History of Varicella</th>");
        out.println("    <td>" + safe(dataStore.isHasHistoryOfVaricella()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Previous After Invalid Interval</th>");
        out.println("    <td>" + safe(dataStore.getPreviousAfterInvalidInterval()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Previous Event Date</th>");
        out.println("    <td>" + safe(dataStore.getPreviousEventDate()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Previous Event Date Valid</th>");
        out.println("    <td>" + safe(dataStore.getPreviousEventDateValid()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Previous Event Was Contra</th>");
        out.println("    <td>" + safe(dataStore.isPreviousEventWasContra()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Today</th>");
        out.println("    <td>" + safe(dataStore.getToday()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Valid Dose Count</th>");
        out.println("    <td>" + safe(dataStore.getValidDoseCount()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Valid Grace</th>");
        out.println("    <td>" + safe(dataStore.getValidGrace()) + "</td>");
        out.println("  </tr>");
        out.println("</table>");

        out.println("    </td>");
        out.println("    <td valign=\"top\" class=\"layout\">");
        if (dataStore.getTraceBuffer() != null) {
          out.print(dataStore.getTraceBuffer());
          if (dataStore.getTraceBuffer().toString().indexOf("</ul>") == -1) {
            out.print("</ul>");
          }
        }

        if (detailLog.length() > 0) {
          out.print("<pre>");
          out.print(detailLog);
          out.println("</pre>");
        }

        if (dataStore.getSeasonal() != null) {
          out.println("<h3>Seasonal</h3>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>Due</th>");
          out.println("    <td>" + safe(dataStore.getSeasonal().getDue()) + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>End</th>");
          out.println("    <td>" + safe(dataStore.getSeasonal().getEnd()) + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Overdue</th>");
          out.println("    <td>" + safe(dataStore.getSeasonal().getOverdue()) + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Start</th>");
          out.println("    <td>" + safe(dataStore.getSeasonal().getStart()) + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Season Start</th>");
          out.println("    <td>" + safe(dataStore.getSeasonStart()) + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Season End</th>");
          out.println("    <td>" + safe(dataStore.getSeasonEnd()) + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Season Completed</th>");
          out.println("    <td>" + safe(dataStore.isSeasonCompleted()) + "</td>");
          out.println("  </tr>");
          out.println("</table>");
        }

        if (dataStore.getTransitionList() != null) {
          out.println("<h3>Transitions</h3>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>Name</th>");
          out.println("    <th>Age</th>");
          out.println("    <th>Vaccine Id</th>");
          out.println("  </tr>");
          for (Transition transition : dataStore.getTransitionList()) {
            out.println("  <tr>");
            out.println("    <td>" + safe(transition.getName()) + "</td>");
            out.println("    <td>" + safe(transition.getAge()) + "</td>");
            out.println("    <td>" + safe(transition.getVaccineId()) + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");
        }

        if (dataStore.getForecast() != null) {
          out.println("<h3>Forecast</h3>");

          // protected VaccineForecastDataBean forecast = null;
        }

        if (dataStore.getResultList() != null) {
          out.println("<h3>Result List</h3>");
          List<ImmunizationForecastDataBean> resultList = dataStore.getResultList();
          out.println("<p>list size = " + resultList.size() + " </p>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>Forecast Name</th>");
          out.println("    <th>Forecast Label</th>");
          out.println("    <th>Comment</th>");
          out.println("    <th>Date Due</th>");
          out.println("    <th>Dose</th>");
          out.println("    <th>Due</th>");
          out.println("    <th>Early</th>");
          out.println("    <th>Finished</th>");
          out.println("    <th>Immregid</th>");
          out.println("    <th>Overdue</th>");
          out.println("    <th>Schedule</th>");
          out.println("    <th>Sort Order</th>");
          out.println("    <th>Valid</th>");
          out.println("  </tr>");
          for (ImmunizationForecastDataBean result : resultList) {
            out.println("  <tr>");
            out.println("    <td>" + safe(result.getForecastName()) + "</td>");
            out.println("    <td>" + safe(result.getForecastLabel()) + "</td>");
            out.println("    <td>" + safe(result.getComment()) + "</td>");
            out.println("    <td>" + safe(sdf.format(result.getDateDue())) + "</td>");
            out.println("    <td>" + safe(result.getDose()) + "</td>");
            out.println("    <td>" + safe(sdf.format(result.getDue())) + "</td>");
            out.println("    <td>" + safe(sdf.format(result.getEarly())) + "</td>");
            out.println("    <td>" + safe(sdf.format(result.getFinished())) + "</td>");
            out.println("    <td>" + safe(result.getImmregid()) + "</td>");
            out.println("    <td>" + safe(sdf.format(result.getOverdue())) + "</td>");
            out.println("    <td>" + safe(result.getSchedule()) + "</td>");
            out.println("    <td>" + safe(result.getSortOrder()) + "</td>");
            out.println("    <td>" + safe(sdf.format(result.getValid())) + "</td>");
            out.println("  </tr>");
          }

          out.println("</table>");
        }
        if (dataStore.getScheduleList() != null) {
          out.println("<h3>Schedule List</h3>");
          // protected List<Schedule> scheduleList;
          // protected int scheduleListPos = -1;
          if (dataStore.getSchedule() != null) {
            // protected VaccineForecastDataBean.Schedule schedule;
          }
        }
        if (dataStore.getTraceList() != null) {
          // protected Trace trace = null;
          // protected TraceList traceList = null;
          // protected Map<String, List<Trace>> traces = null;
        }

        out.println("    </td>");
        out.println("  </tr>");
        out.println("</table>");

      }
      out.println("<p>");
      out.println("[<a href=\"index.jsp?userName=" + URLEncoder.encode(userName, "UTF-8") + "\">Back to Home</a>]");
      out.println("[<a href=\"testCase.jsp?userName=" + URLEncoder.encode(userName, "UTF-8") + "&caseId=" + caseId
          + "\">Test Case</a>]");
      out.println("</p>");
      out.println("  </body>");
      out.println("</html>");
    } catch (Exception e) {
      e.printStackTrace(out);
    } finally {
      out.close();
    }
  }

  public static String convertLineCode(String lineCode) {
    if (lineCode.equals("MMR")) {
      lineCode = "Measles";
    } else if (lineCode.equals("HepB")) {
      lineCode = "HepB";
    } else if (lineCode.equals("Var")) {
      lineCode = "Varicella";
    } else if (lineCode.equals("Hib")) {
      lineCode = "Hib";
    } else if (lineCode.equals("DTaP")) {
      lineCode = "Diphtheria";
    } else if (lineCode.equals("HepA")) {
      lineCode = "HepA";
    } else if (lineCode.equals("Polio")) {
      lineCode = "Polio";
    } else if (lineCode.equals("Meni")) {
      lineCode = "Mening";
    } else if (lineCode.equals("Pneumococcal")) {
      lineCode = "Pneumo";
    } else if (lineCode.equals("HPV")) {
      lineCode = "HPV";
    } else if (lineCode.equals("Flu")) {
      lineCode = "Influenza";
    } else if (lineCode.equals("Rota")) {
      lineCode = "Rotavirus";
    }
    return lineCode;
  }

  private void printSchedules(PrintWriter out, String nextActionName, DataStore dataStore) {
    int indicatesPos = dataStore.getIndicatesPos();
    Schedule startingSchedule = dataStore.getSchedule();
    Map<String, Schedule> schedules = startingSchedule.getVaccineForecast().getSchedules();
    printSchedules(out, nextActionName, indicatesPos, startingSchedule, schedules);
  }

  public static void printSchedules(PrintWriter out, Schedule startingSchedule) {
    printSchedules(out, "", -1, startingSchedule, startingSchedule.getVaccineForecast().getSchedules());
  }

  private static void printSchedules(PrintWriter out, String nextActionName, int indicatesPos,
      Schedule startingSchedule, Map<String, Schedule> schedules) {
    Map<String, Schedule> map = new HashMap<String, VaccineForecastDataBean.Schedule>();
    putOnMap(startingSchedule, map, schedules);
    int maxRow = 0;
    int maxColumn = 0;
    int minRow = Integer.MAX_VALUE;
    int minColumn = Integer.MAX_VALUE;

    for (int row = 1; row <= 10; row++) {
      for (int column = 1; column <= 10; column++) {
        Schedule schedule = map.get(column + "-" + row);
        if (schedule != null) {
          if (row > maxRow) {
            maxRow = row;
          }
          if (column > maxColumn) {
            maxColumn = column;
          }
          if (row < minRow) {
            minRow = row;
          }
          if (column < minColumn) {
            minColumn = column;
          }
        }
      }
    }
    out.println("      <table class=\"layout\">");
    for (int row = minRow; row <= maxRow; row++) {
      out.println("      <tr class=\"layout\">");
      for (int column = minColumn; column <= maxColumn; column++) {
        Schedule schedule = map.get(column + "-" + row);
        if (schedule != null) {
          out.println("        <td class=\"layout\" width=\"180\" valign=\"top\">");
          out.println("          <table width=\"100%\">");
          out.println("            <tr><th class=\"bigHeaderCentered\">" + schedule.getScheduleName() + "</th></tr>");
          out.println("            <tr><th class=\"smallHeaderCentered\">" + schedule.getLabel() + "</th></tr>");
          String sc = " class=\"insideValue\"";
          if (schedule.equals(startingSchedule)) {
            sc = " class=\"pass\"";
          }
          out.println("            <tr><td" + sc + ">");
          if (!schedule.getValidAge().isEmpty()) {
            out.println("Valid at " + schedule.getValidAge() + "<br/>");
          }
          if (!schedule.getDueAge().isEmpty()) {
            out.println("Due at " + schedule.getDueAge());
          }
          out.println("            </td></tr>");
          out.println("          </table>");
          int pos = -1;
          for (Indicate indicate : schedule.getIndicates()) {
            pos++;
            out.println("<table width=\"100%\">");
            out.println("     <tr>");
            out.println("      <th class=\"smallHeaderCentered\" width=\"20%\">IF</th>");

            String c = " class=\"insideValue\"";
            if (schedule.equals(startingSchedule) && pos == indicatesPos
                && !nextActionName.equals(LookForDoseStep.NAME) && !nextActionName.equals(ChooseIndicatorStep.NAME)) {
              c = " class=\"pass\"";
            }

            if (indicate.getVaccines() != null && indicate.getVaccines().length > 0
                && indicate.getVaccines()[0].getVaccineId() < 0) {
              out.println("<td" + c + " width=\"80%\">" + indicate.getVaccineName());
            } else {
              out.println("<td" + c + " width=\"80%\">" + indicate.getVaccineName() + " dose given");
            }
            if (!indicate.getAge().isEmpty()) {
              out.println("<br> age < " + indicate.getAge());
            }
            if (!indicate.getMinInterval().isEmpty()) {
              out.println("<br> interval >= " + indicate.getMinInterval());
            }
            if (!indicate.getMaxInterval().isEmpty()) {
              out.println("<br> interval < " + indicate.getMaxInterval());
            }
            out.println("       </td>");
            out.println("     </tr>");
            out.println("     <tr>");
            out.println("      <th class=\"smallHeaderCentered\" width=\"20%\">THEN</th>");
            out.println("      <td" + c + " width=\"80%\">");
            if (indicate.isComplete()) {
              out.println("Series completed.");
            } else if (indicate.isInvalid()) {
              out.println("Dose invalid.");
            } else if (indicate.isContra()) {
              out.println("Dose contraindicated.");
            } else {
              Schedule indicatedSchedule = schedules.get(indicate.getScheduleName());
              if (indicatedSchedule != null) {
                out.println("Expect " + indicatedSchedule.getLabel() + " dose (" + indicatedSchedule.getScheduleName()
                    + ")");
                if (!indicatedSchedule.getValidInterval().isEmpty()) {
                  out.println("<br> valid in " + indicatedSchedule.getValidInterval());
                }
                if (!indicatedSchedule.getDueInterval().isEmpty()) {
                  out.println("<br> due in " + indicatedSchedule.getDueInterval());
                }
              }
            }
            out.println("</tr>");
            out.println("</table>");
          }
          out.println("        </td>");
        } else {
          out.println("        <td class=\"layout\" width=\"180\"></td>");
        }
      }
      out.println("      </tr>");
    }
    out.println("     </table>");
  }

  public static void printSchedule(PrintWriter out, Schedule schedule) {
    printSchedule(out, null, schedule, null);
  }

  private static void printSchedule(PrintWriter out, DataStore dataStore, Schedule schedule,
      Map<String, String> vaccineNames) {

    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    out.println("      <table>");
    out.println("      <tr>");
    out.println("        <th class=\"smallHeader\"\">Schedule</th>");
    if (schedule != null) {
      out.println("        <td class=\"insideValue\">" + schedule.getScheduleName() + "</td>");
    } else {
      out.println("        <td class=\"insideValue\">&nbsp;</td>");
    }
    if (dataStore != null) {
      out.println("        <th class=\"smallHeader\">Date of Birth</th>");
      out.println("        <td class=\"insideValue\">" + sdf.format(dataStore.getPatient().getDobDateTime().getDate())
          + "</td>");
    }
    out.println("      </tr>");
    out.println("      <tr>");
    out.println("        <th class=\"smallHeader\">Label</th>");
    if (schedule != null) {
      out.println("        <td class=\"insideValue\">" + schedule.getLabel() + "</td>");
    } else {
      out.println("        <td class=\"insideValue\">&nbsp;</td>");
    }
    if (dataStore != null) {
      out.println("        <th class=\"smallHeader\">Forecast Date</th>");
      out.println("        <td class=\"insideValue\">" + sdf.format(dataStore.getForecastDate()) + "</td>");
    }
    out.println("      </tr>");
    out.println("      <tr>");
    out.println("        <th class=\"smallHeader\">Dose</th>");
    if (schedule != null) {
      out.println("        <td class=\"insideValue\">" + schedule.getDose() + "</td>");
    } else {
      out.println("        <td class=\"insideValue\">&nbsp;</td>");
    }
    if (dataStore != null) {
      out.println("        <th class=\"smallHeader\">Previous Event Date</th>");
      out.println("        <td class=\"insideValue\">" + safe(dataStore.getPreviousEventDate()) + "</td>");
    }
    out.println("      </tr>");
    if (dataStore != null) {
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Next</th>");
      out.println("        <td class=\"insideValue\">" + safe(dataStore.getNextAction()) + "</td>");
      out.println("        <th class=\"smallHeader\">Previous Vaccine Ids</th>");
      out.println("        <td class=\"insideValue\">");
      if (dataStore.getPreviousVaccineIdList() == null || dataStore.getPreviousVaccineIdList().size() == 0) {
        out.println("-");
      } else {
        boolean first = true;
        for (Integer vaccineId : dataStore.getPreviousVaccineIdList()) {
          String vaccineName = vaccineNames.get("" + vaccineId);
          if (vaccineName == null) {
            vaccineName = "" + vaccineId;
          }
          if (!first) {
            out.print(", ");
          }
          first = false;
          out.print(vaccineName);
        }
      }
      out.println();
      out.println("</td>");
      out.println("      </tr>");
      out.println("    </table>");
    }
    if (dataStore != null) {
      out.println("      <table width=\"500\">");
      out.println("        <tr>");
      out.println("          <th class=\"smallHeader\">Vaccine</th>");
      out.println("          <th class=\"smallHeader\">Id</th>");
      out.println("          <th class=\"smallHeader\">Date</th>");
      out.println("          <th class=\"smallHeader\">CVX</th>");
      out.println("          <th class=\"smallHeader\">MVX</th>");
      out.println("          <th class=\"smallHeader\">Status</th>");
      out.println("          <th class=\"smallHeader\">Reason</th>");
      out.println("        </tr>");
      for (ImmunizationInterface imm : dataStore.getVaccinations()) {
        boolean foundEvent = false;
        Event event = dataStore.getEvent();
        if (event != null) {
          if (event.getEventDate().equals(imm.getDateOfShot())) {
            for (ImmunizationInterface immCompare : event.getImmList()) {
              if (immCompare.getVaccineId() == imm.getVaccineId()) {
                foundEvent = true;
                break;
              }
            }
          }
        }
        String c = foundEvent ? " class=\"pass\"" : " class=\"insideValue\"";

        out.println("        <tr>");
        out.println("          <td" + c + ">" + imm.getLabel() + "</td>");
        out.println("          <td" + c + ">" + imm.getVaccineId() + "</td>");
        out.println("          <td" + c + ">" + sdf.format(imm.getDateOfShot()) + "</td>");
        out.println("          <td" + c + ">" + imm.getCvx() + "</td>");
        out.println("          <td" + c + ">" + imm.getMvx() + "</td>");
        boolean found = false;

        if (dataStore.getDoseList() != null) {
          for (VaccinationDoseDataBean dose : dataStore.getDoseList()) {
            if (imm.getVaccineId() == dose.getVaccineId() && dose.getAdminDate().equals(imm.getDateOfShot())) {
              if (dose.getStatusCode().equals("")) {
                out.println("    <td" + c + ">" + safe(dose.getStatusCode()) + "</td>");
                out.println("    <td" + c + ">" + safe(dose.getReason()) + "</td>");
              } else {
                out.println("    <td class=\"pass\">" + safe(dose.getStatusCode()) + "</td>");
                out.println("    <td class=\"pass\">" + safe(dose.getReason()) + "</td>");
              }
              found = true;
              break;
            }
          }
        }
        if (!found) {
          out.println("          <td" + c + ">&nbsp;</td>");
          out.println("          <td" + c + ">&nbsp;</td>");
        }
        out.println("        </tr>");
      }
      out.println("      </table>");
    }

    if (schedule != null) {

      out.println("    <table width=\"500\">");
      out.println("      <tr>");
      out.println("        <th class=\"bigHeader\" colspan=\"5\">Determine if dose is valid or when next is due</th>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">&nbsp;</th>");
      out.println("        <th class=\"smallHeader\">Age</th>");
      out.println("        <th class=\"smallHeader\">Interval</th>");
      out.println("        <th class=\"smallHeader\">Grace</th>");
      out.println("        <th class=\"smallHeader\">" + (dataStore != null ? "Date" : "") + "</th>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Valid</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getValidAge()));
      if (dataStore != null && !schedule.getValidAge().isEmpty()) {
        out.println(schedule.getValidAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getValidInterval()));
      if (dataStore != null && !schedule.getValidInterval().isEmpty() && dataStore.getPreviousEventDate() != null) {
        out.println(schedule.getValidInterval().getDateTimeFrom(dataStore.getPreviousEventDate()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getValidGrace()) + "</td>");
      out.println("        <td class=\"insideValue\">" + safe(dataStore != null && dataStore.getValid() != null ? dataStore.getValid().toString("M/D/Y") : "") + "</td>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Eary due</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getEarlyAge()));
      if (dataStore != null && !schedule.getEarlyAge().isEmpty()) {
        out.println(schedule.getEarlyAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getEarlyInterval()));
      if (dataStore != null && !schedule.getEarlyInterval().isEmpty() && dataStore.getPreviousEventDate() != null) {
        out.println(schedule.getEarlyInterval().getDateTimeFrom(dataStore.getPreviousEventDate()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">&nbsp;</td>");
      out.println("        <td class=\"insideValue\">" + safe((dataStore != null && dataStore.getEarly() != null ? dataStore.getEarly().toString("M/D/Y") : ""))
          + "</td>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Due</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getDueAge()));
      if (dataStore != null && !schedule.getDueAge().isEmpty()) {
        out.println(schedule.getDueAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getDueInterval()));
      if (dataStore != null && !schedule.getDueInterval().isEmpty() && dataStore.getPreviousEventDate() != null) {
        out.println(schedule.getDueInterval().getDateTimeFrom(dataStore.getPreviousEventDate()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td>&nbsp;</td>");
      out.println("        <td class=\"insideValue\">" + safe(dataStore != null && dataStore.getDue() != null ? dataStore.getDue().toString("M/D/Y") : "") + "</td>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Overdue</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getOverdueAge()));
      if (dataStore != null && !schedule.getOverdueAge().isEmpty()) {
        out.println(schedule.getOverdueAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getOverdueInterval()));
      if (dataStore != null && !schedule.getOverdueInterval().isEmpty() && dataStore.getPreviousEventDate() != null) {
        out.println(schedule.getOverdueInterval().getDateTimeFrom(dataStore.getPreviousEventDate()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td>&nbsp;</td>");
      out.println("        <td class=\"insideValue\">" + safe(dataStore != null ? dataStore.getOverdue() : "")
          + "</td>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Finished</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getFinishedAge()));
      if (dataStore != null && !schedule.getFinishedAge().isEmpty()) {
        out.println(schedule.getFinishedAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime())
            .toString("M/D/Y"));
      }
      out.println("        <td class=\"insideValue\">" + safe(schedule.getFinishedInterval()));
      if (dataStore != null && !schedule.getFinishedInterval().isEmpty() && dataStore.getPreviousEventDate() != null) {
        out.println(schedule.getFinishedInterval().getDateTimeFrom(dataStore.getPreviousEventDate()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td>&nbsp;</td>");
      out.println("        <td class=\"insideValue\">" + safe(dataStore != null ? dataStore.getFinished() : "")
          + "</td>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\" colspan=\"2\">After invalid dose</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getAfterInvalidInterval()));
      if (dataStore != null && schedule.getAfterInvalidInterval() != null
          && !schedule.getAfterInvalidInterval().isEmpty() && dataStore.getPreviousEventDate() != null) {
        out.println(schedule.getAfterInvalidInterval().getDateTimeFrom(dataStore.getPreviousEventDate())
            .toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getAfterInvalidGrace()) + "</td>");
      out.println("        <td>&nbsp;</td>");
      out.println("      </tr>");
      if (schedule.getAfterContraInterval() != null && !schedule.getAfterContraInterval().isEmpty()) {
        out.println("      <tr>");
        out.println("        <th class=\"smallHeader\" colspan=\"2\">After contraindicated dose</th>");
        out.println("        <td class=\"insideValue\">" + safe(schedule.getAfterContraInterval()));
        if (dataStore != null && schedule.getAfterContraInterval() != null
            && !schedule.getAfterContraInterval().isEmpty() && dataStore.getPreviousEventDate() != null) {
          out.println(schedule.getAfterContraInterval().getDateTimeFrom(dataStore.getPreviousEventDate())
              .toString("M/D/Y"));
        }
        out.println("</td>");
        out.println("        <td>" + safe(schedule.getAfterContraGrace()) + "</td>");
        out.println("        <td>&nbsp;</td>");
        out.println("      </tr>");
      }
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\" colspan=\"2\">Dose before previous</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getBeforePreviousInterval()));
      if (dataStore != null && schedule.getBeforePreviousInterval() != null
          && !schedule.getBeforePreviousInterval().isEmpty() && dataStore.getPreviousEventDate() != null) {
        out.println(schedule.getBeforePreviousInterval().getDateTimeFrom(dataStore.getPreviousEventDate())
            .toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getBeforePreviousGrace()) + "</td>");
      out.println("        <td>&nbsp;</td>");
      out.println("      </tr>");
      out.println("    </table>");
      if (schedule.getContraindicates() != null && schedule.getContraindicates().length > 0) {
        out.println("    <table width=\"500\">");
        out.println("      <tr>");
        out.println("        <th class=\"bigHeader\" colspan=\"5\">Check for contraindications</th>");
        out.println("      </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Vaccine</th>");
        out.println("    <th class=\"smallHeader\">After Interval</th>");
        out.println("    <th class=\"smallHeader\">Before Age</th>");
        out.println("    <th class=\"smallHeader\">Grace</th>");
        out.println("  </tr>");
        for (Contraindicate contraindicate : schedule.getContraindicates()) {
          out.println("  <tr>");
          out.println("    <td class=\"insideValue\">" + safe(contraindicate.getVaccineName()) + "</td>");
          out.println("    <td class=\"insideValue\">" + safe(contraindicate.getAfterInterval()) + "</td>");
          out.println("    <td class=\"insideValue\">" + safe(contraindicate.getAge()));
          if (dataStore != null && !contraindicate.getAge().isEmpty()) {
            out.println(contraindicate.getAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("    <td class=\"insideValue\">" + safe(contraindicate.getGrace()) + "</td>");
          out.println("  </tr>");
          if (!contraindicate.getReason().equals("")) {
            out.println("  <tr>");
            out.println("    <td colspan=\"5\" class=\"insideValue\">&nbsp;&nbsp;&nbsp;"
                + safe(contraindicate.getReason()) + "</td>");
            out.println("  </tr>");
          }
        }
        out.println("</table>");
      }
      out.println("    <table width=\"500\">");
      out.println("      <tr>");
      out.println("        <th class=\"bigHeader\" colspan=\"5\">If valid, pick the next schedule to use</th>");
      out.println("      </tr>");
      int pos = -1;
      out.println("  <tr>");
      out.println("    <th class=\"smallHeader\">Vaccine</th>");
      out.println("    <th class=\"smallHeader\">Schedule</th>");
      out.println("    <th class=\"smallHeader\">Before Age</th>");
      out.println("    <th class=\"smallHeader\">Other</th>");
      out.println("  </tr>");
      for (Indicate indicate : schedule.getIndicates()) {
        pos++;
        String c = " class=\"insideValue\"";
        if (dataStore != null && pos == dataStore.getIndicatesPos()) {
          c = " class=\"pass\"";
        }
        out.println("  <tr>");
        out.println("    <td" + c + ">" + safe(indicate.getVaccineName()) + "</td>");
        out.println("    <td" + c + ">" + safe(indicate.getScheduleName()) + "</td>");
        out.println("    <td" + c + ">" + safe(indicate.getAge()));
        if (dataStore != null && !indicate.getAge().isEmpty()) {
          out.println(indicate.getAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
        }
        out.println("</td>");
        out.println("    <td" + c + ">");
        if (indicate.getMinInterval() != null && !indicate.getMinInterval().isEmpty()) {
          out.println(indicate.getMinInterval() + " min interval");
        }
        if (indicate.getPreviousVaccineName() != null && !indicate.getPreviousVaccineName().equals("")) {
          out.println(" after " + indicate.getPreviousVaccineName());
        }
        if (indicate.getHistoryOfVaccineName() != null && !indicate.getHistoryOfVaccineName().equals("")) {
          out.println(" previously received " + indicate.getHistoryOfVaccineName());
        }
        if (indicate.isHashHad()) {
          out.println(" has had " + indicate.getHasHad());
        }

        out.println("</td>");
        out.println("  </tr>");
        if (!indicate.getReason().equals("")) {
          out.println("  <tr>");
          out.println("    <td colspan=\"5\"" + c + ">&nbsp;&nbsp;&nbsp;" + safe(indicate.getReason()) + "</td>");
          out.println("  </tr>");
        }
      }
      out.println("</table>");

      if (dataStore.getEventList() != null) {
        // protected int eventPosition = 0;
        out.println("<table>");
        out.println("      <tr>");
        out.println("        <th class=\"bigHeader\" colspan=\"" + (dataStore.getEventList().size() + 1)
            + "\">Event List</th>");
        out.println("      </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Date</th>");
        out.println("    <th class=\"smallHeader\">Vaccine</th>");
        out.println("    <th class=\"smallHeader\">Indicated Event</th>");
        out.println("  </tr>");
        pos = -1;
        for (Event event : dataStore.getEventList()) {
          pos++;
          String c = " class=\"insideValue\"";
          if (pos == (dataStore.getEventPosition() - 1)) {
            c = " class=\"pass\"";
          }
          out.println("  <tr>");
          out.println("    <td" + c + ">" + safe(sdf.format(event.getEventDate())) + " </td>");
          out.println("    <td" + c + ">");
          boolean first = true;
          for (ImmunizationInterface imm : event.getImmList()) {
            if (!first) {
              out.print(", ");
              first = false;
            }
            out.println(imm.getVaccineId());
          }
          out.println("    </td>");
          if (pos == (dataStore.getEventPosition() - 1) && dataStore.getEvent() != null) {
            out.println("    <td" + c + ">" + (dataStore.getEvent().isHasEvent() ? "YES" : "NO") + " </td>");
          } else {
            out.println("    <td" + c + ">&nbsp;</td>");

          }

          out.println("  </tr>");
        }
        out.println("</table>");

      }

      out.println("<h4>Vaccines</h4>");
      out.println("<table>");
      out.println("  <tr>");
      out.println("    <th>Vaccine</th>");
      out.println("    <th>Id</th>");
      out.println("  </tr>");
      for (String vaccineName : schedule.getVaccines().keySet()) {
        out.println("  <tr>");
        out.println("    <td class=\"insideValue\">" + vaccineName + "</td>");
        NamedVaccine namedVaccine = schedule.getVaccines().get(vaccineName);
        if (namedVaccine.getValidStartDate() != null) {

          out.println("    <td class=\"insideValue\">" + namedVaccine.getVaccineIds() + " given after "
              + sdf.format(namedVaccine.getValidStartDate()) + "</td>");
        } else {
          out.println("    <td class=\"insideValue\">" + namedVaccine.getVaccineIds() + "</td>");
        }
        out.println("  </tr>");
      }
      out.println("</table>");
    }
  }

  public static void putOnMap(Schedule startingSchedule, Map<String, Schedule> map, Map<String, Schedule> schedules) {
    if (startingSchedule.getPosColumn() > 0 && startingSchedule.getPosRow() > 0) {
      map.put(startingSchedule.getPosColumn() + "-" + startingSchedule.getPosRow(), startingSchedule);
      for (Indicate indicate : startingSchedule.getIndicates()) {
        if (!indicate.isContra() && !indicate.isInvalid() && !indicate.isComplete()) {
          Schedule indicatedSchedule = schedules.get(indicate.getScheduleName());
          if (indicatedSchedule != null) {
            String key = indicatedSchedule.getPosColumn() + "-" + indicatedSchedule.getPosRow();
            if (!map.containsKey(key)) {
              putOnMap(indicatedSchedule, map, schedules);
            }
          }
        }

      }
    }
  }

  private static String safe(Date d) {
    if (d == null) {
      return "-";
    } else {
      DateTime dt = new DateTime(d);
      return dt.toString("M/D/Y");
    }
  }

  private static String safe(DateTime dt) {
    if (dt == null) {
      return "-";
    } else {
      return dt.toString("M/D/Y");
    }
  }

  private static String safe(Object o) {
    if (o == null) {
      return "-";
    } else {
      return o.toString();
    }
  }
}
