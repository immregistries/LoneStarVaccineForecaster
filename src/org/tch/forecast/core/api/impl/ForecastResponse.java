package org.tch.forecast.core.api.impl;

import java.util.ArrayList;
import java.util.List;

import org.tch.forecast.core.api.model.ForecastRecommendationInterface;
import org.tch.forecast.core.api.model.ForecastResponseInterface;

public class ForecastResponse implements ForecastResponseInterface {
  private List<ForecastRecommendationInterface> recommendationList = new ArrayList<ForecastRecommendationInterface>();
  private String evaluationSchedule = "";

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
