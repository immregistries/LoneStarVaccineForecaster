package org.tch.forecast.core.decisionLogic;

public class DecisionLogicFactory
{
  public static final String DL_FLU_2014 = "DL FLU 2014";
  public static final String DL_FLU_2015 = "DL FLU 2015";
  public static final String DL_MENB_TWO_DOSE_A = "DL Trumenba A";
  public static final String DL_MENB_TWO_DOSE_B = "DL Trumenba B";
  public static final String DL_MENB_TWO_DOSE_C = "DL Trumenba C";
//  public static final String DL_MENB_MULTI_DOSE = "DL MenB MultiDose";
//  public static final String DL_HPV_2ND_DOSE_2016= "DL HPV 2nd";
  public static final String DL_HPV_2ND_DOSE_2017= "DL HPV 2-dose";
  public static final String DL_HPV_3RD_DOSE_2017= "DL HPV 3-dose";
  
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
    else if (name.equalsIgnoreCase(DL_MENB_TWO_DOSE_A))
    {
      return new MenB2017TwoDoseStateDecisionLogic();
    }
    else if (name.equalsIgnoreCase(DL_MENB_TWO_DOSE_B))
    {
      return new MenB2017TwoDoseStateDecisionLogic();
    }
    else if (name.equalsIgnoreCase(DL_MENB_TWO_DOSE_C))
    {
      return new MenB2017TwoDoseStateDecisionLogic();
    }
    // Preserve HPV 2016 version
//  else if (name.equalsIgnoreCase(DL_HPV_2ND_DOSE_2016))
//  {
//    return new Hpv2016SecondDoseDecisionLogic();
//  }
    else if (name.equalsIgnoreCase(DL_HPV_2ND_DOSE_2017))
    {
      return new Hpv2017SecondDoseDecisionLogic();
    }
    else if (name.equalsIgnoreCase(DL_HPV_3RD_DOSE_2017))
    {
      return new Hpv2017ThirdDoseDecisionLogic();
    }
    return dl;
  }
}
