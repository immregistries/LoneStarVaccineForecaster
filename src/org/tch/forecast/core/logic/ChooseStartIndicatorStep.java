package org.tch.forecast.core.logic;

public class ChooseStartIndicatorStep extends ActionStep
{

  public static final String NAME = "Choose Start Indicator";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore dataStore) throws Exception
  {
    dataStore.schedule = null;
    dataStore.scheduleListPos++;
    if (dataStore.scheduleListPos >= dataStore.scheduleList.size())
    {
      return FinishStep.NAME;
    }
    dataStore.schedule = dataStore.scheduleList.get(dataStore.scheduleListPos);
    return SetupScheduleStep.NAME;
  }

}
