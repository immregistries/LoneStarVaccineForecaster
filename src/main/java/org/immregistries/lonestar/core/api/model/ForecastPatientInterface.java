package org.immregistries.lonestar.core.api.model;

import java.util.Date;

/**
 * 
 * Represents a patient that needs a vaccination forecast.
 * 
 * @author Nathan Bunker
 */
public interface ForecastPatientInterface {

  /**
   * The date the patient was born. This is required and is used by the
   * forecaster to determine the patient's schedule.
   * 
   * @return
   */
  public Date getBirthDate();

  /**
   * The date the patient was born. This is required and is used by the
   * forecaster to determine the patient's schedule.
   * 
   * @param birthDate
   */
  public void setBirthDate(Date birthDate);

  /**
   * A single digit code, usually M or F, indicating the patient's sex for
   * purposes of evaluation. In the past this value was important for
   * forecasting some vaccine series.
   * 
   * @return
   */
  public String getSex();

  /**
   * A single digit code, usually M or F, indicating the patient's sex for
   * purposes of evaluation. In the past this value was important for
   * forecasting some vaccine series.
   * 
   * @param sex
   */
  public void setSex(String sex);
}
