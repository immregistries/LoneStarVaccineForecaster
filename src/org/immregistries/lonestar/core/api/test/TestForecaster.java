package org.immregistries.lonestar.core.api.test;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.immregistries.lonestar.core.api.impl.ForecastHandler;
import org.immregistries.lonestar.core.api.impl.ForecastPatient;
import org.immregistries.lonestar.core.api.impl.ForecastRequest;
import org.immregistries.lonestar.core.api.impl.ForecastVaccination;
import org.immregistries.lonestar.core.api.model.ForecastHandlerInterface;
import org.immregistries.lonestar.core.api.model.ForecastRequestInterface;
import org.immregistries.lonestar.core.api.model.ForecastResponseInterface;
import org.immregistries.lonestar.core.api.model.ForecastVaccinationInterface;
import org.immregistries.lonestar.core.server.ForecastReportPrinter;

public class TestForecaster {
  // To run this simply:
  // 1. run: ant jar-dist
  // 2. from project directory, run: java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.api.test.TestForecaster
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

    {
      System.out.println("Test 7: CVX Codes");
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
      vaccination.setCvxCode("08");

      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(sdf.parse("08/21/2009"));
      vaccination.setCvxCode("116");

      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(sdf.parse("10/27/2009"));
      vaccination.setCvxCode("83");

      vaccination = new ForecastVaccination();
      vaccinationList.add(vaccination);
      vaccination.setAdminDate(sdf.parse("10/27/2009"));
      vaccination.setCvxCode("8");

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
    
    PrintStream out = System.out;

    ForecastReportPrinter.printTables(sdf, response, out);

  }

 

}
