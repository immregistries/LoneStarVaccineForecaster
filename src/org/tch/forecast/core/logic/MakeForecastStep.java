package org.tch.forecast.core.logic;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.VaccineForecastDataBean;

public class MakeForecastStep extends ActionStep
{
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
    if (ds.trace != null) {
      ds.traceList.setExplanationBulletPointStart();
    }
    // Adjust around black out dates
    if (ds.blackOutDates != null && ds.blackOutDates.size() > 0) {
      ds.log("Looking to see if forecast needs to be adjusted to after black out dates");
      String validReason = null;
      DateTime contraValid = ds.valid;

      for (BlackOut blackOut : ds.blackOutDates) {

        ds.log("Looking at black-out from " + blackOut.getStartBlackOut().toString("M/D/Y") + " to "
            + blackOut.getEndBlackOut().toString("M/D/Y"));
        if (blackOut.isAgainstSpecificVaccines()) {
          ds.log(" + Not Adjusting: Black out is for specific vaccine and not for the entire series");
        } else {
          DateTime start = blackOut.getStartBlackOut();
          DateTime end = blackOut.getEndBlackOut();
          if (ds.valid.isGreaterThan(end)) {
            ds.log(" + Not Adjusting: Black out ends before vaccination is valid");
          } else if (ds.forecastDateTime.isLessThan(start)) {
            ds.log(" + Not Adjusting: Black out starts after forecast date");
          } else {
            ds.log(" + Adjusting: Valid date is before the end of this black out period");
            ds.valid = blackOut.getEndBlackOut();
            validReason = blackOut.getReason();
            if (ds.due.isLessThan(ds.valid)) {
              ds.due = new DateTime(ds.valid);
            }
            if (ds.overdue.isLessThan(ds.valid)) {
              ds.overdue = new DateTime(ds.valid);
            }
            if (ds.finished.isLessThan(ds.valid)) {
              ds.log(" + Too late to finish, not recommending");
              ds.valid = new DateTime(ds.finished);
              validReason = "because it is too late to administer vaccination";
            }
          }
        }
      }
      if (validReason != null) {
        if (ds.trace != null) {
          ds.traceList.setExplanationRed();
          ds.traceList.addExplanation("Adjusted future forecast " + validReason);
        }
      }
    }

    DateTime seasonEnd = null;
    DateTime seasonStart = null;

