package org.immregistries.lonestar.fhir;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.ImmunizationRecommendation;
import org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent;
import org.hl7.fhir.r4.model.ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Reference;
import org.immregistries.lonestar.ForecastManagerSingleton;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.api.impl.CvxCode;
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

  @Operation(global = true, idempotent = true, name = "$immds-forecast")
  public Bundle ImmDSForecastOperation(
      @OperationParam(name = "assessmentDate") DateParam assessmentDate,
      @OperationParam(name = "patient") Patient patient,
      @OperationParam(name = "immunization") List<Immunization> immunizationList)
      throws BaseServerResponseException {

    List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
    PatientRecordDataBean patientRecord = new PatientRecordDataBean();
    List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();
    DateTime forecastDate = new DateTime(assessmentDate.getValue());
    ForecastOptions forecastOptions = new ForecastOptions();
    boolean dueUseEarly = false;

    Map<String, CvxCode> cvxToVaccineIdMap = null;
    try {
      cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
    } catch (Exception e) {
      throw new UnclassifiedServerFailureException(500, "Unable to load CVX map");
    }

    readPatient(patient, patientRecord);
    readImmunization(immunizationList, imms, cvxToVaccineIdMap);

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


    Bundle bundle = new Bundle();

    for (VaccinationDoseDataBean dose : doseList) {
      //      out.print("Vaccination #" + dose.getVaccinationId() + ": ");
      //      out.print(forecastManager.getVaccineName(dose.getVaccineId()));
      //      out.print(" given " + new DateTime(dose.getAdminDate()).toString("M/D/Y"));
      //      out.print(" is " + dose.getStatusCodeLabelA() + " " + dose.getForecastCode());
      //      out.print(" dose " + dose.getDoseCode());
      //      if (dose.getReason() != null && !dose.getReason().equals("")) {
      //        out.print(" because " + dose.getReason());
      //      }
      //      out.println(". " + dose.getWhenValidText() + ".");
      //      ImmunizationEvaluation immunizationEvaluation = new ImmunizationEvaluation();
      //
      //
      //
      //      bundle.addEntry().setResource(immunizationEvaluation);
    }
    ImmunizationRecommendation immunizationRecommendation = new ImmunizationRecommendation();
    immunizationRecommendation
        .setPatient(new Reference().setIdentifier(patient.getIdentifierFirstRep()));
    for (ImmunizationForecastDataBean forecast : resultList) {
      ImmunizationRecommendationRecommendationComponent comp =
          new ImmunizationRecommendationRecommendationComponent();
      immunizationRecommendation.getRecommendation().add(comp);
      String doseDueCode = CaretForecaster.doseDueOutHash.get(forecast.getForecastName());
      if (doseDueCode == null) {
        doseDueCode = forecast.getForecastName();
      }
      comp.addVaccineCode().addCoding().setCode(doseDueCode).setDisplay(forecast.getForecastName())
          .setSystem("http://hl7.org/fhir/sid/cvx");
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

    bundle.addEntry().setResource(immunizationRecommendation);
    return bundle;
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
        if (immunization.getVaccineCode() == null || immunization.getVaccineCode() == null
            || (coding = immunization.getVaccineCode().getCodingFirstRep()) == null) {
          throw new UnprocessableEntityException("No vaccine code for vaccination #" + vaccCount);
        }
        if (!coding.getSystem().equals("http://hl7.org/fhir/sid/cvx")) {
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
