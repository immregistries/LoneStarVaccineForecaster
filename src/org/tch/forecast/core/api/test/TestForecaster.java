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

    System.out.println("-- RESULTS -------------------------------------------------------------------");
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
      System.out.println();
    }
    System.out.println("------------------------------------------------------------------------------");
    System.out.println();
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
