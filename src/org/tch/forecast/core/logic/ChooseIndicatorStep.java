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
    ds.log("Choose Indicator Step");
    ds.nextAction = null;
    ds.indicatesPos++;
    if (ds.indicatesPos < ds.indicates.length)
    {
      ds.log("Looking for dose that matches next indicator");
      return LookForDoseStep.NAME;
    }
    ds.log("No more indicators to look at, going to finish schedule");
    return FinishScheduleStep.NAME;
  }

}
