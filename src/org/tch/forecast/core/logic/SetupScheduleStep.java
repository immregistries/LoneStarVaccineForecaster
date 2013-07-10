package org.tch.forecast.core.logic;

import java.util.Collections;
import java.util.Comparator;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.Trace;
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.VaccineForecastDataBean.Transition;

public class SetupScheduleStep extends ActionStep {
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
    if (ds.traceBuffer != null) {
      ds.traceBuffer.append("<p><b>" + ds.forecast.getForecastCode() + "</b></p><ul><li>");
    }
    if (ds.traces != null) {
      ds.trace = new Trace();
      ds.trace.setSchedule(ds.schedule);
      ds.traceList = new TraceList();
      ds.traceList.add(ds.trace);
      ds.traceList.setForecastName(ds.forecast.getForecastCode());
      ds.traceList.setForecastLabel(ds.forecast.getForecastLabel());
      ds.traces.put(ds.forecast.getForecastCode(), ds.traceList);
      ds.traceList.append("<ul><li>");
    }
    ds.log("Looking at first event");
    ds.eventPosition = 0;
    ds.previousAfterInvalidInterval = null;
    LookForDoseStep.nextEvent(ds);
    return TraverseScheduleStep.NAME;
  }

  private void setupSeasonalAndTransition(DataStore ds) {
    ds.seasonal = ds.forecast.getSeasonal();
    if (ds.seasonal != null && ds.forecastOptions != null)
    {
      copyOverrideSetttings(ds);
    }
    ds.transitionList = ds.forecast.getTransitionList();
    if (ds.seasonal != null || ds.transitionList.size() > 0) {
      ds.originalEventList = ds.eventList;
      if (ds.seasonal != null) {
        ds.seasonCompleted = false;
        ds.seasonEnd = setupSeasonEnd(ds);

        int count = 0;
        while (ds.seasonEnd.isGreaterThanOrEquals(ds.patient.getDobDateTime()) && count < 10) {
          SeasonEndEvent se = new SeasonEndEvent(ds.seasonEnd.getDate());
          ds.event = new Event();
          ds.event.eventDate = se.getDateOfShot();
          ds.event.immList.add(se);
          ds.eventList.add(ds.event);
          count++;
          ds.seasonEnd.addYears(-1);
        }
        if (count == 0) {
          // If no seasonal events were added then put in a season start for
          // this year so that first forecast is good
          ds.seasonStart = ds.seasonal.getStart().getDateTimeFrom(ds.seasonEnd);
        }

      }
      if (ds.transitionList.size() > 0) {
        for (Transition transition : ds.transitionList) {
          DateTime transitionDate = transition.getAge().getDateTimeFrom(ds.patient.getDobDateTime());
          if (transitionDate.isLessThanOrEquals(ds.today)) {
            // Transition happens before or on forecaster test date
            TransitionEvent te = new TransitionEvent(transitionDate.getDate(), transition);
            ds.event = new Event();
            ds.event.eventDate = te.getDateOfShot();
            ds.event.immList.add(te);
            ds.eventList.add(ds.event);
          }
        }
      }

      Collections.sort(ds.eventList, new Comparator<Event>() {
        public int compare(Event event1, Event event2) {
          return event1.eventDate.compareTo(event2.eventDate);
        }
      });
    }
  }

  public void copyOverrideSetttings(DataStore ds) {
    if (ds.forecastOptions.getFluSeasonDue() != null)
    {
      ds.seasonal.setDue(ds.forecastOptions.getFluSeasonDue());
    }
    if (ds.forecastOptions.getFluSeasonEnd() != null)
    {
      ds.seasonal.setEnd(ds.forecastOptions.getFluSeasonEnd());
    }
    if (ds.forecastOptions.getFluSeasonOverdue() != null)
    {
      ds.seasonal.setOverdue(ds.forecastOptions.getFluSeasonOverdue());
    }
    if (ds.forecastOptions.getFluSeasonStart() != null)
    {
      ds.seasonal.setStart(ds.forecastOptions.getFluSeasonStart());
    }
  }

  protected static DateTime setupSeasonEnd(DataStore ds) {
    DateTime seasonEnd = new DateTime(ds.forecastDate);
    seasonEnd.setMonth(1);
    seasonEnd.setDay(1);
    seasonEnd = ds.seasonal.getEnd().getDateTimeFrom(seasonEnd);
    if (seasonEnd.isGreaterThanOrEquals(new DateTime(ds.forecastDate))) {
      seasonEnd.addYears(-1);
    }
    return seasonEnd;
  }

}
