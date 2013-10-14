package org.tch.forecast.core.server;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.SoftwareVersion;
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastManagerInterface;

public class ForecastReportPrinter
{
  
  private VaccineForecastManagerInterface forecastManager = null;
  
  public ForecastReportPrinter(VaccineForecastManagerInterface forecastManager)
  {
    this.forecastManager = forecastManager;
  }

  public void printHTMLVersionOfForecast(Map traceMap, List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName,
      List<ImmunizationForecastDataBean> resultListOriginal, List<ImmunizationForecastDataBean> forecastListDueToday,
      DateTime forecastDate, boolean dueUseEarly, List<VaccinationDoseDataBean> doseList, PrintWriter out) {
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>TCH Immunization Forecaster Results</title>");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <h1>TCH Immunization Forecaster Results</h1>");
    out.println("    <h2>Vaccinations Recommended For " + new DateTime(forecastDate.getDate()).toString("M/D/Y")
        + "</h2>");
  
    out.println("    <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
    out.println("      <tr>");
    out.println("        <th>Vaccine</th>");
    out.println("        <th>Antigen</th>");
    out.println("        <th>Status</th>");
    out.println("        <th>Dose</th>");
    out.println("        <th>Valid</th>");
    out.println("        <th>Due</th>");
    out.println("        <th>Overdue</th>");
    out.println("        <th>Finished</th>");
    out.println("      </tr>");
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
      out.println("      <tr>");
      out.println("        <td>" + forecast.getForecastLabel() + "</td>");
      out.println("        <td>" + forecast.getForecastNameOriginal() + "</td>");
      out.println("        <td>" + statusDescription + "</td>");
      out.println("        <td>" + forecastDose + "</td>");
      out.println("        <td>" + validDate.toString("M/D/Y") + "</td>");
      out.println("        <td>" + dueDate.toString("M/D/Y") + "</td>");
      out.println("        <td>" + overdueDate.toString("M/D/Y") + "</td>");
      out.println("        <td>" + finishedDate.toString("M/D/Y") + "</td>");
      out.println("      </tr>");
    }
    out.println("    </table>");
  
    out.println("<h2>Vaccinations Recommended After " + new DateTime(forecastDate.getDate()).toString("M/D/Y")
        + "</h2>");
  
    out.println("    <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
    out.println("      <tr>");
    out.println("        <th>Vaccine</th>");
    out.println("        <th>Dose</th>");
    out.println("        <th>Valid</th>");
    out.println("        <th>Due</th>");
    out.println("        <th>Overdue</th>");
    out.println("        <th>Finished</th>");
    out.println("      </tr>");
    forecastList = resultList;
    for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();) {
      ImmunizationForecastDataBean forecast = it.next();
      DateTime validDate = new DateTime(forecast.getValid());
      DateTime dueDate = new DateTime(forecast.getDue(dueUseEarly));
      DateTime overdueDate = new DateTime(forecast.getOverdue());
      DateTime finishedDate = new DateTime(forecast.getFinished());
      String forecastDose = forecast.getDose();
      out.println("      <tr>");
      out.println("        <td>" + forecast.getForecastLabel() + "</td>");
      out.println("        <td>" + forecastDose + "</td>");
      out.println("        <td>" + validDate.toString("M/D/Y") + "</td>");
      out.println("        <td>" + dueDate.toString("M/D/Y") + "</td>");
      out.println("        <td>" + overdueDate.toString("M/D/Y") + "</td>");
      out.println("        <td>" + finishedDate.toString("M/D/Y") + "</td>");
      out.println("      </tr>");
    }
    out.println("    </table>");
  
