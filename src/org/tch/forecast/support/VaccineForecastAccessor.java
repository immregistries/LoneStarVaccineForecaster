package org.tch.forecast.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.validator.DataSourceException;

public class VaccineForecastAccessor
{
  private static final String GET_VACCINE_FORECASTS = "SELECT forecast_code, forecast_label, sort_order \n"
      + "FROM forecast_antigen ";

  public static List getVaccineForecasts(Connection conn) throws DataSourceException
  {
    List list = new ArrayList();
    PreparedStatement pstmt = null;
    ResultSet rset = null;
    String sql = GET_VACCINE_FORECASTS;
    try
    {
      pstmt = conn.prepareStatement(sql);
      rset = pstmt.executeQuery();
      while (rset.next())
      {
        String forecastCode = rset.getString(1);
        String forecastLabel = rset.getString(2);
        VaccineForecastDataBean vaccineForecast = new VaccineForecastDataBean(forecastCode + ".xml");
        vaccineForecast.setForcastCode(forecastCode);
        vaccineForecast.setForecastLabel(forecastLabel);
        vaccineForecast.setSortOrder(rset.getInt(3));
        list.add(vaccineForecast);
      }

    }
    catch (SQLException sqle)
    {
      throw new DataSourceException("Unable to get vaccine forecast schedules", sqle, sql);
    }
    catch (Exception e)
    {
      throw new DataSourceException("Unable to create vaccine forecast schedule", e);
    }
    return list;
  }

}
