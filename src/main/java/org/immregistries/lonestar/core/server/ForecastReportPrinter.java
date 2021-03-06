package org.immregistries.lonestar.core.server;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.DecisionProcessFormat;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.SoftwareVersion;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.VaccineForecastManagerInterface;
import org.immregistries.lonestar.core.api.model.ForecastRecommendationInterface;
import org.immregistries.lonestar.core.api.model.ForecastResponseInterface;
import org.immregistries.lonestar.core.api.model.ForecastVaccinationInterface;
import org.immregistries.lonestar.core.model.Assumption;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;

public class ForecastReportPrinter {

  private VaccineForecastManagerInterface forecastManager = null;

  public ForecastReportPrinter(VaccineForecastManagerInterface forecastManager) {
    this.forecastManager = forecastManager;
  }

  public void printHTMLVersionOfForecast(List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName, DateTime forecastDate,
      List<VaccinationDoseDataBean> doseList, PrintWriter out) {
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>Lone Star Vaccine Forecaster Results</title>");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <h1>Lone Star Vaccine Forecaster Results</h1>");
    boolean hasAssumptions = false;
    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.hasAssumptions()) {
        hasAssumptions = true;
        break;
      }
    }
    if (hasAssumptions) {
      out.println("    <h2>Assumptions</h2>");
      out.println("    <table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">");
      out.println("      <tr>");
      out.println("        <th>Vaccine</th>");
      out.println("        <th>Assumption</th>");
      out.println("      </tr>");
      for (ImmunizationForecastDataBean forecast : resultList) {
        for (Assumption assumption : forecast.getAssumptionList()) {
          out.println("      <tr>");
          out.println("        <td>" + forecast.getForecastLabel() + "</td>");
          out.println("        <td>" + assumption.getDescription() + "</td>");
          out.println("      </tr>");
        }
      }
      out.println("    </table>");
    }
    out.println("    <h2>Vaccinations Recommended For "
        + new DateTime(forecastDate.getDate()).toString("M/D/Y") + "</h2>");

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
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED)) {
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue());
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());

        String forecastDose = forecast.getDose();
        out.println("      <tr>");
        out.println("        <td>" + forecast.getForecastLabel() + "</td>");
        out.println("        <td>" + forecast.getForecastNameOriginal() + "</td>");
        out.println("        <td>" + forecast.getStatusDescriptionExternal() + "</td>");
        out.println("        <td>" + forecastDose + "</td>");
        out.println("        <td>" + validDate.toString("M/D/Y") + "</td>");
        out.println("        <td>" + dueDate.toString("M/D/Y") + "</td>");
        out.println("        <td>" + overdueDate.toString("M/D/Y") + "</td>");
        out.println("        <td>" + finishedDate.toString("M/D/Y") + "</td>");
        out.println("      </tr>");
      }
    }
    out.println("    </table>");

    out.println("<h2>Vaccinations Due After "
        + new DateTime(forecastDate.getDate()).toString("M/D/Y") + "</h2>");

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
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)) {
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue());
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());
        String forecastDose = forecast.getDose();
        out.println("      <tr>");
        out.println("        <td>" + forecast.getForecastLabel() + "</td>");
        out.println("        <td>" + forecast.getForecastNameOriginal() + "</td>");
        out.println("        <td>" + forecast.getStatusDescriptionExternal() + "</td>");
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
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE_FOR_SEASON)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE)) {
        out.println("      <tr>");
        out.println("        <td>" + forecast.getForecastLabel() + "</td>");
        out.println("        <td>" + forecast.getStatusDescriptionInternal() + "</td>");
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
    out.println("<p>Forecast generated " + new DateTime().toString("M/D/Y")
        + " according to schedule " + forecasterScheduleName + " using version "
        + SoftwareVersion.VERSION + " of the Lone Star Vaccine Forecaster.</p>");

    out.println("<h2>Explanation of Decision Process</h2>");
    for (ImmunizationForecastDataBean forecast : resultList) {
      out.println("<h3>" + forecast.getForecastLabel() + "</h3>");
      out.println(forecast.getTraceList().getExplanation(DecisionProcessFormat.HTML));
    }
    out.println("  </body>");
    out.println("</html>");
  }

  public void printTextVersionOfForecast(List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName, DateTime forecastDate,
      List<VaccinationDoseDataBean> doseList, PrintWriter out) {
    out.println("Lone Star Vaccine Forecaster");
    out.println();
    out.println(
        "VACCINATIONS RECOMMENDED " + new DateTime(forecastDate.getDate()).toString("M/D/Y"));

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED)) {
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue());
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());

        String forecastDose = forecast.getDose();
        out.print("Forecasting " + forecast.getForecastLabel());
        out.print(" status " + forecast.getStatusDescriptionExternal());
        out.print(" dose " + forecastDose);
        out.print(" due " + dueDate.toString("M/D/Y"));
        out.print(" valid " + validDate.toString("M/D/Y"));
        out.print(" overdue " + overdueDate.toString("M/D/Y"));
        out.println(" finished " + finishedDate.toString("M/D/Y"));
      }
    }
    out.println();
    out.println("VACCCINATIONS RECOMMENDED AFTER "
        + new DateTime(forecastDate.getDate()).toString("M/D/Y"));

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)) {
        DateTime validDate = null;
        if (forecast.getValid() != null) {
          validDate = new DateTime(forecast.getValid());
        }
        DateTime dueDate = null;
        if (forecast.getDue() != null) {
          dueDate = new DateTime(forecast.getDue());
        }
        DateTime overdueDate = null;
        if (forecast.getOverdue() != null) {
          overdueDate = new DateTime(forecast.getOverdue());
        }
        DateTime finishedDate = null;
        if (forecast.getFinished() != null) {
          finishedDate = new DateTime(forecast.getFinished());
        }
        String forecastDose = forecast.getDose();
        out.print("Forecasting " + forecast.getForecastLabel());
        out.print(" status " + forecast.getStatusDescriptionExternal());
        out.print(" dose " + forecastDose);
        if (dueDate != null) {
          out.print(" due " + dueDate.toString("M/D/Y"));
        }
        if (validDate != null) {
          out.print(" valid " + validDate.toString("M/D/Y"));
        }
        if (overdueDate != null) {
          out.print(" overdue " + overdueDate.toString("M/D/Y"));
        }
        if (finishedDate != null) {
          out.print(" finished " + finishedDate.toString("M/D/Y"));
        }
        out.println("");
      }
    }
    out.println();
    out.println("VACCINATIONS COMPLETED OR NOT RECOMMENDED");

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE_FOR_SEASON)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE)) {
        out.println("Forecasting " + forecast.getForecastLabel() + " status "
            + forecast.getStatusDescriptionExternal());
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

      out.print("<h3>Forecast Recommendation for " + forecast.getForecastLabel() + "</h3>");
      out.print(
          "<table><tr><th>Vaccine</th><th>Status</th><th>Dose</th><th>Valid</th><th>Due</th><th>Overdue</th><th>Finished</th></tr>");
      String forecastDose = forecast.getDose();
      out.print("<tr>");
      out.print("<td>" + forecast.getForecastLabel() + "</td>");
      out.print("<td>" + forecast.getStatusDescriptionExternal() + "</td>");
      if (forecast.getDose() != null) {
        out.print("<td>" + forecastDose + "</td>");
      } else {
        out.print("<td>&nbsp;</td>");
      }
      if (forecast.getValid() != null) {
        DateTime validDate = new DateTime(forecast.getValid());
        out.print("<td>" + validDate.toString("M/D/Y") + "</td>");
      } else {
        out.print("<td>&nbsp;</td>");
      }
      if (forecast.getDue() != null) {
        DateTime dueDate = new DateTime(forecast.getDue());
        out.print("<td>" + dueDate.toString("M/D/Y") + "</td>");
      } else {
        out.print("<td>&nbsp;</td>");
      }
      if (forecast.getOverdue() != null) {
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        out.print("<td>" + overdueDate.toString("M/D/Y") + "</td>");
      } else {
        out.print("<td>&nbsp;</td>");
      }
      if (forecast.getFinished() != null) {
        DateTime finishedDate = new DateTime(forecast.getFinished());
        out.print("<td>" + finishedDate.toString("M/D/Y") + "</td>");
      } else {
        out.print("<td>&nbsp;</td>");
      }
      out.print("</tr>");
      out.print("</table>");
      out.print("<h4>Details</h4>");
      out.println(forecast.getTraceList().getExplanation(DecisionProcessFormat.HTML));
      out.println();
    }

    out.println("Forecast generated " + new DateTime().toString("M/D/Y") + " according to schedule "
        + forecasterScheduleName + " using version " + SoftwareVersion.VERSION
        + " of the Lone Star Vaccine Forecaster.");
  }

  public void printFhirVersionOfForecastXML(List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName, DateTime forecastDate,
      List<VaccinationDoseDataBean> doseList, PrintWriter out, PatientRecordDataBean patient) {

    StringBuilder summaryText = new StringBuilder();
    summaryText.append("Lone Star Vaccine Forecaster recommendation for patient born on "
        + new DateTime(patient.getDob()).toString("M/D/Y") + ". ");

    {
      String s =
          makeCheckForList(resultList, ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE);
      if (!s.equals("")) {
        summaryText.append("Overdue to receive: " + s + ". ");
      }
    }
    {
      String s = makeCheckForList(resultList, ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE);
      if (!s.equals("")) {
        summaryText.append("Due to receive: " + s + ". ");
      }
    }
    {
      String s = makeCheckForList(resultList,
          ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED);
      if (!s.equals("")) {
        summaryText.append("CONTRAINDICATION! Must NOT recieve: " + s + ". ");
      }
    }
    {
      String s =
          makeCheckForList(resultList, ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER);
      if (!s.equals("")) {
        summaryText.append("Should receive later: " + s + ". ");
      }
    }

    SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.println("<Parameters xmlns=\"http://hl7.org/fhir\">");
    out.println("  <parameter>");
    out.println("    <name value=\"summary\"/>");
    out.println("    <valueString value=\"" + summaryText + "\"/>");
    out.println("  </parameter>");
    out.println("  <parameter>");
    out.println("    <name value=\"recommendation\"/>");
    out.println("    <resource>");
    out.println("      <ImmunizationRecommendation>");
    out.println("        <id value=\"149c1628-bf6b-4454-b582-03ca962f4aff\"/>");
    out.println("        <meta>");
    out.println("          <versionId value=\"1\"/>");
    out.println("          <lastUpdated value=\"" + sdfFull.format(new Date()) + "\"/>");
    out.println("        </meta>");
    out.println("        <text>");
    out.println("          <div xmlns=\"http://www.w3.org/1999/xhtml\">");
    printTextVersionOfForecast(resultList, imms, forecasterScheduleName, forecastDate, doseList,
        out);
    out.println("</div>");
    out.println("        </text>");
    out.println("        <contained>");
    out.println("          <Patient>");
    out.println("            <id value=\"patient-forecast-data\"/>");
    out.println("            <gender value=\"" + (patient.getSex().equals("M") ? "male" : "female")
        + "\"/>");
    out.println("            <birthDate value=\"" + sdf.format(patient.getDob()) + "\"/>");
    out.println("          </Patient>");
    out.println("        </contained>");
    out.println("        <patient>");
    out.println("          <reference value=\"#patient-forecast-data\"/>");
    out.println("        </patient>");
    for (ImmunizationForecastDataBean forecast : resultList) {
      if (!forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED_FOR_SEASON)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_MUCH_LATER)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE_FOR_SEASON)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE)) {
        String doseDueCode = CaretForecaster.doseDueOutHash.get(forecast.getForecastName());
        if (doseDueCode == null) {
          doseDueCode = forecast.getForecastName();
        }
        out.println("        <recommendation>");
        out.println("          <date value=\"" + sdfFull.format(new Date()) + "\"/>");
        out.println("          <vaccineType>");
        out.println("            <coding>");
        out.println("              <system value=\"http://hl7.org/fhir/v3/vs/VaccineType\"/>");
        out.println("              <code value=\"" + doseDueCode + "\"/>");
        out.println("              <display value=\"" + forecast.getForecastName() + "\"/>");
        out.println("            </coding>");
        out.println("          </vaccineType>");
        if (!forecast.getDose().equals("")) {
          out.println("          <doseNumber value=\"" + forecast.getDose() + "\"/>");
        }
        out.println("          <forecastStatus>");
        out.println("            <coding>");
        out.println(
            "              <system value=\"http://hl7.org/fhir/immunization-recommendation-status\"/>");
        if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED)) {
          out.println("              <code value=\"contraindicated\"/>");
          out.println("              <display value=\"contraindicated\"/>");
        } else if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)) {
          out.println("              <code value=\"due\"/>");
          out.println("              <display value=\"due\"/>");
        } else if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)) {
          out.println("              <code value=\"overdue\"/>");
          out.println("              <display value=\"overdue\"/>");
        } else if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)) {
          out.println("              <code value=\"duelater\"/>");
          out.println("              <display value=\"duelater\"/>");
        } else {
          out.println(
              "              <code value=\"" + forecast.getStatusDescriptionExternal() + "\"/>");
          out.println(
              "              <display value=\"" + forecast.getStatusDescriptionExternal() + "\"/>");
        }
        out.println("            </coding>");
        out.println("          </forecastStatus>");
        if (forecast.getDue() != null) {
          out.println("          <dateCriterion>");
          out.println("            <code>");
          out.println("              <coding>");
          out.println(
              "                <system value=\"http://hl7.org/fhir/immunization-recommendation-date-criterion\"/>");
          out.println("                <code value=\"due\"/>");
          out.println("                <display value=\"due\"/>");
          out.println("              </coding>");
          out.println("            </code>");
          out.println("            <value value=\"" + sdfFull.format(forecast.getDue()) + "\"/>");
          out.println("          </dateCriterion>");
        }
        if (forecast.getOverdue() != null) {
          out.println("          <dateCriterion>");
          out.println("            <code>");
          out.println("              <coding>");
          out.println(
              "                <system value=\"http://hl7.org/fhir/immunization-recommendation-date-criterion\"/>");
          out.println("                <code value=\"overdue\"/>");
          out.println("                <display value=\"overdue\"/>");
          out.println("              </coding>");
          out.println("            </code>");
          out.println(
              "            <value value=\"" + sdfFull.format(forecast.getOverdue()) + "\"/>");
          out.println("          </dateCriterion>");
        }
        if (forecast.getFinished() != null) {
          out.println("          <dateCriterion>");
          out.println("            <code>");
          out.println("              <coding>");
          out.println(
              "                <system value=\"http://hl7.org/fhir/immunization-recommendation-date-criterion\"/>");
          out.println("                <code value=\"latest\"/>");
          out.println("                <display value=\"latest\"/>");
          out.println("              </coding>");
          out.println("            </code>");
          out.println(
              "            <value value=\"" + sdfFull.format(forecast.getFinished()) + "\"/>");
          out.println("          </dateCriterion>");
        }
        if (forecast.getValid() != null) {
          out.println("          <dateCriterion>");
          out.println("            <code>");
          out.println("              <coding>");
          out.println(
              "                <system value=\"http://hl7.org/fhir/immunization-recommendation-date-criterion-extended\"/>");
          out.println("                <code value=\"valid\"/>");
          out.println("                <display value=\"valid\"/>");
          out.println("              </coding>");
          out.println("            </code>");
          out.println("            <value value=\"" + sdfFull.format(forecast.getValid()) + "\"/>");
          out.println("          </dateCriterion>");
        }
        out.println("        </recommendation>");
      }
    }
    out.println("      </ImmunizationRecommendation>");
    out.println("    </resource>");
    out.println("  </parameter>");
    out.println("</Parameters>");
  }

  private static long recommendationId = 1;
  private static String recommendationBase = System.currentTimeMillis() + ".";

  private static String getNextRecommendationId() {
    synchronized (recommendationBase) {
      recommendationId++;
    }
    return recommendationBase + recommendationId;
  }

  public void printFhirVersionOfForecastJSON(List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName, DateTime forecastDate,
      List<VaccinationDoseDataBean> doseList, PrintWriter out, PatientRecordDataBean patient) {

    StringBuilder summaryText = new StringBuilder();
    summaryText.append("Lone Star Vaccine Forecaster recommendation for patient born on "
        + new DateTime(patient.getDob()).toString("M/D/Y") + ". ");

    {
      String s =
          makeCheckForList(resultList, ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE);
      if (!s.equals("")) {
        summaryText.append("Overdue to receive: " + s + ". ");
      }
    }
    {
      String s = makeCheckForList(resultList, ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE);
      if (!s.equals("")) {
        summaryText.append("Due to receive: " + s + ". ");
      }
    }
    {
      String s = makeCheckForList(resultList,
          ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED);
      if (!s.equals("")) {
        summaryText.append("CONTRAINDICATION! Must NOT recieve: " + s + ". ");
      }
    }
    {
      String s =
          makeCheckForList(resultList, ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER);
      if (!s.equals("")) {
        summaryText.append("Should receive later: " + s + ". ");
      }
    }

    SimpleDateFormat sdfFull = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    Date now = new Date();

    out.println("{");
    out.println("  \"resourceType\": \"Parameters\",");
    out.println("  \"id\": \"" + patient.getId() + "\",");
    out.println("  \"parameter\": [");
    out.println("    {");
    out.println("      \"name\": \"recommendation\",");
    out.println("      \"resource\": {");
    out.println("        \"resourceType\": \"ImmunizationRecommendation\",");
    out.println("        \"id\": \"" + getNextRecommendationId() + "\",");
    out.println("        \"meta\": {");
    out.println("          \"versionId\": \"1\",");
    out.println("          \"lastUpdated\": \"" + sdfFull.format(now) + "\"");
    out.println("        },");
    out.println("        \"text\": {");
    out.println("          \"status\": \"generated\",");
    out.println("          \"div\": \"<pre>");
    printTextVersionOfForecast(resultList, imms, forecasterScheduleName, forecastDate, doseList,
        out);
    out.println("        </pre>\"");
    out.println("        },");
    out.println("        \"contained\": [");
    out.println("          {");
    out.println("            \"resourceType\": \"Patient\",");
    out.println("            \"id\": \"patient-forecast-data\",");
    out.println("            \"meta\" : {");
    out.println("              \"profile\" : [");
    out.println(
        "                \"http://hl7.org/fhir/uv/immds/StructureDefinition/immds-patient\"");
    out.println("              ]");
    out.println("            },");
    out.println("            \"identifier\" : [");
    out.println("              {");
    out.println("                \"_system\" : {");
    out.println("                  \"extension\" : [");
    out.println("                    {");
    out.println(
        "                      \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",");
    out.println("                      \"valueCode\" : \"masked\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"_value\" : {");
    out.println("                  \"extension\" : [");
    out.println("                    {");
    out.println(
        "                      \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",");
    out.println("                      \"valueCode\" : \"masked\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                }");
    out.println("              }");
    out.println("            ],");
    out.println("            \"name\" : [");
    out.println("              {");
    out.println("                \"_family\" : {");
    out.println("                  \"extension\" : [");
    out.println("                    {");
    out.println(
        "                      \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",");
    out.println("                      \"valueCode\" : \"masked\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"_given\" : [");
    out.println("                  {");
    out.println("                    \"extension\" : [");
    out.println("                      {");
    out.println(
        "                        \"url\" : \"http://hl7.org/fhir/StructureDefinition/data-absent-reason\",");
    out.println("                        \"valueCode\" : \"masked\"");
    out.println("                      }");
    out.println("                    ]");
    out.println("                  }");
    out.println("                ]");
    out.println("              }");
    out.println("            ],");
    out.println("            \"gender\": \"male\",");
    out.println("            \"birthDate\": \"2019-04-28\"");
    out.println("          }");
    out.println("        ],");
    out.println("        \"patient\": {");
    out.println("          \"reference\": \"#patient-forecast-data\"");
    out.println("        },");
    out.println("        \"date\": \"2019-06-27\",");
    out.println("        \"recommendation\": [");
    out.println("          {");
    out.println("            \"vaccineCode\": [");
    out.println("              {");
    out.println("                \"coding\": [");
    out.println("                  {");
    out.println("                    \"system\": \"http://example.org/cdc/cvx/vaccine-code\",");
    out.println("                    \"code\": \"45\",");
    out.println("                    \"display\": \"HepB\"");
    out.println("                  }");
    out.println("                ]");
    out.println("              }");
    out.println("            ],");
    out.println("            \"forecastStatus\": {");
    out.println("              \"coding\": [");
    out.println("                {");
    out.println(
        "                  \"system\": \"http://terminology.hl7.org/CodeSystem/immunization-recommendation-status\",");
    out.println("                  \"code\": \"due\",");
    out.println("                  \"display\": \"Due\"");
    out.println("                }");
    out.println("              ]");
    out.println("            },");
    out.println("            \"dateCriterion\": [");
    out.println("              {");
    out.println("                \"code\": {");
    out.println("                  \"coding\": [");
    out.println("                    {");
    out.println("                      \"system\": \"http://loinc.org\",");
    out.println("                      \"code\": \"30980-7\",");
    out.println("                      \"display\": \"Date vaccine due\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"value\": \"2019-05-27T00:00:00-04:00\"");
    out.println("              },");
    out.println("              {");
    out.println("                \"code\": {");
    out.println("                  \"coding\": [");
    out.println("                    {");
    out.println("                      \"system\": \"http://loinc.org\",");
    out.println("                      \"code\": \"59778-1\",");
    out.println("                      \"display\": \"Date when overdue for immunization\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"value\": \"2019-07-28T00:00:00-04:00\"");
    out.println("              },");
    out.println("              {");
    out.println("                \"code\": {");
    out.println("                  \"coding\": [");
    out.println("                    {");
    out.println("                      \"system\": \"http://loinc.org\",");
    out.println("                      \"code\": \"59777-3\",");
    out.println("                      \"display\": \"Latest date to give immunization\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"value\": \"2219-04-28T00:00:00-04:00\"");
    out.println("              },");
    out.println("              {");
    out.println("                \"code\": {");
    out.println("                  \"coding\": [");
    out.println("                    {");
    out.println("                      \"system\": \"http://loinc.org\",");
    out.println("                      \"code\": \"30981-5\",");
    out.println("                      \"display\": \"Earliest date to give\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"value\": \"2019-05-27T00:00:00-04:00\"");
    out.println("              }");
    out.println("            ],");
    out.println("            \"doseNumberPositiveInt\": 2");
    out.println("          },");
    out.println("          {");
    out.println("            \"vaccineCode\": [");
    out.println("              {");
    out.println("                \"coding\": [");
    out.println("                  {");
    out.println("                    \"system\": \"http://example.org/cdc/cvx/vaccine-code\",");
    out.println("                    \"code\": \"20\",");
    out.println("                    \"display\": \"DTaP\"");
    out.println("                  }");
    out.println("                ]");
    out.println("              }");
    out.println("            ],");
    out.println("            \"forecastStatus\": {");
    out.println("              \"coding\": [");
    out.println("                {");
    out.println(
        "                  \"system\": \"http://example.org/fhir/immunization-recommendation-status\",");
    out.println("                  \"code\": \"duelater\",");
    out.println("                  \"display\": \"duelater\"");
    out.println("                }");
    out.println("              ]");
    out.println("            },");
    out.println("            \"dateCriterion\": [");
    out.println("              {");
    out.println("                \"code\": {");
    out.println("                  \"coding\": [");
    out.println("                    {");
    out.println("                      \"system\": \"http://loinc.org\",");
    out.println("                      \"code\": \"30980-7\",");
    out.println("                      \"display\": \"Date vaccine due\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"value\": \"2019-06-28T00:00:00-04:00\"");
    out.println("              },");
    out.println("              {");
    out.println("                \"code\": {");
    out.println("                  \"coding\": [");
    out.println("                    {");
    out.println("                      \"system\": \"http://loinc.org\",");
    out.println("                      \"code\": \"59778-1\",");
    out.println("                      \"display\": \"Date when overdue for immunization\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"value\": \"2019-07-28T00:00:00-04:00\"");
    out.println("              },");
    out.println("              {");
    out.println("                \"code\": {");
    out.println("                  \"coding\": [");
    out.println("                    {");
    out.println("                      \"system\": \"http://loinc.org\",");
    out.println("                      \"code\": \"59777-3\",");
    out.println("                      \"display\": \"Latest date to give immunization\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"value\": \"2169-04-28T00:00:00-04:00\"");
    out.println("              },");
    out.println("              {");
    out.println("                \"code\": {");
    out.println("                  \"coding\": [");
    out.println("                    {");
    out.println("                      \"system\": \"http://loinc.org\",");
    out.println("                      \"code\": \"30981-5\",");
    out.println("                      \"display\": \"Earliest date to give\"");
    out.println("                    }");
    out.println("                  ]");
    out.println("                },");
    out.println("                \"value\": \"2019-06-09T00:00:00-04:00\"");
    out.println("              }");
    out.println("            ],");
    out.println("            \"doseNumberPositiveInt\": 1");
    out.println("          },");
    out.println("          [Snipped for brevity...]");
    out.println("        ]");
    out.println("      }");
    out.println("    }");
    out.println("  ]");
    out.println("}");



    out.println("            <gender value=\"" + (patient.getSex().equals("M") ? "male" : "female")
        + "\"/>");
    out.println("            <birthDate value=\"" + sdf.format(patient.getDob()) + "\"/>");
    for (ImmunizationForecastDataBean forecast : resultList) {
      if (!forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED_FOR_SEASON)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_MUCH_LATER)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE_FOR_SEASON)
          && !forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE)) {
        String doseDueCode = CaretForecaster.doseDueOutHash.get(forecast.getForecastName());
        if (doseDueCode == null) {
          doseDueCode = forecast.getForecastName();
        }
        out.println("          <date value=\"" + sdfFull.format(new Date()) + "\"/>");
        out.println("              <code value=\"" + doseDueCode + "\"/>");
        out.println("              <display value=\"" + forecast.getForecastName() + "\"/>");
        if (!forecast.getDose().equals("")) {
          out.println("          <doseNumber value=\"" + forecast.getDose() + "\"/>");
        }
        out.println("            <coding>");
        out.println(
            "              <system value=\"http://hl7.org/fhir/immunization-recommendation-status\"/>");
        if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED)) {
          out.println("              <code value=\"contraindicated\"/>");
          out.println("              <display value=\"contraindicated\"/>");
        } else if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)) {
          out.println("              <code value=\"due\"/>");
          out.println("              <display value=\"due\"/>");
        } else if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)) {
          out.println("              <code value=\"overdue\"/>");
          out.println("              <display value=\"overdue\"/>");
        } else if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)) {
          out.println("              <code value=\"duelater\"/>");
          out.println("              <display value=\"duelater\"/>");
        } else {
          out.println(
              "              <code value=\"" + forecast.getStatusDescriptionExternal() + "\"/>");
          out.println(
              "              <display value=\"" + forecast.getStatusDescriptionExternal() + "\"/>");
        }
        out.println("            </coding>");
        out.println("          </forecastStatus>");
        if (forecast.getDue() != null) {
          out.println("          <dateCriterion>");
          out.println("            <code>");
          out.println("              <coding>");
          out.println(
              "                <system value=\"http://hl7.org/fhir/immunization-recommendation-date-criterion\"/>");
          out.println("                <code value=\"due\"/>");
          out.println("                <display value=\"due\"/>");
          out.println("              </coding>");
          out.println("            </code>");
          out.println("            <value value=\"" + sdfFull.format(forecast.getDue()) + "\"/>");
          out.println("          </dateCriterion>");
        }
        if (forecast.getOverdue() != null) {
          out.println("          <dateCriterion>");
          out.println("            <code>");
          out.println("              <coding>");
          out.println(
              "                <system value=\"http://hl7.org/fhir/immunization-recommendation-date-criterion\"/>");
          out.println("                <code value=\"overdue\"/>");
          out.println("                <display value=\"overdue\"/>");
          out.println("              </coding>");
          out.println("            </code>");
          out.println(
              "            <value value=\"" + sdfFull.format(forecast.getOverdue()) + "\"/>");
          out.println("          </dateCriterion>");
        }
        if (forecast.getFinished() != null) {
          out.println("          <dateCriterion>");
          out.println("            <code>");
          out.println("              <coding>");
          out.println(
              "                <system value=\"http://hl7.org/fhir/immunization-recommendation-date-criterion\"/>");
          out.println("                <code value=\"latest\"/>");
          out.println("                <display value=\"latest\"/>");
          out.println("              </coding>");
          out.println("            </code>");
          out.println(
              "            <value value=\"" + sdfFull.format(forecast.getFinished()) + "\"/>");
          out.println("          </dateCriterion>");
        }
        if (forecast.getValid() != null) {
          out.println("          <dateCriterion>");
          out.println("            <code>");
          out.println("              <coding>");
          out.println(
              "                <system value=\"http://hl7.org/fhir/immunization-recommendation-date-criterion-extended\"/>");
          out.println("                <code value=\"valid\"/>");
          out.println("                <display value=\"valid\"/>");
          out.println("              </coding>");
          out.println("            </code>");
          out.println("            <value value=\"" + sdfFull.format(forecast.getValid()) + "\"/>");
          out.println("          </dateCriterion>");
        }
        out.println("        </recommendation>");
      }
    }
  }

  private String makeCheckForList(List<ImmunizationForecastDataBean> resultList, String checkFor) {
    String dueString = "";
    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescriptionInternal().equals(checkFor)) {
        if (dueString.equals("")) {
          dueString += forecast.getForecastLabel();
        } else {
          dueString += ", " + forecast.getForecastLabel();
        }
      }
    }
    return dueString;
  }

  public void printNarrowTextVersionOfForecast(List<ImmunizationForecastDataBean> resultList,
      List<ImmunizationInterface> imms, String forecasterScheduleName, DateTime forecastDate,
      List<VaccinationDoseDataBean> doseList, PrintWriter out, boolean suppressExtraInfo,
      Date birthDate) {

    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    out.println("Texas Children's Hospital Forecaster v" + SoftwareVersion.VERSION);
    out.println();
    boolean hasAssumptions = false;
    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.hasAssumptions()) {
        hasAssumptions = true;
        break;
      }
    }
    if (hasAssumptions) {
      out.println("-- ASSUMPTIONS ---------------------------------------");
      for (ImmunizationForecastDataBean forecast : resultList) {
        for (Assumption assumption : forecast.getAssumptionList()) {
          out.println(pad(assumption.getDescription(), 54));
        }
      }
      out.println();
    }

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
        String s = printFirstWords(out, dose.getReason(), 59);
        out.println();
        while (s != null) {
          out.print("             ");
          s = printFirstWords(out, s, 59);
          out.println();
        }
      }
      // out.print("     ");
      // out.print(pad(dose.getWhenValidText(), 61));
      // out.println();
    }
    out.println();

    Date suppressionDate = null;
    if (suppressExtraInfo) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(birthDate);
      calendar.add(Calendar.YEAR, 18);
      int suppressionYear = 10;
      if (forecastDate.getDate().before(calendar.getTime())) {
        suppressionYear = 7;
        suppressionDate = null;
      }
      calendar = Calendar.getInstance();
      calendar.add(Calendar.YEAR, suppressionYear);
      suppressionDate = calendar.getTime();
    }

    out.println("-- FORECAST ----------------------------------------------------------");
    out.println("VACCINE TYPE   STATUS           DOSE VALID      DUE        OVERDUE    ");

    for (ImmunizationForecastDataBean forecast : resultList) {
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)) {
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue());
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        out.print(pad(forecast.getForecastLabel(), 14));
        out.print(" ");
        out.print(pad(forecast.getStatusDescriptionExternal(), 16));
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
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED)) {
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue());
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        out.print(pad(forecast.getForecastLabel(), 14));
        out.print(" ");
        out.print(pad(forecast.getStatusDescriptionExternal(), 16));
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
      if (forecast.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE_FOR_SEASON)
          || forecast.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE)) {
        if (suppressionDate != null) {
          if (forecast.getDateDue() == null || suppressionDate.before(forecast.getDateDue())) {
            continue;
          }
        }
        DateTime validDate = null;
        if (forecast.getValid() != null) {
          validDate = new DateTime(forecast.getValid());
        }
        DateTime dueDate = null;
        if (forecast.getDue() != null) {
          dueDate = new DateTime(forecast.getDue());
        }
        DateTime overdueDate = null;
        if (forecast.getOverdue() != null) {
          overdueDate = new DateTime(forecast.getOverdue());
        }
        out.print(pad(forecast.getForecastLabel(), 14));
        out.print(" ");
        out.print(pad(forecast.getStatusDescriptionExternal(), 16));
        out.print(" ");
        out.print(pad(forecast.getDose(), 5));
        if (validDate != null) {
          out.print(validDate.toString("M/D/Y"));
        } else {
          out.print("          ");
        }
        out.print(" ");
        if (dueDate != null) {
          out.print(dueDate.toString("M/D/Y"));
        } else {
          out.print("          ");
        }
        out.print(" ");
        if (overdueDate != null) {
          out.print(overdueDate.toString("M/D/Y"));
        } else {
          out.print("          ");
        }
        out.print(" ");
        out.println();
      }
    }

    if (!suppressExtraInfo) {
      for (ImmunizationForecastDataBean forecast : resultList) {
        if (forecast.getStatusDescriptionInternal()
            .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)
            || forecast.getStatusDescriptionInternal()
                .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)) {
          out.print(pad(forecast.getForecastLabel(), 14));
          out.print(" ");
          out.print(pad(forecast.getStatusDescriptionExternal(), 16));
          out.println();
        }
      }
    }

    out.println();

    out.println("-- EXPLANATION OF DECISION PROCESS -----------------------------------");
    for (ImmunizationForecastDataBean forecast : resultList) {
      out.println(forecast.getForecastLabel());
      forecast.getTraceList().getExplanationInText(out);
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

  public static void printTables(SimpleDateFormat sdf, ForecastResponseInterface response,
      PrintStream out) {
    out.println();
    out.println("-- FORECAST ------------------------------------------------------------------");
    out.println("LABEL     ANTIGEN   STATUS   DOSE  VALID      DUE        OVERDUE    FINISHED ");
    out.println("------------------------------------------------------------------------------");
    for (ForecastRecommendationInterface recommendation : response.getRecommendationList()) {

      out.print(pad(recommendation.getDisplayLabel(), 10));
      out.print(pad(recommendation.getAntigenName(), 10));
      out.print(pad(recommendation.getStatusDescription(), 9));
      out.print(pad(recommendation.getDoseNumber(), 6));
      if (recommendation.getDueDate() != null) {
        out.print(sdf.format(recommendation.getDueDate()));
      } else {
        out.print("          ");
      }
      out.print(" ");
      if (recommendation.getValidDate() != null) {
        out.print(sdf.format(recommendation.getValidDate()));
      } else {
        out.print("          ");
      }
      out.print(" ");
      if (recommendation.getOverdueDate() != null) {
        out.print(sdf.format(recommendation.getOverdueDate()));
      } else {
        out.print("          ");
      }
      out.print(" ");
      if (recommendation.getFinishedDate() != null) {
        out.print(sdf.format(recommendation.getFinishedDate()));
      } else {
        out.print("          ");
      }
      out.print(" ");
      // out.print(" [" + recommendation.getEvaluationExplanation() + "]");
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

  private static String printFirstWords(PrintWriter out, String s, int length) {
    if (s == null) {
      return null;
    }
    s = s.trim();
    if (s.length() <= length) {
      out.print(s);
      return null;
    }
    boolean notFirst = false;
    while (s.length() > 0 && length > 0) {
      int i = s.indexOf(' ');
      if (i == -1) {
        i = s.length();
      }
      if (notFirst) {
        out.print(' ');
        length--;
      }
      notFirst = true;
      if (i < length) {
        out.print(s.substring(0, i));
        s = s.substring(i + 1).trim();
        length = length - i;
      } else {
        return s;
      }
    }
    return s;
  }

}
