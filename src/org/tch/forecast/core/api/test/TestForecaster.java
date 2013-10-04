package org.tch.forecast.core.api.test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.tch.forecast.core.api.impl.ForecastHandler;
import org.tch.forecast.core.api.impl.ForecastPatient;
import org.tch.forecast.core.api.impl.ForecastRequest;
import org.tch.forecast.core.api.impl.ForecastVaccination;
import org.tch.forecast.core.api.model.ForecastHandlerInterface;
import org.tch.forecast.core.api.model.ForecastRecommendationInterface;
import org.tch.forecast.core.api.model.ForecastRequestInterface;
import org.tch.forecast.core.api.model.ForecastResponseInterface;
import org.tch.forecast.core.api.model.ForecastVaccinationInterface;

public class TestForecaster {
  // To run this simply:
  // 1. run: ant jar-dist
  // 2. from deploy directory, run: java -classpath tch-forecaster.jar org.tch.forecast.core.api.test.TestForecaster
  public static void main(String[] args) throws Exception {
    System.out.println("TCH Forecaster");
    System.out.println("------------------------------------------------------------------------------");
    System.out.println("Testing functionality ");
    System.out.println();
    System.out.println("Instantiating Forecaster");
    ForecastHandlerInterface forecastHandler = new ForecastHandler();
    System.out.println();

    {
      System.out.println("Test 1: No vaccinations for baby born today");
      ForecastRequestInterface request = new ForecastRequest();
      request.setEvaluationDate(new Date());
      ForecastPatient patient = new ForecastPatient();
      patient.setBirthDate(new Date());
      patient.setSex("M");
      request.setPatient(patient);
      forecastAndPrintResults(forecastHandler, request);
    }

    {
      System.out.println("Test 2: No vaccinations for older child");
      ForecastRequestInterface request = new ForecastRequest();
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.YEAR, -1);
      calendar.add(Calendar.DAY_OF_MONTH, -3);

      request.setEvaluationDate(new Date());
      ForecastPatient patient = new ForecastPatient();
      patient.setBirthDate(calendar.getTime());
      patient.setSex("M");
      request.setPatient(patient);
      forecastAndPrintResults(forecastHandler, request);
    }

    {
      System.out.println("Test 3: Some vaccinations for older child");
      ForecastRequestInterface request = new ForecastRequest();
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.YEAR, -1);
      calendar.add(Calendar.DAY_OF_MONTH, -3);

      request.setEvaluationDate(new Date());
      ForecastPatient patient = new ForecastPatient();
      patient.setBirthDate(calendar.getTime());
      patient.setSex("M");
      request.setPatient(patient);
      List<ForecastVaccinationInterface> vaccinationList = new ArrayList<ForecastVaccinationInterface>();
      request.setVaccinationList(vaccinationList);
      ForecastVaccination vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(calendar.getTime());
      vaccination.setCvxCode("08");
      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      calendar.add(Calendar.MONTH, 2);
      calendar.add(Calendar.DAY_OF_MONTH, 2);
      vaccination.setAdminDate(calendar.getTime());
      vaccination.setCvxCode("122");

