package org.tch.forecast.core.server;

import java.util.List;
import java.util.Map;

import org.tch.forecast.core.ForecastSchedule;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastManagerInterface;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.model.Vaccines;

public class VaccineForecastManagerWrong implements VaccineForecastManagerInterface {
  
  private ForecastSchedule forecastSchedule;
  private Map<String, List<Schedule>> indications = null;
  private Vaccines vaccines;
  
  public Vaccines getVaccines()
  {
    return vaccines;
  }

  public VaccineForecastManagerWrong() throws Exception
  {
    forecastSchedule = new ForecastSchedule();
    forecastSchedule.initFromResource("ForecastSchedule.xml");
    indications = VaccineForecastDataBean.getIndications();
    vaccines = new Vaccines();
  }
  
  public ForecastSchedule getForecastSchedule() {
    return forecastSchedule;
  }
  
  public String getVaccineName(int id) {
    // TODO, need lookup tables for vaccine name
    return "Vaccine " + id;
  }
  
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
  
  public List<Schedule> getIndications(String indication) throws Exception {
    return indications.get(indication);
  }

}
