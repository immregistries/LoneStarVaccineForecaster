package org.immregistries.lonestar.fhir;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationEvaluation;
import org.hl7.fhir.r4.model.ImmunizationEvaluation.ImmunizationEvaluationStatus;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent;
import org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.PositiveIntType;
import org.hl7.fhir.r4.model.Reference;
import org.immregistries.lonestar.ForecastManagerSingleton;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.api.impl.CvxCode;
import org.immregistries.lonestar.core.api.impl.ForecastAntigen;
import org.immregistries.lonestar.core.api.impl.ForecastHandler;
import org.immregistries.lonestar.core.api.impl.ForecastHandlerCore;
import org.immregistries.lonestar.core.api.impl.ForecastOptions;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;
import org.immregistries.lonestar.core.server.CaretForecaster;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.UnclassifiedServerFailureException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;



public class ImmDSForecastProvider {

  private static final String IR_STATUS_DUE = "due";
  private static final String IR_STATUS_OVERDUE = "overdue";
  private static final String IR_STATUS_IMMUNE = "immune";
  private static final String IR_STATUS_CONTRAINDICATED = "contraindicated";
  private static final String IR_STATUS_COMPLETE = "complete";

  private static final String IR_DATE_CRITERION_EARLIEST = "30981-5";
  private static final String IR_DATE_CRITERION_DUE = "30980-7";
  private static final String IR_DATE_CRITERION_LATEST = "59777-3";
  private static final String IR_DATE_CRITERION_OVERDUE = "59778-1";

  private static final String SYSTEM_SNOMED_CT = "http://snomed.info/sct";
  private static final String SYSTEM_LONESTAR = "http://immregistries.org/lonestar";
  private static final String SYSTEM_EVALUATION_DOSE_STATUS =
      "http://terminology.hl7.org/CodeSystem/immunization-evaluation-dose-status";

  private static final Coding EVALUATION_DOSE_STATUS_VALID =
      new Coding().setCode("valid").setDisplay("Valid").setSystem(SYSTEM_EVALUATION_DOSE_STATUS);
  private static final Coding EVALUATION_DOSE_STATUS_NOTVALID = new Coding().setCode("notvalid")
      .setDisplay("Not valid").setSystem(SYSTEM_EVALUATION_DOSE_STATUS);

