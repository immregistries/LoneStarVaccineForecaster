package org.tch.forecast.core.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.Trace;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.hl7.core.util.DateTime;

public class LookForDoseStep extends ActionStep
{
  public static final String NAME = "Look For Dose";

  private static final String COMPLETE = "COMPLETE";
  private static final String KEEP_LOOKING = "KEEP LOOKING";
  private static final String CONTRA = "CONTRA";
  private static final String INVALID = "INVALID";

  @Override
  public String getName()
  {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception
  {
    VaccineForecastDataBean.Indicate indicate = ds.indicates[ds.indicatesPos];
    ds.log("Looking for next dose");
    ds.nextAction = lookForDose(ds, indicate);
    ds.log("Next action = " + ds.nextAction);
    if (ds.nextAction == null || ds.nextAction.equalsIgnoreCase(COMPLETE)
        || (ds.nextAction.equalsIgnoreCase(KEEP_LOOKING) && (ds.indicatesPos + 1) == ds.indicates.length))
    {
      if (ds.traceBuffer != null)
      {
        if (ds.nextAction != null)
        {
          if (ds.nextAction.equalsIgnoreCase(COMPLETE))
          {
            ds.traceBuffer.append("</li><li>Vaccination series complete, patient vaccinated.");
          }
        }
      }
      if (ds.trace != null)
      {
        if (ds.nextAction != null)
        {
          if (ds.nextAction.equalsIgnoreCase(COMPLETE))
          {
            ds.trace.setComplete(true);
            ds.traceList.append("</li><li>Vaccination series complete, patient vaccinated.");
          }
        }
      }
      if (ds.nextAction == null || ds.nextAction.equalsIgnoreCase(KEEP_LOOKING))
      {
        ds.log("Time to make forcast recommendations");
        return MakeForecastStep.NAME;
      }
      ds.log("Schedule is finished, no more recommendations.");
      return FinishScheduleStep.NAME;
    } else if (ds.nextAction.equalsIgnoreCase(KEEP_LOOKING))
    {
      ds.log("Dose found was past cutoff for this indicator, need to look at the next indicator");
      return ChooseIndicatorStep.NAME;
    } else if (ds.nextAction.equalsIgnoreCase(CONTRA))
    {
      ds.log("Schedule was contraindicated, same schedule is kept.");
      if (ds.traceBuffer != null)
      {
        ds.traceBuffer.append("</li><li>");
      }
      if (ds.trace != null)
      {
        ds.trace.setContraindicated(true);
        ds.trace = new Trace();
        ds.traceList.add(ds.trace);
        ds.traceList.append("</li><li>");
      }
      return TraverseScheduleStep.NAME;
    } else if (ds.nextAction.equalsIgnoreCase(INVALID))
    {
      ds.log("Dose was invalid for schedule, same schedule to be kept.");
      if (ds.traceBuffer != null)
      {
        ds.traceBuffer.append("</li><li>");
      }
      if (ds.trace != null)
      {
        ds.trace.setInvalid(true);
        ds.trace = new Trace();
        ds.traceList.add(ds.trace);
        ds.traceList.append("</li><li>");
      }
      return TraverseScheduleStep.NAME;
    } else
    {
      ds.log("Moving to schedule " + ds.nextAction);
      return TransitionScheduleStep.NAME;
    }
  }

  protected static void nextEvent(DataStore ds)
  {
    if (ds.eventPosition < ds.eventList.size())
    {
      ds.event = ds.eventList.get(ds.eventPosition);
      ds.event.hasEvent = false;
      setHasEvent(ds);
      ds.eventPosition++;
    } else
    {
      ds.event = null;
    }
  }

  private static void setHasEvent(DataStore ds)
  {
    VaccineForecastDataBean.Indicate[] ind = ds.schedule.getIndicates();
    for (int i = 0; i < ind.length; i++)
    {
      int[] vaccineIds = ind[i].getVaccines();
      for (int j = 0; j < vaccineIds.length; j++)
      {
        for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();)
        {
          ImmunizationInterface imm = it.next();
          if (vaccineIds[j] == imm.getVaccineId())
          {
            ds.event.hasEvent = true;
            return;
          }
        }
      }
    }
  }

