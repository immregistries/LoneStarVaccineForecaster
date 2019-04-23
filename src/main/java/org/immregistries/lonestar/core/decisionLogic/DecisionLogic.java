package org.immregistries.lonestar.core.decisionLogic;

import java.util.HashMap;
import java.util.Map;

import org.immregistries.lonestar.core.logic.DataStore;

public abstract class DecisionLogic
{
  private Map<String, String> constantMap = new HashMap<String, String>();
  private Map<String, String> transitionMap = new HashMap<String, String>();

  public Map<String, String> getConstantMap() {
    return constantMap;
  }
  
  protected String getConstantValue(String value)
  {
    return constantMap.get(value);
  }

  public Map<String, String> getTransitionMap() {
    return transitionMap;
  }
  
  protected String getTransitionValue(String value)
  {
    return transitionMap.get(value);
  }

  public abstract String getTransition(DataStore ds);

}