  private static final Coding DISEASE_DIPHTHERIA =
      new Coding().setCode("397428000").setDisplay("Diphtheria").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_PERTUSSIS =
      new Coding().setCode("27836007").setDisplay("Pertussis").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_TETANUS =
      new Coding().setCode("76902006").setDisplay("Tetanus").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_INFECTION_CAUSED_BY_HUMAN_POLIOVIRUS =
      new Coding().setCode("721764008").setDisplay("Infection caused by Human poliovirus")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_MEASLES =
      new Coding().setCode("14189004").setDisplay("Measles").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_MUMPS =
      new Coding().setCode("36989005").setDisplay("Mumps").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_RUBELLA =
      new Coding().setCode("36653000").setDisplay("Rubella").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_PNEUMOCOCCAL_INFECTIOUS_DISEASE =
      new Coding().setCode("16814004").setDisplay("Pneumococcal infectious disease")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_MENINGOCOCCAL_INFECTIOUS_DISEASE =
      new Coding().setCode("23511006").setDisplay("Meningococcal infectious disease")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_HAEMOPHILUS_INFLUENZAE_TYPE_B_INFECTION =
      new Coding().setCode("709410003").setDisplay("Haemophilus influenzae type b infection")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_TUBERCULOSIS =
      new Coding().setCode("56717001").setDisplay("Tuberculosis").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_MALIGNANT_TUMOUR_OF_CERVIX = new Coding().setCode("363354003")
      .setDisplay("Malignant tumour of cervix").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_GENITAL_WARTS =
      new Coding().setCode("266113007").setDisplay("Genital warts").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_VIRAL_GASTROENTERITIS_DUE_TO_ROTAVIRUS =
      new Coding().setCode("415822001").setDisplay("Viral gastroenteritis due to Rotavirus")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_TYPE_B_VIRAL_HEPATITIS = new Coding().setCode("66071002")
      .setDisplay("Type B viral hepatitis").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_VIRAL_HEPATITIS_TYPE_A = new Coding().setCode("40468003")
      .setDisplay("Viral hepatitis, type A").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_HEPATITIS_E_VIRUS_INFECTION =
      new Coding().setCode("7111000119109").setDisplay("Hepatitis E virus infection")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_INFLUENZA_CAUSED_BY_SEASONAL_INFLUENZA_VIRUS =
      new Coding().setCode("719590007").setDisplay("Influenza caused by seasonal influenza virus")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_INFLUENZA_CAUSED_BY_PANDEMIC_INFLUENZA_VIRUS =
      new Coding().setCode("719865001").setDisplay("Influenza caused by pandemic influenza virus")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_VARICELLA =
      new Coding().setCode("38907003").setDisplay("Varicella").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_HERPES_ZOSTER =
      new Coding().setCode("4740000").setDisplay("Herpes zoster").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_RESPIRATORY_SYNCYTIAL_VIRUS_INFECTION =
      new Coding().setCode("55735004").setDisplay("Respiratory syncytial virus infection")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_TYPHUS_GROUP_RICKETTSIAL_DISEASE =
      new Coding().setCode("240613006").setDisplay("Typhus group rickettsial disease")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_TYPHOID_FEVER =
      new Coding().setCode("4834000").setDisplay("Typhoid fever").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_PARATYPHOID_FEVER =
      new Coding().setCode("85904008").setDisplay("Paratyphoid fever").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_CHOLERA =
      new Coding().setCode("63650001").setDisplay("Cholera").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_YELLOW_FEVER =
      new Coding().setCode("16541001").setDisplay("Yellow fever").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_DENGUE =
      new Coding().setCode("38362002").setDisplay("Dengue").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_MALARIA =
      new Coding().setCode("61462000").setDisplay("Malaria").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_RABIES =
      new Coding().setCode("14168008").setDisplay("Rabies").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_RMSF_ROCKY_MOUNTAIN_SPOTTED_FEVER =
      new Coding().setCode("186772009").setDisplay("RMSF - Rocky Mountain spotted fever")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_TICKBORNE_ENCEPHALITIS = new Coding().setCode("712986001")
      .setDisplay("Tickborne encephalitis").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_JAPANESE_ENCEPHALITIS_VIRUS_DISEASE =
      new Coding().setCode("52947006").setDisplay("Japanese encephalitis virus disease")
          .setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_Q_FEVER =
      new Coding().setCode("186788009").setDisplay("Q fever").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_ANTHRAX =
      new Coding().setCode("409498004").setDisplay("Anthrax").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_PLAGUE =
      new Coding().setCode("58750007").setDisplay("Plague").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_TULAREMIA =
      new Coding().setCode("19265001").setDisplay("Tularemia").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_LYME_DISEASE =
      new Coding().setCode("23502006").setDisplay("Lyme disease").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_BRUCELLOSIS =
      new Coding().setCode("75702008").setDisplay("Brucellosis").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_LEISHMANIASIS =
      new Coding().setCode("80612004").setDisplay("Leishmaniasis").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_CORONAVIRUS_INFECTION = new Coding().setCode("186747009")
      .setDisplay("Coronavirus infection").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_SMALLPOX =
      new Coding().setCode("67924001").setDisplay("Smallpox").setSystem(SYSTEM_SNOMED_CT);
  private static final Coding DISEASE_COWPOX =
      new Coding().setCode("70090004").setDisplay("Cowpox").setSystem(SYSTEM_SNOMED_CT);


