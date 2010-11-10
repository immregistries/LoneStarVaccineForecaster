package org.tch.forecast.support;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastManagerInterface;
import org.tch.forecast.validator.DataSourceException;
import org.tch.forecast.validator.DataSourceUnavailableException;
import org.tch.forecast.validator.db.DatabasePool;

public class VaccineForecastManager implements VaccineForecastManagerInterface
{
  private static Map indications = null;
  
  public List getIndications(String indication) throws DataSourceException, DataSourceUnavailableException
  {
    init();
    return (List) indications.get(indication);
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
  
  private static void init() throws DataSourceException, DataSourceUnavailableException
  {
    if (indications == null)
    {
      getVaccineForecasts();
      indications = VaccineForecastDataBean.getIndications();
    }
  }

  private static List getVaccineForecasts() throws DataSourceException, DataSourceUnavailableException
  {
    Connection conn = DatabasePool.getConnection();
    try
    {
      return VaccineForecastAccessor.getVaccineForecasts(conn);
    }
    finally
    {
      DatabasePool.close(conn);
    }

  }
}
