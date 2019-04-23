package org.immregistries.lonestar.core.logic;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.VaccineForecastDataBean.ValidVaccine;

public class BlackOut
{
  private DateTime startBlackOut = null;
  private DateTime endBlackOut = null;
  private DateTime endBlackOutGrace = null;
  private DateTime eventDate = null;
  private String vaccineName = "";
  private ValidVaccine[] againstVaccineIds = null;
  private String againstContra = "";
  private String againstAllowed = "";
  private String reason = "";

  public String getAgainstContra() {
    return againstContra;
  }

  public void setAgainstContra(String againstContra) {
    this.againstContra = againstContra;
  }

  public String getAgainstAllowed() {
    return againstAllowed;
  }

  public void setAgainstAllowed(String againstAllowed) {
    this.againstAllowed = againstAllowed;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getVaccineName() {
    return vaccineName;
  }

  public void setVaccineName(String vaccineName) {
    this.vaccineName = vaccineName;
  }

  public ValidVaccine[] getAgainstVaccineIds() {
    return againstVaccineIds;
  }
  
  public boolean isAgainstSpecificVaccines()
  {
    return againstVaccineIds != null && againstVaccineIds.length > 0;
  }

  public void setAgainstVaccineIds(ValidVaccine[] vaccineIds) {
    this.againstVaccineIds = vaccineIds;
  }

  public DateTime getStartBlackOut() {
    return startBlackOut;
  }

  public void setStartBlackOut(DateTime startBlackOut) {
    this.startBlackOut = startBlackOut;
  }

  public DateTime getEndBlackOut() {
    return endBlackOut;
  }

  public void setEndBlackOut(DateTime endBlackOut) {
    this.endBlackOut = endBlackOut;
  }

  public DateTime getEndBlackOutGrace() {
    return endBlackOutGrace;
  }

  public void setEndBlackOutGrace(DateTime endBlackOutGrace) {
    this.endBlackOutGrace = endBlackOutGrace;
  }

  public DateTime getEventDate() {
    return eventDate;
  }

  public void setEventDate(DateTime eventDate) {
    this.eventDate = eventDate;
  }
}