  private static Map<String, Coding[]> forecastCodeToSnomedMap = new HashMap<>();
  static {
    forecastCodeToSnomedMap.put("Influenza",
        new Coding[] {DISEASE_INFLUENZA_CAUSED_BY_SEASONAL_INFLUENZA_VIRUS});
    forecastCodeToSnomedMap.put("HepB", new Coding[] {DISEASE_TYPE_B_VIRAL_HEPATITIS});
    forecastCodeToSnomedMap.put("Diphtheria", new Coding[] {DISEASE_DIPHTHERIA, DISEASE_TETANUS});
    forecastCodeToSnomedMap.put("Pertussis", new Coding[] {DISEASE_PERTUSSIS});
    forecastCodeToSnomedMap.put("Hib",
        new Coding[] {DISEASE_HAEMOPHILUS_INFLUENZAE_TYPE_B_INFECTION});
    forecastCodeToSnomedMap.put("Pneumo", new Coding[] {DISEASE_PNEUMOCOCCAL_INFECTIOUS_DISEASE});
    forecastCodeToSnomedMap.put("Polio",
        new Coding[] {DISEASE_INFECTION_CAUSED_BY_HUMAN_POLIOVIRUS});
    forecastCodeToSnomedMap.put("Rotavirus",
        new Coding[] {DISEASE_VIRAL_GASTROENTERITIS_DUE_TO_ROTAVIRUS});
    forecastCodeToSnomedMap.put("Measles", new Coding[] {DISEASE_MEASLES});
    forecastCodeToSnomedMap.put("Mumps", new Coding[] {DISEASE_MUMPS});
    forecastCodeToSnomedMap.put("Rubella", new Coding[] {DISEASE_RUBELLA});
    forecastCodeToSnomedMap.put("Varicella", new Coding[] {DISEASE_VARICELLA});
    forecastCodeToSnomedMap.put("Mening", new Coding[] {DISEASE_MENINGOCOCCAL_INFECTIOUS_DISEASE});
    forecastCodeToSnomedMap.put("HepA", new Coding[] {});
    forecastCodeToSnomedMap.put("HPV", new Coding[] {});
    forecastCodeToSnomedMap.put("Zoster", new Coding[] {DISEASE_HERPES_ZOSTER});
    forecastCodeToSnomedMap.put("Pneumo65", new Coding[] {DISEASE_PNEUMOCOCCAL_INFECTIOUS_DISEASE});
    forecastCodeToSnomedMap.put("MeningococcalB",
        new Coding[] {DISEASE_MENINGOCOCCAL_INFECTIOUS_DISEASE});
    forecastCodeToSnomedMap.put("MeningBexsero",
        new Coding[] {DISEASE_MENINGOCOCCAL_INFECTIOUS_DISEASE});
    forecastCodeToSnomedMap.put("MeningTrumenba",
        new Coding[] {DISEASE_MENINGOCOCCAL_INFECTIOUS_DISEASE});
  }

