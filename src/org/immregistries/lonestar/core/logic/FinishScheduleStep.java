package org.immregistries.lonestar.core.logic;


public class FinishScheduleStep extends ActionStep
{
  public static final String NAME = "Finish Schedule";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception {
    finishSeasonalAndTransitions(ds);
    ds.nextAction = null;
    // TODO Auto-generated method stub
    return ChooseStartIndicatorStep.NAME;
  }

  private void finishSeasonalAndTransitions(DataStore ds) {
    if (ds.seasonal != null) {
      // TODO?
    }
    ds.seasonal = null;
    if (ds.originalEventList != null) {
      ds.eventList = ds.originalEventList;
      ds.originalEventList = null;
      ds.seasonal = null;
      ds.seasonStartDateTime = null;
      ds.seasonEndDateTime = null;
    }
  }

}
