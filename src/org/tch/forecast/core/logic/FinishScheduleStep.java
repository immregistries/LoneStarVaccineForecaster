package org.tch.forecast.core.logic;

public class FinishScheduleStep extends ActionStep
{
  public static final String NAME = "Finish Schedule";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception
  {
    if (ds.traceBuffer != null)
    {
      ds.traceBuffer.append("</li></ul>");
    }
    if (ds.traces != null)
    {
      ds.traceList.append("</li></ul>");
    }
    finishSeasonal(ds);
    ds.nextAction = null;
    // TODO Auto-generated method stub
    return ChooseStartIndicatorStep.NAME;
  }

  private void finishSeasonal(DataStore ds)
  {
    if (ds.seasonal != null)
    {
      ds.eventList = ds.originalEventList;
      ds.originalEventList = null;
      ds.seasonal = null;
      ds.seasonStart = null;
      ds.seasonEnd = null;
    }
  }

}