package org.tch.forecast.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.api.impl.ForecastOptions;
import org.tch.forecast.core.logic.ActionStep;
import org.tch.forecast.core.logic.ActionStepFactory;
import org.tch.forecast.core.logic.DataStore;
import org.tch.forecast.core.logic.EndStep;
import org.tch.forecast.core.logic.StartStep;

public class Forecaster
{

  public static final int VARICELLA_HISTORY = 378;

  private List<ImmunizationInterface> vaccinations;
  private PatientForecastRecordDataBean patient = null;
  private List<Event> eventList = null;
  private Map<String, List<Trace>> traces = null;
  private Date forecastDate = new Date();
  boolean hasHistoryOfVaricella = false;
  private StringBuffer detailLog = null;
  private ForecastOptions forecastOptions = null;

  public ForecastOptions getForecastOptions() {
    return forecastOptions;
  }

  public void setForecastOptions(ForecastOptions forecastOptions) {
    this.forecastOptions = forecastOptions;
  }

  public Map<String, List<Trace>> getTraces() {
    return traces;
  }

  public PatientForecastRecordDataBean getPatient() {
    return patient;
  }

  public void setDetailLog(StringBuffer detailLog) {
    this.detailLog = detailLog;
  }

  public StringBuffer getDetailLog() {
    return detailLog;
  }

  private VaccineForecastManagerInterface forecastManager = null;

  public Forecaster(VaccineForecastManagerInterface forecastManager) {
    this.forecastManager = forecastManager;
  }

  public ForecastSchedule getForecastSchedule() {
    return forecastManager.getForecastSchedule();
  }

  public List<ImmunizationForecastDataBean> forecast(List<ImmunizationForecastDataBean> resultList,
      List<VaccinationDoseDataBean> doseList, Map<String, List<Trace>> traces) throws Exception {
    if (forecastOptions == null) {
      forecastOptions = new ForecastOptions();
    }
    DataStore dataStore = new DataStore(forecastManager);
    dataStore.setResultList(resultList);
    dataStore.setDoseList(doseList);
    dataStore.setDetailLog(detailLog);
    dataStore.setTraces(traces);
    dataStore.setForecastDate(forecastDate);
    dataStore.setPatient(patient);
    dataStore.setVaccinations(vaccinations);
    dataStore.setForecastOptions(forecastOptions);
    String nextActionName = StartStep.NAME;
    ActionStep actionStep = ActionStepFactory.get(nextActionName);
    while (!actionStep.getName().equals(EndStep.NAME)) {
      nextActionName = actionStep.doAction(dataStore);
      actionStep = ActionStepFactory.get(nextActionName);
    }
    return dataStore.getResultList();
  }

  public void setPatient(PatientForecastRecordDataBean patient) {
    this.patient = patient;
  }

  public List<ImmunizationInterface> getVaccinations() {
    return vaccinations;
  }

  public void setVaccinations(List<ImmunizationInterface> vaccList) {
    this.vaccinations = new ArrayList<ImmunizationInterface>(vaccList);
    vaccList = new ArrayList<ImmunizationInterface>(vaccList);
    Collections.sort(vaccList, new Comparator<ImmunizationInterface>() {
      public int compare(ImmunizationInterface imm1, ImmunizationInterface imm2) {
        return imm1.getDateOfShot().compareTo(imm2.getDateOfShot());
      }
    });
    eventList = new ArrayList<Event>();
    Event event = null;
    hasHistoryOfVaricella = false;
    for (ImmunizationInterface imm : vaccList) {
      if (event == null || !event.eventDate.equals(imm.getDateOfShot())) {
        event = new Event();
        eventList.add(event);
        event.eventDate = imm.getDateOfShot();
      }
      if (imm.getVaccineId() == VARICELLA_HISTORY) {
        hasHistoryOfVaricella = true;
      }
      event.immList.add(imm);
    }
  }

  public Date getForecastDate() {
    return forecastDate;
  }

  public void setForecastDate(Date forecastDate) {
    this.forecastDate = forecastDate;
  }

  private class Event
  {
    private Date eventDate = null;
    private List<ImmunizationInterface> immList = new ArrayList<ImmunizationInterface>();
    private boolean hasEvent = false;
  }

}
