package org.tch.forecast.core.api.model;

import java.util.Date;

/**
 * 
 * 
 * Represents a recommendation for a dose of vaccine.
 * 
 * @author Nathan Bunker
 */
public interface ForecastRecommendationInterface {
  /**
   * A human readable label for this recommendation (for example 'DTaP')
   * 
   * @return
   */
  public String getDisplayLabel();

  /**
   * A human readable label for this recommendation (for example 'DTaP')
   * 
   * @param displayLabel
   */
  public void setDisplayLabel(String displayLabel);

  /**
   * The name of the antigen being forecast for. This name is more stable than
   * the display label.
   * 
   * @return
   */
  public String getAntigenName();

  /**
   * The name of the antigen being forecast for. This name is more stable than
   * the display label.
   * 
   * @param antigenName
   */
  public void setAntigenName(String antigenName);

  /**
   * A human readable description of how the result was obtained.
   * 
   * @return
   */
  public String getEvaluationExplanation();

  /**
   * A human readable description of how the result was obtained.
   * 
   * @param evaluationExplanation
   */
  public void setEvaluationExplanation(String evaluationExplanation);

  /**
   * The next dose number recommended.
   * 
   * @return
   */
  public String getDoseNumber();

  /**
   * The next dose number recommended.
   * 
   * @param doseNumber
   */
  public void setDoseNumber(String doseNumber);

  /**
   * The earliest date the vaccination is recommended to be given.
   * 
   * @return
   */
  public Date getDueDate();

  /**
   * The earliest date the vaccination is recommended to be given.
   * 
   * @param dueDate
   */
  public void setDueDate(Date dueDate);

  /**
   * The date the vaccination could be given and be considered valid.
   * 
   * @return
   */
  public Date getValidDate();

  /**
   * The date the vaccination could be given and be considered valid.
   * 
   * @param validDate
   */
  public void setValidDate(Date validDate);

  /**
   * The earliest date the vaccination would be considered overdue to have been
   * given.
   * 
   * @return
   */
  public Date getOverdueDate();

  /**
   * The earliest date the vaccination would be considered overdue to have been
   * given.
   * 
   * @param overdueDate
   */
  public void setOverdueDate(Date overdueDate);

  /**
   * The date this vaccination should not longer be given or is not expected to
   * be given.
   * 
   * @return
   */
  public Date getFinishedDate();

  /**
   * The date this vaccination should not longer be given or is not expected to
   * be given.
   * 
   * @param finishedDate
   */
  public void setFinishedDate(Date finishedDate);

  /**
   * A human readable status indicating whether a dose is due today or not.
   * 
   * @return
   */
  public String getStatusDescription();

  /**
   * A human readable status indicating whether a dose is due today or not.
   * 
   * @param statusDescription
   */
  public void setStatusDescription(String statusDescription);

}
