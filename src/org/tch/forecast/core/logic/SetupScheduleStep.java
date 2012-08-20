package org.tch.forecast.core.logic;

import java.util.Collections;
import java.util.Comparator;

import org.tch.forecast.core.Trace;
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.DateTime;

public class SetupScheduleStep extends ActionStep
{
  public static final String NAME = "Setup Schedule";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception
  {

    ds.forecast = ds.schedule.getVaccineForecast();
    ds.previousEventDate = new DateTime(ds.patient.getDobDateTime());
    ds.previousEventDateValid = ds.previousEventDate;
    ds.beforePreviousEventDate = null;
    ds.validDoseCount = 0;

    setupSeasonal(ds);
    if (ds.traceBuffer != null)
    {
      ds.traceBuffer.append("<p><b>" + ds.forecast.getForecastCode() + "</b></p><ul><li>");
    }
    if (ds.traces != null)
    {
      ds.trace = new Trace();
      ds.trace.setSchedule(ds.schedule);
      ds.traceList = new TraceList();
      ds.traceList.add(ds.trace);
      ds.traceList.setForecastName(ds.forecast.getForecastCode());
      ds.traceList.setForecastLabel(ds.forecast.getForecastLabel());
      ds.traces.put(ds.forecast.getForecastCode(), ds.traceList);
      ds.traceList.append("<ul><li>");
    }
    ds.eventPosition = 0;
    ds.previousAfterInvalidInterval = null;
    LookForDoseStep.nextEvent(ds);
    return TraverseScheduleStep.NAME;
  }
  
  private void setupSeasonal(DataStore ds)
  {
    ds.seasonal = ds.forecast.getSeasonal();
    if (ds.seasonal != null)
    {
      ds.seasonCompleted = false;
      ds.originalEventList = ds.eventList;
      ds.seasonEnd = setupSeasonEnd(ds);

      int count = 0;
      while (ds.seasonEnd.isGreaterThanOrEquals(ds.patient.getDobDateTime()) && count < 10)
      {
        SeasonEndEvent se = new SeasonEndEvent(ds.seasonEnd.getDate());
        ds.event = new Event();
        ds.event.eventDate = se.getDateOfShot();
        ds.event.immList.add(se);
        ds.eventList.add(ds.event);
        count++;
        ds.seasonEnd.addYears(-1);
      }
      if (count == 0)
      {
        // If no seasonal events were added then put in a season start for
        // this year so that first forecast is good
        ds.seasonStart = ds.seasonal.getStart().getDateTimeFrom(ds.seasonEnd);
      }

      Collections.sort(ds.eventList, new Comparator<Event>() {
        public int compare(Event event1, Event event2)
        {
          return event1.eventDate.compareTo(event2.eventDate);
        }
      });
    }
  }
  
  protected static DateTime setupSeasonEnd(DataStore ds)
  {
    DateTime seasonEnd = new DateTime(ds.forecastDate);
    seasonEnd.setMonth(1);
    seasonEnd.setDay(1);
    seasonEnd = ds.seasonal.getEnd().getDateTimeFrom(seasonEnd);
    if (seasonEnd.isGreaterThanOrEquals(new DateTime(ds.forecastDate)))
    {
      seasonEnd.addYears(-1);
    }
    return seasonEnd;
  }




}
