package org.immregistries.lonestar.core.api.impl;

import java.util.ArrayList;
import java.util.List;
import org.immregistries.lonestar.core.api.model.ForecastRecommendationInterface;
import org.immregistries.lonestar.core.api.model.ForecastResponseInterface;
import org.immregistries.lonestar.core.api.model.ForecastVaccinationInterface;

public class ForecastResponse implements ForecastResponseInterface {
  private List<ForecastRecommendationInterface> recommendationList =
      new ArrayList<ForecastRecommendationInterface>();
  private String evaluationSchedule = "";
  private List<ForecastVaccinationInterface> forecastVaccinationList =
      new ArrayList<ForecastVaccinationInterface>();

  public List<ForecastVaccinationInterface> getVaccinationList() {
    return forecastVaccinationList;
  }

  public void setVaccinationList(List<ForecastVaccinationInterface> forecastVaccinationList) {
    this.forecastVaccinationList = forecastVaccinationList;
  }

  public String getEvaluationSchedule() {
    return evaluationSchedule;
  }

  public void setEvaluationSchedule(String evaluationSchedule) {
    this.evaluationSchedule = evaluationSchedule;
  }

  public List<ForecastRecommendationInterface> getRecommendationList() {
    return recommendationList;
  }

  public void setRecommendationList(List<ForecastRecommendationInterface> recommendationList) {
    this.recommendationList = recommendationList;
  }

}
