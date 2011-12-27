package org.tch.forecast.core.logic;

import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.hl7.core.util.DateTime;

public class MakeForecastStep extends ActionStep
{
  public static final String NAME = "Make Forecast";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception
  {
    addForecastRecommendations(ds);
    return FinishScheduleStep.NAME;
  }

  private void addForecastRecommendations(DataStore ds)
  {
    if (ds.seasonStart != null && ds.seasonCompleted)
    {
      ds.seasonStart = new DateTime(ds.seasonStart);
      ds.seasonStart.addYears(1);
      ds.seasonEnd = new DateTime(ds.seasonEnd);
      ds.seasonEnd.addYears(1);
      DetermineRangesStep.determineRanges(ds);
    }
    ImmunizationForecastDataBean forecastBean = new ImmunizationForecastDataBean();
    forecastBean.setValid(ds.valid.getDate());
    forecastBean.setEarly(ds.early.getDate());
    forecastBean.setDue(ds.due.getDate());
    forecastBean.setOverdue(ds.overdue.getDate());
    forecastBean.setFinished(ds.finished.getDate());
    forecastBean.setDateDue(ds.due.getDate());
    forecastBean.setForecastName(ds.forecast.getForecastCode());
    forecastBean.setForecastLabel(ds.forecast.getForecastLabel());
    forecastBean.setSortOrder(ds.forecast.getSortOrder());
    forecastBean.setDose(getValidDose(ds, ds.schedule));
    forecastBean.setSchedule(ds.schedule.getScheduleName());
    forecastBean.setImmregid(ds.patient.getImmregid());
    forecastBean.setTraceList(ds.traceList);
    ds.resultList.add(forecastBean);
    if (ds.traceBuffer != null)
    {
      ds.traceBuffer.append("</li><li>Forecasting for dose " + DetermineRangesStep.getNextValidDose(ds, ds.schedule) + " due at " + ds.dueReason + ", "
          + ds.due.toString("M/D/Y") + ".");
    }
    if (ds.trace != null)
    {
      ds.traceList.append("</li><li>Forecasting for dose " + DetermineRangesStep.getNextValidDose(ds, ds.schedule) + " due at " + ds.dueReason + ", "
          + ds.due.toString("M/D/Y") + ".");
    }
  }
  
  private String getValidDose(DataStore ds, VaccineForecastDataBean.Schedule schedule)
  {
    String dose = schedule.getDose();
    if (dose.equals("*"))
    {
      dose = Integer.toString(ds.validDoseCount);
    }
    return dose;
  }


}
