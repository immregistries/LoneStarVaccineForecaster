package org.tch.forecast.core.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CvxCode
{
  public static enum UseStatus {
    SUPPORTED, NOT_SUPPORTED, PENDING
  };

  private String cvxCode = "";
  private String cvxLabel = "";
  private String vaccineLabel = "";
  private int vaccineId = 0;
  private UseStatus useStatus = null;
  private boolean locationSet = false;
  
  public boolean isLocationSet() {
    return locationSet;
  }

  public void setLocationSet(boolean locationSet) {
    this.locationSet = locationSet;
  }

  private Map<String, Set<String>> locationMapSet = null;

  public Map<String, Set<String>> getLocationMapSet() {
    return locationMapSet;
  }

  public void setLocationMapSet(Map<String, Set<String>> locationMapSet) {
    this.locationMapSet = locationMapSet;
  }

  public String getVaccineLabel() {
    return vaccineLabel;
  }

  public void setVaccineLabel(String vaccineLabel) {
    this.vaccineLabel = vaccineLabel;
  }

  public String getCvxLabel() {
    return cvxLabel;
  }

  public void setCvxLabel(String cvxLabel) {
    this.cvxLabel = cvxLabel;
  }

  public String getCvxCode() {
    return cvxCode;
  }

  public void setCvxCode(String cvxCode) {
    this.cvxCode = cvxCode;
  }

  public int getVaccineId() {
    return vaccineId;
  }

  public void setVaccineId(int vaccineId) {
    this.vaccineId = vaccineId;
  }

  public UseStatus getUseStatus() {
    return useStatus;
  }

  public void setUseStatus(UseStatus useStatus) {
    this.useStatus = useStatus;
  }

}
