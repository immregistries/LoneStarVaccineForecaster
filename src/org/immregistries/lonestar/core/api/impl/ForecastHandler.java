package org.immregistries.lonestar.core.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.DecisionProcessFormat;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.api.model.ForecastHandlerInterface;
import org.immregistries.lonestar.core.api.model.ForecastPatientInterface;
import org.immregistries.lonestar.core.api.model.ForecastRecommendationInterface;
import org.immregistries.lonestar.core.api.model.ForecastRequestInterface;
import org.immregistries.lonestar.core.api.model.ForecastResponseInterface;
import org.immregistries.lonestar.core.api.model.ForecastVaccinationInterface;
import org.immregistries.lonestar.core.model.Immunization;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;

public class ForecastHandler implements ForecastHandlerInterface {

  private static Map<String, CvxCode> cvxToVaccineIdMap = null;

  public static Map<String, CvxCode> getCvxToVaccineIdMap() throws Exception {
    if (cvxToVaccineIdMap == null) {
      cvxToVaccineIdMap = CvxCodes.getCvxToCvxCodeMap();
    }
    return cvxToVaccineIdMap;
  }

  private synchronized void initCvxCodes() throws Exception {
    if (cvxToVaccineIdMap == null) {
      cvxToVaccineIdMap = CvxCodes.getCvxToCvxCodeMap();
    }
  }
 

  private static ForecastHandlerCore forecastHandlerCore = null;

  public ForecastHandler() throws Exception {
    initCvxCodes();
  }

  public ForecastResponseInterface forecast(ForecastRequestInterface forecastRequest) throws Exception {

    ForecastResponseInterface forecastResponse = new ForecastResponse();

    List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
    PatientRecordDataBean patient = new PatientRecordDataBean();
    List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();

    DateTime forecastDate = new DateTime();
    if (forecastRequest.getEvaluationDate() != null) {
      forecastDate = new DateTime(forecastRequest.getEvaluationDate());
    }

    ForecastPatientInterface forecastPatient = forecastRequest.getPatient();
    patient.setDob(new DateTime(forecastPatient.getBirthDate()));
    patient.setSex(forecastPatient.getSex().toUpperCase());
    for (ForecastVaccinationInterface forecastVaccination : forecastRequest.getVaccinationList()) {

      String vaccineCvx = forecastVaccination.getCvxCode();
      String vaccineMvx = forecastVaccination.getMvxCode();
      int vaccineId = 0;
      if (vaccineCvx == null) {
        throw new Exception("CVX code not indicated, required field");
      } else {
        if (!cvxToVaccineIdMap.containsKey(vaccineCvx) && !cvxToVaccineIdMap.containsKey("0" + vaccineCvx)) {
          throw new Exception("CVX code '" + vaccineCvx + "' is not recognized");
        }
        CvxCode cvxCode = null;
        if (cvxToVaccineIdMap.containsKey(vaccineCvx)) {
          cvxCode = cvxToVaccineIdMap.get(vaccineCvx);
        } else {
          cvxCode = cvxToVaccineIdMap.get("0" + vaccineCvx);
        }
        if (cvxCode != null)
        {
          vaccineId = cvxCode.getVaccineId();
        }
        if (vaccineId == 0) {
          throw new Exception("CVX code '" + vaccineCvx + "' is not recognized");
        }
      }
      Immunization imm = new Immunization();
      imm.setCvx(vaccineCvx);
      imm.setDateOfShot(forecastVaccination.getAdminDate());
      imm.setVaccineId(vaccineId);
      imm.setMvx(vaccineMvx);
      imm.setVaccinationId(forecastVaccination.getVaccinationId());
      imms.add(imm);
    }
    
    ForecastOptions forecastOptions = new ForecastOptions();

    Map traceMap = new HashMap();
    List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
    VaccineForecastManager vaccineForecastManager = new VaccineForecastManager();
    ForecastHandlerCore forecastHandlerCore = new ForecastHandlerCore(vaccineForecastManager);
    String forecasterScheduleName = forecastHandlerCore.forecast(doseList, patient, imms, forecastDate, traceMap,
        resultList, forecastOptions);

    forecastResponse.setEvaluationSchedule(forecasterScheduleName);

    List<ForecastRecommendationInterface> forecastRecommendationList = new ArrayList<ForecastRecommendationInterface>();
    forecastResponse.setRecommendationList(forecastRecommendationList);

    {

      traceMap.remove(ImmunizationForecastDataBean.PERTUSSIS);

      for (Iterator<ImmunizationForecastDataBean> it = resultList.iterator(); it.hasNext();) {
        ImmunizationForecastDataBean forecast = it.next();
        ForecastRecommendationInterface forecastRecommendation = new ForecastRecommendation();

        forecastRecommendation.setAntigenName(forecast.getForecastNameOriginal());
        forecastRecommendation.setDisplayLabel(forecast.getForecastLabel());
        forecastRecommendation.setDoseNumber(forecast.getDose());
        forecastRecommendation.setDueDate(forecast.getDue());
        forecastRecommendation.setValidDate(forecast.getValid());
        forecastRecommendation.setOverdueDate(forecast.getOverdue());
        forecastRecommendation.setFinishedDate(forecast.getFinished());
        forecastRecommendation.setDecisionProcessTextHTML(forecast.getTraceList().getExplanation(DecisionProcessFormat.HTML).toString());
        forecastRecommendation.setStatusDescription(forecast.getStatusDescription());
        forecastRecommendationList.add(forecastRecommendation);
      }
    }
    List<ForecastVaccinationInterface> forecastVaccinationList = new ArrayList<ForecastVaccinationInterface>();
    for (VaccinationDoseDataBean dose : doseList) {
      ForecastVaccinationInterface fv = new ForecastVaccination();
      fv.setAdminDate(dose.getAdminDate());
      fv.setCvxCode(dose.getCvxCode());
      fv.setMvxCode(dose.getMvxCode());
      fv.setDoseCode(dose.getDoseCode());
      fv.setForecastCode(dose.getForecastCode());
      fv.setReasonText(dose.getReason());
      fv.setWhenValidText(dose.getWhenValidText());
      fv.setScheduleCode(dose.getScheduleCode());
      fv.setInternalCode(String.valueOf(dose.getVaccineId()));
      fv.setVaccinationId(dose.getVaccinationId());
      fv.setStatusCode(dose.getStatusCode());
      forecastVaccinationList.add(fv);
    }
    forecastResponse.setVaccinationList(forecastVaccinationList);
    return forecastResponse;
  }

}
