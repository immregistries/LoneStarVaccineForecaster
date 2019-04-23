package org.immregistries.lonestar;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.DecisionProcessFormat;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.Transition;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.VaccineForecastDataBean;
import org.immregistries.lonestar.core.VaccineForecastDataBean.Contraindicate;
import org.immregistries.lonestar.core.VaccineForecastDataBean.Indicate;
import org.immregistries.lonestar.core.VaccineForecastDataBean.InvalidateSameDay;
import org.immregistries.lonestar.core.VaccineForecastDataBean.NamedVaccine;
import org.immregistries.lonestar.core.VaccineForecastDataBean.Schedule;
import org.immregistries.lonestar.core.VaccineForecastDataBean.ValidVaccine;
import org.immregistries.lonestar.core.api.impl.ForecastAntigen;
import org.immregistries.lonestar.core.api.impl.ForecastOptions;
import org.immregistries.lonestar.core.logic.ActionStep;
import org.immregistries.lonestar.core.logic.ActionStepFactory;
import org.immregistries.lonestar.core.logic.BlackOut;
import org.immregistries.lonestar.core.logic.ChooseIndicatorStep;
import org.immregistries.lonestar.core.logic.DataStore;
import org.immregistries.lonestar.core.logic.EndStep;
import org.immregistries.lonestar.core.logic.Event;
import org.immregistries.lonestar.core.logic.FinishStep;
import org.immregistries.lonestar.core.logic.LookForDoseStep;
import org.immregistries.lonestar.core.logic.SetupStep;
import org.immregistries.lonestar.core.logic.StartStep;
import org.immregistries.lonestar.core.model.Assumption;

