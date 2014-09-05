package org.tch.forecast.core.logic;

import java.util.ArrayList;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.TimePeriod;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Contraindicate;

public class DetermineRangesStep extends ActionStep
{
  public static final String NAME = "Determine Ranges";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception {
    ds.log("Determine Ranges");
    determineRanges(ds);
    ds.indicates = ds.schedule.getIndicates();
    ds.indicatesPos = -1;
    return ChooseIndicatorStep.NAME;
  }

  protected static void determineRanges(DataStore ds) {
    ds.whenValidText = "";
    ds.blackOutDates = new ArrayList<BlackOut>();
    // adjust dates past contraindications
    if (ds.schedule.getContraindicates() != null && ds.schedule.getContraindicates().length > 0) {
      ds.log("This schedule has contraindications");
      for (Contraindicate contraindicate : ds.schedule.getContraindicates()) {
        ds.log("Looking at contraindication " + contraindicate.getVaccineName());
        for (Event event : ds.eventList) {
          ds.log(" + Looking at events on date " + new DateTime(event.getEventDate()));
          if (LookForDoseStep.indicatedEventVaccine(contraindicate.getVaccines(), ds, event)) {
            DateTime startBlackOut = new DateTime(event.getEventDate());
            DateTime endBlackOut = contraindicate.getAfterInterval().getDateTimeFrom(startBlackOut);
            DateTime endBlackOutGrace = contraindicate.getGrace().getDateTimeBefore(endBlackOut);
            startBlackOut.addDays(1);

            BlackOut blackOut = new BlackOut();
            blackOut.setStartBlackOut(startBlackOut);
            blackOut.setEndBlackOut(endBlackOut);
            blackOut.setEndBlackOutGrace(endBlackOutGrace);
            blackOut.setEventDate(new DateTime(event.getEventDate()));
            if (contraindicate.hasAgainst()) {
              blackOut.setVaccineName(contraindicate.getAgainst());
              blackOut.setAgainstVaccineIds(contraindicate.getAgainstVaccines());
              blackOut.setAgainstContra(contraindicate.getAgainstContra());
              blackOut.setAgainstAllowed(contraindicate.getAgainstAllowed());
            }
            String contraindicationDoseLabel = LookForDoseStep.createIndicatedEventVaccineLabel(
                contraindicate.getVaccines(), ds, event);
            if (contraindicationDoseLabel != null) {
              blackOut.setReason(" - must be given at least " + contraindicate.getAfterInterval()
                  + " after previous dose of " + contraindicationDoseLabel + " given "
                  + new DateTime(event.eventDate).toString("M/D/Y"));
            } else {
              blackOut.setReason(" - must be given at least " + contraindicate.getAfterInterval()
                  + " after previous dose given " + new DateTime(event.eventDate).toString("M/D/Y"));
            }
            ds.blackOutDates.add(blackOut);
            ds.log("Contraindicating event found, setting black out dates from " + startBlackOut.toString("M/D/Y")
                + " to " + endBlackOut.toString("M/D/Y"));
          }
        }
      }
    }

    String validReason = "";
    String validBecause = "";
    if (ds.schedule.getValidAge().isEmpty()) {
      ds.valid = ds.schedule.getValidInterval().getDateTimeFrom(ds.previousEventDateValid);
      validReason = ds.schedule.getValidInterval() + " after previous valid dose";
      validBecause = "INTERVAL";
      ds.log("Setting validity based on interval of " + ds.schedule.getValidInterval() + " from "
          + ds.previousEventDateValid.toString("M/D/Y"));
    } else {
      ds.valid = ds.schedule.getValidAge().getDateTimeFrom(ds.patient.getDobDateTime());
      if (ds.schedule.getValidAge().getAmount() == 0) {
        validReason = "birth";
        validBecause = "AGE";
        ds.log("Setting validity same day as patient birth ");
      } else {
        validReason = ds.schedule.getValidAge() + " of age";
        validBecause = "AGE";
        ds.log("Setting validity " + ds.schedule.getValidAge() + " of age");
      }
      if (!ds.schedule.getValidInterval().isEmpty() && ds.previousEventDateValidNotBirth != null) {
        DateTime validInterval = ds.schedule.getValidInterval().getDateTimeFrom(ds.previousEventDateValidNotBirth);
        if (validInterval.isGreaterThan(ds.valid)) {
          ds.valid = validInterval;
          validReason = ds.schedule.getValidInterval() + " after previous valid dose";
          validBecause = "INTERVAL";
          ds.log("Interval has later date than age, so re-setting validity to " + ds.schedule.getValidAge() + " from "
              + ds.previousEventDateValidNotBirth.toString("M/D/Y"));
        }
      }
    }

    ds.validGrace = ds.valid;

    ds.finished = ds.schedule.getFinishedAge().getDateTimeFrom(ds.patient.getDobDateTime());
    if (ds.previousEventDate.equals(ds.previousEventDateValid)) {
      if (notIgnoreGracePeriod(ds, ds.schedule.getValidGrace())) {
        ds.validGrace = ds.schedule.getValidGrace().getDateTimeBefore(ds.valid);
      }
    } else {
      if (ds.previousEventWasContra && ds.schedule.getAfterContraInterval() != null
          && !ds.schedule.getAfterContraInterval().isEmpty()) {
        DateTime validInterval = ds.schedule.getAfterContraInterval().getDateTimeFrom(ds.previousEventDate);
        if (validInterval.isGreaterThan(ds.valid)) {
          ds.valid = validInterval;
          validReason = "Must be given at least " + ds.schedule.getAfterContraInterval()
              + " after previous contraindicating dose";
          validBecause = "CONTRA";
          ds.log("Contraindication indication pushes validity date back by " + ds.schedule.getAfterContraInterval());
        }

        setGrace(ds, ds.schedule.getAfterContraGrace());

      } else {
        DateTime validInterval = ds.schedule.getAfterInvalidInterval().getDateTimeFrom(ds.previousEventDate);
        if (validInterval.isGreaterThan(ds.valid)) {
          ds.valid = validInterval;
          validReason = ds.schedule.getAfterInvalidInterval() + " after previous invalid dose";
          validBecause = "INVALID";
          ds.log("Applying minimum interval after invalid vaccination of " + ds.schedule.getAfterInvalidInterval());
        }
        setGrace(ds, ds.schedule.getAfterInvalidGrace());
      }
    }
    if (!ds.schedule.getBeforePreviousInterval().isEmpty() && ds.beforePreviousEventDate != null) {
      DateTime beforePreviousInterval = ds.schedule.getBeforePreviousInterval().getDateTimeFrom(
          ds.beforePreviousEventDate);
      if (beforePreviousInterval.isGreaterThan(ds.valid)) {
        ds.valid = beforePreviousInterval;
        validReason = ds.schedule.getBeforePreviousInterval() + " after valid dose given before previous valid dose";
        validBecause = "BEFORE";
        ds.log("Setting minimum interval for before previous dose of " + ds.schedule.getBeforePreviousInterval());
      }
    }
    if (ds.previousAfterInvalidInterval != null) {
      DateTime previousAfterInvalidIntervalDate = ds.previousAfterInvalidInterval.getDateTimeFrom(ds.previousEventDate);
      if (previousAfterInvalidIntervalDate.isGreaterThan(ds.valid)) {
        ds.valid = previousAfterInvalidIntervalDate;
        validReason = ds.previousAfterInvalidInterval + " after previous invalid/contraindicated dose";
        validBecause = "INVALID";
        ds.log("Setting minimum interval for after previous invalid of " + ds.previousAfterInvalidInterval);
      }
    }
    ds.log("Now determining due date");
    ds.dueReason = "";
    if (ds.schedule.getDueAge().isEmpty()) {
      ds.due = ds.schedule.getDueInterval().getDateTimeFrom(ds.previousEventDateValid);
      ds.dueReason = ds.schedule.getDueInterval() + " after previous valid dose";
      ds.log("Setting due date to interval since last dose.");
    } else {
      ds.log("Calculating and setting due date from birth");
      TimePeriod dueAge;
      TimePeriod dueInterval;
      if (ds.forecastOptions.isUseEarlyDue()
          && (!ds.schedule.getEarlyAge().isEmpty() || !ds.schedule.getEarlyInterval().isEmpty())) {
        dueAge = ds.schedule.getEarlyAge();
        ds.log("Using early due age " + dueAge);
        dueInterval = ds.schedule.getEarlyInterval();
        ds.log("Using early due interval " + dueInterval);
      } else {
        dueAge = ds.schedule.getDueAge();
        dueInterval = ds.schedule.getDueInterval();
      }
      ds.due = dueAge.getDateTimeFrom(ds.patient.getDobDateTime());
      if (dueAge.getAmount() == 0) {
        ds.dueReason = "birth";
      } else {
        ds.dueReason = dueAge + " of age";
      }
      if (!dueInterval.isEmpty()) {
        ds.log("Calculating due date from interval");
        DateTime dueIntervalDate = dueInterval.getDateTimeFrom(ds.previousEventDateValid);
        if (dueIntervalDate.isLessThan(ds.due)) {
          ds.due = dueIntervalDate;
          ds.dueReason = dueInterval + " after previous valid dose";
          ds.log("Interval date is after age date, selecting interval date");
        }
      }
    }
    if (ds.seasonStartDateTime != null) {
      ds.log("Calculating season rules");
      //      if (ds.seasonStartThis.isGreaterThan(ds.valid)) {
      //        ds.valid = new DateTime(ds.seasonStartThis);
      //        validReason = "start of next season";
      //        validBecause = "SEASON";
      //        ds.log("Season start is is after valid date, moving valid date forward to " + ds.valid.toString("M/D/Y"));
      //      }
      DateTime seasonDue = ds.seasonal.getDue().getDateTimeFrom(ds.seasonStartDateTime);
      if (seasonDue.isGreaterThan(ds.due)) {
        ds.due = seasonDue;
        if (ds.seasonal.getDue().getAmount() == 0) {
          ds.dueReason = "season start";
        } else {
          ds.dueReason = ds.seasonal.getDue() + " after season start";
        }
        ds.log("Season start is is after due date, moving due date forward to " + ds.due.toString("M/D/Y"));
      }
    }

    for (int i = 0; i < ds.blackOutDates.size(); i++) {
      ds.log("Looking to see if valid and due dates need to be adjusted around black out dates");
      BlackOut blackOut = ds.blackOutDates.get(i);

      if (blackOut.isAgainstSpecificVaccines()) {
        ds.log("Black out is against specific dates so not changing the due or valid dates");
      } else {
        if (ds.valid.isGreaterThan(blackOut.getStartBlackOut()) && ds.valid.isLessThan(blackOut.getEndBlackOutGrace())) {
          ds.valid = blackOut.getEndBlackOutGrace();
          ds.log("Moving valid date to end of blackOut period, setting to " + ds.due.toString("M/D/Y"));
        }
        if (ds.due.isGreaterThan(blackOut.getStartBlackOut()) && ds.due.isLessThan(blackOut.getEndBlackOutGrace())) {
          ds.due = blackOut.getEndBlackOutGrace();
          ds.dueReason = blackOut.getReason();
          ds.log("Moving due date to end of blackOut period, setting to " + ds.due.toString("M/D/Y"));
        }
      }
    }

    TimePeriod overdueInterval;
    TimePeriod overdueAge;
    if (ds.forecastOptions.isUseEarlyOverdue() && ds.schedule.getEarlyOverdueAge() != null) {
      overdueInterval = ds.schedule.getEarlyOverdueInterval();
      overdueAge = ds.schedule.getEarlyOverdueAge();
      ds.log("Using early overdue dates instead of standard overdue dates");
    } else {
      overdueInterval = ds.schedule.getOverdueInterval();
      overdueAge = ds.schedule.getOverdueAge();
    }
    if (overdueAge.isEmpty()) {
      ds.overdue = overdueInterval.getDateTimeFrom(ds.previousEventDateValid);
      ds.log("Setting overdue date for " + overdueInterval + " after previous valid dose");
    } else {
      ds.overdue = overdueAge.getDateTimeFrom(ds.patient.getDobDateTime());
      ds.log("Setting overdue date for " + overdueAge + " of age");
      if (!overdueInterval.isEmpty()) {
        ds.log("Calculating at overdue interval");
        DateTime overdueIntervalDate = overdueInterval.getDateTimeFrom(ds.previousEventDateValid);
        if (overdueIntervalDate.isGreaterThan(ds.overdue)) {
          ds.overdue = overdueIntervalDate;
          ds.log("Setting overdue date for " + overdueInterval + " after previous valid dose");
        }
      }
    }

    if (ds.seasonStartDateTime != null) {
      ds.log("Season started, looking to set season overdue");
      DateTime seasonOverdue = ds.seasonal.getOverdue().getDateTimeFrom(ds.seasonStartDateTime);
      if (seasonOverdue.isLessThan(ds.overdue)) {
        ds.overdue = seasonOverdue;
        ds.log("Setting overdue to the end of the season");
      }
    }
    if (ds.due.isLessThan(ds.valid)) {
      ds.due = new DateTime(ds.valid);
      ds.dueReason = "same time as valid";
      ds.log("Adjusting due to not be before valid date");
    }
    if (ds.overdue.isLessThan(ds.due)) {
      ds.overdue = new DateTime(ds.due);
      ds.log("Adjusting overdue to not be before due date");
    }

    if (ds.finished.isLessThan(ds.overdue)) {
      ds.overdue = new DateTime(ds.finished);
      ds.log("Adjusting overdue date to not be after finished date");
      if (ds.overdue.isLessThan(ds.due)) {
        ds.due = new DateTime(ds.overdue);
        ds.dueReason = "same time as over due";
        ds.log("Adjusting due date to not be after the over due date");
        if (ds.due.isLessThan(ds.valid)) {
          ds.valid = new DateTime(ds.due);
          ds.log("Vaccination is not valid to be administered before time has finished for vaccinating");
          validReason = "vaccine does not need to be administered";
        }
      }
    }
    ds.whenValidText = "Dose " + getNextValidDose(ds, ds.schedule) + " valid at " + validReason + ", "
        + ds.valid.toString("M/D/Y");
    if (ds.trace != null) {
      ds.trace.setDueDate(ds.due);
      ds.trace.setOverdueDate(ds.overdue);
      ds.trace.setValidDate(ds.valid);
      ds.trace.setFinishedDate(ds.finished);
      ds.trace.setValidReason(ds.whenValidText + ". ");
      ds.trace.setValidBecause(validBecause);
      ds.traceList.addExplanation(ds.whenValidText + ". ");
    }

  }

  public static void setGrace(DataStore ds, TimePeriod grace) {
    if (notIgnoreGracePeriod(ds, grace)) {
      DateTime validIntervalGrace = grace.getDateTimeBefore(ds.valid);
      if (validIntervalGrace.isGreaterThan(ds.validGrace)) {
        ds.validGrace = validIntervalGrace;
      }
    }
  }

  private static boolean notIgnoreGracePeriod(DataStore ds, TimePeriod validGrace) {
    boolean ignoreGrace = ds.getForecastOptions() != null && ds.getForecastOptions().isIgnoreFourDayGrace()
        && validGrace.isFourDay();
    if (ignoreGrace) {
      ds.log("Standard grace period of 4 days is being ignored, no grace period will be applied");
      return false;
    }
    return true;
  }

  protected static String getNextValidDose(DataStore ds, VaccineForecastDataBean.Schedule schedule) {
    String dose = schedule.getDose();
    if (dose.equals("*")) {
      dose = Integer.toString(ds.validDoseCount + 1);
    }
    return dose;
  }

}
