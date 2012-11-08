package org.tch.forecast.core.api.model;

import java.util.List;

public interface ForecastResponseInterface {
  /**
   * A human readable description of the evaluation schedule used. This
   * indicates which schedule was used by the forecaster to list the result.
   * This value may be different than what was requested.
   * 
   * @return
   */
  public String getEvaluationSchedule();

  /**
   * The CDC assigned MVX code for the manufacturer. This is optional. For the
   * list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @param evaluationSchedule
   */
  public void setEvaluationSchedule(String evaluationSchedule);

  /**
   * A list of recommendations, one for each antigen.
   * 
   * @return
   */
  public List<ForecastRecommendationInterface> getRecommendationList();

  /**
   * A list of recommendations, one for each vaccination.
   * 
   * @param recommendationList
   */
  public void setRecommendationList(List<ForecastRecommendationInterface> recommendationList);
}
