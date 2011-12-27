package org.tch.forecast.core.logic;

import java.util.Iterator;

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
    ds.nextAction = lookForDose(ds, indicate);
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
        // Evaluation finished without COMPLETING so generate forecast
        // recommendations
        return MakeForecastStep.NAME;
      }
      return FinishScheduleStep.NAME;
    } else if (ds.nextAction.equalsIgnoreCase(KEEP_LOOKING))
    {
      // Dose found was past cutoff for this indicator, need to look at next
      // one
      return ChooseIndicatorStep.NAME;
    } else if (ds.nextAction.equalsIgnoreCase(CONTRA))
    {
      // Schedule was contraindicated, same schedule is kept
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
      // Dose was invalid for schedule, same schedule to be kept
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
        DateTime cutoff = figureCutoff(ds, indicate);
        DateTime vaccDate = new DateTime(ds.event.eventDate);
        if (!indicatedEvent(ds, vaccineIds))
        {
          return KEEP_LOOKING;
        }
        if (cutoff != null && !vaccDate.isLessThan(cutoff))
        {
          return KEEP_LOOKING;
        }
        if (checkSeasonEnd(ds, ds.event))
        {
          if (ds.seasonCompleted)
          {
            if (ds.traceBuffer != null)
            {
              ds.traceBuffer.append("Season ended " + DataStore.dateFormat.format(ds.event.eventDate) + ". ");
            }
          } else if (ds.event.eventDate.before(ds.valid.getDate()))
          {
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
        } else if (checkInvalid(ds, vaccDate))
        {
          addInvalidDose(ds, vaccineIds, "before valid date");
          ds.previousEventDate = vaccDate;
          ds.previousEventWasContra = false;
          DetermineRangesStep.determineRanges(ds);
          nextEvent(ds);
          ds.previousAfterInvalidInterval = ds.schedule.getAfterInvalidInterval();
          return INVALID;
        } else if (indicate.isInvalid())
        {
          addInvalidDose(ds, vaccineIds, indicate.getVaccineName() + " dose "
              + (indicate.getAge().isEmpty() ? "" : indicate.getAge().toString()));
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
          addContra(ds, vaccineIds, indicate.getVaccineName() + " dose"
              + (indicate.getAge().isEmpty() ? "" : " given before " + indicate.getAge().toString()));
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
          ds.validDoseCount++;
          addValidDose(ds, vaccineIds);
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

  private DateTime figureCutoff(DataStore ds, VaccineForecastDataBean.Indicate indicate)
  {
    DateTime cutoff = null;
    if (!indicate.getAge().isEmpty())
    {
      cutoff = indicate.getAge().getDateTimeFrom(ds.patient.getDobDateTime());
    }
    DateTime cutoffInterval = null;
    if (!indicate.getMinInterval().isEmpty())
    {
      cutoffInterval = indicate.getMinInterval().getDateTimeFrom(ds.previousEventDate);
    }
    if (cutoff == null)
    {
      cutoff = cutoffInterval;
    } else if (cutoffInterval != null)
    {
      if (cutoffInterval.isGreaterThan(cutoff))
      {
        cutoff = cutoffInterval;
      }
    }
    return cutoff;
  }

  private boolean indicatedEvent(DataStore ds, int[] vaccineIds)
  {
    boolean indicatedEvent = false;
    for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();)
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

  private boolean checkInvalid(DataStore ds, DateTime vaccDate)
  {
    if (ds.validGrace.isEmpty())
    {
      return vaccDate.isLessThan(ds.valid);
    } else
    {
      DateTime dt = ds.schedule.getValidGrace().getDateTimeFrom(vaccDate);
      return dt.isLessThan(ds.valid);
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
