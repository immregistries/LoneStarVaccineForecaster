package org.tch.forecast.core.api.impl;

import org.tch.forecast.core.TimePeriod;

public class ForecastOptions {
  private TimePeriod fluSeasonStart = null;
  private TimePeriod fluSeasonDue = null;
  private TimePeriod fluSeasonOverdue = null;
  private TimePeriod fluSeasonEnd = null;
  
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

}
