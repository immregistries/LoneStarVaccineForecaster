package org.immregistries.lonestar.core.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Seasonal;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.TraceList;
import org.immregistries.lonestar.core.Transition;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.VaccineForecastDataBean.InvalidateSameDay;
import org.immregistries.lonestar.core.VaccineForecastDataBean.ValidVaccine;
import org.immregistries.lonestar.core.model.Assumption;

public class SetupScheduleStep extends ActionStep
{
  public static final String NAME = "Setup Schedule";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception {

    ds.forecast = ds.schedule.getVaccineForecast();
    ds.previousEventDate = new DateTime(ds.patient.getDobDateTime());
    ds.previousEventDateValid = ds.previousEventDate;
    ds.previousEventDateValidNotBirth = null;
    ds.beforePreviousEventDate = null;
    ds.validDoseCount = 0;

    setupSeasonalAndTransition(ds);
    if (ds.traces != null) {
      ds.trace = new Trace();
      ds.trace.setSchedule(ds.schedule);
      ds.traceList = new TraceList();
      ds.traceList.add(ds.trace);
      ds.traceList.setForecastName(ds.forecast.getForecastCode());
      ds.traceList.setForecastLabel(ds.forecast.getForecastLabel());
      ds.traces.put(ds.forecast.getForecastCode(), ds.traceList);
      ds.traceList.setExplanationBulletPointStart();
    }
    ds.assumptionList = new ArrayList<Assumption>();
    ds.log("Looking at first event");
    ds.eventPosition = 0;
    ds.previousAfterInvalidInterval = null;
    ds.valid = null;
    ds.due = null;
    ds.overdue = null;
    LookForDoseStep.nextEvent(ds);
    return TraverseScheduleStep.NAME;
  }

  private void setupSeasonalAndTransition(DataStore ds) {
    ds.seasonal = null;
    if (ds.forecast.getSeasonal() != null) {
      ds.seasonal = new Seasonal(ds.forecast.getSeasonal());
      copyOverrideSetttings(ds);
    }
    ds.transitionList = ds.forecast.getTransitionList();
    if (ds.seasonal != null) {
      ds.seasonEndDateTime = setupLastSeasonEnd(ds);
      ds.log("Setting season end = " + ds.seasonEndDateTime.toString("M/D/Y"));
      ds.seasonStartDateTime = new DateTime(ds.seasonEndDateTime);
      ds.seasonStartDateTime.addDays(1);
      ds.originalEventList = new ArrayList<Event>(ds.eventList);
      {
        SeasonStartEvent se = new SeasonStartEvent(ds.seasonStartDateTime.getDate());
        ds.event = new Event();
        ds.event.eventDate = se.getDateOfShot();
        ds.event.immList.add(se);
        ds.eventList.add(ds.event);
      }
      Collections.sort(ds.eventList, eventComparator);
    }
    if (ds.transitionList.size() > 0) {
      if (ds.originalEventList == null) {
        ds.originalEventList = new ArrayList<Event>(ds.eventList);
      }
      for (Transition transition : ds.transitionList) {
        DateTime transitionDate = transition.getAge().getDateTimeFrom(ds.patient.getDobDateTime());
        if (transitionDate.isLessThanOrEquals(ds.forecastDateTime)) {
          // Transition happens before or on forecaster test date
          TransitionEvent te = new TransitionEvent(transitionDate.getDate(), transition);
          ds.event = new Event();
          ds.event.eventDate = te.getDateOfShot();
          ds.event.immList.add(te);
          ds.eventList.add(ds.event);
        }
      }
      Collections.sort(ds.eventList, eventComparator);
    }

  }

  public void copyOverrideSetttings(DataStore ds) {
    if (ds.forecastOptions.getFluSeasonDue() != null) {
      ds.seasonal.setDue(ds.forecastOptions.getFluSeasonDue());
    }
    if (ds.forecastOptions.getFluSeasonEnd() != null) {
      ds.seasonal.setEnd(ds.forecastOptions.getFluSeasonEnd());
    }
    if (ds.forecastOptions.getFluSeasonOverdue() != null) {
      ds.seasonal.setOverdue(ds.forecastOptions.getFluSeasonOverdue());
    }
    if (ds.forecastOptions.getFluSeasonFinished() != null) {
      ds.seasonal.setFinished(ds.forecastOptions.getFluSeasonFinished());
    }
  }

  protected static DateTime setupLastSeasonEnd(DataStore ds) {
    DateTime seasonEnd = new DateTime(ds.forecastDateTime);
    seasonEnd.setMonth(1);
    seasonEnd.setDay(1);
    seasonEnd = ds.seasonal.getEnd().getDateTimeFrom(seasonEnd);
    if (seasonEnd.isGreaterThanOrEquals(new DateTime(ds.forecastDateTime))) {
      seasonEnd.addYears(-1);
    }
    seasonEnd.addDays(-1);
    return seasonEnd;
  }

  private static Comparator<Event> eventComparator = new Comparator<Event>() {
    public int compare(Event event1, Event event2) {
      DateTime dt1 = new DateTime(event1.getEventDate());
      DateTime dt2 = new DateTime(event2.getEventDate());
      if (dt1.getYear() == dt2.getYear() && dt1.getDay() == dt2.getDay() && dt1.getMonth() == dt2.getMonth()) {
        Integer vaccineId1 = event1.getImmList().size() > 0 ? event1.getImmList().get(0).getVaccineId() : 0;
        Integer vaccineId2 = event2.getImmList().size() > 0 ? event2.getImmList().get(0).getVaccineId() : 0;
        return vaccineId1.compareTo(vaccineId2);
      }
      return event1.eventDate.compareTo(event2.eventDate);
    }
  };

}
