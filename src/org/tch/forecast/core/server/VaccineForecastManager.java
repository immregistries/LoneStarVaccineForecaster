package org.tch.forecast.core.server;

import java.util.List;
import java.util.Map;

import org.tch.forecast.core.ForecastSchedule;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastManagerInterface;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.model.Vaccines;

public class VaccineForecastManager implements VaccineForecastManagerInterface {
  
  private ForecastSchedule forecastSchedule;
  private Map<String, List<Schedule>> indications = null;
  private Vaccines vaccines;
  
  public Vaccines getVaccines()
  {
    return vaccines;
  }

  public VaccineForecastManager() throws Exception
  {
    forecastSchedule = new ForecastSchedule();
    forecastSchedule.initFromResource("ForecastSchedule.xml");
    indications = VaccineForecastDataBean.getIndications();
    vaccines = new Vaccines();
  }
  
  @Override
  public ForecastSchedule getForecastSchedule() {
    return forecastSchedule;
  }
  
  @Override
  public String getVaccineName(int id) {
    // TODO, need lookup tables for vaccine name
    return "Vaccine " + id;
  }
  
  @Override
  public Schedule getSchedule(String lineCode) throws Exception {
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
  
  @Override
  public List<Schedule> getIndications(String indication) throws Exception {
    return indications.get(indication);
  }

}
