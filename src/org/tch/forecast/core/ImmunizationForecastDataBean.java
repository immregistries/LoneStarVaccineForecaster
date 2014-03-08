package org.tch.forecast.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.model.Assumption;

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
  public static final String PCV13 = "PCV13";
  public static final String HEPA = "HepA";
  public static final String HPV = "HPV";
  public static final String MENING = "Mening";
  public static final String MCV4 = "MCV4";
  public static final String MEASLES = "Measles";
  public static final String MUMPS = "Mumps";
  public static final String RUBELLA = "Rubella";
  public static final String VARICELLA = "Varicella";
  public static final String PERTUSSIS = "Pertussis";
  public static final String INFLUENZA = "Influenza";
  public static final String INFLUENZA_IIV = "Influenza IIV";
  public static final String INFLUENZA_LAIV = "Influenza LAIV";
  public static final String PPSV = "PPSV";
  public static final String ZOSTER = "Zoster";
  public static final String MMR = "MMR";
  public static final String DTAP = "DTaP";
  public static final String TDAP = "Tdap";
  public static final String TD = "Td";
  public static final String DT = "DT";

  public static final String STATUS_DESCRIPTION_DUE_LATER = "due later";
  public static final String STATUS_DESCRIPTION_DUE = "due";
  public static final String STATUS_DESCRIPTION_OVERDUE = "overdue";
  public static final String STATUS_DESCRIPTION_FINISHED = "finished";
  public static final String STATUS_DESCRIPTION_COMPLETE = "complete";
  public static final String STATUS_DESCRIPTION_CONTRAINDICATED = "contraindicated";
  public static final String STATUS_DESCRIPTION_COMPLETE_FOR_SEASON = "complete for season";
  public static final String STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE = "assumed complete or immune";

  public static final String SCHEDULE_COMPLETED = "COMP";

  private String forecastNameOriginal = "";
  private String forecastName = "";
  private String forecastLabel = "";
  private int immregid = 0;
  private Date valid = null;
  private Date due = null;
  private Date overdue = null;
  private Date finished = null;
  private String dose = "";
  private String schedule = "";
  private int sortOrder = 0;
  private TraceList traceList = null;
  private String comment = "";
  private String statusDescription = "";
  private Date seasonStart = null;
  private Date seasonEnd = null;
  private List<Assumption> assumptionList = new ArrayList<Assumption>();

  public List<Assumption> getAssumptionList() {
    return assumptionList;
  }

  public boolean hasAssumptions() {
    return assumptionList.size() > 0;
  }

  public Date getSeasonStart() {
    return seasonStart;
  }

  public void setSeasonStart(Date seasonStart) {
    this.seasonStart = seasonStart;
  }

  public Date getSeasonEnd() {
    return seasonEnd;
  }

  public void setSeasonEnd(Date seasonEnd) {
    this.seasonEnd = seasonEnd;
  }

  public String getForecastNameOriginal() {
    return forecastNameOriginal;
  }

  public void setForecastNameOriginal(String forecastNameOrginal) {
    this.forecastNameOriginal = forecastNameOrginal;
  }

  public String getStatusDescription() {
    return statusDescription;
  }

  public void setStatusDescription(String dueDescription) {
    this.statusDescription = dueDescription;
  }

  public TraceList getTraceList() {
    return traceList;
  }

  public void setTraceList(TraceList traceList) {
    this.traceList = traceList;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return " {" + immregid + "," + forecastName + "," + new DateTime(due == null ? 0 : due.getTime()).toString("M/D/Y")
        + "}";
  }

  public Date getDateDue() {
    return this.due;
  }

  public void setDateDue(Date dateDue) {
    this.due = dateDue;
  }

  public int getImmregid() {
    return this.immregid;
  }

  public void setImmregid(int immregid) {
    this.immregid = immregid;
  }

  public String getForecastName() {
    return this.forecastName;
  }

  public void setForecastName(String forecastName) {
    if (this.forecastNameOriginal.equals("")) {
      this.forecastNameOriginal = forecastName;
    }
    this.forecastName = forecastName;
  }

  public Date getValid() {
    return valid;
  }

  public void setValid(Date valid) {
    this.valid = valid;
  }

  public Date getDue() {
    return due;
  }

  public void setDue(Date due) {
    this.due = due;
  }

  public Date getOverdue() {
    return overdue;
  }

  public void setOverdue(Date overdue) {
    this.overdue = overdue;
  }

  public Date getFinished() {
    return finished;
  }

  public void setFinished(Date finished) {
    this.finished = finished;
  }

  public String getDose() {
    return dose;
  }

  public void setDose(String dose) {
    this.dose = dose;
  }

  public String getSchedule() {
    return schedule;
  }

  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }

  public String getForecastLabel() {
    return forecastLabel;
  }

  public void setForecastLabel(String forecastLabel) {
    this.forecastLabel = forecastLabel;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

}
