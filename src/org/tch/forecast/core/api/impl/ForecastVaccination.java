package org.tch.forecast.core.api.impl;

import java.util.Date;

import org.tch.forecast.core.api.model.ForecastVaccinationInterface;

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
  
}
