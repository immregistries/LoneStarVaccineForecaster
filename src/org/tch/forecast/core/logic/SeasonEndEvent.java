package org.tch.forecast.core.logic;

import java.util.Date;

import org.tch.forecast.core.ImmunizationInterface;

public class SeasonEndEvent implements ImmunizationInterface
{
  private Date date = null;

  public SeasonEndEvent(Date date) {
    this.date = date;
  }

  public Date getDateOfShot()
  {
    return date;
  }

  public int getVaccineId()
  {
    return -504;
  }
  
  public String getCvx()
  {
    return "";
  }
  
  public String getLabel()
  {
    return "Season End";
  }
  
  public String getMvx()
  {
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
  
  @Override
  public boolean isSubPotent() {
    return false;
  }

}