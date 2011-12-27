package org.tch.forecast.core.logic;

public abstract class ActionStep
{
  public abstract String getName();

  public abstract String doAction(DataStore dataStore) throws Exception;
}
