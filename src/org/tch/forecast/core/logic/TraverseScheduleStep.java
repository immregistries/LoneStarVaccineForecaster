package org.tch.forecast.core.logic;

public class TraverseScheduleStep extends ActionStep
{
  public static final String NAME = "Traverse Schedule";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception
  {
    ds.nextAction = null;
    if (ds.schedule != null)
    {
      return DetermineRangesStep.NAME;
    }
    return ChooseStartIndicatorStep.NAME;
  }

}
