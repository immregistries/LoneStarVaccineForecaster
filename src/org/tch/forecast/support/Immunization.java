package org.tch.forecast.support;

import java.util.Date;

import org.tch.forecast.core.ImmunizationInterface;

public class Immunization implements ImmunizationInterface
{
  private int vaccineId = 0;
  private Date dateOfShot = null;

  public Date getDateOfShot()
  {
    return dateOfShot;
  }

  public void setDateOfShot(Date dateOfShot)
  {
    this.dateOfShot = dateOfShot;
  }

  public int getVaccineId()
  {
    return vaccineId;
  }

  public void setVaccineId(int vaccineId)
  {
    this.vaccineId = vaccineId;
  }
}