  @Operation(global = true, idempotent = true, name = "$immds-forecast")
  public Parameters ImmDSForecastOperation(
      @OperationParam(name = "assessmentDate") DateParam assessmentDate,
      @OperationParam(name = "patient") Patient patient,
      @OperationParam(name = "immunization") List<Immunization> immunizationList,
      @OperationParam(name = "Immunization") List<Immunization> immunizationList2)
      throws BaseServerResponseException {

    System.out.println("--> Calling ImmDSForecastOperation");
    try {
      List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
      PatientRecordDataBean patientRecord = new PatientRecordDataBean();
      List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();
      DateTime forecastDate;
      if (assessmentDate != null) {
        forecastDate = new DateTime(assessmentDate.getValue());
      } else {
        forecastDate = new DateTime();
      }
      ForecastOptions forecastOptions = new ForecastOptions();
      boolean dueUseEarly = false;

      Map<String, CvxCode> cvxToVaccineIdMap = null;
      try {
        cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
      } catch (Exception e) {
        throw new UnclassifiedServerFailureException(500, "Unable to load CVX map");
      }

      System.out.println("--> Reading patient");
      readPatient(patient, patientRecord);
      System.out.println("--> Reading immunizations");
      if (immunizationList == null) {
        immunizationList = immunizationList2;
      }
      if (immunizationList != null) {
        readImmunization(immunizationList, imms, cvxToVaccineIdMap);
      }

      System.out.println("--> Forecasting");
      ForecastHandlerCore forecastHandlerCore =
          ForecastManagerSingleton.getForecastManagerSingleton().getForecastHandlerCore();

      List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
      String forecasterScheduleName = "";
      try {
        Map<String, List<Trace>> traceMap = new HashMap<String, List<Trace>>();
        forecasterScheduleName = forecastHandlerCore.forecast(doseList, patientRecord, imms,
            forecastDate, traceMap, resultList, forecastOptions);
      } catch (Exception e) {
        throw new UnclassifiedServerFailureException(500,
            "Unexpected exception when trying to generate forecast");
      }
      ForecastHandlerCore.sort(resultList);

      System.out.println("--> Writing out response");

      Parameters parameters = new Parameters();



      Date today = new Date();

      for (VaccinationDoseDataBean dose : doseList) {
        if (dose.getStatusCode() == null
            || dose.getStatusCode() == VaccinationDoseDataBean.STATUS_MISSED) {
          continue;
        }
        Coding[] snomedCodings = forecastCodeToSnomedMap.get(dose.getForecastCode());
        Immunization immunization =
            immunizationList.get(Integer.parseInt(dose.getVaccinationId()) - 1);
        if (snomedCodings != null && immunization != null) {
          for (Coding snomedCoding : snomedCodings) {
            ImmunizationEvaluation immunizationEvaluation = new ImmunizationEvaluation();
            immunizationEvaluation.setId(dose.getVaccinationId());
            immunizationEvaluation.setStatus(ImmunizationEvaluationStatus.COMPLETED);
            immunizationEvaluation.setPatient(new Reference(patient));
            immunizationEvaluation.setDate(today);
            immunizationEvaluation.getTargetDisease().addCoding(snomedCoding);
            immunizationEvaluation.getTargetDisease().addCoding().setSystem(SYSTEM_LONESTAR)
                .setCode(dose.getForecastCode()).setDisplay(dose.getForecastCode());
            immunizationEvaluation.setImmunizationEvent(new Reference(immunization));
            if (dose.getStatusCode() == VaccinationDoseDataBean.STATUS_INVALID) {
              immunizationEvaluation.getDoseStatus().addCoding(EVALUATION_DOSE_STATUS_VALID);
            } else {
              immunizationEvaluation.getDoseStatus().addCoding(EVALUATION_DOSE_STATUS_NOTVALID);
            }
            if (dose.getReason() != null && !dose.getReason().equals("")) {
              immunizationEvaluation.addDoseStatusReason().setText(dose.getReason());
            }
            try {
              int theValue = Integer.parseInt(dose.getDoseCode());
              immunizationEvaluation.setDoseNumber(new PositiveIntType().setValue(theValue));
            } catch (NumberFormatException nfe) {
              // ignore
            }
            parameters.addParameter().setName("evaluation").setResource(immunizationEvaluation);
          }
        }
      }
      ImmunizationRecommendation immunizationRecommendation = new ImmunizationRecommendation();
      immunizationRecommendation.setPatient(new Reference(patient));
      immunizationRecommendation.setDate(today);
      for (ImmunizationForecastDataBean forecast : resultList) {
        ImmunizationRecommendationRecommendationComponent comp =
            new ImmunizationRecommendationRecommendationComponent();
        comp.setId(forecast.getForecastLabel());


        immunizationRecommendation.getRecommendation().add(comp);
        String doseDueCode = CaretForecaster.doseDueOutHash.get(forecast.getForecastName());
        if (doseDueCode == null) {
          doseDueCode = forecast.getForecastName();
        }
        comp.addVaccineCode().addCoding().setCode(doseDueCode)
            .setDisplay(forecast.getForecastName()).setSystem("http://hl7.org/fhir/sid/cvx");
        // comp.setTargetDisease(value);

        String statusText = forecast.getStatusDescriptionInternal();
        String statusId = statusText;
        switch (statusText) {
          case ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE:
          case ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER:
            statusId = IR_STATUS_DUE;
            break;
          case ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE:
            statusId = IR_STATUS_OVERDUE;
            break;
          case ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED:
            statusId = IR_STATUS_CONTRAINDICATED;
            break;
          case ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED:
          case ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE:
          case ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE_FOR_SEASON:
            statusId = IR_STATUS_COMPLETE;
            break;
          case ImmunizationForecastDataBean.STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE:
            statusId = IR_STATUS_IMMUNE;
            break;

        }
        CodeableConcept forecastStatus = new CodeableConcept();
        forecastStatus.addCoding().setCode(statusId)
            .setSystem("http://hl7.org/fhir/us/ImmunizationFHIRDS/CodeSystem/ForecastStatus")
            .setDisplay(statusText);
        comp.setForecastStatus(forecastStatus);
        addDateCriterion(comp, IR_DATE_CRITERION_DUE, "Due", forecast.getDue());
        addDateCriterion(comp, IR_DATE_CRITERION_OVERDUE, "Overdue", forecast.getOverdue());
        addDateCriterion(comp, IR_DATE_CRITERION_LATEST, "Latest", forecast.getFinished());
        addDateCriterion(comp, IR_DATE_CRITERION_EARLIEST, "Valid", forecast.getValid());
      }
      parameters.addParameter().setName("recommendation").setResource(immunizationRecommendation);
      System.out.println("--> Returning result");
      return parameters;
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    } finally {
      System.out.println("--> Finally");
    }
  }

