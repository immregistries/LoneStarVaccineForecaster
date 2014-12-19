package org.tch.forecast.core.logic;

import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.api.impl.ForecastAntigen;

public abstract class ActionStep
{
  public abstract String getName();

  public abstract String doAction(DataStore dataStore) throws Exception;

  public void addResultToList(DataStore ds, ImmunizationForecastDataBean forecastBean) {
    ds.resultList.add(forecastBean);
    if (forecastBean.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)
        || forecastBean.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)) {
      if (ds.schedule.getCompletesList().size() > 1) {
        for (int i = 1; i < ds.schedule.getCompletesList().size(); i++) {
          ForecastAntigen fa = ds.schedule.getCompletesList().get(i);
          ImmunizationForecastDataBean fb = new ImmunizationForecastDataBean(forecastBean);
          fb.setForecastName(fa.getForecastCode());
          fb.setForecastLabel(fa.getForecastLabel());
          ds.resultList.add(fb);
        }
      }
    } else {
      if (ds.schedule.getCompleted() != null) {
        ForecastAntigen fa = ds.schedule.getCompleted();
        forecastBean = new ImmunizationForecastDataBean();
        forecastBean.setFinished(ds.finished.getDate());
        forecastBean.setForecastName(fa.getForecastCode());
        forecastBean.setForecastLabel(fa.getForecastLabel());
        forecastBean.setSortOrder(fa.getSortOrder());
        forecastBean.setImmregid(ds.patient.getImmregid());
        forecastBean.setTraceList(ds.traceList);
        forecastBean.setStatusDescription(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE);
        ds.resultList.add(forecastBean);
      }
    }
  }
}
