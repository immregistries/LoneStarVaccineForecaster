package org.tch.forecast.core.decisionLogic;

public class DecisionLogicFactory
{
  public static final String DL_FLU_2014 = "DL FLU 2014";
  public static final String DL_FLU_2015 = "DL FLU 2015";
  public static final String DL_MENB_TWO_DOSE = "DL MenB TwoDose";
  public static final String DL_MENB_MULTI_DOSE = "DL MenB MultiDose";
  public static final String DL_HPV_2ND_DOSE= "DL HPV 2nd";
  
  public static DecisionLogic getDecisionLogic(String name)
  {
    DecisionLogic dl = null;
    if (name.equalsIgnoreCase(DL_FLU_2014))
    {
      return new Flu2014DecisionLogic();
    }
    else if (name.equalsIgnoreCase(DL_FLU_2015))
    {
      return new Flu2015DecisionLogic();
    }
    else if (name.equalsIgnoreCase(DL_MENB_TWO_DOSE))
    {
      return new MenBTwoDoseStateDecisionLogic();
    }
    else if (name.equalsIgnoreCase(DL_MENB_MULTI_DOSE))
    {
      return new MenBMultiDoseStateDecisionLogic();
    }
    else if (name.equalsIgnoreCase(DL_HPV_2ND_DOSE))
    {
      return new HpvSecondDoseDecisionLogic();
    }
    return dl;
  }
}
