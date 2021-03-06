package org.immregistries.lonestar.core.model;

import java.util.Date;
import org.immregistries.lonestar.core.ImmunizationInterface;

public class Immunization implements ImmunizationInterface {
  public static final int ASSUME_DTAP_SERIES_COMPLETE = -110;
  public static final int ASSUME_HEPA_COMPLETE = -145;
  public static final int ASSUME_HEPB_COMPLETE = -137;
  public static final int ASSUME_MMR_COMPLETE = -158;
  public static final int ASSUME_VAR_COMPLETE = -178;

  private int vaccineId = 0;
  private Date dateOfShot = null;
  private String label = "";
  private String cvx = "";
  private String mvx = "";
  private String vaccinationId = "";
  private boolean assumption = false;
  private boolean forceValid = false;
  private boolean subPotent = false;
  private boolean inSeason = false;

  public boolean isInSeason() {
    return inSeason;
  }

  public void setInSeason(boolean inSeason) {
    this.inSeason = inSeason;
  }


  public boolean isSubPotent() {
    return subPotent;
  }

  public void setSubPotent(boolean subPotent) {
    this.subPotent = subPotent;
  }

  public boolean isForceValid() {
    return forceValid;
  }

  public void setForceValid(boolean forceValid) {
    this.forceValid = forceValid;
  }

  public boolean isAssumption() {
    return assumption;
  }

  public void setAssumption(boolean assumption) {
    this.assumption = assumption;
  }

  public String getVaccinationId() {
    return vaccinationId;
  }

  public void setVaccinationId(String vaccinationId) {
    this.vaccinationId = vaccinationId;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getCvx() {
    return cvx;
  }

  public void setCvx(String cvx) {
    this.cvx = cvx;
  }

  public String getMvx() {
    return mvx;
  }

  public void setMvx(String mvx) {
    this.mvx = mvx;
  }

  public Date getDateOfShot() {
    return dateOfShot;
  }

  public void setDateOfShot(Date dateOfShot) {
    this.dateOfShot = dateOfShot;
  }

  public int getVaccineId() {
    return vaccineId;
  }

  public void setVaccineId(int vaccineId) {
    this.vaccineId = vaccineId;
  }
}
