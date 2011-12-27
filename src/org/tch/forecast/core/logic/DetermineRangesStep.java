package org.tch.forecast.core.logic;

import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.hl7.core.util.DateTime;

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
    String validReason = "";
    String validBecause = "";
    if (ds.schedule.getValidAge().isEmpty())
    {
      ds.valid = ds.schedule.getValidInterval().getDateTimeFrom(ds.previousEventDateValid);
      validReason = ds.schedule.getValidInterval() + " after previous valid dose";
      validBecause = "INTERVAL";
    } else
    {
      ds.valid = ds.schedule.getValidAge().getDateTimeFrom(ds.patient.getDobDateTime());
      if (ds.schedule.getValidAge().getAmount() == 0)
      {
        validReason = "birth";
        validBecause = "AGE";
      } else
      {
        validReason = ds.schedule.getValidAge() + " of age";
        validBecause = "AGE";
      }
      if (!ds.schedule.getValidInterval().isEmpty())
      {
        DateTime validInterval = ds.schedule.getValidInterval().getDateTimeFrom(ds.previousEventDateValid);
        if (validInterval.isGreaterThan(ds.valid))
        {
          ds.valid = validInterval;
          validReason = ds.schedule.getValidInterval() + " after previous valid dose";
          validBecause = "INTERVAL";
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
        }
        ds.validGrace = ds.schedule.getAfterContraGrace();
      } else
      {
        DateTime validInterval = ds.schedule.getAfterInvalidInterval().getDateTimeFrom(ds.previousEventDate);
        if (validInterval.isGreaterThan(ds.valid))
        {
          ds.valid = validInterval;
          validReason = ds.schedule.getAfterInvalidInterval() + " after previous dose";
          validBecause = "INVALID";
        }
        ds.validGrace = ds.schedule.getAfterInvalidGrace();
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
      }
    }
    ds.dueReason = "";
    if (ds.schedule.getDueAge().isEmpty())
    {
      ds.due = ds.schedule.getDueInterval().getDateTimeFrom(ds.previousEventDate);
      ds.dueReason = ds.schedule.getDueInterval() + " after previous dose";
    } else
    {
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
        DateTime dueInterval = ds.schedule.getDueInterval().getDateTimeFrom(ds.previousEventDate);
        if (dueInterval.isLessThan(ds.due))
        {
          ds.due = dueInterval;
          ds.dueReason = ds.schedule.getDueInterval() + " after previous dose";
        }
      }
    }
    if (ds.seasonStart != null)
    {
      if (ds.seasonStart.isGreaterThan(ds.valid))
      {
        ds.valid = new DateTime(ds.seasonStart);
        validReason = "at start of next season";
        validBecause = "SEASON";
      }
      DateTime seasonDue = ds.seasonal.getDue().getDateTimeFrom(ds.seasonStart);
      if (seasonDue.isGreaterThan(ds.due))
      {
        ds.due = seasonDue;
        ds.dueReason = ds.seasonal.getDue() + " after season start";
      }
    }
    if (ds.schedule.getOverdueAge().isEmpty())
    {
      ds.overdue = ds.schedule.getOverdueInterval().getDateTimeFrom(ds.previousEventDate);
    } else
    {
      ds.overdue = ds.schedule.getOverdueAge().getDateTimeFrom(ds.patient.getDobDateTime());

      if (!ds.schedule.getOverdueInterval().isEmpty())
      {
        DateTime overdueInterval = ds.schedule.getOverdueInterval().getDateTimeFrom(ds.previousEventDate);
        if (overdueInterval.isGreaterThan(ds.overdue))
        {
          ds.overdue = overdueInterval;
        }
      }
    }
    if (ds.seasonStart != null)
    {
      DateTime seasonOverdue = ds.seasonal.getOverdue().getDateTimeFrom(ds.seasonStart);
      if (seasonOverdue.isLessThan(ds.overdue))
      {
        ds.overdue = seasonOverdue;
      }
    }
    if (!ds.schedule.getEarlyAge().isEmpty())
    {
      ds.early = ds.schedule.getEarlyAge().getDateTimeFrom(ds.patient.getDobDateTime());
    } else
    {
      ds.early = ds.due;
    }
    if (!ds.schedule.getEarlyInterval().isEmpty())
    {
      DateTime earlyInterval = ds.schedule.getEarlyInterval().getDateTimeFrom(ds.previousEventDate);
      if (earlyInterval.isLessThan(ds.early))
      {
        ds.early = earlyInterval;
      }
    }
    if (ds.early.isLessThan(ds.valid))
    {
      ds.early = new DateTime(ds.valid);
    }
    if (ds.due.isLessThan(ds.valid))
    {
      ds.due = new DateTime(ds.valid);
      ds.dueReason = "same time as valid";
    }
    if (ds.early.isGreaterThan(ds.due))
    {
      ds.early = ds.due;
    }
    if (ds.overdue.isLessThan(ds.due))
    {
      ds.overdue = new DateTime(ds.due);
    }
    if (ds.finished.isLessThan(ds.overdue))
    {
      ds.overdue = new DateTime(ds.finished);
      if (ds.overdue.isLessThan(ds.due))
      {
        ds.due = new DateTime(ds.overdue);
        ds.dueReason = "same time as over due";
        if (ds.due.isLessThan(ds.early))
        {
          ds.early = new DateTime(ds.due);
          if (ds.valid.isLessThan(ds.early))
          {
            ds.valid = new DateTime(ds.early);
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
