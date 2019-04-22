package org.immregistries.lonestar.core.logic;

import java.util.Date;

import org.immregistries.lonestar.core.ImmunizationInterface;

public class SeasonStartEvent implements ImmunizationInterface
{
  private Date date = null;

  public SeasonStartEvent(Date date) {
    this.date = date;
  }

  public Date getDateOfShot()
  {
    return date;
  }

  public int getVaccineId()
  {
    return -503;
  }
  
  public String getCvx()
  {
    return "";
  }
  
  public String getLabel()
  {
    return "Flu Season Start";
  }
  
  public String getMvx()
  {
    return "";
  }
  
  public String getVaccinationId() {
    return "";
  }
  
  // JDK 1.5 Override is not applicable to interfaces
  //@Override
  public boolean isAssumption() {
    return false;
  }
  
  // JDK 1.5 Override is not applicable to interfaces
  //@Override
  public boolean isForceValid() {
    return false;
  }
  
  // JDK 1.5 Override is not applicable to interfaces
  //@Override
  public boolean isSubPotent() {
    return false;
  }

}