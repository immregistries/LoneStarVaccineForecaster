package org.tch.forecast.core.logic;

import java.util.Date;

import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.Transition;

public class TransitionEvent implements ImmunizationInterface {
  private Date date = null;
  private int vaccineId = 0;
  private String name = "";
  private boolean inSeason = false;

  public boolean isInSeason() {
    return inSeason;
  }

  public void setInSeason(boolean inSeason) {
    this.inSeason = inSeason;
  }


  public boolean isSubPotent() {
    return false;
  }

  public TransitionEvent(Date date, Transition transition) {
    this.date = date;
    this.vaccineId = transition.getVaccineId();
    this.name = transition.getName();
  }

  public Date getDateOfShot() {
    return date;
  }

  public int getVaccineId() {
    return vaccineId;
  }

  public String getCvx() {
    return "";
  }

  public String getLabel() {
    return name;
  }

  public String getMvx() {
    return "";
  }

  public String getVaccinationId() {
    return "";
  }
  
  @Override
  public boolean isAssumption() {
    return false;
  }

  @Override
  public boolean isForceValid() {
    return false;
  }


}
