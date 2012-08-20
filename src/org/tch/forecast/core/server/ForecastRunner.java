package org.tch.forecast.core.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.Forecaster;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.model.PatientRecordDataBean;

public class ForecastRunner {
  private VaccineForecastManager vaccineForecastManager;

  private List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
  public List<VaccinationDoseDataBean> getDoseList() {
    return doseList;
  }
  public void setDoseList(List<VaccinationDoseDataBean> doseList) {
    this.doseList = doseList;
  }
  public PatientRecordDataBean getPatient() {
    return patient;
  }
  public void setPatient(PatientRecordDataBean patient) {
    this.patient = patient;
  }
  public List<ImmunizationInterface> getImms() {
    return imms;
  }
  public void setImms(List<ImmunizationInterface> imms) {
    this.imms = imms;
  }
  public Date getForecastDate() {
    return forecastDate;
  }
  public void setForecastDate(Date forecastDate) {
    this.forecastDate = forecastDate;
  }
  public List<ImmunizationForecastDataBean> getResultList() {
    return resultList;
  }
  public void setResultList(List<ImmunizationForecastDataBean> resultList) {
    this.resultList = resultList;
  }
  public Map getTraceMap() {
    return traceMap;
  }
  public void setTraceMap(Map traceMap) {
    this.traceMap = traceMap;
  }
  private PatientRecordDataBean patient = new PatientRecordDataBean();
  private List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();
  private Date forecastDate = null;
  private List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
  private Map traceMap = new HashMap();
  
  
  public ForecastRunner(VaccineForecastManager vaccineForecastManager) throws Exception
  {
    this.vaccineForecastManager = vaccineForecastManager;
  }
  public void forecast() throws Exception
  {
    StringBuffer traceBuffer = new StringBuffer();
    Forecaster forecaster = new Forecaster(vaccineForecastManager);
    forecaster.setPatient(patient);
    forecaster.setVaccinations(imms);
    forecaster.setForecastDate(forecastDate);
    forecaster.forecast(resultList, doseList, traceBuffer, traceMap);

  }
}