      forecastAndPrintResults(forecastHandler, request);
    }
    {
      System.out.println("Test 4: Invalid vaccination");
      ForecastRequestInterface request = new ForecastRequest();
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.YEAR, -1);
      calendar.add(Calendar.DAY_OF_MONTH, -3);

      request.setEvaluationDate(new Date());
      ForecastPatient patient = new ForecastPatient();
      patient.setBirthDate(calendar.getTime());
      patient.setSex("M");
      request.setPatient(patient);
      List<ForecastVaccinationInterface> vaccinationList = new ArrayList<ForecastVaccinationInterface>();
      request.setVaccinationList(vaccinationList);
      ForecastVaccination vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(calendar.getTime());
      vaccination.setCvxCode("08");
      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      calendar.add(Calendar.MONTH, 1);
      calendar.add(Calendar.DAY_OF_MONTH, 2);
      vaccination.setAdminDate(calendar.getTime());
      vaccination.setCvxCode("122");
      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      calendar.add(Calendar.MONTH, 6);
      calendar.add(Calendar.DAY_OF_MONTH, 3);
      vaccination.setAdminDate(calendar.getTime());
      vaccination.setCvxCode("110");

      forecastAndPrintResults(forecastHandler, request);
    }
    {
      System.out.println("Test 5: Invalid Multiple");
      ForecastRequestInterface request = new ForecastRequest();
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.YEAR, -1);
      calendar.add(Calendar.DAY_OF_MONTH, -3);
      
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

      request.setEvaluationDate(new Date());
      ForecastPatient patient = new ForecastPatient();
      patient.setBirthDate(sdf.parse("06/26/2008"));
      patient.setSex("M");
      request.setPatient(patient);
      List<ForecastVaccinationInterface> vaccinationList = new ArrayList<ForecastVaccinationInterface>();
      request.setVaccinationList(vaccinationList);

      ForecastVaccination vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(sdf.parse("12/04/2008"));
      vaccination.setCvxCode("120");

      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(sdf.parse("01/20/2009"));
      vaccination.setCvxCode("120");

      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(sdf.parse("08/28/2008"));
      vaccination.setCvxCode("20");

      forecastAndPrintResults(forecastHandler, request);
    }
    System.out.println();
    
    {
      System.out.println("Test 6: MMR Contraindication");
      ForecastRequestInterface request = new ForecastRequest();
      SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

      request.setEvaluationDate(new Date());
      ForecastPatient patient = new ForecastPatient();
      patient.setBirthDate(sdf.parse("07/27/2008"));
      patient.setSex("M");
      request.setPatient(patient);
      List<ForecastVaccinationInterface> vaccinationList = new ArrayList<ForecastVaccinationInterface>();
      request.setVaccinationList(vaccinationList);

      ForecastVaccination vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(sdf.parse("08/02/2009"));
      vaccination.setCvxCode("03");

      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(sdf.parse("08/21/2009"));
      vaccination.setCvxCode("21");

      forecastAndPrintResults(forecastHandler, request);
    }
    System.out.println();
    System.out.println("Tests complete");

  }

  public static void forecastAndPrintResults(ForecastHandlerInterface forecastHandler, ForecastRequestInterface request)
      throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    ForecastResponseInterface response = forecastHandler.forecast(request);
    System.out.println("Patient Birth Date: " + sdf.format(request.getPatient().getBirthDate()));
    for (ForecastVaccinationInterface forecastVaccination : request.getVaccinationList()) {
      System.out.println("Vaccination: " + forecastVaccination.getCvxCode() + " given "
          + sdf.format(forecastVaccination.getAdminDate()));
    }

    System.out.println();
    System.out.println("-- FORECAST ------------------------------------------------------------------");
    System.out.println("LABEL     ANTIGEN   STATUS   DOSE  VALID      DUE        OVERDUE    FINISHED ");
    System.out.println("------------------------------------------------------------------------------");
    for (ForecastRecommendationInterface recommendation : response.getRecommendationList()) {

      System.out.print(pad(recommendation.getDisplayLabel(), 10));
      System.out.print(pad(recommendation.getAntigenName(), 10));
      System.out.print(pad(recommendation.getStatusDescription(), 9));
      System.out.print(pad(recommendation.getDoseNumber(), 6));
      System.out.print(sdf.format(recommendation.getDueDate()));
      System.out.print(" ");
      System.out.print(sdf.format(recommendation.getValidDate()));
      System.out.print(" ");
      System.out.print(sdf.format(recommendation.getOverdueDate()));
      System.out.print(" ");
      System.out.print(sdf.format(recommendation.getFinishedDate()));
      System.out.print(" ");
//      System.out.print(" [" + recommendation.getEvaluationExplanation() + "]");
      System.out.println();
    }
    System.out.println("------------------------------------------------------------------------------");
    System.out.println();
    System.out.println("-- EVALUATION ----------------------------------------------------------------");
    System.out.println("DATE       CVX  FORECAST   SCH DO ST REASON                           ");
    System.out.println("------------------------------------------------------------------------------");
    for (ForecastVaccinationInterface forecastVaccination : response.getVaccinationList()) {
      System.out.print(sdf.format(forecastVaccination.getAdminDate()));
      System.out.print(" ");
      System.out.print(pad(forecastVaccination.getCvxCode(), 5));
      System.out.print(pad(forecastVaccination.getForecastCode(), 11));
      System.out.print(pad(forecastVaccination.getScheduleCode(), 4));
      System.out.print(pad(forecastVaccination.getDoseCode(), 3));
      System.out.print(pad(forecastVaccination.getStatusCode(), 3));
      System.out.print(pad(forecastVaccination.getReasonText(), 42));
      System.out.println();
      if (forecastVaccination.getReasonText().length() > 42) {
        System.out.println("                                     "
            + pad(forecastVaccination.getReasonText().substring(42), 42));
        if (forecastVaccination.getReasonText().length() > 84) {
          System.out.println("                                     "
              + pad(forecastVaccination.getReasonText().substring(84), 42));
        }
      }
    }
    System.out.println("------------------------------------------------------------------------------");
    System.out.println();
    System.out.println("-- EVALUATION DETAILS --------------------------------------------------------");
    System.out.println("DATE       CVX  FORECAST   SCH DETAILS                                        ");
    System.out.println("------------------------------------------------------------------------------");
    for (ForecastVaccinationInterface forecastVaccination : response.getVaccinationList()) {
      System.out.print(sdf.format(forecastVaccination.getAdminDate()));
      System.out.print(" ");
      System.out.print(pad(forecastVaccination.getCvxCode(), 5));
      System.out.print(pad(forecastVaccination.getForecastCode(), 11));
      System.out.print(pad(forecastVaccination.getScheduleCode(), 4));
      System.out.print(pad(forecastVaccination.getWhenValidText(), 51));
      System.out.println();
    }
    System.out.println("------------------------------------------------------------------------------");
    System.out.println();
    System.out.println("-- EVALUATION EXPLANATION TEXT (HTML) ----------------------------------------");
    for (ForecastRecommendationInterface recommendation : response.getRecommendationList()) {
      System.out.println(recommendation.getDecisionProcessTextHTML());
    }
    System.out.println("------------------------------------------------------------------------------");

  }

  private static String pad(String s, int size) {
    if (s.length() > size) {
      return s.substring(0, size);
    } else {
      while (s.length() < size) {
        s = s + " ";
      }
    }
    return s;
  }
}
