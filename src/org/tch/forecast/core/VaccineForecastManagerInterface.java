package org.tch.forecast.core;

import java.util.List;

public interface VaccineForecastManagerInterface
{
  public List getIndications(String indication) throws Exception;
  
  public String getVaccineName(int id);

}
