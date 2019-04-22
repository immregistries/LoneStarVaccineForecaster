package org.immregistries.lonestar.core;

import java.util.ArrayList;
import java.util.List;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.VaccineForecastDataBean.Schedule;

public class Trace
{
  private boolean complete = false;
  private boolean finished = false;
  private boolean contraindicated = false;
  private boolean invalid = false;
  private Schedule schedule = null;
  private String reason = "";
  private List<VaccinationDoseDataBean> doses = new ArrayList<VaccinationDoseDataBean>();
  private DateTime dueDate = null;
  private DateTime finishedDate = null;
  private DateTime overdueDate = null;
  private DateTime validDate = null;
  private String validReason = "";
  private String validBecause = "";

  public String getValidBecause()
  {
    return validBecause;
  }

  public void setValidBecause(String validBecause)
  {
    this.validBecause = validBecause;
  }

  public List<VaccinationDoseDataBean> getDoses()
  {
    return doses;
  }

  public void setDoses(List<VaccinationDoseDataBean> doses)
  {
    this.doses = doses;
  }

  public String getReason()
  {
    return reason;
  }

  public void setReason(String reason)
  {
    this.reason = reason;
  }

  public boolean isInvalid()
  {
    return invalid;
  }

  public void setInvalid(boolean invalid)
  {
    this.invalid = invalid;
  }

  public boolean isContraindicated()
  {
    return contraindicated;
  }

  public void setContraindicated(boolean contraindicated)
  {
    this.contraindicated = contraindicated;
  }

  public boolean isComplete()
  {
    return complete;
  }

  public void setComplete(boolean complete)
  {
    this.complete = complete;
  }

  public boolean isFinished()
  {
    return finished;
  }

  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }

  public Schedule getSchedule()
  {
    return schedule;
  }

  public void setSchedule(Schedule schedule)
  {
    this.schedule = schedule;
  }

  public DateTime getFinishedDate()
  {
    return finishedDate;
  }

  public void setFinishedDate(DateTime finishedDate)
  {
    this.finishedDate = finishedDate;
  }

  public DateTime getDueDate()
  {
    return dueDate;
  }

  public void setDueDate(DateTime dueDate)
  {
    this.dueDate = dueDate;
  }

  public DateTime getOverdueDate()
  {
    return overdueDate;
  }

  public void setOverdueDate(DateTime overdueDate)
  {
    this.overdueDate = overdueDate;
  }

  public DateTime getValidDate()
  {
    return validDate;
  }

  public void setValidDate(DateTime validDate)
  {
    this.validDate = validDate;
  }

  public String getValidReason()
  {
    return validReason;
  }

  public void setValidReason(String validReason)
  {
    this.validReason = validReason;
  }

}
