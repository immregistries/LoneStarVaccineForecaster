package org.tch.forecast.core.model;

public class ImmunizationMDA extends Immunization {
  private String doseNote = "";
  private String hl7CodeErrorCode = "";
  private String doseOverride = "";

  public String getDoseOverride() {
    return doseOverride;
  }

  public void setDoseOverride(String doseOverride) {
    this.doseOverride = doseOverride;
  }

  public String getHl7CodeErrorCode() {
    return hl7CodeErrorCode;
  }

  public void setHl7CodeErrorCode(String hl7CodeErrorCode) {
    this.hl7CodeErrorCode = hl7CodeErrorCode;
  }

  public String getDoseNote() {
    return doseNote;
  }

  public void setDoseNote(String doseNote) {
    this.doseNote = doseNote;
  }
  
}
