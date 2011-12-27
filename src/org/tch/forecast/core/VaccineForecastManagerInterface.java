package org.tch.forecast.core;

import java.util.List;

import org.tch.forecast.core.VaccineForecastDataBean.Schedule;

public interface VaccineForecastManagerInterface
{
  public List<Schedule> getIndications(String indication) throws Exception;
  
  public String getVaccineName(int id);

}
