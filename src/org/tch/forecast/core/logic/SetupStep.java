package org.tch.forecast.core.logic;

import java.util.ArrayList;
import java.util.List;

import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.hl7.core.util.DateTime;

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
      System.out.println("No schedules found for indication '" + indication + "'");
    } else
    {
      if (ds.forecastCode != null)
      {
        for (Schedule schedule : vaccineForecastList)
        {
          if (schedule.getForecastCode().equals(ds.forecastCode))
          {
            ds.scheduleList.add(schedule);
          }
        }
      } else
      {
        ds.scheduleList.addAll(vaccineForecastList);
      }
    }
  }

}
