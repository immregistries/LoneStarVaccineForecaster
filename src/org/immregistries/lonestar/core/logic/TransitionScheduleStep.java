package org.immregistries.lonestar.core.logic;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.Trace;

public class TransitionScheduleStep extends ActionStep
{
  public static final String NAME = "Transition Schedule";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception {
    ds.log("Transition Schedule");
    ds.log(" + Setting current schedule to the new one indicated");
    ds.schedule = ds.forecast.getSchedules().get(ds.nextAction);
    ds.indicatesPos = -1;
    ds.previousAfterInvalidInterval = null;
    if (ds.schedule == null) {
      throw new Exception("Unable to find schedule " + ds.nextAction);
    }
    if (ds.trace != null) {
      ds.trace = new Trace();
      ds.trace.setSchedule(ds.schedule);
      ds.traceList.add(ds.trace);
      String label = ds.schedule.getLabel();
      if (label == null || label.equals("")) {
        label = "[Schedule " + ds.schedule.getScheduleName() + "]";
      }
      ds.traceList.addExplanation("Now expecting " + label + " dose");
      ds.traceList.setExplanationBulletPointStart();
    }
    ds.log("Checking finished date");
    ds.finished = ds.schedule.getFinishedAge().getDateTimeFrom(ds.patient.getDobDateTime());
    DateTime referenceDate = ds.forecastDateTime;
    if (ds.event != null) {
      DateTime eventDateTime = new DateTime(ds.event.eventDate);
      if (eventDateTime.isLessThan(referenceDate)) {
        referenceDate = eventDateTime;
      }
    }
    if (referenceDate.isGreaterThan(ds.finished)) {
      ds.log(" + Patient is now too old to get any more doses");
      if (ds.trace != null) {
        ds.trace.setFinished(true);
        ds.traceList.addExplanation("No need for further vaccinations after " + ds.finished.toString("M/D/Y"));
      }
      ImmunizationForecastDataBean forecastBean = new ImmunizationForecastDataBean();
      forecastBean.setFinished(ds.finished.getDate());
      forecastBean.setForecastName(ds.forecast.getForecastCode());
      forecastBean.setForecastLabel(ds.forecast.getForecastLabel());
      forecastBean.setSortOrder(ds.forecast.getSortOrder());
      forecastBean.setImmregid(ds.patient.getImmregid());
      forecastBean.setTraceList(ds.traceList);
      if (ds.assumptionList.size() > 0) {
        forecastBean.setStatusDescription(ImmunizationForecastDataBean.STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE);
        forecastBean.getAssumptionList().addAll(ds.assumptionList);
      } else {
        forecastBean.setStatusDescription(ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED);
      }
      addResultToList(ds, forecastBean);
      return FinishScheduleStep.NAME;
    }
    ds.log("Going to next step");
    LookForDoseStep.setHasEvent(ds);
    return TraverseScheduleStep.NAME;
  }
}
