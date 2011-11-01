package org.tch.forecast.core;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.VaccineForecastDataBean.Seasonal;
import org.tch.hl7.core.util.DateTime;

public class Forecaster
{

  private static final String COMPLETE = "COMPLETE";

  private static final String KEEP_LOOKING = "PAST_CUTOFF";
  private static final String CONTRA = "CONTRA";
  private static final String INVALID = "INVALID";

  public static final int VARICELLA_HISTORY = 378;

  private PatientForecastRecordDataBean patient = null;
  private List eventList = null;
  private List originalEventList = null;
  private DateTime previousEventDate;
  private DateTime previousEventDateValid;
  private boolean previousEventWasContra = false;
  private TimePeriod previousAfterInvalidInterval;
  private DateTime beforePreviousEventDate;
  private VaccineForecastDataBean forecast = null;
  private VaccineForecastDataBean.Schedule schedule;
  private VaccineForecastDataBean.Indicate[] indicates = null;
  private DateTime valid = null;
  private TimePeriod validGrace = null;
  private DateTime early = null;
  private DateTime due = null;
  private String dueReason = "";
  private DateTime overdue = null;
  private DateTime finished = null;
  private DateTime today = null;
  private List resultList = null;
  private List doseList = null;
  private Event event = null;
  private int eventPosition = 0;
  private int validDoseCount = 0;
  private Trace trace = null;
  private TraceList traceList = null;
  private Map traces = null;
  private Date forecastDate = new Date();
  private StringBuffer traceBuffer = null;
  boolean hasHistoryOfVaricella = false;
  private Seasonal seasonal = null;
  private boolean seasonCompleted = false;
  private DateTime seasonStart = null;
  private DateTime seasonEnd = null;

  private VaccineForecastManagerInterface forecastManager = null;

  public Forecaster(VaccineForecastManagerInterface forecastManager) {
    this.forecastManager = forecastManager;
  }

  public List forecast(List resultList, List doseList, StringBuffer traceBuffer, Map traces) throws Exception
  {
    this.resultList = resultList;
    this.doseList = doseList;
    this.traceBuffer = traceBuffer;
    this.traces = traces;
    this.today = new DateTime(forecastDate);
    forecastForAllIndications("BIRTH");
    if (patient.getSex() == null || !patient.getSex().equals("M"))
    {
      forecastForAllIndications("FEMALE");
    }
    if (!hasHistoryOfVaricella)
    {
      forecastForAllIndications("NO-VAR-HIS");
    }
    this.traceBuffer = null;
    this.traces = null;
    return resultList;
  }

  private void forecastForAllIndications(String indication) throws Exception, Exception
  {
    List vaccineForecastList = forecastManager.getIndications(indication);
    if (vaccineForecastList == null)
    {
      System.out.println("No schedules found for indication '" + indication + "'");
    } else
    {
      for (Iterator fit = vaccineForecastList.iterator(); fit.hasNext();)
      {
        schedule = (VaccineForecastDataBean.Schedule) fit.next();
        try
        {
          forecast = schedule.getVaccineForecast();
          previousEventDate = new DateTime(patient.getDobDateTime());
          previousEventDateValid = previousEventDate;
          beforePreviousEventDate = null;
          validDoseCount = 0;

          setupSeasonal();
          if (traceBuffer != null)
          {
            traceBuffer.append("<p><b>" + forecast.getForecastCode() + "</b></p><ul><li>");
          }
          if (traces != null)
          {
            trace = new Trace();
            trace.setSchedule(schedule);
            traceList = new TraceList();
            traceList.add(trace);
            traceList.setForecastName(forecast.getForecastCode());
            traceList.setForecastLabel(forecast.getForecastLabel());
            traces.put(forecast.getForecastCode(), traceList);
            traceList.append("<ul><li>");
          }
          eventPosition = 0;
          previousAfterInvalidInterval = null;
          nextEvent();

          traverseSchedules();
          if (traceBuffer != null)
          {
            traceBuffer.append("</li></ul>");
          }
          if (traces != null)
          {
            traceList.append("</li></ul>");
          }
        } catch (Exception e)
        {
          throw new Exception("Unable to forecast for schedule " + schedule.getScheduleName() + " because " + e.getMessage(), e);
        } finally
        {
          finishSeasonal();
        }
      }
    }
  }

