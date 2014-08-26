package org.tch.forecast.core.decisionLogic;

public class DecisionLogicFactory
{
  public static final String DL_FLU_2014 = "DL FLU 2014";
  public static DecisionLogic getDecisionLogic(String name)
  {
    DecisionLogic dl = null;
    if (name.equalsIgnoreCase(DL_FLU_2014))
    {
      return new Flu2014DecisionLogic();
    }
    return dl;
  }
}
