package org.tch.forecast.core.logic;


public class ChooseIndicatorStep extends ActionStep
{
  public static final String NAME = "Choose Indicator";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception
  {
    ds.nextAction = null;
    ds.indicatesPos++;
    if (ds.indicatesPos < ds.indicates.length)
    {
      return LookForDoseStep.NAME;
    }
    return FinishScheduleStep.NAME;
  }

}