    out.println("<h2>Vaccinations Completed or Not Recommended</h2>");
    out.println("    <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
    out.println("      <tr>");
    out.println("        <th>Vaccine</th>");
    out.println("      </tr>");
    for (Iterator it = traceMap.keySet().iterator(); it.hasNext();) {
      String key = (String) it.next();
      TraceList traceList = (TraceList) traceMap.get(key);
      out.println("      <tr>");
      out.println("        <td>" + traceList.getForecastLabel() + "</td>");
      out.println("      </tr>");
    }
    out.println("    </table>");
  
    out.println("<h2>Immunization Evaluation</h2>");
    out.println("    <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
    out.println("      <tr>");
    out.println("        <th>Vaccine</th>");
    out.println("        <th>Date</th>");
    out.println("        <th>CVX</th>");
    out.println("        <th>MVX</th>");
    out.println("        <th>Forecast Code</th>");
    out.println("        <th>Dose</th>");
    out.println("        <th>Schedule</th>");
    out.println("        <th>Status</th>");
    out.println("        <th>When Valid</th>");
    out.println("        <th>Reason</th>");
    out.println("      </tr>");
    for (VaccinationDoseDataBean dose : doseList) {
      out.println("      <tr>");
      out.println("        <td>" + forecastManager.getVaccineName(dose.getVaccineId()) + "</td>");
      out.println("        <td>" + new DateTime(dose.getAdminDate()).toString("M/D/Y") + "</td>");
      out.println("        <td>" + ForecastReportPrinter.n(dose.getCvxCode()) + "</td>");
      out.println("        <td>" + ForecastReportPrinter.n(dose.getMvxCode()) + "</td>");
      out.println("        <td>" + ForecastReportPrinter.n(dose.getForecastCode()) + "</td>");
      out.println("        <td>" + ForecastReportPrinter.n(dose.getDoseCode()) + "</td>");
      out.println("        <td>" + ForecastReportPrinter.n(dose.getScheduleCode()) + "</td>");
      out.println("        <td>" + ForecastReportPrinter.n(dose.getStatusCode()) + "</td>");
      out.println("        <td>" + ForecastReportPrinter.n(dose.getWhenValidText()) + "</td>");
      out.println("        <td>" + ForecastReportPrinter.n(dose.getReason()) + "</td>");
      out.println("      </tr>");
    }
    out.println("    </table>");
    out.println();
    out.println("<p>Forecast generated " + new DateTime().toString("M/D/Y") + " according to schedule "
        + forecasterScheduleName + " using version " + SoftwareVersion.VERSION + " of the TCH Forecaster.</p>");
  
    out.println("<h2>Detail Information</h2>");
    for (ImmunizationForecastDataBean forecast : resultListOriginal) {
      out.println("<h3>" + forecast.getForecastLabel() + "</h3>");
      out.print(forecast.getTraceList().getExplanation());
    }
    out.println("  </body>");
    out.println("</html>");
  }

  public void printTextVersionOfForecast(Map traceMap, List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName,
      List<ImmunizationForecastDataBean> forecastListDueToday, DateTime forecastDate, boolean dueUseEarly,
      List<VaccinationDoseDataBean> doseList, PrintWriter out) {
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
    out.println("IMMUNIZATION EVALUATION");
    for (VaccinationDoseDataBean dose : doseList) {
      out.print("Vaccination #" + dose.getVaccinationId() + ": ");
      out.print(forecastManager.getVaccineName(dose.getVaccineId()));
      out.print(" given " + new DateTime(dose.getAdminDate()).toString("M/D/Y"));
      out.print(" is " + dose.getStatusCodeLabelA() + " " + dose.getForecastCode());
      out.print(" dose " + dose.getDoseCode());
      if (dose.getReason() != null && !dose.getReason().equals("")) {
        out.print(" because " + dose.getReason());
      }
      out.println(". " + dose.getWhenValidText() + ".");
    }
    for (ImmunizationInterface imm : imms) {
    }
    out.println();
    out.println("Forecast generated " + new DateTime().toString("M/D/Y") + " according to schedule "
        + forecasterScheduleName + " using version " + SoftwareVersion.VERSION + " of the TCH Forecaster.");
  }

  public static String n(String s) {
    if (s == null || s.equals("")) {
      return "&nbsp;";
    } else
      return s;
  }

}
