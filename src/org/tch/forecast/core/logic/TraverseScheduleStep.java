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
    ds.log("Traverse Schedule");
    ds.log("Setting next action to null");
    ds.nextAction = null;
    if (ds.schedule != null)
    {
      ds.log("Schedule has been set so starting there");
      return DetermineRangesStep.NAME;
    }
    ds.log("Schedule has NOT been set, going back to the start");
    return ChooseStartIndicatorStep.NAME;
  }

}
