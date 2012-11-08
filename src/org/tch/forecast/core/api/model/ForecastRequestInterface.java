package org.tch.forecast.core.api.model;

import java.util.Date;
import java.util.List;

/**
 * 
 * Represents the request for a single patient forecast.
 * 
 * @author Nathan Bunker
 */
public interface ForecastRequestInterface {

  /**
   * List of vaccinations the patient has received.
   * 
   * @return
   */
  public List<ForecastVaccinationInterface> getVaccinationList();

  /**
   * List of vaccinations the patient has received.
   * 
   * @param forecastVaccinationList
   */
  public void setVaccinationList(List<ForecastVaccinationInterface> forecastVaccinationList);

  /**
   * The schedule being requested. Optional.
   * 
   * @return
   */
  public String getEvaluationSchedule();

  /**
   * The schedule being requested. Optional.
   * 
   * @param evaluationSchedule
   */
  public void setEvaluationSchedule(String evaluationSchedule);

  /**
   * The date the evaluation and forecast should be run for. Optional.
   * 
   * @return
   */
  public Date getEvaluationDate();

  /**
   * The date the evaluation and forecast should be run for. Optional.
   * 
   * @param forecastEvaluationDate
   */
  public void setEvaluationDate(Date forecastEvaluationDate);

  /**
   * The patient for which this vaccination forecast is being generated.
   * Required.
   * 
   * @return
   */
  public ForecastPatientInterface getPatient();

  /**
   * The patient for which this vaccination forecast is being generated.
   * Required.
   * 
   * @param forecastPatient
   */
  public void setPatient(ForecastPatientInterface forecastPatient);

}
