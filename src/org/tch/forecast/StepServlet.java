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

import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.Trace;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Indicate;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.logic.ActionStep;
import org.tch.forecast.core.logic.ActionStepFactory;
import org.tch.forecast.core.logic.ChooseIndicatorStep;
import org.tch.forecast.core.logic.DataStore;
import org.tch.forecast.core.logic.EndStep;
import org.tch.forecast.core.logic.Event;
import org.tch.forecast.core.logic.LookForDoseStep;
import org.tch.forecast.core.logic.SetupStep;
import org.tch.forecast.core.logic.StartStep;
import org.tch.forecast.support.Immunization;
import org.tch.forecast.support.PatientRecordDataBean;
import org.tch.forecast.support.VaccineForecastManager;
import org.tch.forecast.validator.db.DatabasePool;
import org.tch.hl7.core.util.DateTime;

public class StepServlet extends HttpServlet
{
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    HttpSession session = req.getSession(true);
    try
    {
      out.println("<html>");
      out.println("  <head>");
      out.println("    <title>Step Through Forecaster</title>");
      out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"step.css\" />");
      out.println("  </head>");
      out.println("  <body>");
      String nextActionName = req.getParameter("nextActionName");
      if (nextActionName == null)
      {
        nextActionName = StartStep.NAME;
      }
      ActionStep actionStep = ActionStepFactory.get(nextActionName);
      DataStore dataStore = (DataStore) session.getAttribute("dataStore");

