package org.immregistries.lonestar.core.logic;

import java.util.HashMap;
import java.util.Map;

public class ActionStepFactory
{
  private static Map<String, ActionStep> actionStepMap;
  
  public static ActionStep get(String name)
  {
    if (actionStepMap == null)
    {
      actionStepMap = new HashMap<String, ActionStep>();
      addToMap(new StartStep());
      addToMap(new SetupStep());
      addToMap(new ChooseStartIndicatorStep());
      addToMap(new SetupScheduleStep());
      addToMap(new TraverseScheduleStep());
      addToMap(new DetermineRangesStep());
      addToMap(new ChooseIndicatorStep());
      addToMap(new LookForDoseStep());
      addToMap(new TransitionScheduleStep());
      addToMap(new MakeForecastStep());
      addToMap(new FinishScheduleStep());
      addToMap(new FinishStep());
      addToMap(new EndStep());
    }
    return actionStepMap.get(name);
  }
  
  private static void addToMap(ActionStep actionStep)
  {
    actionStepMap.put(actionStep.getName(), actionStep);
  }
}
