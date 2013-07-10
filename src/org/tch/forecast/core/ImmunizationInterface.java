package org.tch.forecast.core;

import java.util.Date;

public interface ImmunizationInterface
{
  public Date getDateOfShot();

  public int getVaccineId();

  public String getLabel();

  public String getCvx();

  public String getMvx();
  
  public String getVaccinationId();
  
}
