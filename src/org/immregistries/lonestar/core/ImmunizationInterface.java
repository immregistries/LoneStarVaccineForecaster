package org.immregistries.lonestar.core;

import java.util.Date;

public interface ImmunizationInterface
{
  public Date getDateOfShot();

  public int getVaccineId();

  public String getLabel();

  public String getCvx();

  public String getMvx();

  public String getVaccinationId();

  public boolean isAssumption();
  
  public boolean isForceValid();
  
  public boolean isSubPotent();
  
}
