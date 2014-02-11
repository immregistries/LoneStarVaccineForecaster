package org.tch.forecast.core.server;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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
import org.tch.forecast.core.api.model.ForecastRecommendationInterface;
import org.tch.forecast.core.api.model.ForecastResponseInterface;
import org.tch.forecast.core.api.model.ForecastVaccinationInterface;

public class ForecastReportPrinter
{

  private VaccineForecastManagerInterface forecastManager = null;

  public ForecastReportPrinter(VaccineForecastManagerInterface forecastManager) {
    this.forecastManager = forecastManager;
  }

  public void printHTMLVersionOfForecast(List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName, DateTime forecastDate, boolean dueUseEarly,
      List<VaccinationDoseDataBean> doseList, PrintWriter out) {
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
    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED)) {
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue(dueUseEarly));
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());

        String forecastDose = forecast.getDose();
        out.println("      <tr>");
        out.println("        <td>" + forecast.getForecastLabel() + "</td>");
        out.println("        <td>" + forecast.getForecastNameOriginal() + "</td>");
        out.println("        <td>" + forecast.getStatusDescription() + "</td>");
        out.println("        <td>" + forecastDose + "</td>");
        out.println("        <td>" + validDate.toString("M/D/Y") + "</td>");
        out.println("        <td>" + dueDate.toString("M/D/Y") + "</td>");
        out.println("        <td>" + overdueDate.toString("M/D/Y") + "</td>");
        out.println("        <td>" + finishedDate.toString("M/D/Y") + "</td>");
        out.println("      </tr>");
      }
    }
    out.println("    </table>");

    out.println("<h2>Vaccinations Due After " + new DateTime(forecastDate.getDate()).toString("M/D/Y") + "</h2>");

    out.println("    <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
    out.println("      <tr>");
    out.println("        <th>Vaccine</th>");
    out.println("        <th>Dose</th>");
    out.println("        <th>Valid</th>");
    out.println("        <th>Due</th>");
    out.println("        <th>Overdue</th>");
    out.println("        <th>Finished</th>");
    out.println("      </tr>");
    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)) {
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
    }
    out.println("    </table>");

    out.println("<h2>Vaccinations Completed or Not Recommended</h2>");
    out.println("    <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
    out.println("      <tr>");
    out.println("        <th>Vaccine</th>");
    out.println("        <th>Status</th>");
    out.println("      </tr>");
    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)) {
        out.println("      <tr>");
        out.println("        <td>" + forecast.getForecastLabel() + "</td>");
        out.println("        <td>" + forecast.getStatusDescription() + "</td>");
        out.println("      </tr>");
      }
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
      out.println("        <td>" + n(dose.getCvxCode()) + "</td>");
      out.println("        <td>" + n(dose.getMvxCode()) + "</td>");
      out.println("        <td>" + n(dose.getForecastCode()) + "</td>");
      out.println("        <td>" + n(dose.getDoseCode()) + "</td>");
      out.println("        <td>" + n(dose.getScheduleCode()) + "</td>");
      out.println("        <td>" + n(dose.getStatusCode()) + "</td>");
      out.println("        <td>" + n(dose.getWhenValidText()) + "</td>");
      out.println("        <td>" + n(dose.getReason()) + "</td>");
      out.println("      </tr>");
    }
    out.println("    </table>");
    out.println();
    out.println("<p>Forecast generated " + new DateTime().toString("M/D/Y") + " according to schedule "
        + forecasterScheduleName + " using version " + SoftwareVersion.VERSION + " of the TCH Forecaster.</p>");

    out.println("<h2>Detail Information</h2>");
    for (ImmunizationForecastDataBean forecast : resultList) {
      out.println("<h3>" + forecast.getForecastLabel() + "</h3>");
      out.println(forecast.getTraceList().getExplanation());
    }
    out.println("  </body>");
    out.println("</html>");
  }

  public void printTextVersionOfForecast(List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName, DateTime forecastDate, boolean dueUseEarly,
      List<VaccinationDoseDataBean> doseList, PrintWriter out) {
    out.println("TCH Immunization Forecaster");
    out.println();
    out.println("VACCINATIONS RECOMMENDED " + new DateTime(forecastDate.getDate()).toString("M/D/Y"));

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED)) {
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
        out.print(" finished " + finishedDate.toString("M/D/Y"));
        out.println(" status " + forecast.getStatusDescription());
      }
    }
    out.println();
    out.println("VACCCINATIONS RECOMMENDED AFTER " + new DateTime(forecastDate.getDate()).toString("M/D/Y"));

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)) {
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
        out.print(" finished " + finishedDate.toString("M/D/Y"));
        out.println(" status " + forecast.getStatusDescription());
      }
    }
    out.println();
    out.println("VACCINATIONS COMPLETED OR NOT RECOMMENDED");

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)) {
        out.println("Forecasting " + forecast.getForecastLabel() + " status " + forecast.getStatusDescription());
      }
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
    out.println();

    for (ImmunizationForecastDataBean forecast : resultList) {
      out.println("DETAILS FOR: " + forecast.getForecastLabel());
      out.println(forecast.getTraceList().getExplanation());
      out.println();
    }

    out.println("Forecast generated " + new DateTime().toString("M/D/Y") + " according to schedule "
        + forecasterScheduleName + " using version " + SoftwareVersion.VERSION + " of the TCH Forecaster.");
  }

  public void printNarrowTextVersionOfForecast(List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName, DateTime forecastDate, boolean dueUseEarly,
      List<VaccinationDoseDataBean> doseList, PrintWriter out) {

    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    out.println("Evaluation and Forecast Report");
    out.println();
    out.println("-- EVALUATION --------------------------------------------------------");
    out.println("DATE       CVX  FORECAST   SCHEDULE DOSE STATUS ");
    for (VaccinationDoseDataBean dose : doseList) {
      out.print(sdf.format(dose.getAdminDate()));
      out.print(" ");
      out.print(pad(dose.getCvxCode(), 5));
      out.print(pad(dose.getForecastCode(), 11));
      out.print(pad(dose.getScheduleCode(), 9));
      out.print(pad(dose.getDoseCode(), 5));
      out.print(pad(dose.getStatusCode(), 7));
      out.println();
      if (dose.getReason().length() > 0) {
        out.print("     Reason: ");
        out.print(pad(dose.getReason(), 59));
        out.println();
        if (dose.getReason().length() > 59) {
          out.println("     " + pad(dose.getReason().substring(59), 61));
        }
      }
      //      out.print("     ");
      //      out.print(pad(dose.getWhenValidText(), 61));
      //      out.println();
    }
    out.println();

    out.println("-- FORECAST ----------------------------------------------------------");
    out.println("VACCINE TYPE   STATUS           DOSE VALID      DUE        OVERDUE    ");

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED)) {
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue(dueUseEarly));
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        out.print(pad(forecast.getForecastLabel(), 14));
        out.print(" ");
        out.print(pad(forecast.getStatusDescription(), 16));
        out.print(" ");
        out.print(pad(forecast.getDose(), 5));
        out.print(validDate.toString("M/D/Y"));
        out.print(" ");
        out.print(dueDate.toString("M/D/Y"));
        out.print(" ");
        out.print(overdueDate.toString("M/D/Y"));
        out.print(" ");
        out.println();
      }
    }

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)) {
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue(dueUseEarly));
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());
        out.print(pad(forecast.getForecastLabel(), 14));
        out.print(" ");
        out.print(pad(forecast.getStatusDescription(), 16));
        out.print(" ");
        out.print(pad(forecast.getDose(), 5));
        out.print(validDate.toString("M/D/Y"));
        out.print(" ");
        out.print(dueDate.toString("M/D/Y"));
        out.print(" ");
        out.print(overdueDate.toString("M/D/Y"));
        out.print(" ");
        out.println();
      }
    }

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)
          || forecast.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)) {
        out.print(pad(forecast.getForecastLabel(), 14));
        out.print(" ");
        out.print(pad(forecast.getStatusDescription(), 16));
        out.println();
      }
    }

    out.println();

    out.println("Texas Children's Hospital Forecaster v" + SoftwareVersion.VERSION);
    out.println(" + run date: " + new DateTime().toString("M/D/Y"));
    out.println(" + schedule: " + forecasterScheduleName);

  }

  public static String n(String s) {
    if (s == null || s.equals("")) {
      return "&nbsp;";
    } else
      return s;
  }

  public static String pad(String s, int size) {
    if (s.length() > size) {
      return s.substring(0, size);
    } else {
      while (s.length() < size) {
        s = s + " ";
      }
    }
    return s;
  }

  public static void printTables(SimpleDateFormat sdf, ForecastResponseInterface response, PrintStream out) {
    out.println();
    out.println("-- FORECAST ------------------------------------------------------------------");
    out.println("LABEL     ANTIGEN   STATUS   DOSE  VALID      DUE        OVERDUE    FINISHED ");
    out.println("------------------------------------------------------------------------------");
    for (ForecastRecommendationInterface recommendation : response.getRecommendationList()) {

      out.print(pad(recommendation.getDisplayLabel(), 10));
      out.print(pad(recommendation.getAntigenName(), 10));
      out.print(pad(recommendation.getStatusDescription(), 9));
      out.print(pad(recommendation.getDoseNumber(), 6));
      out.print(sdf.format(recommendation.getDueDate()));
      out.print(" ");
      out.print(sdf.format(recommendation.getValidDate()));
      out.print(" ");
      out.print(sdf.format(recommendation.getOverdueDate()));
      out.print(" ");
      out.print(sdf.format(recommendation.getFinishedDate()));
      out.print(" ");
      //      out.print(" [" + recommendation.getEvaluationExplanation() + "]");
      out.println();
    }

    out.println("------------------------------------------------------------------------------");
    out.println();
    out.println("-- EVALUATION ----------------------------------------------------------------");
    out.println("DATE       CVX  FORECAST   SCH DO ST REASON                           ");
    out.println("------------------------------------------------------------------------------");
    for (ForecastVaccinationInterface forecastVaccination : response.getVaccinationList()) {
      out.print(sdf.format(forecastVaccination.getAdminDate()));
      out.print(" ");
      out.print(pad(forecastVaccination.getCvxCode(), 5));
      out.print(pad(forecastVaccination.getForecastCode(), 11));
      out.print(pad(forecastVaccination.getScheduleCode(), 4));
      out.print(pad(forecastVaccination.getDoseCode(), 3));
      out.print(pad(forecastVaccination.getStatusCode(), 3));
      out.print(pad(forecastVaccination.getReasonText(), 42));
      out.println();
      if (forecastVaccination.getReasonText().length() > 42) {
        out.println("                                     "
            + pad(forecastVaccination.getReasonText().substring(42), 42));
        if (forecastVaccination.getReasonText().length() > 84) {
          out.println("                                     "
              + pad(forecastVaccination.getReasonText().substring(84), 42));
        }
      }
    }
    out.println("------------------------------------------------------------------------------");
    out.println();
    out.println("-- EVALUATION DETAILS --------------------------------------------------------");
    out.println("DATE       CVX  FORECAST   SCH DETAILS                                        ");
    out.println("------------------------------------------------------------------------------");
    for (ForecastVaccinationInterface forecastVaccination : response.getVaccinationList()) {
      out.print(sdf.format(forecastVaccination.getAdminDate()));
      out.print(" ");
      out.print(pad(forecastVaccination.getCvxCode(), 5));
      out.print(pad(forecastVaccination.getForecastCode(), 11));
      out.print(pad(forecastVaccination.getScheduleCode(), 4));
      out.print(pad(forecastVaccination.getWhenValidText(), 51));
      out.println();
    }
    out.println("------------------------------------------------------------------------------");
    out.println();
    out.println("-- EVALUATION EXPLANATION TEXT (HTML) ----------------------------------------");
    for (ForecastRecommendationInterface recommendation : response.getRecommendationList()) {
      out.println(recommendation.getDecisionProcessTextHTML());
    }
    out.println("------------------------------------------------------------------------------");
  }

}