  private String lookForDose(DataStore ds, VaccineForecastDataBean.Indicate indicate)
  {
    int[] vaccineIds = indicate.getVaccines();
    while (ds.event != null)
    {
      if (ds.event.hasEvent)
      {
        DateTime vaccDate = new DateTime(ds.event.eventDate);
        if (!indicatedEvent(ds, indicate, vaccineIds))
        {
          ds.log("Not indicated event, must keep looking.");
          return KEEP_LOOKING;
        }
        String invalidReason = null;
        if (checkSeasonEnd(ds, ds.event))
        {
          ds.log("Season end reached");
          if (ds.seasonCompleted)
          {
            ds.log("Season completed.");
            if (ds.traceBuffer != null)
            {
              ds.traceBuffer.append("Season ended " + DataStore.dateFormat.format(ds.event.eventDate) + ". ");
            }
          } else if (ds.event.eventDate.before(ds.valid.getDate()))
          {
            ds.log("Season ended before dose could be administered");
            if (ds.traceBuffer != null)
            {
              ds.traceBuffer.append("Season ended " + DataStore.dateFormat.format(ds.event.eventDate)
                  + " before next dose was valid to give. ");
            }
            if (ds.trace != null)
            {
              ds.traceList.append("Season ended " + DataStore.dateFormat.format(ds.event.eventDate)
                  + " before next dose was valid to give. ");
            }
          } else
          {
            ds.log("Season ended before expected dose received.");
            if (ds.traceBuffer != null)
            {
              ds.traceBuffer.append("Season ended " + DataStore.dateFormat.format(ds.event.eventDate)
                  + " without valid dose given. ");
            }
            if (ds.trace != null)
            {
              ds.traceList.append("Season ended " + DataStore.dateFormat.format(ds.event.eventDate)
                  + " without valid dose given. ");
            }
          }
          ds.seasonCompleted = false;
          if (ds.traceBuffer != null && !indicate.getReason().equals(""))
          {
            ds.traceBuffer.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          if (ds.trace != null && !indicate.getReason().equals(""))
          {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          ds.seasonStart = ds.seasonal.getStart().getDateTimeFrom(new DateTime(ds.event.eventDate));
          if (ds.seasonEnd == null)
          {
            ds.seasonEnd = SetupScheduleStep.setupSeasonEnd(ds);
          }
          nextEvent(ds);
          return indicate.getScheduleName();
        } else if ((invalidReason = checkInvalid(ds, vaccDate)) != null)
        {
          ds.log("Dose is invalid.");
          addInvalidDose(ds, vaccineIds, invalidReason);
          addPreviousDose(ds, vaccineIds);
          ds.previousEventDate = vaccDate;
          ds.previousEventWasContra = false;
          DetermineRangesStep.determineRanges(ds);
          nextEvent(ds);
          ds.previousAfterInvalidInterval = ds.schedule.getAfterInvalidInterval();
          return INVALID;
        } else if (indicate.isInvalid())
        {
          ds.log("Indicator says dose is invalid");
          addInvalidDose(ds, vaccineIds, indicate.getVaccineName() + " dose "
              + (indicate.getAge().isEmpty() ? "" : indicate.getAge().toString()));
          addPreviousDose(ds, vaccineIds);
          if (ds.traceBuffer != null && !indicate.getReason().equals(""))
          {
            ds.traceBuffer.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          if (ds.trace != null)
          {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          ds.previousEventDate = vaccDate;
          ds.previousEventWasContra = false;
          DetermineRangesStep.determineRanges(ds);
          nextEvent(ds);
          ds.previousAfterInvalidInterval = ds.schedule.getAfterInvalidInterval();
          return INVALID;
        } else if (indicate.isContra())
        {
          ds.log("Dose is contraindication.");
          addContra(ds, vaccineIds, indicate.getVaccineName() + " dose"
              + (indicate.getAge().isEmpty() ? "" : " given before " + indicate.getAge().toString()));
          addPreviousDose(ds, vaccineIds);
          if (ds.traceBuffer != null && !indicate.getReason().equals(""))
          {
            ds.traceBuffer.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          if (ds.trace != null)
          {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          ds.previousEventDate = vaccDate;
          ds.previousEventWasContra = true;
          DetermineRangesStep.determineRanges(ds);
          nextEvent(ds);
          return CONTRA;
        } else
        {
          ds.log("Valid dose.");
          ds.validDoseCount++;
          addValidDose(ds, vaccineIds);
          addPreviousDose(ds, vaccineIds);
          if (ds.traceBuffer != null && !indicate.getReason().equals(""))
          {
            ds.traceBuffer.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          if (ds.trace != null)
          {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          ds.beforePreviousEventDate = ds.previousEventDateValid;
          ds.previousEventDateValid = vaccDate;
          ds.previousEventWasContra = true;
          ds.previousEventDate = vaccDate;
          if (ds.seasonal != null && indicate.isSeasonCompleted())
          {
            ds.seasonCompleted = true;
            if (ds.traceBuffer != null)
            {
              ds.traceBuffer.append("Season completed. ");
            }
          }
          nextEvent(ds);
          return indicate.getScheduleName();
        }
      }
      nextEvent(ds);
    }
    return null;
  }

  protected boolean indicatedEvent(DataStore ds, VaccineForecastDataBean.Indicate indicate, int[] vaccineIds)
  {
    return indicatedEventVaccine(ds, vaccineIds) && indicatedEventAfterPrevious(ds, indicate)
        && indicatedHasHistory(ds, indicate) && indicatedEventWithinDateRange(ds, indicate);
  }

  protected boolean indicatedEventWithinDateRange(DataStore ds, VaccineForecastDataBean.Indicate indicate)
  {
    DateTime vaccDate = new DateTime(new DateTime(ds.event.eventDate).toString("M/D/Y"));
    DateTime minDate = figureMinDate(ds, indicate);
    DateTime maxDate = figureMaxDate(ds, indicate);
    if (minDate == null && maxDate == null)
    {
      return true;
    }
    ds.log(" + Vacc date = " + vaccDate);
    if (minDate != null)
    {
      minDate = new DateTime(new DateTime(minDate.toString("M/D/Y")));
      ds.log(" + Indicated minimum date = " + minDate);
      if (vaccDate.isLessThan(minDate))
      {
        ds.log(" + Vaccination given too early for indicated event");
        return false;
      }
    }
    if (maxDate != null)
    {
      maxDate = new DateTime(new DateTime(maxDate.toString("M/D/Y")));
      ds.log(" + Indicated maximum date = " + maxDate);
      if (vaccDate.isGreaterThanOrEquals(maxDate))
      {
        ds.log(" + Vaccination given too late for indicated event");
        return false;
      }
    }
    ds.log(" + Vaccination within indicated range");
    return true;
  }

  protected boolean indicatedEventAfterPrevious(DataStore ds, VaccineForecastDataBean.Indicate indicate)
  {
    boolean foundPreviousMatch = true;
    if (indicate.getPreviousVaccines() != null && indicate.getPreviousVaccines().length > 0)
    {
      foundPreviousMatch = false;
      if (ds.getPreviousVaccineIdList() != null)
      {
        for (int previousVaccineId : ds.getPreviousVaccineIdList())
        {
          for (int indicatedVaccineId : indicate.getPreviousVaccines())
          {
            if (previousVaccineId == indicatedVaccineId)
            {
              foundPreviousMatch = true;
              break;
            }
          }
        }
      }
      if (foundPreviousMatch)
      {
        ds.log(" + Indicated event after previous event of " + indicate.getPreviousVaccineName());
      } else
      {
        ds.log(" + Indicated event NOT after previous event of " + indicate.getPreviousVaccineName());
      }
    }
    return foundPreviousMatch;
  }

  private boolean indicatedHasHistory(DataStore ds, VaccineForecastDataBean.Indicate indicate)
  {
    boolean foundHistory = true;
    if (indicate.getHistoryOfVaccineName() != null && indicate.getHistoryOfVaccineName().length() > 0)
    {
      foundHistory = false;
      for (int indicatedVaccineId : indicate.getPreviousVaccines())
      {
        if (ds.getPreviousVaccineIdHistory().contains(indicatedVaccineId))
        {
          foundHistory = true;
          break;
        }
      }
      if (foundHistory)
      {
        ds.log("Indicated event has history of " + indicate.getHistoryOfVaccineName());
      } else
      {
        ds.log("Indicated event does NOT have history of " + indicate.getHistoryOfVaccineName());
      }
    }
    return foundHistory;
  }

  private DateTime figureMinDate(DataStore ds, VaccineForecastDataBean.Indicate indicate)
  {
    if (!indicate.getMinInterval().isEmpty())
    {
      return indicate.getMinInterval().getDateTimeFrom(ds.previousEventDate);
    }
    return null;
  }

  private DateTime figureMaxDate(DataStore ds, VaccineForecastDataBean.Indicate indicate)
  {
    DateTime cutoff = null;
    if (!indicate.getAge().isEmpty())
    {
      cutoff = indicate.getAge().getDateTimeFrom(ds.patient.getDobDateTime());
    }
    DateTime cutoffInterval = null;
    if (!indicate.getMaxInterval().isEmpty())
    {
      cutoffInterval = indicate.getMaxInterval().getDateTimeFrom(ds.previousEventDate);
    }
    if (cutoff == null)
    {
      cutoff = cutoffInterval;
    } else if (cutoffInterval != null)
    {
      if (cutoffInterval.isLessThan(cutoff))
      {
        cutoff = cutoffInterval;
      }
    }
    return cutoff;
  }

  protected static boolean indicatedEventVaccine(DataStore ds, int[] vaccineIds)
  {
    Event event = ds.event;
    boolean iev = indicatedEventVaccine(vaccineIds, event);
    if (iev)
    {
      ds.log(" + Indicated vaccine event");
    } else
    {
      ds.log(" + Not indicated vaccine event");
    }
    return iev;
  }

  protected static boolean indicatedEventVaccine(int[] vaccineIds, Event event)
  {
    boolean indicatedEvent = false;
    for (Iterator<ImmunizationInterface> it = event.immList.iterator(); it.hasNext();)
    {
      ImmunizationInterface imm = it.next();
      for (int i = 0; i < vaccineIds.length; i++)
      {
        if (imm.getVaccineId() == vaccineIds[i])
        {
          indicatedEvent = true;
        }
      }
    }
    return indicatedEvent;
  }

  private boolean checkSeasonEnd(DataStore ds, Event event)
  {
    if (ds.seasonal != null)
    {
      for (Iterator<ImmunizationInterface> it = event.immList.iterator(); it.hasNext();)
      {
        ImmunizationInterface imm = it.next();
        if (imm instanceof SeasonEndEvent)
        {
          ds.seasonCompleted = false;
          return true;
        }
      }
    }
    return false;
  }

  private String checkInvalid(DataStore ds, DateTime vaccDate)
  {
    if (ds.validGrace.isEmpty())
    {
      if (vaccDate.isLessThan(ds.valid))
      {
        return "before valid date";
      }
    } else
    {
      DateTime dt = ds.schedule.getValidGrace().getDateTimeFrom(vaccDate);
      if (dt.isLessThan(ds.valid))
      {
        return "before valid date";
      }
    }
    // Adjust around black out dates
    if (ds.blackOutDates != null && ds.blackOutDates.size() > 0)
    {
      ds.log("Checking validity against black out dates");
      int i = -1;
      for (DateTime[] blackOut : ds.blackOutDates)
      {
        i++;
        if (vaccDate.isGreaterThan(blackOut[0]) && vaccDate.isLessThan(blackOut[1]))
        {
          return ds.blackOutReasons.get(i);
        }
      }
    }
    return null;
  }

  private void addPreviousDose(DataStore ds, int[] vaccineIds)
  {
    List<Integer> previousVaccineIdList = new ArrayList<Integer>();
    ds.setPreviousVaccineIdList(previousVaccineIdList);
    for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();)
    {
      ImmunizationInterface imm = it.next();
      for (int i = 0; i < vaccineIds.length; i++)
      {
        if (imm.getVaccineId() == vaccineIds[i])
        {
          previousVaccineIdList.add(imm.getVaccineId());
          ds.getPreviousVaccineIdHistory().add(imm.getVaccineId());
        }
      }
    }
  }

  private void addInvalidDose(DataStore ds, int[] vaccineIds, String invalidReason)
  {
    if (!getValidDose(ds, ds.schedule).equals(""))
    {
      for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();)
      {
        ImmunizationInterface imm = it.next();
        for (int i = 0; i < vaccineIds.length; i++)
        {
          if (imm.getVaccineId() == vaccineIds[i])
          {
            VaccinationDoseDataBean dose = new VaccinationDoseDataBean();
            dose.setAdminDate(imm.getDateOfShot());
            dose.setDoseCode(getValidDose(ds, ds.schedule));
            dose.setImmregid(ds.patient.getImmregid());
            dose.setForecastCode(ds.forecast.getForecastCode());
            dose.setScheduleCode(ds.schedule.getScheduleName());
            dose.setStatusCode(VaccinationDoseDataBean.STATUS_INVALID);
            dose.setVaccineId(imm.getVaccineId());
            dose.setReason((ds.forecastManager.getVaccineName(imm.getVaccineId()) + (" given " + DataStore.dateFormat
                .format(imm.getDateOfShot()))) + " is invalid " + invalidReason + "");
            ds.doseList.add(dose);
            if (ds.traceBuffer != null)
            {
              ds.traceBuffer.append(" <font color=\"#FF0000\">" + dose.getReason() + ".</font> ");
            }
            if (ds.trace != null)
            {
              ds.trace.getDoses().add(dose);
              ds.traceList.append(" <font color=\"#FF0000\">" + dose.getReason() + ".</font> ");
            }
          }
        }
      }
    }
  }

  private String getValidDose(DataStore ds, VaccineForecastDataBean.Schedule schedule)
  {
    String dose = schedule.getDose();
    if (dose.equals("*"))
    {
      dose = Integer.toString(ds.validDoseCount);
    }
    return dose;
  }

  private void addContra(DataStore ds, int[] vaccineIds, String contraReason)
  {
    if (ds.traceBuffer != null)
    {
      if (!getValidDose(ds, ds.schedule).equals(""))
      {
        for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();)
        {
          ImmunizationInterface imm = it.next();
          for (int i = 0; i < vaccineIds.length; i++)
          {
            if (imm.getVaccineId() == vaccineIds[i])
            {
              if (ds.traceBuffer != null)
              {
                ds.traceBuffer.append(" <font color=\"#FF0000\">");
                ds.traceBuffer.append(ds.forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                    + DataStore.dateFormat.format(imm.getDateOfShot()));
                ds.traceBuffer.append(" is a contraindicated ");
                ds.traceBuffer.append(contraReason);
                ds.traceBuffer.append(".</font> ");
              }
              if (ds.trace != null)
              {
                ds.traceList.append(" <font color=\"#FF0000\">");
                ds.traceList.append(ds.forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                    + DataStore.dateFormat.format(imm.getDateOfShot()));
                ds.traceList.append(" is a contraindicated ");
                ds.traceList.append(contraReason);
                ds.traceList.append(".</font> ");
              }
            }
          }
        }
      }
    }
  }

  private void addValidDose(DataStore ds, int[] vaccineIds)
  {
    if (!getValidDose(ds, ds.schedule).equals(""))
    {
      for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();)
      {
        ImmunizationInterface imm = it.next();
        for (int i = 0; i < vaccineIds.length; i++)
        {
          if (imm.getVaccineId() == vaccineIds[i])
          {
            VaccinationDoseDataBean dose = new VaccinationDoseDataBean();
            dose.setAdminDate(imm.getDateOfShot());
            dose.setDoseCode(getValidDose(ds, ds.schedule));
            dose.setImmregid(ds.patient.getImmregid());
            dose.setForecastCode(ds.forecast.getForecastCode());
            dose.setScheduleCode(ds.schedule.getScheduleName());
            dose.setStatusCode(VaccinationDoseDataBean.STATUS_VALID);
            dose.setVaccineId(imm.getVaccineId());
            ds.doseList.add(dose);
            if (ds.traceBuffer != null)
            {
              ds.traceBuffer.append(" <font color=\"#0000FF\">");
              ds.traceBuffer.append(ds.forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                  + DataStore.dateFormat.format(imm.getDateOfShot()));
              ds.traceBuffer.append(" is valid (dose #");
              ds.traceBuffer.append(ds.validDoseCount);
              ds.traceBuffer.append(").</font> ");
            }
            if (ds.trace != null)
            {
              ds.trace.getDoses().add(dose);
              ds.traceList.append(" <font color=\"#0000FF\">");
              ds.traceList.append(ds.forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                  + DataStore.dateFormat.format(imm.getDateOfShot()));
              ds.traceList.append(" is valid (dose #");
              ds.traceList.append(String.valueOf(ds.validDoseCount));
              ds.traceList.append(").</font> ");
            }
          }
        }
      }
    }
  }

}
