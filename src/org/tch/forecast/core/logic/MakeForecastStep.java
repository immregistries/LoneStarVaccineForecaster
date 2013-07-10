package org.tch.forecast.core.logic;

import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.VaccineForecastDataBean.Seasonal;

public class MakeForecastStep extends ActionStep {
  public static final String NAME = "Make Forecast";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception {
    addForecastRecommendations(ds);
    return FinishScheduleStep.NAME;
  }

  private void addForecastRecommendations(DataStore ds) {
    ds.log("Making recommendations");
    if (ds.seasonStart != null && ds.seasonCompleted) {
      ds.log("Adjusting for season start");
      ds.seasonStart = new DateTime(ds.seasonStart);
      ds.seasonStart.addYears(1);
      ds.seasonEnd = new DateTime(ds.seasonEnd);
      ds.seasonEnd.addYears(1);
      DetermineRangesStep.determineRanges(ds);
    }
    if (ds.traceBuffer != null) {
      ds.traceBuffer.append("</li><li>");
    }
    // Adjust around black out dates
    if (ds.blackOutDates != null && ds.blackOutDates.size() > 0) {
      ds.log("Adjusting forecast for black out dates");
      int i = -1;
      String validReason = null;
      for (DateTime[] blackOut : ds.blackOutDates) {
        i++;
        ds.log("Looking at black-out from " + blackOut[0] + " to " + blackOut[2]);
        if (ds.valid.isLessThanOrEquals(blackOut[0]) && ds.overdue.isGreaterThan(blackOut[2])) {
          ds.log("Recommendation is valid to give but a black out period starts before vaccination is overdue");
          ds.log("Moving valid date back to after black out date " + blackOut[2]);
          ds.valid = blackOut[2];
          validReason = ds.blackOutReasons.get(i);
          if (ds.early.isLessThan(ds.valid)) {
            ds.early = new DateTime(ds.valid);
          }
          if (ds.due.isLessThan(ds.early)) {
            ds.due = new DateTime(ds.early);
          }
          if (ds.overdue.isLessThan(ds.valid)) {
            ds.overdue = new DateTime(ds.due);
          }
          if (ds.finished.isLessThan(ds.valid)) {
            ds.valid = new DateTime(ds.finished);
            validReason = "because it is too late to administer vaccination";
          }
        } else if (ds.valid.isLessThanOrEquals(blackOut[0])
            && (new DateTime(ds.forecastDate)).isGreaterThan(blackOut[0])) {
          ds.log("A contraindication event starts after the valid date but before the forecast date");
          ds.log("Moving valid date back to after black out date " + blackOut[2]);
          ds.valid = blackOut[2];
          validReason = ds.blackOutReasons.get(i);
          if (ds.early.isLessThan(ds.valid)) {
            ds.early = new DateTime(ds.valid);
          }
          if (ds.due.isLessThan(ds.early)) {
            ds.due = new DateTime(ds.early);
          }
          if (ds.overdue.isLessThan(ds.valid)) {
            ds.overdue = new DateTime(ds.due);
          }
          if (ds.finished.isLessThan(ds.valid)) {
            ds.valid = new DateTime(ds.finished);
            validReason = "because it is too late to administer vaccination";
          }
        }
      }
      if (validReason != null) {
        if (ds.traceBuffer != null) {
          ds.traceBuffer.append(" <font color=\"#FF0000\">Adjusted future forecast " + validReason + ".</font> ");
        }
        if (ds.trace != null) {
          ds.traceList.append(" <font color=\"#FF0000\">Adjusted future forecast " + validReason + ".</font> ");
        }
      }
    }

    DateTime seasonEnd = null;
    DateTime seasonStart = null;

    if (ds.seasonal != null) {
      seasonEnd = new DateTime(ds.forecastDate);
      seasonEnd.setMonth(1);
      seasonEnd.setDay(1);
      seasonEnd = ds.seasonal.getEnd().getDateTimeFrom(seasonEnd);
      seasonStart = ds.seasonal.getStart().getDateTimeFrom(ds.seasonEnd);
      DateTime forecastDateTime = new DateTime(ds.forecastDate);
      if (forecastDateTime.isLessThan(seasonStart)) {
        // today is before start of next season
        if (forecastDateTime.isGreaterThanOrEquals(seasonEnd)) {
          // today is after the end of previous season
          // send end date of the next season, which is next year
          seasonEnd.addYears(1);
        } else {
          // today is before end of current season
          // change startDate to last year
          seasonStart.addYears(-1);
        }
      } else {
        // today is in season
        // sent end date to next year
        seasonEnd.addYears(1);
      }
      if (ds.due.isLessThan(seasonStart)) {
        ds.due = seasonStart;
      }

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
    if (seasonStart != null) {
      forecastBean.setSeasonStart(seasonStart.getDate());
    }
    if (seasonEnd != null) {
      forecastBean.setSeasonEnd(seasonEnd.getDate());
    }
    ds.resultList.add(forecastBean);
    if (ds.traceBuffer != null) {
      ds.traceBuffer.append("Forecasting for dose " + DetermineRangesStep.getNextValidDose(ds, ds.schedule)
          + " due at " + ds.dueReason + ", " + ds.due.toString("M/D/Y") + ". ");
    }
    if (ds.trace != null) {
      ds.traceList.append("Forecasting for dose " + DetermineRangesStep.getNextValidDose(ds, ds.schedule) + " due at "
          + ds.dueReason + ", " + ds.due.toString("M/D/Y") + ". ");
    }
  }

  private String getValidDose(DataStore ds, VaccineForecastDataBean.Schedule schedule) {
    String dose = schedule.getDose();
    if (dose.equals("*")) {
      dose = Integer.toString(ds.validDoseCount);
    }
    return dose;
  }

}
