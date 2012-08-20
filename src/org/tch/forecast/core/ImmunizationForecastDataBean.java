package org.tch.forecast.core;

import java.util.Date;

import org.tch.forecast.core.DateTime;

/**
 * @author Nathan Bunker
 */
public class ImmunizationForecastDataBean
{

  public static final String HIB = "Hib";
  public static final String POLIO = "Polio";
  public static final String HEPB = "HepB";
  public static final String DIPHTHERIA = "Diphtheria";
  public static final String ROTAVIRUS = "Rotavirus";
  public static final String PNEUMO = "Pneumo";
  public static final String HEPA = "HepA";
  public static final String HPV = "HPV";
  public static final String MENING = "Mening";
  public static final String MEASLES = "Measles";
  public static final String MUMPS = "Mumps";
  public static final String RUBELLA = "Rubella";
  public static final String VARICELLA = "Varicella";
  public static final String PERTUSSIS = "Pertussis";
  public static final String INFLUENZA = "Influenza";

  public static final String SCHEDULE_COMPLETED = "COMP";

  private String forecastName = "";
  private String forecastLabel = "";
  private int immregid = 0;
  private Date valid = null;
  private Date early = null;
  private Date due = null;
  private Date overdue = null;
  private Date finished = null;
  private String dose = "";
  private String schedule = "";
  private int sortOrder = 0;
  private TraceList traceList = null;
  private String comment = "";
  public TraceList getTraceList()
  {
    return traceList;
  }

  public void setTraceList(TraceList traceList)
  {
    this.traceList = traceList;
  }

  public String getComment()
  {
    return comment;
  }

  public void setComment(String comment)
  {
    this.comment = comment;
  }
 /**
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    return " {" + immregid + "," + forecastName + "," + new DateTime(due == null ? 0 : due.getTime()).toString("M/D/Y") + "}";
  }

  public Date getDateDue()
  {
    return this.due;
  }

  public void setDateDue(Date dateDue)
  {
    this.due = dateDue;
  }

  public int getImmregid()
  {
    return this.immregid;
  }

  public void setImmregid(int immregid)
  {
    this.immregid = immregid;
  }

  public String getForecastName()
  {
    return this.forecastName;
  }

  public void setForecastName(String forecastName)
  {
    this.forecastName = forecastName;
  }

  public Date getValid()
  {
    return valid;
  }

  public void setValid(Date valid)
  {
    this.valid = valid;
  }

  public Date getDue()
  {
    return due;
  }

  public void setDue(Date due)
  {
    this.due = due;
  }

  public Date getOverdue()
  {
    return overdue;
  }

  public void setOverdue(Date overdue)
  {
    this.overdue = overdue;
  }

  public Date getFinished()
  {
    return finished;
  }

  public void setFinished(Date finished)
  {
    this.finished = finished;
  }

  public String getDose()
  {
    return dose;
  }

  public void setDose(String dose)
  {
    this.dose = dose;
  }

  public String getSchedule()
  {
    return schedule;
  }

  public void setSchedule(String schedule)
  {
    this.schedule = schedule;
  }

  public Date getEarly()
  {
    return early;
  }

  public void setEarly(Date early)
  {
    this.early = early;
  }

  public String getForecastLabel()
  {
    return forecastLabel;
  }

  public void setForecastLabel(String forecastLabel)
  {
    this.forecastLabel = forecastLabel;
  }

  public int getSortOrder()
  {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder)
  {
    this.sortOrder = sortOrder;
  }


}