    if (ds.seasonal != null) {
      seasonEnd = new DateTime(ds.forecastDateTime);
      seasonEnd.setMonth(1);
      seasonEnd.setDay(1);
      seasonEnd = ds.seasonal.getEnd().getDateTimeFrom(seasonEnd);
      seasonStart = ds.seasonal.getStart().getDateTimeFrom(ds.seasonEnd);
      if (ds.forecastDateTime.isLessThan(seasonStart)) {
        // today is before start of next season
        if (ds.forecastDateTime.isGreaterThanOrEquals(seasonEnd)) {
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

    String statusDescription = "";
    if (!ds.forecastDateTime.isLessThan(ds.finished)) {
      statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED;
      if (ds.traceList != null) {
        ds.traceList.setExplanationBulletPointStart();
        ds.traceList.addExplanation("Too late to complete. Next dose was expected before " + ds.finished);
        ds.traceList.setStatusDescription("Too late to complete. Next dose was expected before " + ds.finished + ".");
      }
    } else {
      if ((ds.seasonal != null && ds.seasonCompleted)
          || (seasonEnd != null && ds.due.getDate().after(seasonEnd.getDate()))) {
        statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE_FOR_SEASON;
      } else {
        if (ds.due.isGreaterThan(ds.forecastDateTime)) {
          statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER;
        } else if (ds.forecastDateTime.isLessThan(ds.overdue)) {
          statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE;
        } else if (ds.forecastDateTime.isLessThan(ds.finished)) {
          statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE;
        }
      }
      if (ds.trace != null) {
        ds.traceList.addExplanation("Forecasting for dose " + DetermineRangesStep.getNextValidDose(ds, ds.schedule)
            + " due at " + ds.dueReason + ", " + ds.due.toString("M/D/Y"));
      }
    }
    forecastBean.setStatusDescription(statusDescription);
    forecastBean.getAssumptionList().addAll(ds.assumptionList);

    ds.resultList.add(forecastBean);

    // Adjust around black out dates
    if (ds.blackOutDates != null && ds.blackOutDates.size() > 0) {
      ds.log("Checking to see if there is a need to create contraindication forecast");
      String validReason = null;
      ImmunizationForecastDataBean forecastContraindication = null;
      DateTime contraValid = ds.valid;
      DateTime contraDue = ds.due;
      DateTime contraOverdue = ds.overdue;
      DateTime contraFinished = ds.finished;
      for (BlackOut blackOut : ds.blackOutDates) {

        if (blackOut.isAgainstSpecificVaccines()) {
          ds.log("Looking at vaccine specific black-out from " + blackOut.getStartBlackOut().toString("YMD") + " to "
              + blackOut.getEndBlackOutGrace());

          TraceList traceList = null;
          if (ds.traceList != null) {
            traceList = new TraceList(ds.traceList);
          }
          if (contraValid.isLessThanOrEquals(blackOut.getStartBlackOut())
              && contraOverdue.isGreaterThan(blackOut.getEndBlackOut())) {
            ds.log("Recommendation is valid to give but a black out period starts before vaccination is overdue. "
                + blackOut.getAgainstContra() + " can not be given yet.");
            ds.log("Moving valid date back to after black out date " + blackOut.getEndBlackOut().toString("YMD") + ".");
            if (traceList != null) {
              traceList.setExplanationBulletPointStart();
              traceList.addExplanation("Adjusting due date for " + blackOut.getAgainstContra()
                  + " to after contraindication black out date of " + blackOut.getEndBlackOut().toString("YMD"));
            }
            contraValid = blackOut.getEndBlackOut();
            validReason = blackOut.getReason();
            if (contraDue.isLessThan(contraValid)) {
              contraDue = new DateTime(contraValid);
            }
            if (contraOverdue.isLessThan(contraValid)) {
              contraOverdue = new DateTime(contraDue);
            }
            if (contraFinished.isLessThan(contraValid)) {
              contraValid = new DateTime(contraFinished);
              validReason = "because it is too late to administer vaccination";
            }
          } else if (contraValid.isLessThanOrEquals(blackOut.getStartBlackOut())
              && ds.forecastDateTime.isGreaterThan(blackOut.getStartBlackOut())) {
            ds.log("A contraindication event starts after the valid date but before the forecast date");
            ds.log("Moving valid date back to after black out date " + blackOut.getEndBlackOut().toString("YMD"));
            if (traceList != null) {
              traceList.setExplanationBulletPointStart();
              traceList.addExplanation("Adjusting due date for " + blackOut.getAgainstContra()
                  + " to after contraindication black out date of " + blackOut.getEndBlackOut().toString("YMD"));
            }
            contraValid = blackOut.getEndBlackOut();
            validReason = blackOut.getReason();
            if (contraDue.isLessThan(contraValid)) {
              contraDue = new DateTime(contraValid);
            }
            if (contraOverdue.isLessThan(contraValid)) {
              contraOverdue = new DateTime(contraDue);
            }
            if (contraFinished.isLessThan(contraValid)) {
              contraValid = new DateTime(contraFinished);
              validReason = "because it is too late to administer vaccination";
            }
          }

          ds.log("Administration is contraindicated now or sometime in the future for " + blackOut.getAgainstContra());
          forecastContraindication = new ImmunizationForecastDataBean();

          forecastContraindication.setValid(contraValid.getDate());
          forecastContraindication.setDue(contraDue.getDate());
          forecastContraindication.setOverdue(contraOverdue.getDate());
          forecastContraindication.setFinished(contraFinished.getDate());
          forecastContraindication.setDateDue(contraDue.getDate());
          forecastContraindication.setForecastName(blackOut.getAgainstContra());
          forecastContraindication.setForecastLabel(blackOut.getAgainstContra());
          forecastContraindication.setSortOrder(ds.forecast.getSortOrder());
          forecastContraindication.setDose(getValidDose(ds, ds.schedule));
          forecastContraindication.setSchedule(ds.schedule.getScheduleName());
          forecastContraindication.setImmregid(ds.patient.getImmregid());
          forecastContraindication.setTraceList(traceList);
          if (seasonStart != null) {
            forecastContraindication.setSeasonStart(seasonStart.getDate());
          }
          if (seasonEnd != null) {
            forecastContraindication.setSeasonEnd(seasonEnd.getDate());
          }

          statusDescription = "";
          if (!ds.forecastDateTime.isLessThan(contraFinished)) {
            statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_FINISHED;
          } else {
            if (ds.forecastDateTime.isLessThan(contraDue)) {
              statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_CONTRAINDICATED;
            } else if (ds.forecastDateTime.isLessThan(contraOverdue)) {
              statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE;
            } else if (ds.forecastDateTime.isLessThan(contraFinished)) {
              statusDescription = ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE;
            }
          }
          forecastContraindication.setStatusDescription(statusDescription);

          if (traceList != null) {
            traceList.setExplanationBulletPointStart();
          }

          if (blackOut.getAgainstAllowed() != null && !blackOut.getAgainstAllowed().equals("")) {
            forecastBean.setForecastName(blackOut.getAgainstAllowed());
            forecastBean.setForecastLabel(blackOut.getAgainstAllowed());
          }
        }
      }
      if (forecastContraindication != null) {
        ds.resultList.add(forecastContraindication);
      }
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
