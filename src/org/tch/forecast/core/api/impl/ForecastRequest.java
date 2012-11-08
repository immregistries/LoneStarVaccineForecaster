package org.tch.forecast.core.api.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tch.forecast.core.api.model.ForecastPatientInterface;
import org.tch.forecast.core.api.model.ForecastRequestInterface;
import org.tch.forecast.core.api.model.ForecastVaccinationInterface;

public class ForecastRequest implements ForecastRequestInterface {
  private Date forecastEvaluationDate = null;
  private String evaluationSchedule = "";
  private ForecastPatientInterface forecastPatient = null;
  private List<ForecastVaccinationInterface> forecastVaccinationList = new ArrayList<ForecastVaccinationInterface>();

  public List<ForecastVaccinationInterface> getVaccinationList() {
    return forecastVaccinationList;
  }

  public void setVaccinationList(List<ForecastVaccinationInterface> forecastVaccinationList) {
    this.forecastVaccinationList = forecastVaccinationList;
  }

  public ForecastPatientInterface getPatient() {
    return forecastPatient;
  }

  public void setPatient(ForecastPatientInterface forecastPatient) {
    this.forecastPatient = forecastPatient;
  }

  public String getEvaluationSchedule() {
    return evaluationSchedule;
  }

  public void setEvaluationSchedule(String evaluationSchedule) {
    this.evaluationSchedule = evaluationSchedule;
  }

  public Date getEvaluationDate() {
    return forecastEvaluationDate;
  }

  public void setEvaluationDate(Date forecastEvaluationDate) {
    this.forecastEvaluationDate = forecastEvaluationDate;
  }

}
