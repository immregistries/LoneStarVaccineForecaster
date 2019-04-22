package org.immregistries.lonestar.core.logic;

import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.api.impl.ForecastAntigen;

public abstract class ActionStep
{
  public abstract String getName();

  public abstract String doAction(DataStore dataStore) throws Exception;

  public void addResultToList(DataStore ds, ImmunizationForecastDataBean forecastBean) {
    boolean added = false;
    if (forecastBean.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE)
        || forecastBean.getStatusDescription().equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED)) {
      ds.log("Adding a complete or finished schedule, checking to see if there is a need to add alternative completes instead");
      if (ds.schedule.getCompletesList().size() > 1) {
        for (int i = 0; i < ds.schedule.getCompletesList().size(); i++) {
          ForecastAntigen fa = ds.schedule.getCompletesList().get(i);
          ImmunizationForecastDataBean fb = new ImmunizationForecastDataBean(forecastBean);
          fb.setForecastName(fa.getForecastCode());
          fb.setForecastLabel(fa.getForecastLabel());
          fb.setImmregid(ds.patient.getImmregid());
          fb.setSortOrder(fa.getSortOrder());
          fb.setImmregid(ds.patient.getImmregid());
          ds.resultList.add(fb);
          fb.setTraceList(ds.traceList);
          fb.setStatusDescription(forecastBean.getStatusDescription());
          added = true;
        }
      }
    } else if (ds.schedule.getCompleted() != null) {
      ForecastAntigen fa = ds.schedule.getCompleted();
      ImmunizationForecastDataBean fb = new ImmunizationForecastDataBean();
      fb.setFinished(ds.finished.getDate());
      fb.setForecastName(fa.getForecastCode());
      fb.setForecastLabel(fa.getForecastLabel());
      fb.setSortOrder(fa.getSortOrder());
      fb.setImmregid(ds.patient.getImmregid());
      fb.setTraceList(ds.traceList);
      fb.setStatusDescription(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE);
      ds.resultList.add(fb);
    }

    if (!added) {
      ds.resultList.add(forecastBean);
    }
  }
}