      if (nextActionName.equals(StartStep.NAME))
      {
        dataStore = new DataStore(new VaccineForecastManager());
        session.setAttribute("dataStore", dataStore);
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
        while (rset.next())
        {
          out.println("<option value=\"" + rset.getString(2) + "\">" + rset.getString(1) + ": " + rset.getString(3)
              + "</option>");
        }
        out.println("</select></td>");
        out.println("</tr>");
        sql = "SELECT line_code, line_label FROM forecast_line";
        pstmt = conn.prepareStatement(sql);
        rset = pstmt.executeQuery();
        out.println("<tr>");
        out.println("<td>Forecast Line</td>");
        out.println("<td><select name=\"lineCode\">");
        while (rset.next())
        {
          out.println("<option value=\"" + rset.getString(1) + "\">" + rset.getString(2) + "</option>");
        }
        out.println("</select></td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<input type=\"submit\" value=\"" + SetupStep.NAME + "\" name=\"nextActionName\"/>");
        out.println("</form>");
        pstmt.close();
        conn.close();
      } else if (nextActionName.equals(EndStep.NAME))
      {
        out.println("<h1>Step Through Forecaster</h1>");
        out.println("<a href=\"step\">Start Again</a>");
      } else
      {

        if (nextActionName.equals(SetupStep.NAME))
        {

          String caseId = req.getParameter("caseId");
          String lineCode = req.getParameter("lineCode");
          dataStore.setForecastCode(lineCode);

          Connection conn = DatabasePool.getConnection();
          PreparedStatement pstmt = null;
          ResultSet rset = null;

          String sql = "SELECT tv.cvx_code, cvx.cvx_label, date_format(admin_date, '%m/%d/%Y'), mvx_code, cvx.vaccine_id \n"
              + "FROM test_vaccine tv, vaccine_cvx cvx \n"
              + "WHERE tv.cvx_code = cvx.cvx_code \n"
              + "  AND tv.case_id = ? \n";
          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, caseId);
          rset = pstmt.executeQuery();
          List<Immunization> imms = new ArrayList<Immunization>();

          while (rset.next())
          {
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
          if (rset.next())
          {
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

          pstmt.close();
          conn.close();

        }
        String previousActionName = nextActionName;
        nextActionName = actionStep.doAction(dataStore);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String imageName = previousActionName + "-" + nextActionName + ".png";
        out.println("<table class=\"layout\">");
        out.println("  <tr class=\"layout\">");
        out.println("    <td valign=\"top\" class=\"layout\" width=\"667\">");
        out.println("      <a href=\"step?nextActionName=" + URLEncoder.encode(nextActionName, "UTF-8")
            + "\"><img src=\"img/" + imageName + "\"/ width=\"662\" height=\"240\" class=\"stepimg\"></a>");
        out.println("    <br/>");

        if (dataStore.getTraceBuffer() != null)
        {
          out.print(dataStore.getTraceBuffer());
          out.print("</ul>");
        }

        if (dataStore.getSchedule() != null && dataStore.getForecast() != null)
        {
          Map<String, Schedule> map = new HashMap<String, VaccineForecastDataBean.Schedule>();
          putOnMap(dataStore.getSchedule(), map, dataStore);
          int maxRow = 0;
          int maxColumn = 0;
          int minRow = Integer.MAX_VALUE;
          int minColumn = Integer.MAX_VALUE;

          for (int row = 1; row <= 10; row++)
          {
            for (int column = 1; column <= 10; column++)
            {
              Schedule schedule = map.get(column + "-" + row);
              if (schedule != null)
              {
                if (row > maxRow)
                {
                  maxRow = row;
                }
                if (column > maxColumn)
                {
                  maxColumn = column;
                }
                if (row < minRow)
                {
                  minRow = row;
                }
                if (column < minColumn)
                {
                  minColumn = column;
                }
              }
            }
          }
          out.println("      <table class=\"layout\">");
          for (int row = minRow; row <= maxRow; row++)
          {
            out.println("      <tr class=\"layout\">");
            for (int column = minColumn; column <= maxColumn; column++)
            {
              Schedule schedule = map.get(column + "-" + row);
              if (schedule != null)
              {
                out.println("        <td class=\"layout\" width=\"180\" valign=\"top\">");
                out.println("          <table width=\"100%\">");
                out.println("            <tr><th class=\"bigHeaderCentered\">" + schedule.getScheduleName()
                    + "</th></tr>");
                out.println("            <tr><th class=\"smallHeaderCentered\">" + schedule.getLabel() + "</th></tr>");
                String sc = " class=\"insideValue\"";
                if (schedule.equals(dataStore.getSchedule()))
                {
                  sc = " class=\"pass\"";
                }
                out.println("            <tr><td" + sc + ">");
                if (!schedule.getValidAge().isEmpty())
                {
                  out.println("Valid at " + schedule.getValidAge() + "<br/>");
                }
                if (!schedule.getDueAge().isEmpty())
                {
                  out.println("Due at " + schedule.getDueAge());
                }
                out.println("            </td></tr>");
                out.println("          </table>");
                int pos = -1;
                for (Indicate indicate : schedule.getIndicates())
                {
                  pos++;
                  out.println("<table width=\"100%\">");
                  out.println("     <tr>");
                  out.println("      <th class=\"smallHeaderCentered\" width=\"20%\">IF</th>");

                  String c = " class=\"insideValue\"";
                  if (schedule.equals(dataStore.getSchedule()) && pos == dataStore.getIndicatesPos()
                      && !nextActionName.equals(LookForDoseStep.NAME)
                      && !nextActionName.equals(ChooseIndicatorStep.NAME))
                  {
                    c = " class=\"pass\"";
                  }

                  out.println("<td" + c + " width=\"80%\">" + indicate.getVaccineName() + "  dose given");
                  if (!indicate.getAge().isEmpty())
                  {
                    out.println("<br> age < " + indicate.getAge());
                  }
                  if (!indicate.getMinInterval().isEmpty())
                  {
                    out.println("<br> interval >= " + indicate.getMinInterval());
                  }
                  if (!indicate.getMaxInterval().isEmpty())
                  {
                    out.println("<br> interval < " + indicate.getMaxInterval());
                  }
                  out.println("       </td>");
                  out.println("     </tr>");
                  out.println("     <tr>");
                  out.println("      <th class=\"smallHeaderCentered\" width=\"20%\">THEN</th>");
                  out.println("      <td" + c + " width=\"80%\">");
                  if (indicate.isComplete())
                  {
                    out.println("Series completed.");
                  } else if (indicate.isInvalid())
                  {
                    out.println("Dose invalid.");
                  } else if (indicate.isContra())
                  {
                    out.println("Dose contraindicated.");
                  } else
                  {
                    Schedule indicatedSchedule = dataStore.getForecast().getSchedules().get(indicate.getScheduleName());
                    if (indicatedSchedule != null)
                    {
                      out.println("Expect " + indicatedSchedule.getLabel() + " dose ("
                          + indicatedSchedule.getScheduleName() + ")");
                      if (!indicatedSchedule.getValidInterval().isEmpty())
                      {
                        out.println("<br> valid in " + indicatedSchedule.getValidInterval());
                      }
                      if (!indicatedSchedule.getDueInterval().isEmpty())
                      {
                        out.println("<br> due in " + indicatedSchedule.getDueInterval());
                      }
                    }
                  }
                  out.println("</tr>");
                  out.println("</table>");
                }
                out.println("        </td>");
              } else
              {
                out.println("        <td class=\"layout\" width=\"180\"></td>");
              }
            }
            out.println("      </tr>");
          }
          out.println("     </table>");
        }

        out.println("    </td>");
        out.println("    <td valign=\"top\" class=\"layout\">");
        if (dataStore.getResultList() != null && dataStore.getResultList().size() > 0)
        {
          List<ImmunizationForecastDataBean> resultList = dataStore.getResultList();
          out.println("<p>list size = " + resultList.size() + " </p>");
          out.println("<table width=\"500\">");
          out.println("  <tr>");
          out.println("    <th class=\"bigHeader\"\" colspan=\"7\">Forecast Results</th>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th class=\"smallHeader\"\">Forecast Label</th>");
          out.println("    <th class=\"smallHeader\"\">Dose</th>");
          out.println("    <th class=\"smallHeader\"\">Valid</th>");
          out.println("    <th class=\"smallHeader\"\">Due</th>");
          out.println("    <th class=\"smallHeader\"\">Overdue</th>");
          out.println("    <th class=\"smallHeader\"\">Finished</th>");
          out.println("    <th class=\"smallHeader\"\">Comment</th>");
          out.println("  </tr>");
          for (ImmunizationForecastDataBean result : resultList)
          {
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

        if (dataStore.getSchedule() != null)
        {
          out.println("      <table>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\"\">Schedule</th>");
          out.println("        <td class=\"insideValue\">" + dataStore.getSchedule().getScheduleName() + "</td>");
          out.println("        <th class=\"smallHeader\">Date of Birth</th>");
          out.println("        <td class=\"insideValue\">"
              + sdf.format(dataStore.getPatient().getDobDateTime().getDate()) + "</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">Label</th>");
          out.println("        <td class=\"insideValue\">" + dataStore.getSchedule().getLabel() + "</td>");
          out.println("        <th class=\"smallHeader\">Forecast Date</th>");
          out.println("        <td class=\"insideValue\">" + sdf.format(dataStore.getForecastDate()) + "</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">Dose</th>");
          out.println("        <td class=\"insideValue\">" + dataStore.getSchedule().getDose() + "</td>");
          out.println("        <th class=\"smallHeader\">Previous Event Date</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getPreviousEventDate()) + "</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">Next</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getNextAction()) + "</td>");
          out.println("        <th class=\"smallHeader\">&nbsp;</th>");
          out.println("        <td class=\"insideValue\">" + safe("&nbsp;") + "</td>");
          out.println("      </tr>");
          out.println("    </table>");
          out.println("      <table width=\"500\">");
          out.println("        <tr>");
          out.println("          <th class=\"smallHeader\">Vaccine</th>");
          out.println("          <th class=\"smallHeader\">Id</th>");
          out.println("          <th class=\"smallHeader\">Date</th>");
          out.println("          <th class=\"smallHeader\">MVX</th>");
          out.println("          <th class=\"smallHeader\">CVX</th>");
          out.println("          <th class=\"smallHeader\">Status</th>");
          out.println("          <th class=\"smallHeader\">Reason</th>");
          out.println("        </tr>");
          for (Immunization imm : dataStore.getVaccinations())
          {
            boolean foundEvent = false;
            Event event = dataStore.getEvent();
            if (event != null)
            {
              if (event.getEventDate().equals(imm.getDateOfShot()))
              {
                for (ImmunizationInterface immCompare : event.getImmList())
                {
                  if (immCompare.getVaccineId() == imm.getVaccineId())
                  {
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

            if (dataStore.getDoseList() != null)
            {
              for (VaccinationDoseDataBean dose : dataStore.getDoseList())
              {
                if (imm.getVaccineId() == dose.getVaccineId() && dose.getAdminDate().equals(imm.getDateOfShot()))
                {
                  if (dose.getStatusCode().equals(""))
                  {
                    out.println("    <td" + c + ">" + safe(dose.getStatusCode()) + "</td>");
                    out.println("    <td" + c + ">" + safe(dose.getReason()) + "</td>");
                  } else
                  {
                    out.println("    <td class=\"pass\">" + safe(dose.getStatusCode()) + "</td>");
                    out.println("    <td class=\"pass\">" + safe(dose.getReason()) + "</td>");
                  }
                  found = true;
                  break;
                }
              }
            }
            if (!found)
            {
              out.println("          <td" + c + ">&nbsp;</td>");
              out.println("          <td" + c + ">&nbsp;</td>");
            }
            out.println("        </tr>");
          }
          out.println("      </table>");
          out.println("    <table width=\"500\">");
          out.println("      <tr>");
          out.println("        <th class=\"bigHeader\" colspan=\"5\">Determine if dose is valid or when next is due</th>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">&nbsp;</th>");
          out.println("        <th class=\"smallHeader\">Age</th>");
          out.println("        <th class=\"smallHeader\">Interval</th>");
          out.println("        <th class=\"smallHeader\">Grace</th>");
          out.println("        <th class=\"smallHeader\">Date</th>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">Valid</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getValidAge()));
          if (!dataStore.getSchedule().getValidAge().isEmpty())
          {
            out.println(dataStore.getSchedule().getValidAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getValidInterval()));
          if (!dataStore.getSchedule().getValidInterval().isEmpty() && dataStore.getPreviousEventDate() != null)
          {
            out.println(dataStore.getSchedule().getValidInterval().getDateTimeFrom(dataStore.getPreviousEventDate())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getValidGrace()) + "</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getValid()) + "</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">Eary due</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getEarlyAge()));
          if (!dataStore.getSchedule().getEarlyAge().isEmpty())
          {
            out.println(dataStore.getSchedule().getEarlyAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getEarlyInterval()));
          if (!dataStore.getSchedule().getEarlyInterval().isEmpty() && dataStore.getPreviousEventDate() != null)
          {
            out.println(dataStore.getSchedule().getEarlyInterval().getDateTimeFrom(dataStore.getPreviousEventDate())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td class=\"insideValue\">&nbsp;</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getEarly()) + "</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">Due</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getDueAge()));
          if (!dataStore.getSchedule().getDueAge().isEmpty())
          {
            out.println(dataStore.getSchedule().getDueAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getDueInterval()));
          if (!dataStore.getSchedule().getDueInterval().isEmpty() && dataStore.getPreviousEventDate() != null)
          {
            out.println(dataStore.getSchedule().getDueInterval().getDateTimeFrom(dataStore.getPreviousEventDate())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td>&nbsp;</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getDue()) + "</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">Overdue</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getOverdueAge()));
          if (!dataStore.getSchedule().getOverdueAge().isEmpty())
          {
            out.println(dataStore.getSchedule().getOverdueAge()
                .getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getOverdueInterval()));
          if (!dataStore.getSchedule().getOverdueInterval().isEmpty() && dataStore.getPreviousEventDate() != null)
          {
            out.println(dataStore.getSchedule().getOverdueInterval().getDateTimeFrom(dataStore.getPreviousEventDate())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td>&nbsp;</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getOverdue()) + "</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\">Finished</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getFinishedAge()));
          if (!dataStore.getSchedule().getFinishedAge().isEmpty())
          {
            out.println(dataStore.getSchedule().getFinishedAge()
                .getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
          }
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getFinishedInterval()));
          if (!dataStore.getSchedule().getFinishedInterval().isEmpty() && dataStore.getPreviousEventDate() != null)
          {
            out.println(dataStore.getSchedule().getFinishedInterval().getDateTimeFrom(dataStore.getPreviousEventDate())
                .toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td>&nbsp;</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getFinished()) + "</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\" colspan=\"2\">After invalid dose</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getAfterInvalidInterval()));
          if (!dataStore.getSchedule().getAfterInvalidInterval().isEmpty() && dataStore.getPreviousEventDate() != null)
          {
            out.println(dataStore.getSchedule().getAfterInvalidInterval()
                .getDateTimeFrom(dataStore.getPreviousEventDate()).toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getAfterInvalidGrace())
              + "</td>");
          out.println("        <td>&nbsp;</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\" colspan=\"2\">After contraindicated dose</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getAfterContraInterval()));
          if (!dataStore.getSchedule().getAfterContraInterval().isEmpty() && dataStore.getPreviousEventDate() != null)
          {
            out.println(dataStore.getSchedule().getAfterContraInterval()
                .getDateTimeFrom(dataStore.getPreviousEventDate()).toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td>" + safe(dataStore.getSchedule().getAfterContraGrace()) + "</td>");
          out.println("        <td>&nbsp;</td>");
          out.println("      </tr>");
          out.println("      <tr>");
          out.println("        <th class=\"smallHeader\" colspan=\"2\">Dose before previous</th>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getBeforePreviousInterval()));
          if (!dataStore.getSchedule().getBeforePreviousInterval().isEmpty() && dataStore.getPreviousEventDate() != null)
          {
            out.println(dataStore.getSchedule().getBeforePreviousInterval()
                .getDateTimeFrom(dataStore.getPreviousEventDate()).toString("M/D/Y"));
          }
          out.println("</td>");
          out.println("        <td class=\"insideValue\">" + safe(dataStore.getSchedule().getBeforePreviousGrace())
              + "</td>");
          out.println("        <td>&nbsp;</td>");
          out.println("      </tr>");
          out.println("    </table>");
          out.println("    <table width=\"500\">");
          out.println("      <tr>");
          out.println("        <th class=\"bigHeader\" colspan=\"5\">If valid, pick the next schedule to use</th>");
          out.println("      </tr>");
          int pos = -1;
          out.println("  <tr>");
          out.println("    <th class=\"smallHeader\" width=\"20%\">Vaccine</th>");
          out.println("    <th class=\"smallHeader\" width=\"20%\">Schedule</th>");
          out.println("    <th class=\"smallHeader\" width=\"20%\">Before Age</th>");
          out.println("    <th class=\"smallHeader\" width=\"20%\">Min Int</th>");
          out.println("    <th class=\"smallHeader\" width=\"20%\">Max Int</th>");
          out.println("  </tr>");
          for (Indicate indicate : dataStore.getSchedule().getIndicates())
          {
            pos++;
            String c = " class=\"insideValue\"";
            if (pos == dataStore.getIndicatesPos())
            {
              c = " class=\"pass\"";
            }
            out.println("  <tr>");
            out.println("    <td" + c + ">" + safe(indicate.getVaccineName()) + "</td>");
            out.println("    <td" + c + ">" + safe(indicate.getScheduleName()) + "</td>");
            out.println("    <td" + c + ">" + safe(indicate.getAge()));
            if (!indicate.getAge().isEmpty())
            {
              out.println(indicate.getAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
            }
            out.println("</td>");
            out.println("    <td" + c + ">" + safe(indicate.getMinInterval()) + "</td>");
            out.println("    <td" + c + ">" + safe(indicate.getMaxInterval()) + "</td>");
            out.println("  </tr>");
            if (!indicate.getReason().equals(""))
            {
              out.println("  <tr>");
              out.println("    <td colspan=\"5\"" + c + ">&nbsp;&nbsp;&nbsp;" + safe(indicate.getReason()) + "</td>");
              out.println("  </tr>");
            }
          }
          out.println("</table>");
          out.println("<h4>Vaccines</h4>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>Vaccine</th>");
          out.println("    <th>Id</th>");
          out.println("  </tr>");
          for (String vaccineName : dataStore.getSchedule().getVaccines().keySet())
          {
            out.println("  <tr>");
            out.println("    <td class=\"insideValue\">" + vaccineName + "</td>");
            out.println("    <td class=\"insideValue\">" + dataStore.getSchedule().getVaccines().get(vaccineName)
                + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");
        }
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
        out.println("  </tr>");
        out.println("</table>");

        // All Data
        out.println("<br>");
        out.println("<br>");
        out.println("<br>");
        out.println("<br>");
        out.println("<br>");
        out.println("<br>");
        out.println("<br>");

        if (dataStore.getSeasonal() != null)
        {
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

        if (dataStore.getEventList() != null)
        {
          out.println("<h3>Event List</h3>");
          // protected List<Event> eventList = null;
          // protected int eventPosition = 0;
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th class=\"smallHeader\">Date</th>");
          out.println("    <th class=\"smallHeader\">Vaccines</th>");
          out.println("  <tr>");
          int pos = -1;
          for (Event event : dataStore.getEventList())
          {
            pos++;
            String c = " class=\"insideValue\"";
            if (pos == (dataStore.getEventPosition() - 1))
            {
              c = " class=\"pass\"";
            }
            out.println("  <tr>");
            out.println("    <td" + c + ">" + safe(sdf.format(event.getEventDate())) + "</td>");
            out.println("    <td" + c + ">");
            boolean first = true;
            for (ImmunizationInterface imm : event.getImmList())
            {
              if (!first)
              {
                out.print(", ");
                first = false;
              }
              out.println(imm.getVaccineId());
            }
            out.println("    </td>");
            out.println("  <tr>");

          }
          out.println("</table>");

        }
        if (dataStore.getForecast() != null)
        {
          out.println("<h3>Forecast</h3>");

          // protected VaccineForecastDataBean forecast = null;
        }

        if (dataStore.getResultList() != null)
        {
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
          for (ImmunizationForecastDataBean result : resultList)
          {
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
        if (dataStore.getScheduleList() != null)
        {
          out.println("<h3>Schedule List</h3>");
          // protected List<Schedule> scheduleList;
          // protected int scheduleListPos = -1;
          if (dataStore.getSchedule() != null)
          {
            // protected VaccineForecastDataBean.Schedule schedule;
          }
        }
        if (dataStore.getTraceList() != null)
        {
          // protected Trace trace = null;
          // protected TraceList traceList = null;
          // protected Map<String, List<Trace>> traces = null;
        }

      }
      out.println("  </body>");
      out.println("</html>");
    } catch (Exception e)
    {
      e.printStackTrace(out);
    } finally
    {
      out.close();
    }
  }

  public void putOnMap(Schedule schedule, Map<String, Schedule> map, DataStore dataStore)
  {
    if (schedule.getPosColumn() > 0 && schedule.getPosRow() > 0)
    {
      map.put(schedule.getPosColumn() + "-" + schedule.getPosRow(), schedule);
      for (Indicate indicate : schedule.getIndicates())
      {
        if (!indicate.isContra() && !indicate.isInvalid() && !indicate.isComplete())
        {
          Schedule indicatedSchedule = dataStore.getForecast().getSchedules().get(indicate.getScheduleName());
          if (indicatedSchedule != null)
          {
            String key = indicatedSchedule.getPosColumn() + "-" + indicatedSchedule.getPosRow();
            if (!map.containsKey(key))
            {
              putOnMap(indicatedSchedule, map, dataStore);
            }
          }
        }

      }
    }
  }

  private static String safe(Object o)
  {
    if (o == null)
    {
      return "-";
    } else
    {
      return o.toString();
    }
  }
}
