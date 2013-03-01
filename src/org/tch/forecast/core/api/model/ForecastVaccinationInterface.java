package org.tch.forecast.core.api.model;

import java.util.Date;

/**
 * Represents a vaccination given to a patient.
 * 
 * @author Nathan Bunker
 * 
 */
public interface ForecastVaccinationInterface {

  public static final String STATUS_VALID = "V";
  public static final String STATUS_INVALID = "I";
  public static final String STATUS_MISSED = "M";

  /**
   * The date the vaccination was administered. This is required. If the date is
   * not know then the vaccination should not be included in the evaluation.
   * 
   * @return
   */
  public Date getAdminDate();

  /**
   * The date the vaccination was administered. This is required. If the date is
   * not know then the vaccination should not be included in the evaluation.
   * 
   * @param adminDate
   */
  public void setAdminDate(Date adminDate);

  /**
   * The CDC assigned CVX code for the vaccination. This is required. If the
   * code is not known then the vaccination should not be included in the
   * evaluation. For the list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @return
   */
  public String getCvxCode();

  /**
   * The CDC assigned CVX code for the vaccination. This is required. If the
   * code is not known then the vaccination should not be included in the
   * evaluation. For the list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @param cvxCode
   */
  public void setCvxCode(String cvxCode);

  /**
   * The CDC assigned MVX code for the manufacturer. This is optional. For the
   * list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @return
   */
  public String getMvxCode();

  /**
   * The CDC assigned MVX code for the manufacturer. This is optional. For the
   * list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @param mvxCode
   */
  public void setMvxCode(String mvxCode);

  /**
   * The internal TCH code assigned to this dose for the purposes of
   * forecasting.
   * 
   * @return
   */
  public String getTchCode();

  /**
   * The internal TCH code assigned to this dose for the purposes of
   * forecasting.
   * 
   * @param tchCode
   */
  public void setTchCode(String tchCode);

  /**
   * The type of antigen this vaccination was evaluated against. A combination
   * vaccination is normally evaulated against more than one antigen.
   * 
   * @return
   */
  public String getForecastCode();

  /**
   * The type of antigen this vaccination was evaluated against. A combination
   * vaccination is normally evaulated against more than one antigen.
   * 
   * @param forecastCode
   */
  public void setForecastCode(String forecastCode);

  /**
   * The specific schedule this dose was evaluated against. The TCH forecaster
   * breaks a series up into a set of schedule steps. This indicates which
   * schedule step this dose was evaluated on.
   * 
   * @return
   */
  public String getScheduleCode();

  /**
   * The specific schedule this dose was evaluated against. The TCH forecaster
   * breaks a series up into a set of schedule steps. This indicates which
   * schedule step this dose was evaluated on.
   * 
   * @param scheduleCode
   */
  public void setScheduleCode(String scheduleCode);

  /**
   * The dose code that this vacccination was matched to. Does not indicate that
   * this dose was valid, but only that it was considered to be the indicated
   * dose.
   * 
   * @return
   */
  public String getDoseCode();

  /**
   * The dose code that this vacccination was matched to. Does not indicate that
   * this dose was valid, but only that it was considered to be the indicated
   * dose.
   * 
   * @param doseCode
   */
  public void setDoseCode(String doseCode);

  /**
   * Indicates the validity of the vaccination for the specific forecast. A
   * combination may be valid for one forecast and invalid for another.
   * 
   * @return
   */
  public String getStatusCode();

  /**
   * Indicates the validity of the vaccination for the specific forecast. A
   * combination may be valid for one forecast and invalid for another.
   * 
   * @param statusCode
   */
  public void setStatusCode(String statusCode);

  /**
   * Gives a human readable description of the reason why the determination was
   * made.
   * 
   * @return
   */
  public String getReasonText();

  /**
   * Gives a human readable description of the reason why the determination was
   * made.
   * 
   * @param reasonText
   */
  public void setReasonText(String reasonText);

  /**
   * A unique identifier for this particular vaccination event that is assigned
   * by the submitter in order to connect the validation of the vaccinations to
   * what was submitted. This is not required but will be reported back just as
   * submitted.
   * 
   * @return
   */
  public String getVaccinationId();

  /**
   * A unique identifier for this particular vaccination event that is assigned
   * by the submitter in order to connect the validation of the vaccinations to
   * what was submitted. This is not required but will be reported back just as
   * submitted.
   * 
   * @param vaccinationId
   */
  public void setVaccinationId(String vaccinationId);

  /**
   * A human readable text that describes which valid date was chosen and why.
   * This helps providers understand whether the date was selected because of
   * the age of the patient, the interval since the last vaccination, or to
   * space it away from a conflicting vaccinations.
   * 
   * @return
   */
  public String getWhenValidText();
  
  /**
   * A human readable text that describes which valid date was chosen and why.
   * This helps providers understand whether the date was selected because of
   * the age of the patient, the interval since the last vaccination, or to
   * space it away from a conflicting vaccinations.
   * 
   * @param whenValidText
   */
  public void setWhenValidText(String whenValidText);
}