  private void finishSeasonal()
  {
    if (seasonal != null)
    {
      eventList = originalEventList;
      originalEventList = null;
      seasonal = null;
      seasonStart = null;
      seasonEnd = null;
    }
  }

  private void setupSeasonal()
  {
    seasonal = forecast.getSeasonal();
    if (seasonal != null)
    {
      seasonCompleted = false;
      originalEventList = eventList;
      seasonEnd = setupSeasonEnd();

      int count = 0;
      while (seasonEnd.isGreaterThanOrEquals(patient.getDobDateTime()) && count < 10)
      {
        SeasonEndEvent se = new SeasonEndEvent(seasonEnd.getDate());
        event = new Event();
        event.eventDate = se.getDateOfShot();
        event.immList.add(se);
        eventList.add(event);
        count++;
        seasonEnd.addYears(-1);
      }
      if (count == 0)
      {
        // If no seasonal events were added then put in a season start for
        // this year so that first forecast is good
        seasonStart = seasonal.getStart().getDateTimeFrom(seasonEnd);
      }

      Collections.sort(eventList, new Comparator() {
        public int compare(Object o1, Object o2)
        {
          Event event1 = (Event) o1;
          Event event2 = (Event) o2;
          return event1.eventDate.compareTo(event2.eventDate);
        }
      });
    }
  }

  private DateTime setupSeasonEnd()
  {
    DateTime seasonEnd = new DateTime(forecastDate);
    seasonEnd.setMonth(1);
    seasonEnd.setDay(1);
    seasonEnd = seasonal.getEnd().getDateTimeFrom(seasonEnd);
    if (seasonEnd.isGreaterThanOrEquals(new DateTime(forecastDate)))
    {
      seasonEnd.addYears(-1);
    }
    return seasonEnd;
  }

  private void traverseSchedules() throws Exception
  {
    while (schedule != null)
    {
      determineRanges();
      indicates = schedule.getIndicates();
      for (int j = 0; j < indicates.length; j++)
      {
        VaccineForecastDataBean.Indicate indicate = indicates[j];
        String nextAction = lookForDose(indicate);
        if (nextAction == null || nextAction.equalsIgnoreCase(COMPLETE)
            || (nextAction.equalsIgnoreCase(KEEP_LOOKING) && (j + 1) == indicates.length))
        {
          if (traceBuffer != null)
          {
            if (nextAction != null)
            {
              if (nextAction.equalsIgnoreCase(COMPLETE))
              {
                traceBuffer.append("</li><li>#1 Vaccination series complete, patient vaccinated.");
              }
            }
          }
          if (trace != null)
          {
            if (nextAction != null)
            {
              if (nextAction.equalsIgnoreCase(COMPLETE))
              {
                trace.setComplete(true);
                traceList.append("</li><li>Vaccination series complete, patient vaccinated.");
              }
            }
          }
          return;
        } else if (nextAction.equalsIgnoreCase(KEEP_LOOKING))
        {
          // Dose found was past cutoff for this indicator, need to look at next
          // one
          continue;
        } else if (nextAction.equalsIgnoreCase(CONTRA))
        {
          // Schedule was contraindicated, same schedule is kept
          if (traceBuffer != null)
          {
            traceBuffer.append("</li><li>");
          }
          if (trace != null)
          {
            trace.setContraindicated(true);
            trace = new Trace();
            traceList.add(trace);
            traceList.append("</li><li>");
          }
          break;
        } else if (nextAction.equalsIgnoreCase(INVALID))
        {
          // Dose was invalid for schedule, same schedule to be kept
          if (traceBuffer != null)
          {
            traceBuffer.append("</li><li>");
          }
          if (trace != null)
          {
            trace.setInvalid(true);
            trace = new Trace();
            traceList.add(trace);
            traceList.append("</li><li>");
          }
          break;
        } else
        {
          // A different schedule is the new action
          schedule = (VaccineForecastDataBean.Schedule) forecast.getSchedules().get(nextAction);
          previousAfterInvalidInterval = null;
          if (schedule == null)
          {
            throw new Exception("Unable to find schedule " + nextAction);
          }
          if (traceBuffer != null)
          {
            String label = schedule.getLabel();
            if (label == null || label.equals(""))
            {
              label = "[Schedule " + schedule.getScheduleName() + "]";
            }
            traceBuffer.append("Now expecting " + label + " dose.</li><li>");
          }
          if (trace != null)
          {
            trace = new Trace();
            trace.setSchedule(schedule);
            traceList.add(trace);
            String label = schedule.getLabel();
            if (label == null || label.equals(""))
            {
              label = "[Schedule " + schedule.getScheduleName() + "]";
            }
            traceList.append("Now expecting " + label + " dose.</li><li>");
          }
          finished = schedule.getFinishedAge().getDateTimeFrom(patient.getDobDateTime());
          if (today.isGreaterThan(finished))
          {
            if (traceBuffer != null)
            {
              traceBuffer.append("</li><li>No need for further vaccinations.");
            }
            if (trace != null)
            {
              trace.setFinished(true);
              traceList.append("</li><li>No need for further vaccinations.");
            }
            return;
          }
          break;
        }
      }
    }
  }

