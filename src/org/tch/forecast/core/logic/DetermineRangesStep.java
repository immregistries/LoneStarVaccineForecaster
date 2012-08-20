package org.tch.forecast.core.logic;

import java.util.ArrayList;

import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Contraindicate;
import org.tch.forecast.core.DateTime;

public class DetermineRangesStep extends ActionStep
{
  public static final String NAME = "Determine Ranges";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception
  {
    determineRanges(ds);
    ds.indicates = ds.schedule.getIndicates();
    ds.indicatesPos = -1;
    return ChooseIndicatorStep.NAME;
  }

  
  protected static void determineRanges(DataStore ds)
  {
    ds.blackOutDates = new ArrayList<DateTime[]>();
    ds.blackOutReasons = new ArrayList<String>();
    // adjust dates past contraindications
    if (ds.schedule.getContraindicates() != null && ds.schedule.getContraindicates().length > 0)
    {
      for (Contraindicate contraindicate : ds.schedule.getContraindicates())
      {
        for (Event event : ds.eventList)
        {
          if (LookForDoseStep.indicatedEventVaccine(contraindicate.getVaccines(), event))
          {
            DateTime startBlackOut = new DateTime(event.getEventDate());
            DateTime endBlackOut = contraindicate.getAfterInterval().getDateTimeFrom(startBlackOut);
            ds.blackOutDates.add(new DateTime[] {startBlackOut, endBlackOut});
            ds.blackOutReasons.add(contraindicate.getAfterInterval() + " after contraindicated dose");
            ds.log("Contraindicated event found, setting black out dates from " + startBlackOut.toString("M/D/Y") + " to " + endBlackOut.toString("M/D/Y"));
          }
        }
      }
    }
    
    String validReason = "";
    String validBecause = "";
    if (ds.schedule.getValidAge().isEmpty())
    {
      ds.valid = ds.schedule.getValidInterval().getDateTimeFrom(ds.previousEventDateValid);
      validReason = ds.schedule.getValidInterval() + " after previous valid dose";
      validBecause = "INTERVAL";
      ds.log("Setting validity based on interval of " + ds.schedule.getValidInterval() + " from " + ds.previousEventDate.toString("M/D/Y"));
    } else
    {
      ds.valid = ds.schedule.getValidAge().getDateTimeFrom(ds.patient.getDobDateTime());
      if (ds.schedule.getValidAge().getAmount() == 0)
      {
        validReason = "birth";
        validBecause = "AGE";
        ds.log("Setting validity same day as patient birth ");
      } else
      {
        validReason = ds.schedule.getValidAge() + " of age";
        validBecause = "AGE";
        ds.log("Setting validity " + ds.schedule.getValidAge() + " of age");
      }
      if (!ds.schedule.getValidInterval().isEmpty())
      {
        DateTime validInterval = ds.schedule.getValidInterval().getDateTimeFrom(ds.previousEventDateValid);
        if (validInterval.isGreaterThan(ds.valid))
        {
          ds.valid = validInterval;
          validReason = ds.schedule.getValidInterval() + " after previous valid dose";
          validBecause = "INTERVAL";
          ds.log("Interval has later date than age, so re-setting validity to " + ds.schedule.getValidAge() + " from " +  ds.previousEventDate.toString("M/D/Y"));
        }
      }
    }
    ds.finished = ds.schedule.getFinishedAge().getDateTimeFrom(ds.patient.getDobDateTime());
    if (ds.previousEventDate.equals(ds.previousEventDateValid))
    {
      ds.validGrace = ds.schedule.getValidGrace();
    } else
    {
      if (ds.previousEventWasContra && ds.schedule.getAfterContraInterval() != null
          && !ds.schedule.getAfterContraInterval().isEmpty())
      {
        DateTime validInterval = ds.schedule.getAfterContraInterval().getDateTimeFrom(ds.previousEventDate);
        if (validInterval.isGreaterThan(ds.valid))
        {
          ds.valid = validInterval;
          validReason = ds.schedule.getAfterContraInterval() + " after contraindicated dose";
          validBecause = "CONTRA";
          ds.log("Contraindication indication pushes validity date back by " + ds.schedule.getAfterContraInterval());
        }
        ds.validGrace = ds.schedule.getAfterContraGrace();
        ds.log("Setting valid grace to contraindication grace of " + ds.validGrace);
      } else
      {
        DateTime validInterval = ds.schedule.getAfterInvalidInterval().getDateTimeFrom(ds.previousEventDate);
        if (validInterval.isGreaterThan(ds.valid))
        {
          ds.valid = validInterval;
          validReason = ds.schedule.getAfterInvalidInterval() + " after previous dose";
          validBecause = "INVALID";
          ds.log("Applying minimum interval after invalid vaccination of " + ds.schedule.getAfterInvalidInterval());
        }
        ds.validGrace = ds.schedule.getAfterInvalidGrace();
        ds.log("Setting valid grace to after invalid grace interval of " + ds.validGrace);
      }
    }
    if (!ds.schedule.getBeforePreviousInterval().isEmpty() && ds.beforePreviousEventDate != null)
    {
      DateTime beforePreviousInterval = ds.schedule.getBeforePreviousInterval().getDateTimeFrom(ds.beforePreviousEventDate);
      if (beforePreviousInterval.isGreaterThan(ds.valid))
      {
        ds.valid = beforePreviousInterval;
        validReason = ds.schedule.getBeforePreviousInterval() + " after valid dose given before previous valid dose";
        validBecause = "BEFORE";
        ds.log("Setting minimum interval for before previous dose of " + ds.schedule.getBeforePreviousInterval());
      }
    }
    if (ds.previousAfterInvalidInterval != null)
    {
      DateTime previousAfterInvalidIntervalDate = ds.previousAfterInvalidInterval.getDateTimeFrom(ds.previousEventDate);
      if (previousAfterInvalidIntervalDate.isGreaterThan(ds.valid))
      {
        ds.valid = previousAfterInvalidIntervalDate;
        validReason = ds.previousAfterInvalidInterval + " after previous invalid/contraindicated dose";
        validBecause = "INVALID";
        ds.log("Setting minimum interval for after previous invalid of " + ds.previousAfterInvalidInterval);
      }
    }
    ds.log("Now determining due date");
    ds.dueReason = "";
    if (ds.schedule.getDueAge().isEmpty())
    {
      ds.due = ds.schedule.getDueInterval().getDateTimeFrom(ds.previousEventDate);
      ds.dueReason = ds.schedule.getDueInterval() + " after previous dose";
      ds.log("Setting due date to interval since last dose.");
    } else
    {
      ds.log("Calculating and setting due date from birth");
      ds.due = ds.schedule.getDueAge().getDateTimeFrom(ds.patient.getDobDateTime());
      if (ds.schedule.getDueAge().getAmount() == 0)
      {
        ds.dueReason = "birth";
      } else
      {
        ds.dueReason = ds.schedule.getDueAge() + " of age";
      }
      if (!ds.schedule.getDueInterval().isEmpty())
      {
        ds.log("Calculating due date from interval");
        DateTime dueInterval = ds.schedule.getDueInterval().getDateTimeFrom(ds.previousEventDate);
        if (dueInterval.isLessThan(ds.due))
        {
          ds.due = dueInterval;
          ds.dueReason = ds.schedule.getDueInterval() + " after previous dose";
          ds.log("Interval date is after age date, selecting interval date");
        }
      }
    }
    if (ds.seasonStart != null)
    {
      ds.log("Calculating season rules");
      if (ds.seasonStart.isGreaterThan(ds.valid))
      {
        ds.valid = new DateTime(ds.seasonStart);
        validReason = "at start of next season";
        validBecause = "SEASON";
        ds.log("Season start is is after valid date, moving valid date forward to " + ds.valid.toString("M/D/Y"));
      }
      DateTime seasonDue = ds.seasonal.getDue().getDateTimeFrom(ds.seasonStart);
      if (seasonDue.isGreaterThan(ds.due))
      {
        ds.due = seasonDue;
        ds.dueReason = ds.seasonal.getDue() + " after season start";
        ds.log("Season start is is after due date, moving due date forward to " + ds.due.toString("M/D/Y"));
      }
    }
    
    for (int i = 0; i < ds.blackOutDates.size(); i++) {
      ds.log("Looking to see if valid and due dates need to be adjusted around black out dates");
      DateTime[] blackOut = ds.blackOutDates.get(i);
      
      if (ds.valid.isGreaterThan(blackOut[0]) && ds.valid.isLessThan(blackOut[1]))
      {
        // valid date is in contraindication window, need to move backwards
        ds.valid = blackOut[1];
        validReason = ds.blackOutReasons.get(i);
        validBecause = "CONTRA";
        ds.log("Moving valid date to end of blackOut period, setting to " + ds.valid.toString("M/D/Y"));
      }
      if (ds.due.isGreaterThan(blackOut[0]) && ds.due.isLessThan(blackOut[1]))
      {
        ds.due = blackOut[1];
        ds.dueReason = ds.blackOutReasons.get(i);
        ds.log("Moving due date to end of blackOut period, setting to " + ds.due.toString("M/D/Y"));
      }
    }

    if (ds.schedule.getOverdueAge().isEmpty())
    {
      ds.overdue = ds.schedule.getOverdueInterval().getDateTimeFrom(ds.previousEventDate);
      ds.log("Setting overdue date for " + ds.schedule.getOverdueInterval() + " after previous dose");
    } else
    {
      ds.overdue = ds.schedule.getOverdueAge().getDateTimeFrom(ds.patient.getDobDateTime());
      ds.log("Setting overdue date for " + ds.schedule.getOverdueAge() + " of age");
      if (!ds.schedule.getOverdueInterval().isEmpty())
      {
        ds.log("Calculating at overdue interval");
        DateTime overdueInterval = ds.schedule.getOverdueInterval().getDateTimeFrom(ds.previousEventDate);
        if (overdueInterval.isGreaterThan(ds.overdue))
        {
          ds.overdue = overdueInterval;
          ds.log("Setting overdue date for " + ds.schedule.getOverdueInterval() + " after previous dose");
        }
      }
    }
    if (ds.seasonStart != null)
    {
      ds.log("Season started, looking to set season overdue");
      DateTime seasonOverdue = ds.seasonal.getOverdue().getDateTimeFrom(ds.seasonStart);
      if (seasonOverdue.isLessThan(ds.overdue))
      {
        ds.overdue = seasonOverdue;
        ds.log("Setting overdue to the end of the season");
      }
    }
    if (!ds.schedule.getEarlyAge().isEmpty())
    {
      ds.log("Setting early date to " + ds.schedule.getEarlyAge() + " of age");
      ds.early = ds.schedule.getEarlyAge().getDateTimeFrom(ds.patient.getDobDateTime());
    } else
    {
      ds.early = ds.due;
    }
    if (!ds.schedule.getEarlyInterval().isEmpty())
    {
      ds.log("Looking at early interval of " + ds.schedule.getEarlyInterval() + " to see if it is earlier");
      DateTime earlyInterval = ds.schedule.getEarlyInterval().getDateTimeFrom(ds.previousEventDate);
      if (earlyInterval.isLessThan(ds.early))
      {
        ds.log("Setting early to " + ds.early.toString("M/D/Y"));
        ds.early = earlyInterval;
      }
    }
    if (ds.early.isLessThan(ds.valid))
    {
      ds.early = new DateTime(ds.valid);
      ds.log("Adjusting early to not be before valid date");
    }
    if (ds.due.isLessThan(ds.valid))
    {
      ds.due = new DateTime(ds.valid);
      ds.dueReason = "same time as valid";
      ds.log("Adjusting due to not be before valid date");
    }
    if (ds.early.isGreaterThan(ds.due))
    {
      ds.early = ds.due;
      ds.log("Adjusting early to not be past due date");
    }
    if (ds.overdue.isLessThan(ds.due))
    {
      ds.overdue = new DateTime(ds.due);
      ds.log("Adjusting overdue to not be before due date");
    }
    
    if (ds.finished.isLessThan(ds.overdue))
    {
      ds.overdue = new DateTime(ds.finished);
      ds.log("Adjusting overdue date to not be after finished date");
      if (ds.overdue.isLessThan(ds.due))
      {
        ds.due = new DateTime(ds.overdue);
        ds.dueReason = "same time as over due";
        ds.log("Adjusting due date to not be after the over due date");
        if (ds.due.isLessThan(ds.early))
        {
          ds.early = new DateTime(ds.due);
          ds.log("Adjusting early date to not be after the due date");
          if (ds.valid.isLessThan(ds.early))
          {
            ds.valid = new DateTime(ds.early);
            ds.log("Vaccination is not valid to be administered before time has finished for vaccinating");
            validReason = "vaccine does not need to be administered";
          }
        }
      }
    }
    if (ds.traceBuffer != null)
    {
      ds.traceBuffer.append("Dose " + getNextValidDose(ds, ds.schedule) + " valid at " + validReason + ", "
          + ds.valid.toString("M/D/Y") + ". ");
    }
    if (ds.trace != null)
    {
      ds.trace.setDueDate(ds.due);
      ds.trace.setOverdueDate(ds.overdue);
      ds.trace.setValidDate(ds.valid);
      ds.trace.setFinishedDate(ds.finished);
      ds.trace.setValidReason("Dose " + getNextValidDose(ds, ds.schedule) + " valid at " + validReason + ", "
          + ds.valid.toString("M/D/Y") + ". ");
      ds.trace.setValidBecause(validBecause);
      ds.traceList.append("Dose " + getNextValidDose(ds, ds.schedule) + " valid at " + validReason + ", "
          + ds.valid.toString("M/D/Y") + ". ");
    }
  }
  
  protected static String getNextValidDose(DataStore ds, VaccineForecastDataBean.Schedule schedule)
  {
    String dose = schedule.getDose();
    if (dose.equals("*"))
    {
      dose = Integer.toString(ds.validDoseCount + 1);
    }
    return dose;
  }


}
