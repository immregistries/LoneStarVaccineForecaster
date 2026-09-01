package org.immregistries.lonestar.core.logic;

public class TraverseScheduleStep extends ActionStep {
  public static final String NAME = "Traverse Schedule";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception {
    ds.log("Traverse Schedule");
    ds.nextAction = null;
    if (ds.schedule != null) {
      ds.log("Setting next action to: " + DetermineRangesStep.NAME);
      ds.log("Schedule has been set so starting there");
      return DetermineRangesStep.NAME;
    }
    ds.log("Setting next action to: " + ChooseStartIndicatorStep.NAME);
    ds.log("Schedule has NOT been set, going back to the start");
    return ChooseStartIndicatorStep.NAME;
  }

}
