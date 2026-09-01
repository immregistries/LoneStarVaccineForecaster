package org.immregistries.lonestar.core.logic;

public class StartStep extends ActionStep {
  public static final String NAME = "Start";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore dataStore) throws Exception {
    return SetupStep.NAME;
  }

}
