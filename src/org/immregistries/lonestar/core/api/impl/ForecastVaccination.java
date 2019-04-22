package org.immregistries.lonestar.core.api.impl;

import java.util.Date;

import org.immregistries.lonestar.core.api.model.ForecastVaccinationInterface;

public class ForecastVaccination implements ForecastVaccinationInterface {
  
  private Date adminDate = null;
  private String cvxCode = "";
  private String mvxCode = "";
  
  public Date getAdminDate() {
    return adminDate;
  }
  public void setAdminDate(Date adminDate) {
    this.adminDate = adminDate;
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
  
  private String internalCode = "";
  private String forecastCode = "";
  private String scheduleCode = "";
  private String doseCode = "";
  private String statusCode = "";
  private String reasonText = "";
  private String whenValidText = "";
  private String vaccinationId = "";

  public String getWhenValidText() {
    return whenValidText;
  }
  public void setWhenValidText(String whenValidText) {
    this.whenValidText = whenValidText;
  }

  public String getVaccinationId() {
    return vaccinationId;
  }
  public void setVaccinationId(String vaccinationId) {
    this.vaccinationId = vaccinationId;
  }
  public String getInternalCode() {
    return internalCode;
  }
  public void setInternalCode(String tchCode) {
    this.internalCode = tchCode;
  }
  public String getForecastCode() {
    return forecastCode;
  }
  public void setForecastCode(String forecastCode) {
    this.forecastCode = forecastCode;
  }
  public String getScheduleCode() {
    return scheduleCode;
  }
  public void setScheduleCode(String scheduleCode) {
    this.scheduleCode = scheduleCode;
  }
  public String getDoseCode() {
    return doseCode;
  }
  public void setDoseCode(String doseCode) {
    this.doseCode = doseCode;
  }
  public String getStatusCode() {
    return statusCode;
  }
  public void setStatusCode(String statusCode) {
    this.statusCode = statusCode;
  }
  public String getReasonText() {
    return reasonText;
  }
  public void setReasonText(String reasonText) {
    this.reasonText = reasonText;
  }
  
}
