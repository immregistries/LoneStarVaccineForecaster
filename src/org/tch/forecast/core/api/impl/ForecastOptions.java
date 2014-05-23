package org.tch.forecast.core.api.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tch.forecast.core.DecisionProcessFormat;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.TimePeriod;

public class ForecastOptions
{

  private TimePeriod fluSeasonStart = null;
  private TimePeriod fluSeasonDue = null;
  private TimePeriod fluSeasonOverdue = null;
  private TimePeriod fluSeasonEnd = null;
  private TimePeriod fluSeasonFinished = null;

  private boolean ignoreFourDayGrace = false;
  private DecisionProcessFormat decisionProcessFormat = DecisionProcessFormat.HTML;
  private boolean useEarlyDue = false;
  private boolean useEarlyOverdue = false;
  private boolean recommendWhenValid = false;
  private Set<String> recommendWhenValidSet = null;

  public Set<String> getRecommendWhenValidSet() {
    return recommendWhenValidSet;
  }

  public void setRecommendWhenValidSet(Set<String> recommendWhenValidSet) {
    this.recommendWhenValidSet = recommendWhenValidSet;
  }

  public boolean isRecommendWhenValid() {
    return recommendWhenValid;
  }

  public boolean isRecommendWhenValid(ImmunizationForecastDataBean forecastBean) {
    if (recommendWhenValid && recommendWhenValidSet != null) {
      return recommendWhenValidSet.contains(forecastBean.getForecastName() + "-" + forecastBean.getDose());
    }
    return recommendWhenValid;
  }

  public void setRecommendWhenValid(boolean recommendWhenValid) {
    this.recommendWhenValid = recommendWhenValid;
  }

  private Set<String> assumeCompleteScheduleNameSet = new HashSet<String>();

  public void setAssumeCompleteScheduleName(String scheduleName) {
    assumeCompleteScheduleNameSet.add(scheduleName);
  }

  public Set<String> getAssumeCompleteScheduleNameSet() {
    return assumeCompleteScheduleNameSet;
  }

  public boolean isUseEarlyDue() {
    return useEarlyDue;
  }

  public void setUseEarlyDue(boolean useEarlyDue) {
    this.useEarlyDue = useEarlyDue;
  }

  public boolean isUseEarlyOverdue() {
    return useEarlyOverdue;
  }

  public void setUseEarlyOverdue(boolean useEarlyOverdue) {
    this.useEarlyOverdue = useEarlyOverdue;
  }

  public DecisionProcessFormat getDecisionProcessFormat() {
    return decisionProcessFormat;
  }

  public void setDecisionProcessFormat(DecisionProcessFormat decisionProcessFormat) {
    this.decisionProcessFormat = decisionProcessFormat;
  }

  public boolean isIgnoreFourDayGrace() {
    return ignoreFourDayGrace;
  }

  public void setIgnoreFourDayGrace(boolean ignoreFourDayGrace) {
    this.ignoreFourDayGrace = ignoreFourDayGrace;
  }

  public TimePeriod getFluSeasonStart() {
    return fluSeasonStart;
  }

  public void setFluSeasonStart(TimePeriod fluSeasonStart) {
    this.fluSeasonStart = fluSeasonStart;
  }

  public TimePeriod getFluSeasonDue() {
    return fluSeasonDue;
  }

  public void setFluSeasonDue(TimePeriod fluSeasonDue) {
    this.fluSeasonDue = fluSeasonDue;
  }

  public TimePeriod getFluSeasonOverdue() {
    return fluSeasonOverdue;
  }

  public void setFluSeasonOverdue(TimePeriod fluSeasonOverdue) {
    this.fluSeasonOverdue = fluSeasonOverdue;
  }

  public TimePeriod getFluSeasonEnd() {
    return fluSeasonEnd;
  }

  public void setFluSeasonEnd(TimePeriod fluSeasonEnd) {
    this.fluSeasonEnd = fluSeasonEnd;
  }

  public TimePeriod getFluSeasonFinished() {
    return fluSeasonFinished;
  }

  public void setFluSeasonFinished(TimePeriod fluSeasonFinished) {
    this.fluSeasonFinished = fluSeasonFinished;
  }

}