  private String lookForDose(VaccineForecastDataBean.Indicate indicate)
  {
    int[] vaccineIds = indicate.getVaccines();
    while (event != null)
    {
      if (event.hasEvent)
      {
        DateTime cutoff = figureCutoff(indicate);
        DateTime vaccDate = new DateTime(event.eventDate);
        if (!indicatedEvent(vaccineIds))
        {
          return KEEP_LOOKING;
        }
        if (cutoff != null && !vaccDate.isLessThan(cutoff))
        {
          return KEEP_LOOKING;
        }
        if (checkSeasonEnd(event))
        {
          if (seasonCompleted)
          {
            if (traceBuffer != null)
            {
              traceBuffer.append("Season ended " + dateFormat.format(event.eventDate) + ". ");
            }
          } else if (event.eventDate.before(valid.getDate()))
          {
            if (traceBuffer != null)
            {
              traceBuffer.append("Season ended " + dateFormat.format(event.eventDate)
                  + " before next dose was valid to give. ");
            }
            if (trace != null)
            {
              traceList.append("Season ended " + dateFormat.format(event.eventDate)
                  + " before next dose was valid to give. ");
            }
          } else
          {
            if (traceBuffer != null)
            {
              traceBuffer.append("Season ended " + dateFormat.format(event.eventDate) + " without valid dose given. ");
            }
            if (trace != null)
            {
              traceList.append("Season ended " + dateFormat.format(event.eventDate) + " without valid dose given. ");
            }
          }
          seasonCompleted = false;
          if (traceBuffer != null && !indicate.getReason().equals(""))
          {
            traceBuffer.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          if (trace != null && !indicate.getReason().equals(""))
          {
            trace.setReason(indicate.getReason());
            traceList.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          seasonStart = seasonal.getStart().getDateTimeFrom(new DateTime(event.eventDate));
          if (seasonEnd == null)
          {
            seasonEnd = setupSeasonEnd();
          }
          nextEvent();
          return indicate.getScheduleName();
        } else if (checkInvalid(vaccDate))
        {
          addInvalidDose(vaccineIds, "before valid date");
          previousEventDate = vaccDate;
          previousEventWasContra = false;
          determineRanges();
          nextEvent();
          previousAfterInvalidInterval = schedule.getAfterInvalidInterval();
          return INVALID;
        } else if (indicate.isInvalid())
        {
          addInvalidDose(vaccineIds, indicate.getVaccineName() + " dose "
              + (indicate.getAge().isEmpty() ? "" : indicate.getAge().toString()));
          if (traceBuffer != null && !indicate.getReason().equals(""))
          {
            traceBuffer.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          if (trace != null)
          {
            trace.setReason(indicate.getReason());
            traceList.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          previousEventDate = vaccDate;
          previousEventWasContra = false;
          determineRanges();
          nextEvent();
          previousAfterInvalidInterval = schedule.getAfterInvalidInterval();
          return INVALID;
        } else if (indicate.isContra())
        {
          addContra(vaccineIds, indicate.getVaccineName() + " dose"
              + (indicate.getAge().isEmpty() ? "" : " given before " + indicate.getAge().toString()));
          if (traceBuffer != null && !indicate.getReason().equals(""))
          {
            traceBuffer.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          if (trace != null)
          {
            trace.setReason(indicate.getReason());
            traceList.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          previousEventDate = vaccDate;
          previousEventWasContra = true;
          determineRanges();
          nextEvent();
          return CONTRA;
        } else
        {
          validDoseCount++;
          addValidDose(vaccineIds);
          if (traceBuffer != null && !indicate.getReason().equals(""))
          {
            traceBuffer.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          if (trace != null)
          {
            trace.setReason(indicate.getReason());
            traceList.append("<font color=\"#FF0000\">" + indicate.getReason() + "</font> ");
          }
          beforePreviousEventDate = previousEventDateValid;
          previousEventDateValid = vaccDate;
          previousEventWasContra = true;
          previousEventDate = vaccDate;
          if (seasonal != null && indicate.isSeasonCompleted())
          {
            seasonCompleted = true;
            if (traceBuffer != null)
            {
              traceBuffer.append("Season completed. ");
            }
          }
          nextEvent();
          return indicate.getScheduleName();
        }
      }
      nextEvent();
    }
    // this schedule was not satisfied so this person needs to have a forecast
    // generated
    addForecastRecommendations();
    return null;
  }

  private boolean indicatedEvent(int[] vaccineIds)
  {
    boolean indicatedEvent = false;
    for (Iterator it = event.immList.iterator(); it.hasNext();)
    {
      ImmunizationInterface imm = (ImmunizationInterface) it.next();
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

  private void nextEvent()
  {
    if (eventPosition < eventList.size())
    {
      event = (Event) eventList.get(eventPosition);
      event.hasEvent = false;
      setHasEvent();
      eventPosition++;
    } else
    {
      event = null;
    }
  }

  private void setHasEvent()
  {
    VaccineForecastDataBean.Indicate[] ind = schedule.getIndicates();
    for (int i = 0; i < ind.length; i++)
    {
      int[] vaccineIds = ind[i].getVaccines();
      for (int j = 0; j < vaccineIds.length; j++)
      {
        for (Iterator it = event.immList.iterator(); it.hasNext();)
        {
          ImmunizationInterface imm = (ImmunizationInterface) it.next();
          if (vaccineIds[j] == imm.getVaccineId())
          {
            event.hasEvent = true;
            return;
          }
        }
      }
    }
  }

  private void addInvalidDose(int[] vaccineIds, String invalidReason)
  {
    if (!getValidDose(schedule).equals(""))
    {
      for (Iterator it = event.immList.iterator(); it.hasNext();)
      {
        ImmunizationInterface imm = (ImmunizationInterface) it.next();
        for (int i = 0; i < vaccineIds.length; i++)
        {
          if (imm.getVaccineId() == vaccineIds[i])
          {
            VaccinationDoseDataBean dose = new VaccinationDoseDataBean();
            dose.setAdminDate(imm.getDateOfShot());
            dose.setDoseCode(getValidDose(schedule));
            dose.setImmregid(patient.getImmregid());
            dose.setForecastCode(forecast.getForecastCode());
            dose.setScheduleCode(schedule.getScheduleName());
            dose.setStatusCode(VaccinationDoseDataBean.STATUS_INVALID);
            dose.setVaccineId(imm.getVaccineId());
            dose.setReason((forecastManager.getVaccineName(imm.getVaccineId()) + (" given " + dateFormat.format(imm
                .getDateOfShot()))) + " is invalid " + invalidReason + "");
            doseList.add(dose);
            if (traceBuffer != null)
            {
              traceBuffer.append(" <font color=\"#FF0000\">" + dose.getReason() + ".</font> ");
            }
            if (trace != null)
            {
              trace.getDoses().add(dose);
              traceList.append(" <font color=\"#FF0000\">" + dose.getReason() + ".</font> ");
            }
          }
        }
      }
    }
  }

  private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

  private void addContra(int[] vaccineIds, String contraReason)
  {
    if (traceBuffer != null)
    {
      if (!getValidDose(schedule).equals(""))
      {
        for (Iterator it = event.immList.iterator(); it.hasNext();)
        {
          ImmunizationInterface imm = (ImmunizationInterface) it.next();
          for (int i = 0; i < vaccineIds.length; i++)
          {
            if (imm.getVaccineId() == vaccineIds[i])
            {
              if (traceBuffer != null)
              {
                traceBuffer.append(" <font color=\"#FF0000\">");
                traceBuffer.append(forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                    + dateFormat.format(imm.getDateOfShot()));
                traceBuffer.append(" is a contraindicated ");
                traceBuffer.append(contraReason);
                traceBuffer.append(".</font> ");
              }
              if (trace != null)
              {
                traceList.append(" <font color=\"#FF0000\">");
                traceList.append(forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                    + dateFormat.format(imm.getDateOfShot()));
                traceList.append(" is a contraindicated ");
                traceList.append(contraReason);
                traceList.append(".</font> ");
              }
            }
          }
        }
      }
    }
  }

  private void addMissedDose(int[] vaccineIds)
  {
    if (!getValidDose(schedule).equals(""))
    {
      if (vaccineIds.length > 0)
      {
        VaccinationDoseDataBean dose = new VaccinationDoseDataBean();
        dose.setAdminDate(event.eventDate);
        dose.setDoseCode(getValidDose(schedule));
        dose.setImmregid(patient.getImmregid());
        dose.setForecastCode(forecast.getForecastCode());
        dose.setScheduleCode(schedule.getScheduleName());
        dose.setStatusCode(VaccinationDoseDataBean.STATUS_MISSED);
        dose.setVaccineId(vaccineIds[0]);
        doseList.add(dose);
      }
    }
  }

  private void addValidDose(int[] vaccineIds)
  {
    if (!getValidDose(schedule).equals(""))
    {
      for (Iterator it = event.immList.iterator(); it.hasNext();)
      {
        ImmunizationInterface imm = (ImmunizationInterface) it.next();
        for (int i = 0; i < vaccineIds.length; i++)
        {
          if (imm.getVaccineId() == vaccineIds[i])
          {
            VaccinationDoseDataBean dose = new VaccinationDoseDataBean();
            dose.setAdminDate(imm.getDateOfShot());
            dose.setDoseCode(getValidDose(schedule));
            dose.setImmregid(patient.getImmregid());
            dose.setForecastCode(forecast.getForecastCode());
            dose.setScheduleCode(schedule.getScheduleName());
            dose.setStatusCode(VaccinationDoseDataBean.STATUS_VALID);
            dose.setVaccineId(imm.getVaccineId());
            doseList.add(dose);
            if (traceBuffer != null)
            {
              traceBuffer.append(" <font color=\"#0000FF\">");
              traceBuffer.append(forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                  + dateFormat.format(imm.getDateOfShot()));
              traceBuffer.append(" is valid (dose #");
              traceBuffer.append(validDoseCount);
              traceBuffer.append(").</font> ");
            }
            if (trace != null)
            {
              trace.getDoses().add(dose);
              traceList.append(" <font color=\"#0000FF\">");
              traceList.append(forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                  + dateFormat.format(imm.getDateOfShot()));
              traceList.append(" is valid (dose #");
              traceList.append(String.valueOf(validDoseCount));
              traceList.append(").</font> ");
            }
          }
        }
      }
    }
  }

  private boolean checkInvalid(DateTime vaccDate)
  {
    if (validGrace.isEmpty())
    {
      return vaccDate.isLessThan(valid);
    } else
    {
      DateTime dt = schedule.getValidGrace().getDateTimeFrom(vaccDate);
      return dt.isLessThan(valid);
    }
  }

  private boolean checkSeasonEnd(Event event)
  {
    if (seasonal != null)
    {
      for (Iterator it = event.immList.iterator(); it.hasNext();)
      {
        ImmunizationInterface imm = (ImmunizationInterface) it.next();
        if (imm instanceof SeasonEndEvent)
        {
          seasonCompleted = false;
          return true;
        }
      }
    }
    return false;
  }

  private void addForecastRecommendations()
  {
    if (seasonStart != null && seasonCompleted)
    {
      seasonStart = new DateTime(seasonStart);
      System.out.println("seasonStart = " + seasonStart);
      System.out.println("seasonEnd = " + seasonEnd);
      seasonStart.addYears(1);
      seasonEnd = new DateTime(seasonEnd);
      seasonEnd.addYears(1);
      determineRanges();
    }
    ImmunizationForecastDataBean forecastBean = new ImmunizationForecastDataBean();
    forecastBean.setValid(valid.getDate());
    forecastBean.setEarly(early.getDate());
    forecastBean.setDue(due.getDate());
    forecastBean.setOverdue(overdue.getDate());
    forecastBean.setFinished(finished.getDate());
    forecastBean.setDateDue(due.getDate());
    forecastBean.setForecastName(forecast.getForecastCode());
    forecastBean.setForecastLabel(forecast.getForecastLabel());
    forecastBean.setSortOrder(forecast.getSortOrder());
    forecastBean.setDose(getValidDose(schedule));
    forecastBean.setSchedule(schedule.getScheduleName());
    forecastBean.setImmregid(patient.getImmregid());
    forecastBean.setTraceList(traceList);
    resultList.add(forecastBean);
    if (traceBuffer != null)
    {
      traceBuffer.append("</li><li>Forecasting for dose " + getNextValidDose(schedule) + " due at " + dueReason + ", "
          + due.toString("M/D/Y") + ".");
    }
    if (trace != null)
    {
      traceList.append("</li><li>Forecasting for dose " + getNextValidDose(schedule) + " due at " + dueReason + ", "
          + due.toString("M/D/Y") + ".");
    }
  }

  private DateTime figureCutoff(VaccineForecastDataBean.Indicate indicate)
  {
    DateTime cutoff = null;
    if (!indicate.getAge().isEmpty())
    {
      cutoff = indicate.getAge().getDateTimeFrom(patient.getDobDateTime());
    }
    DateTime cutoffInterval = null;
    if (!indicate.getMinInterval().isEmpty())
    {
      cutoffInterval = indicate.getMinInterval().getDateTimeFrom(previousEventDate);
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

  private void determineRanges()
  {
    String validReason = "";
    String validBecause = "";
    if (schedule.getValidAge().isEmpty())
    {
      valid = schedule.getValidInterval().getDateTimeFrom(previousEventDateValid);
      validReason = schedule.getValidInterval() + " after previous valid dose";
      validBecause = "INTERVAL";
    } else
    {
      valid = schedule.getValidAge().getDateTimeFrom(patient.getDobDateTime());
      if (schedule.getValidAge().getAmount() == 0)
      {
        validReason = "birth";
        validBecause = "AGE";
      } else
      {
        validReason = schedule.getValidAge() + " of age";
        validBecause = "AGE";
      }
      if (!schedule.getValidInterval().isEmpty())
      {
        DateTime validInterval = schedule.getValidInterval().getDateTimeFrom(previousEventDateValid);
        if (validInterval.isGreaterThan(valid))
        {
          valid = validInterval;
          validReason = schedule.getValidInterval() + " after previous valid dose";
          validBecause = "INTERVAL";
        }
      }
    }
    finished = schedule.getFinishedAge().getDateTimeFrom(patient.getDobDateTime());
    if (previousEventDate.equals(previousEventDateValid))
    {
      validGrace = schedule.getValidGrace();
    } else
    {
      if (previousEventWasContra && schedule.getAfterContraInterval() != null
          && !schedule.getAfterContraInterval().isEmpty())
      {
        DateTime validInterval = schedule.getAfterContraInterval().getDateTimeFrom(previousEventDate);
        if (validInterval.isGreaterThan(valid))
        {
          valid = validInterval;
          validReason = schedule.getAfterContraInterval() + " after contraindicated dose";
          validBecause = "CONTRA";
        }
        validGrace = schedule.getAfterContraGrace();
      } else
      {
        DateTime validInterval = schedule.getAfterInvalidInterval().getDateTimeFrom(previousEventDate);
        if (validInterval.isGreaterThan(valid))
        {
          valid = validInterval;
          validReason = schedule.getAfterInvalidInterval() + " after previous dose";
          validBecause = "INVALID";
        }
        validGrace = schedule.getAfterInvalidGrace();
      }
    }
    if (!schedule.getBeforePreviousInterval().isEmpty() && beforePreviousEventDate != null)
    {
      DateTime beforePreviousInterval = schedule.getBeforePreviousInterval().getDateTimeFrom(beforePreviousEventDate);
      if (beforePreviousInterval.isGreaterThan(valid))
      {
        valid = beforePreviousInterval;
        validReason = schedule.getBeforePreviousInterval() + " after valid dose given before previous valid dose";
        validBecause = "BEFORE";
      }
    }
    if (previousAfterInvalidInterval != null)
    {
      DateTime previousAfterInvalidIntervalDate = previousAfterInvalidInterval.getDateTimeFrom(previousEventDate);
      if (previousAfterInvalidIntervalDate.isGreaterThan(valid))
      {
        valid = previousAfterInvalidIntervalDate;
        validReason = previousAfterInvalidInterval + " after previous invalid/contraindicated dose";
        validBecause = "INVALID";
      }
    }
    dueReason = "";
    if (schedule.getDueAge().isEmpty())
    {
      due = schedule.getDueInterval().getDateTimeFrom(previousEventDate);
      dueReason = schedule.getDueInterval() + " after previous dose";
    } else
    {
      due = schedule.getDueAge().getDateTimeFrom(patient.getDobDateTime());
      if (schedule.getDueAge().getAmount() == 0)
      {
        dueReason = "birth";
      } else
      {
        dueReason = schedule.getDueAge() + " of age";
      }
      if (!schedule.getDueInterval().isEmpty())
      {
        DateTime dueInterval = schedule.getDueInterval().getDateTimeFrom(previousEventDate);
        if (dueInterval.isLessThan(due))
        {
          due = dueInterval;
          dueReason = schedule.getDueInterval() + " after previous dose";
        }
      }
    }
    if (seasonStart != null)
    {
      if (seasonStart.isGreaterThan(valid))
      {
        valid = new DateTime(seasonStart);
        validReason = "at start of next season";
        validBecause = "SEASON";
      }
      DateTime seasonDue = seasonal.getDue().getDateTimeFrom(seasonStart);
      if (seasonDue.isGreaterThan(due))
      {
        due = seasonDue;
        dueReason = seasonal.getDue() + " after season start";
      }
    }
    if (schedule.getOverdueAge().isEmpty())
    {
      overdue = schedule.getOverdueInterval().getDateTimeFrom(previousEventDate);
    } else
    {
      overdue = schedule.getOverdueAge().getDateTimeFrom(patient.getDobDateTime());

      if (!schedule.getOverdueInterval().isEmpty())
      {
        DateTime overdueInterval = schedule.getOverdueInterval().getDateTimeFrom(previousEventDate);
        if (overdueInterval.isGreaterThan(overdue))
        {
          overdue = overdueInterval;
        }
      }
    }
    if (seasonStart != null)
    {
      DateTime seasonOverdue = seasonal.getOverdue().getDateTimeFrom(seasonStart);
      if (seasonOverdue.isLessThan(overdue))
      {
        overdue = seasonOverdue;
      }
    }
    if (!schedule.getEarlyAge().isEmpty())
    {
      early = schedule.getEarlyAge().getDateTimeFrom(patient.getDobDateTime());
    } else
    {
      early = due;
    }
    if (!schedule.getEarlyInterval().isEmpty())
    {
      DateTime earlyInterval = schedule.getEarlyInterval().getDateTimeFrom(previousEventDate);
      if (earlyInterval.isLessThan(early))
      {
        early = earlyInterval;
      }
    }
    if (early.isLessThan(valid))
    {
      early = new DateTime(valid);
    }
    if (due.isLessThan(valid))
    {
      due = new DateTime(valid);
      dueReason = "same time as valid";
    }
    if (early.isGreaterThan(due))
    {
      early = due;
    }
    if (overdue.isLessThan(due))
    {
      overdue = new DateTime(due);
    }
    if (finished.isLessThan(overdue))
    {
      overdue = new DateTime(finished);
      if (overdue.isLessThan(due))
      {
        due = new DateTime(overdue);
        dueReason = "same time as over due";
        if (due.isLessThan(early))
        {
          early = new DateTime(due);
          if (valid.isLessThan(early))
          {
            valid = new DateTime(early);
            validReason = "vaccine does not need to be administered";
          }
        }
      }

    }
    if (traceBuffer != null)
    {
      traceBuffer.append("Dose " + getNextValidDose(schedule) + " valid at " + validReason + ", "
          + valid.toString("M/D/Y") + ". ");
    }
    if (trace != null)
    {
      trace.setDueDate(due);
      trace.setOverdueDate(overdue);
      trace.setValidDate(valid);
      trace.setFinishedDate(finished);
      trace.setValidReason("Dose " + getNextValidDose(schedule) + " valid at " + validReason + ", "
          + valid.toString("M/D/Y") + ". ");
      trace.setValidBecause(validBecause);
      traceList.append("Dose " + getNextValidDose(schedule) + " valid at " + validReason + ", "
          + valid.toString("M/D/Y") + ". ");
    }
  }

  public void setPatient(PatientForecastRecordDataBean patient)
  {
    this.patient = patient;
  }

  public void setVaccinations(List vaccinations)
  {
    vaccinations = new ArrayList(vaccinations);
    Collections.sort(vaccinations, new Comparator() {
      public int compare(Object o1, Object o2)
      {
        ImmunizationInterface imm1 = (ImmunizationInterface) o1;
        ImmunizationInterface imm2 = (ImmunizationInterface) o2;
        return imm1.getDateOfShot().compareTo(imm2.getDateOfShot());
      }
    });
    eventList = new ArrayList();
    Event event = null;
    hasHistoryOfVaricella = false;
    for (Iterator it = vaccinations.iterator(); it.hasNext();)
    {
      ImmunizationInterface imm = (ImmunizationInterface) it.next();
      if (event == null || !event.eventDate.equals(imm.getDateOfShot()))
      {
        event = new Event();
        eventList.add(event);
        event.eventDate = imm.getDateOfShot();
      }
      if (imm.getVaccineId() == VARICELLA_HISTORY)
      {
        hasHistoryOfVaricella = true;
      }
      event.immList.add(imm);
    }
  }

  private class SeasonEndEvent implements ImmunizationInterface
  {
    private Date date = null;

    public SeasonEndEvent(Date date) {
      this.date = date;
    }

    public Date getDateOfShot()
    {
      return date;
    }

    public int getVaccineId()
    {
      return -504;
    }

  }

  public Date getForecastDate()
  {
    return forecastDate;
  }

  public void setForecastDate(Date forecastDate)
  {
    this.forecastDate = forecastDate;
  }

  private class Event
  {
    private Date eventDate = null;
    private List immList = new ArrayList();
    private boolean hasEvent = false;
  }

  private String getValidDose(VaccineForecastDataBean.Schedule schedule)
  {
    String dose = schedule.getDose();
    if (dose.equals("*"))
    {
      dose = Integer.toString(validDoseCount);
    }
    return dose;
  }

  private String getNextValidDose(VaccineForecastDataBean.Schedule schedule)
  {
    String dose = schedule.getDose();
    if (dose.equals("*"))
    {
      dose = Integer.toString(validDoseCount + 1);
    }
    return dose;
  }
}