public class StepServlet extends ForecastServlet
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String nextActionName = req.getParameter("nextActionName");
    if (nextActionName == null) {
      nextActionName = StartStep.NAME;
    }
    initSchedule(SCHEDULE_NAME_DEFAULT);

    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    HttpSession session = req.getSession(true);
    try {
      out.println("<html>");
      out.println("  <head>");
      out.println("    <title>Step Through Forecaster</title>");
      out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"step.css\" />");
      out.println("  </head>");
      out.println("  <body>");
      String nextEventPosition = req.getParameter("nextEventPosition");
      ActionStep actionStep = ActionStepFactory.get(nextActionName);
      DataStore dataStore = (DataStore) session.getAttribute("dataStore");

      if (nextActionName.equals(StartStep.NAME)) {
        out.println("<h1>Step Through Forecaster</h1>");
        out.println("<form action=\"step\" method=\"GET\">");
        out.println("<table>");
        out.println("<tr>");
        out.println("<td>Forecast Line</td>");
        out.println("<td><select name=\"lineCode\">");
        for (ForecastAntigen forecastAntigen : ForecastAntigen.getForecastAntigenList()) {
          out.println("<option value=\"" + forecastAntigen.getForecastCode() + "\">"
              + forecastAntigen.getForecastLabel() + "</option>");
        }
        out.println("</select></td>");
        out.println("</tr>");
        out.println("</table>");
        out.println("<input type=\"submit\" value=\"" + SetupStep.NAME + "\" name=\"nextActionName\"/>");
        out.println("</form>");

        ForecastInput forecastInput = new ForecastInput();
        super.readRequest(req, forecastInput);

        dataStore = new DataStore(forecastManager);
        session.setAttribute("dataStore", dataStore);
        dataStore.setPatient(forecastInput.patient);
        dataStore.setVaccinations(forecastInput.imms);
        dataStore.setForecastDate(forecastInput.forecastDate.getDate());
        dataStore.setForecastOptions(forecastInput.forecastOptions);
        List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
        List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
        Map<String, List<Trace>> traces = new HashMap<String, List<Trace>>();
        dataStore.setResultList(resultList);
        dataStore.setDoseList(doseList);
        dataStore.setTraces(traces);

      } else if (nextActionName.equals(EndStep.NAME)) {
        out.println("<h1>Finished</h1>");
      } else {
        if (nextActionName.equals(SetupStep.NAME)) {
          String lineCode = req.getParameter("lineCode");
          //            lineCode = convertLineCode(lineCode);
          dataStore.setForecastCode(lineCode);
        }
        StringBuffer detailLog = new StringBuffer();
        dataStore.setDetailLog(detailLog);
        String previousActionName = nextActionName;
        nextActionName = actionStep.doAction(dataStore);
        if (nextEventPosition != null) {
          int nextEventPositionInt = Integer.parseInt(nextEventPosition);
          while (dataStore.getEventPosition() < nextEventPositionInt && !nextActionName.equals(FinishStep.NAME)) {
            actionStep = ActionStepFactory.get(nextActionName);
            detailLog = new StringBuffer();
            dataStore.setDetailLog(detailLog);
            previousActionName = nextActionName;
            nextActionName = actionStep.doAction(dataStore);
          }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String imageName = previousActionName + "-" + nextActionName + ".png";
        out.println("<table class=\"layout\">");
        out.println("  <tr class=\"layout\">");
        out.println("    <td valign=\"top\" class=\"layout\" width=\"667\">");
        String baseLink;
        baseLink = "step?nextActionName=" + URLEncoder.encode(nextActionName, "UTF-8");
        out.println("      <a href=\"" + baseLink + "\"><img src=\"img/" + imageName
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
            out.println("    <td class=\"insideValue\">" + safe(result.getValid()) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(result.getDue()) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(result.getOverdue()) + "</td>");
            out.println("    <td class=\"insideValue\">" + safe(result.getFinished()) + "</td>");
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
              String vaccineName = forecastManager.getVaccineName(dose.getVaccineId());
              out.println("    <td class=\"insideValue\">" + vaccineName + "</td>");
              out.println("    <td class=\"insideValue\">" + safe(dose.getAdminDate()) + "</td>");
            }
          }
          if (found) {
            out.println("</table>");
          }
        }

        Schedule schedule = dataStore.getSchedule();
        printSchedule(out, dataStore, schedule, baseLink);
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
        out.println("    <th class=\"smallHeader\">Previous Event Date Valid, But not Birth</th>");
        out.println("    <td>" + safe(dataStore.getPreviousEventDateValidNotBirth()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Previous Event Was Contra</th>");
        out.println("    <td>" + safe(dataStore.isPreviousEventWasContra()) + "</td>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Forecast Date</th>");
        out.println("    <td>" + safe(dataStore.getForecastDate()) + "</td>");
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
        if (dataStore.getTrace() != null) {
          String explanation = dataStore.getTraceList().getExplanation(DecisionProcessFormat.HTML);
          out.print(explanation);
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
          out.println("    <th>Season Start</th>");
          out.println("    <td>" + safe(dataStore.getSeasonStartDateTime()) + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Season End</th>");
          out.println("    <td>" + safe(dataStore.getSeasonEndDateTime()) + "</td>");
          out.println("  </tr>");
          out.println("</table>");
        }

        if (dataStore.getAssumptionList() != null && dataStore.getAssumptionList().size() > 0) {
          out.println("<h3>Assumptions</h3>");
          out.println("  <tr>");
          out.println("    <th>Assumption</th>");
          out.println("  </tr>");
          for (Assumption assumption : dataStore.getAssumptionList()) {
            out.println("  <tr>");
            out.println("    <td>" + safe(assumption.getDescription()) + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");
        }

        if (dataStore.getBlackOutDates() != null && dataStore.getBlackOutDates().size() > 0) {
          out.println("<h3>Black Out Dates</h3>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>Start</th>");
          out.println("    <th>End Grace</th>");
          out.println("    <th>End</th>");
          out.println("    <th>Event Date</th>");
          out.println("    <th>Vaccine Name</th>");
          out.println("    <th>Vaccine Ids</th>");
          out.println("    <th>Against Contra</th>");
          out.println("    <th>Against Allowed</th>");
          out.println("    <th>Reason</th>");
          out.println("  </tr>");
          for (BlackOut blackOut : dataStore.getBlackOutDates()) {
            out.println("  <tr>");
            out.println("    <td>" + safe(blackOut.getStartBlackOut()) + "</td>");
            out.println("    <td>" + safe(blackOut.getEndBlackOutGrace()) + "</td>");
            out.println("    <td>" + safe(blackOut.getEndBlackOut()) + "</td>");
            out.println("    <td>" + safe(blackOut.getEventDate()) + "</td>");
            out.println("    <td>" + safe(blackOut.getVaccineName()) + "</td>");
            out.println("    <td>");
            if (blackOut.getAgainstVaccineIds() == null) {
              out.println("&nbsp;");
            } else {
              boolean first = true;
              for (ValidVaccine validVaccine : blackOut.getAgainstVaccineIds()) {
                if (!first) {
                  out.print(", ");
                }
                out.print(validVaccine.getVaccineId());
                first = false;
              }
            }
            out.println("</td>");
            out.println("    <td>" + safe(blackOut.getAgainstContra()) + "</td>");
            out.println("    <td>" + safe(blackOut.getAgainstAllowed()) + "</td>");
            out.println("    <td>" + safe(blackOut.getReason()) + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");

          //          private DateTime startBlackOut = null;
          //          private DateTime endBlackOut = null;
          //          private DateTime endBlackOutGrace = null;
          //          private DateTime eventDate = null;
          //          private String vaccineName = "";
          //          private ValidVaccine[] againstVaccineIds = null;
          //          private String againstContra = "";
          //          private String againstAllowed = "";
          //          private String reason = "";
        }

        if (dataStore.getForecastOptions() != null) {
          out.println("<h3>Forecast Options</h3>");
          out.println("<table>");
          ForecastOptions forecastOptions = dataStore.getForecastOptions();
          if (forecastOptions.getFluSeasonDue() != null) {
            out.println("  <tr>");
            out.println("    <th>Flu Season  Due</th>");
            out.println("    <td>" + safe(forecastOptions.getFluSeasonDue()) + "</td>");
            out.println("  </tr>");
          }
          if (forecastOptions.getFluSeasonOverdue() != null) {
            out.println("  <tr>");
            out.println("    <th>Flu Season  Overdue</th>");
            out.println("    <td>" + safe(forecastOptions.getFluSeasonOverdue()) + "</td>");
            out.println("  </tr>");
          }
          if (forecastOptions.getFluSeasonEnd() != null) {
            out.println("  <tr>");
            out.println("    <th>Flu Season End</th>");
            out.println("    <td>" + safe(forecastOptions.getFluSeasonEnd()) + "</td>");
            out.println("  </tr>");
          }
          if (forecastOptions.isIgnoreFourDayGrace()) {
            out.println("  <tr>");
            out.println("    <th>Ignore Four Day Grace</th>");
            out.println("    <td>True</td>");
            out.println("  </tr>");
          }
          out.println("  <tr>");
          out.println("    <th>Assume Complete</th>");
          out.println("    <td>");
          boolean first = true;
          for (String s : forecastOptions.getAssumeCompleteScheduleNameSet()) {
            if (!first) {
              out.print(", ");
            }
            out.print(s);
            first = false;
          }
          out.println("    </td>");
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

        if (dataStore.getForecast() != null && dataStore.getForecast().getInvalidateSameDayList() != null
            && dataStore.getForecast().getInvalidateSameDayList().size() > 0) {
          out.println("<h3>Invalid Same Day Combinations</h3>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>Invalidate</th>");
          out.println("    <th>If given same day as</th>");
          out.println("  </tr>");
          for (InvalidateSameDay invalidateSameDay : dataStore.getForecast().getInvalidateSameDayList()) {
            out.println("  <tr>");
            out.println("    <td>" + safe(invalidateSameDay.getInvalidateVaccineName()) + "</td>");
            out.println("    <td>" + safe(invalidateSameDay.getIfGivenVaccineName()) + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");
          
          if (dataStore.getInvalidatedSameDayVaccineIdMapToReason() != null)
          {
            out.println("<h3>Invalid Vaccinations Found</h3>");
            out.println("<table>");
            out.println("  <tr>");
            out.println("    <th>Vaccine Id</th>");
            out.println("    <th>Reason</th>");
            out.println("  </tr>");
            for (Integer vaccineId : dataStore.getInvalidatedSameDayVaccineIdMapToReason().keySet())
            {
              out.println("  <tr>");
              out.println("    <td>" + safe(vaccineId) + "</td>");
              out.println("    <td>" + safe(dataStore.getInvalidatedSameDayVaccineIdMapToReason().get(vaccineId)) + "</td>");
              out.println("  </tr>");
            }
            out.println("</table>");
          }
        }

        if (dataStore.getResultList() != null) {
          List<ImmunizationForecastDataBean> resultList = dataStore.getResultList();
          for (ImmunizationForecastDataBean result : resultList) {
            out.println("<h3>Forecast Result</h3>");
            out.println("<table>");
            out.println("  <tr>");
            out.println("    <th>Immregid</th>");
            out.println("    <td>" + safe(result.getImmregid()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Forecast Name</th>");
            out.println("    <td>" + safe(result.getForecastName()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Forecast Label</th>");
            out.println("    <td>" + safe(result.getForecastLabel()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Schedule</th>");
            out.println("    <td>" + safe(result.getSchedule()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Date Due</th>");
            out.println("    <td>" + safe(result.getDateDue()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Dose</th>");
            out.println("    <td>" + safe(result.getDose()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Valid</th>");
            out.println("    <td>" + safe(result.getValid()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Due</th>");
            out.println("    <td>" + safe(result.getDue()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Overdue</th>");
            out.println("    <td>" + safe(result.getOverdue()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Finished</th>");
            out.println("    <td>" + safe(result.getFinished()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Sort Order</th>");
            out.println("    <td>" + safe(result.getSortOrder()) + "</td>");
            out.println("  </tr>");
            out.println("  <tr>");
            out.println("    <th>Comment</th>");
            out.println("    <td>" + safe(result.getComment()) + "</td>");
            out.println("  </tr>");
            out.println("</table>");
          }

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
      out.println("  </body>");
      out.println("</html>");
    } catch (Exception e) {
      handleException(resp, e);
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
    } else if (lineCode.equals("Zos")) {
      lineCode = "Zoster";
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
          boolean doBr = false;
          if (!schedule.getValidAge().isEmpty()) {
            out.println("Valid at " + schedule.getValidAge());
            doBr = true;
          }
          if (!schedule.getDueAge().isEmpty()) {
            if (doBr) {
              out.println("<br/>");
            }
            out.println("Due at " + schedule.getDueAge());
            doBr = true;
          }
          if (schedule.getCompleted() != null) {
            if (doBr) {
              out.println("<br/>");
            }
            out.println("Has completed " + schedule.getCompleted().getForecastLabel());
            doBr = true;
          }
          if (schedule.getCompletesList() != null && schedule.getCompletesList().size() > 0) {
            for (ForecastAntigen fa : schedule.getCompletesList()) {
              if (doBr) {
                out.println("<br/>");
              }
              out.println("Series will complete " + fa.getForecastLabel());
            }
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

  public void printSchedule(PrintWriter out, Schedule schedule) {
    printSchedule(out, null, schedule, null);
  }

  private void printSchedule(PrintWriter out, DataStore dataStore, Schedule schedule, String baseLink) {

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
          String vaccineName = forecastManager.getVaccineName(vaccineId);
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
        out.println(schedule.getValidInterval().getDateTimeFrom(dataStore.getPreviousEventDateValid())
            .toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getValidGrace()) + "</td>");
      out.println("        <td class=\"insideValue\">"
          + safe(dataStore != null && dataStore.getValid() != null ? dataStore.getValid().toString("M/D/Y") : "")
          + "</td>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Eary due</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getEarlyAge()));
      if (dataStore != null && !schedule.getEarlyAge().isEmpty()) {
        out.println(schedule.getEarlyAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getEarlyInterval()));
      if (dataStore != null && !schedule.getEarlyInterval().isEmpty() && dataStore.getPreviousEventDateValid() != null) {
        out.println(schedule.getEarlyInterval().getDateTimeFrom(dataStore.getPreviousEventDateValid())
            .toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">&nbsp;</td>");
      out.println("        <td class=\"insideValue\">&nbsp;</td>");
      out.println("      </tr>");
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Due</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getDueAge()));
      if (dataStore != null && !schedule.getDueAge().isEmpty()) {
        out.println(schedule.getDueAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getDueInterval()));
      if (dataStore != null && !schedule.getDueInterval().isEmpty() && dataStore.getPreviousEventDateValid() != null) {
        out.println(schedule.getDueInterval().getDateTimeFrom(dataStore.getPreviousEventDateValid()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td>&nbsp;</td>");
      out.println("        <td class=\"insideValue\">"
          + safe(dataStore != null && dataStore.getDue() != null ? dataStore.getDue().toString("M/D/Y") : "") + "</td>");
      out.println("      </tr>");
      if (schedule.getEarlyOverdueAge() != null) {
        out.println("      <tr>");
        out.println("        <th class=\"smallHeader\">Early overdue</th>");
        out.println("        <td class=\"insideValue\">" + safe(schedule.getEarlyOverdueAge()));
        if (dataStore != null && !schedule.getEarlyOverdueAge().isEmpty()) {
          out.println(schedule.getEarlyOverdueAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime())
              .toString("M/D/Y"));
        }
        out.println("</td>");
        out.println("        <td class=\"insideValue\">" + safe(schedule.getEarlyOverdueInterval()));
        if (dataStore != null && !schedule.getEarlyOverdueInterval().isEmpty()
            && dataStore.getPreviousEventDateValid() != null) {
          out.println(schedule.getEarlyOverdueInterval().getDateTimeFrom(dataStore.getPreviousEventDateValid())
              .toString("M/D/Y"));
        }
        out.println("</td>");
        out.println("        <td>&nbsp;</td>");
        out.println("        <td class=\"insideValue\">&nbsp;</td>");
        out.println("      </tr>");
      }
      out.println("      <tr>");
      out.println("        <th class=\"smallHeader\">Overdue</th>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getOverdueAge()));
      if (dataStore != null && !schedule.getOverdueAge().isEmpty()) {
        out.println(schedule.getOverdueAge().getDateTimeFrom(dataStore.getPatient().getDobDateTime()).toString("M/D/Y"));
      }
      out.println("</td>");
      out.println("        <td class=\"insideValue\">" + safe(schedule.getOverdueInterval()));
      if (dataStore != null && !schedule.getOverdueInterval().isEmpty()
          && dataStore.getPreviousEventDateValid() != null) {
        out.println(schedule.getOverdueInterval().getDateTimeFrom(dataStore.getPreviousEventDateValid())
            .toString("M/D/Y"));
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
      if (dataStore != null && !schedule.getFinishedInterval().isEmpty()
          && dataStore.getPreviousEventDateValid() != null) {
        out.println(schedule.getFinishedInterval().getDateTimeFrom(dataStore.getPreviousEventDateValid())
            .toString("M/D/Y"));
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
        out.println(schedule.getBeforePreviousInterval().getDateTimeFrom(dataStore.getBeforePreviousEventDate())
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
        out.println("  <tr>");
        out.println("    <th class=\"bigHeader\" colspan=\"" + (baseLink == null ? 4 : 5) + "\">Event List</th>");
        out.println("  </tr>");
        out.println("  <tr>");
        out.println("    <th class=\"smallHeader\">Date</th>");
        out.println("    <th class=\"smallHeader\">Vaccine</th>");
        out.println("    <th class=\"smallHeader\">Condition</th>");
        out.println("    <th class=\"smallHeader\">Indicated Event</th>");
        if (baseLink != null) {
          out.println("    <th class=\"smallHeader\"></th>");
        }
        out.println("  </tr>");
        pos = -1;
        for (Event event : dataStore.getEventList()) {
          pos++;
          String c = " class=\"insideValue\"";
          if (pos == (dataStore.getEventPosition() - 1)) {
            c = " class=\"pass\"";
          }
          out.println("  <tr>");
          out.println("    <td" + c + ">" + safe(event.getEventDate()) + " </td>");
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
          out.println("    <td" + c + ">");
          for (ImmunizationInterface imm : event.getImmList()) {
            if (imm.isForceValid()) {
              out.println("Force Valid");
            }
            if (imm.isSubPotent()) {
              out.println("Sub Potent");
            }
            if (imm.isAssumption()) {
              out.println("Assumption");
            }
            if (imm instanceof Transition) {
              out.println("Transition");
            }
          }
          out.println("    </td>");
          if (pos == (dataStore.getEventPosition() - 1) && dataStore.getEvent() != null) {
            out.println("    <td" + c + ">" + (dataStore.getEvent().isHasEvent() ? "YES" : "NO") + " </td>");
          } else {
            out.println("    <td" + c + ">&nbsp;</td>");
          }
          if (baseLink != null) {
            out.println("    <td" + c + "><a href=\"" + baseLink + "&nextEventPosition=" + (pos + 1)
                + "\">Jump</a></td>");
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