  private void addDateCriterion(ImmunizationRecommendationRecommendationComponent comp,
      String dcCode, String dcLabel, Date date) {
    if (date != null) {
      ImmunizationRecommendationRecommendationDateCriterionComponent dateCriterion =
          comp.addDateCriterion();
      CodeableConcept code = new CodeableConcept();
      code.addCoding().setSystem("http://loinc.org").setCode(dcCode).setDisplay(dcLabel);
      dateCriterion.setCode(code);
      dateCriterion.setValue(date);
    }
  }

  private void readImmunization(List<Immunization> immunizationList,
      List<ImmunizationInterface> imms, Map<String, CvxCode> cvxToVaccineIdMap) {
    int vaccCount = 0;
    for (Immunization immunization : immunizationList) {
      vaccCount++;
      String vaccineCvx;
      {
        Coding coding;
        if (immunization.getVaccineCode() == null
            || (coding = immunization.getVaccineCode().getCodingFirstRep()) == null) {
          throw new UnprocessableEntityException("No vaccine code for vaccination #" + vaccCount);
        }
        if (coding.getSystem() != null && !coding.getSystem().equals("http://hl7.org/fhir/sid/cvx")
            && !coding.getSystem().equals("")) {
          throw new UnprocessableEntityException("Unable to read code, unspported code system '"
              + coding.getSystem() + "' at vaccination #" + vaccCount);
        }
        vaccineCvx = coding.getCode();
        if (vaccineCvx == null || vaccineCvx.equals("")) {
          throw new UnprocessableEntityException(
              "No CVX code specified for vaccination #" + vaccCount);
        }
      }
      int vaccineId = 0;
      {
        CvxCode cvxCode = cvxToVaccineIdMap.get(vaccineCvx);
        if (cvxCode == null) {
          cvxCode = cvxToVaccineIdMap.get("0" + vaccineCvx);
        }
        if (cvxCode == null) {
          throw new UnprocessableEntityException(
              "Code '" + vaccineCvx + "' is not recognized for vaccination #" + vaccCount);
        } else {
          vaccineId = cvxCode.getVaccineId();
        }
      }

      if (immunization.getOccurrenceDateTimeType() == null
          || immunization.getOccurrenceDateTimeType().getValue() == null) {
        throw new UnprocessableEntityException(
            "Occurance date was not found for vaccination #" + vaccCount);
      }

      org.immregistries.lonestar.core.model.Immunization imm =
          new org.immregistries.lonestar.core.model.Immunization();

      imm.setCvx(vaccineCvx);
      imm.setDateOfShot(immunization.getOccurrenceDateTimeType().getValue());
      imm.setVaccineId(vaccineId);
      imm.setVaccinationId("" + vaccCount);
      imms.add(imm);
    }
  }

  private void readPatient(Patient patient, PatientRecordDataBean patientRecord) {
    if (patient.getBirthDate() == null) {
      throw new UnprocessableEntityException("Patient birth date was not specified");
    }
    Date today = new Date();
    if (patient.getBirthDate().after(today)) {
      throw new UnprocessableEntityException("Patient birth date is in the future");
    }
    Calendar c = Calendar.getInstance();
    c.add(Calendar.YEAR, -150);
    Date longTimeAgo = c.getTime();
    if (patient.getBirthDate().before(longTimeAgo)) {
      throw new UnprocessableEntityException("Patient birth date was not specified");
    }
    patientRecord.setDob(new DateTime(patient.getBirthDate()));
    patientRecord.setSex(AdministrativeGender.MALE == patient.getGender() ? "M" : "F");
  }

}
