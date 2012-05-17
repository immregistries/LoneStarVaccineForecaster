package org.tch.forecast.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.ForecastSchedule;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.VaccineForecastManagerInterface;
import org.tch.forecast.validator.db.DatabasePool;

public class VaccineForecastManager implements VaccineForecastManagerInterface
{
  private static Map<String, List<Schedule>> indications = null;
  private static ForecastSchedule forecastSchedule = null;
  
  public Schedule getSchedule(String lineCode) throws Exception
  {
    init();
    for (List<Schedule> scheduleList : indications.values())
    {
      for (Schedule schedule : scheduleList)
      {
        if (schedule.getForecastCode().equals(lineCode))
        {
          return schedule;
        }
      }
    }
    return null;
  }
  
  public ForecastSchedule getForecastSchedule()
  {
    return forecastSchedule;
  }
  
  public List<Schedule> getIndications(String indication) throws Exception
  {
    init();
    return indications.get(indication);
  }
  
  public String getVaccineName(int id)
  {
    Vaccine vacc = null;
    try { 
      vacc = Vaccines.getByID(id);
    }
    catch (Exception e)
    {
      vacc = null;
    }
    if (vacc == null)
    {
      return "Unknown vaccine " + id; 
    }
    else 
    {
      return vacc.getDisplayName();
    }
  }
  
  private static void init() throws Exception
  {
    if (indications == null)
    {
      forecastSchedule = new ForecastSchedule("ForecastSchedule.xml");
      getVaccineForecasts();
      indications = VaccineForecastDataBean.getIndications();
    }
  }

  private static void getVaccineForecasts() throws Exception
  {
    Connection conn = DatabasePool.getConnection();
    try
    {
       VaccineForecastAccessor.getVaccineForecasts(forecastSchedule, conn);
    }
    finally
    {
      DatabasePool.close(conn);
    }

  }
}
