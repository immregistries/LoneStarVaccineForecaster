package org.immregistries.lonestar.core.logic;

public class FinishStep extends ActionStep
{

  public static final String NAME = "Finish";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore dataStore) throws Exception
  {
    dataStore.traces = null;
    return EndStep.NAME;
  }

}
