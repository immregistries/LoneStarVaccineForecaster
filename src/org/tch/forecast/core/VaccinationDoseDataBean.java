package org.tch.forecast.core;

import java.util.Date;

public class VaccinationDoseDataBean
{

  public static final String STATUS_VALID = "V";
  public static final String STATUS_INVALID = "I";
  public static final String STATUS_MISSED = "M";
  
  private int vaccineId = 0;
  private String cvxCode = "";
  private String mvxCode = "";
  private int immregid = 0;
  private Date adminDate = null;
  private String forecastCode = "";
  private String scheduleCode = "";
  private String doseCode = "";
  private String statusCode = "";
  private String reason = "";
  private String vaccinationId = "";
  
  public String getVaccinationId() {
    return vaccinationId;
  }
  public void setVaccinationId(String vaccinationId) {
    this.vaccinationId = vaccinationId;
  }
  public String getCvxCode() {
    return cvxCode;
  }
  public void setCvxCode(String cvxCode) {
    this.cvxCode = cvxCode;
  }
  public String getMvxCode() {
    return mvxCode;
  }
  public void setMvxCode(String mvxCode) {
    this.mvxCode = mvxCode;
  }
  public String getReason()
  {
    return reason;
  }
  public void setReason(String reason)
  {
    this.reason = reason;
  }
  public int getVaccineId()
  {
    return vaccineId;
  }
  public void setVaccineId(int vaccineId)
  {
    this.vaccineId = vaccineId;
  }
  public int getImmregid()
  {
    return immregid;
  }
  public void setImmregid(int immregid)
  {
    this.immregid = immregid;
  }
  public Date getAdminDate()
  {
    return adminDate;
  }
  public void setAdminDate(Date adminDate)
  {
    this.adminDate = adminDate;
  }
  public String getScheduleCode()
  {
    return scheduleCode;
  }
  public void setScheduleCode(String scheduleCode)
  {
    this.scheduleCode = scheduleCode;
  }
  public String getDoseCode()
  {
    return doseCode;
  }
  public void setDoseCode(String doseCode)
  {
    this.doseCode = doseCode;
  }
  public String getStatusCode()
  {
    return statusCode;
  }
  public void setStatusCode(String statusCode)
  {
    this.statusCode = statusCode;
  }
  public String getForecastCode()
  {
    return forecastCode;
  }
  public void setForecastCode(String forecastCode)
  {
    this.forecastCode = forecastCode;
  }
}
