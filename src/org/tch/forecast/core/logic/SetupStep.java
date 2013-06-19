package org.tch.forecast.core.logic;

import java.util.ArrayList;
import java.util.List;

import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.DateTime;

public class SetupStep extends ActionStep
{
  public static final String NAME = "Setup";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore dataStore) throws Exception
  {
    dataStore.today = new DateTime(dataStore.forecastDate);
    dataStore.scheduleList = new ArrayList<VaccineForecastDataBean.Schedule>();
    dataStore.scheduleListPos = -1;

    setupAll("BIRTH", dataStore);
    if (dataStore.patient.getSex() == null || !dataStore.patient.getSex().equals("M"))
    {
      setupAll("FEMALE", dataStore);
    }
    if (dataStore.patient.getSex() == null || dataStore.patient.getSex().equals("M"))
    {
      setupAll("MALE", dataStore);
    }
    if (!dataStore.hasHistoryOfVaricella)
    {
      setupAll("NO-VAR-HIS", dataStore);
    }
    return ChooseStartIndicatorStep.NAME;
  }

  private void setupAll(String indication, DataStore ds) throws Exception
  {
    List<Schedule> vaccineForecastList = ds.forecastManager.getIndications(indication);
    if (vaccineForecastList == null)
    {
      ds.log("No schedules found for indication '" + indication + "'");
    } else
    {
      ds.log("Found schedule for indicatoin '" + indication + "'");
      if (ds.forecastCode != null)
      {
        ds.log("Forecast code = '" + ds.forecastCode + "'");
        for (Schedule schedule : vaccineForecastList)
        {
          if (schedule.getForecastCode().equals(ds.forecastCode))
          {
            ds.log("Adding schedule '" + schedule.getForecastCode() + "' to schedule list");
            ds.scheduleList.add(schedule);
          }
        }
      } else
      {
        ds.log("Adding all to schedule list");
        ds.scheduleList.addAll(vaccineForecastList);
      }
    }
  }

}
