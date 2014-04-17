package org.tch.forecast.core;


public class Seasonal
{
  private TimePeriod start = null;
  private TimePeriod due = null;
  private TimePeriod overdue = null;
  private TimePeriod end = null;
  private TimePeriod finished = null;

  public Seasonal() {
    // default;
  }

  public Seasonal(Seasonal copy) {
    start = copy.getStart();
    due = copy.getDue();
    overdue = copy.getOverdue();
    end = copy.getEnd();
  }

  public TimePeriod getStart() {
    return start;
  }

  public void setStart(TimePeriod start) {
    this.start = start;
  }

  public TimePeriod getDue() {
    return due;
  }

  public void setDue(TimePeriod due) {
    this.due = due;
  }

  public TimePeriod getOverdue() {
    return overdue;
  }

  public void setOverdue(TimePeriod overdue) {
    this.overdue = overdue;
  }

  public TimePeriod getEnd() {
    return end;
  }

  public void setEnd(TimePeriod end) {
    this.end = end;
  }
  
  public TimePeriod getFinished() {
    return finished;
  }

  public void setFinished(TimePeriod finished) {
    this.finished = finished;
  }



}