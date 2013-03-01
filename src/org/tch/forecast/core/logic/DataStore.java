package org.tch.forecast.core.logic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.PatientForecastRecordDataBean;
import org.tch.forecast.core.TimePeriod;
import org.tch.forecast.core.Trace;
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.VaccineForecastDataBean.Seasonal;
import org.tch.forecast.core.VaccineForecastDataBean.Transition;
import org.tch.forecast.core.VaccineForecastManagerInterface;

public class DataStore
{

  protected static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  public static final int VARICELLA_HISTORY = 378;

  public static DateFormat getDateFormat()
  {
    return dateFormat;
  }

  public static void setDateFormat(DateFormat dateFormat)
  {
    DataStore.dateFormat = dateFormat;
  }

  protected DateTime beforePreviousEventDate;
  protected List<DateTime[]> blackOutDates = null;
  protected List<String> blackOutReasons = null;
  protected StringBuffer detailLog = null;
  protected List<VaccinationDoseDataBean> doseList = null;
  protected DateTime due = null;
  protected String dueReason = "";
  protected DateTime early = null;
  protected Event event = null;
  protected List<Event> eventList = null;
  protected int eventPosition = 0;
  protected DateTime finished = null;
  protected VaccineForecastDataBean forecast = null;
  protected String forecastCode = null;
  protected Date forecastDate = new Date();
  protected VaccineForecastManagerInterface forecastManager = null;
  protected boolean hasHistoryOfVaricella = false;
  protected VaccineForecastDataBean.Indicate[] indicates = null;
  protected int indicatesPos = -1;
  protected String nextAction = null;
  protected List<Event> originalEventList = null;
  protected DateTime overdue = null;
  protected PatientForecastRecordDataBean patient = null;
  protected TimePeriod previousAfterInvalidInterval;
  protected DateTime previousEventDate;
  protected DateTime previousEventDateValid;
  protected boolean previousEventWasContra = false;
  protected Set<Integer> previousVaccineIdHistory = new HashSet<Integer>();
  protected List<Integer> previousVaccineIdList = null;
  protected List<ImmunizationForecastDataBean> resultList = null;
  protected VaccineForecastDataBean.Schedule schedule;
  protected List<Schedule> scheduleList;
  protected int scheduleListPos = -1;
  protected Seasonal seasonal = null;
  protected List<Transition> transitionList = null;
  protected boolean seasonCompleted = false;
  protected DateTime seasonEnd = null;
  protected DateTime seasonStart = null;
  protected DateTime today = null;
  protected Trace trace = null;
  protected StringBuffer traceBuffer = null;
  protected TraceList traceList = null;
  protected Map<String, List<Trace>> traces = null;
  protected List<ImmunizationInterface> vaccinations;
  protected DateTime valid = null;
  protected int validDoseCount = 0;
  protected TimePeriod validGrace = null;
  protected String whenValidText = null;

  public DataStore(VaccineForecastManagerInterface forecastManager) {
    this.forecastManager = forecastManager;
  }

  public DateTime getBeforePreviousEventDate()
  {
    return beforePreviousEventDate;
  }

  public List<DateTime[]> getBlackOutDates()
  {
    return blackOutDates;
  }

  public List<String> getBlackOutReasons()
  {
    return blackOutReasons;
  }

  public StringBuffer getDetailLog()
  {
    return detailLog;
  }

  public List<VaccinationDoseDataBean> getDoseList()
  {
    return doseList;
  }

  public DateTime getDue()
  {
    return due;
  }

  public String getDueReason()
  {
    return dueReason;
  }

  public DateTime getEarly()
  {
    return early;
  }

  public Event getEvent()
  {
    return event;
  }

  public List<Event> getEventList()
  {
    return eventList;
  }

  public int getEventPosition()
  {
    return eventPosition;
  }

  public DateTime getFinished()
  {
    return finished;
  }

  public VaccineForecastDataBean getForecast()
  {
    return forecast;
  }

  public String getForecastCode()
  {
    return forecastCode;
  }

  public Date getForecastDate()
  {
    return forecastDate;
  }

  public VaccineForecastManagerInterface getForecastManager()
  {
    return forecastManager;
  }

  public VaccineForecastDataBean.Indicate[] getIndicates()
  {
    return indicates;
  }

  public int getIndicatesPos()
  {
    return indicatesPos;
  }

  public String getNextAction()
  {
    return nextAction;
  }

  public List<Event> getOriginalEventList()
  {
    return originalEventList;
  }

  public DateTime getOverdue()
  {
    return overdue;
  }

  public PatientForecastRecordDataBean getPatient()
  {
    return patient;
  }

  public TimePeriod getPreviousAfterInvalidInterval()
  {
    return previousAfterInvalidInterval;
  }

  public DateTime getPreviousEventDate()
  {
    return previousEventDate;
  }

  public DateTime getPreviousEventDateValid()
  {
    return previousEventDateValid;
  }

  public Set<Integer> getPreviousVaccineIdHistory()
  {
    return previousVaccineIdHistory;
  }

  public List<Integer> getPreviousVaccineIdList()
  {
    return previousVaccineIdList;
  }

  public List<ImmunizationForecastDataBean> getResultList()
  {
    return resultList;
  }

  public VaccineForecastDataBean.Schedule getSchedule()
  {
    return schedule;
  }

  public List<Schedule> getScheduleList()
  {
    return scheduleList;
  }

  public int getScheduleListPos()
  {
    return scheduleListPos;
  }

  public Seasonal getSeasonal()
  {
    return seasonal;
  }
  
  public void setTransitionList(List<Transition> transitionList)
  {
    this.transitionList = transitionList;
  }
  
  public List<Transition> getTransitionList()
  {
    return transitionList;
  }

  public DateTime getSeasonEnd()
  {
    return seasonEnd;
  }

  public DateTime getSeasonStart()
  {
    return seasonStart;
  }

  public DateTime getToday()
  {
    return today;
  }

  public Trace getTrace()
  {
    return trace;
  }

  public StringBuffer getTraceBuffer()
  {
    return traceBuffer;
  }

  public TraceList getTraceList()
  {
    return traceList;
  }

  public Map<String, List<Trace>> getTraces()
  {
    return traces;
  }

  public List<ImmunizationInterface> getVaccinations()
  {
    return vaccinations;
  }

  public DateTime getValid()
  {
    return valid;
  }

  public int getValidDoseCount()
  {
    return validDoseCount;
  }

  public TimePeriod getValidGrace()
  {
    return validGrace;
  }

  public boolean isHasHistoryOfVaricella()
  {
    return hasHistoryOfVaricella;
  }

  public boolean isPreviousEventWasContra()
  {
    return previousEventWasContra;
  }

  public boolean isSeasonCompleted()
  {
    return seasonCompleted;
  }

  public void log(String s)
  {
    if (s != null && detailLog != null)
    {
      detailLog.append(s);
      detailLog.append("\n");
    }
  }
  
  public boolean isLog()
  {
    return detailLog != null;
  }

  public void setBeforePreviousEventDate(DateTime beforePreviousEventDate)
  {
    this.beforePreviousEventDate = beforePreviousEventDate;
  }

  public void setBlackOutDates(List<DateTime[]> blackOutDates)
  {
    this.blackOutDates = blackOutDates;
  }

  public void setBlackOutReasons(List<String> blackOutReasons)
  {
    this.blackOutReasons = blackOutReasons;
  }

  public void setDetailLog(StringBuffer detailLog)
  {
    this.detailLog = detailLog;
  }

  public void setDoseList(List<VaccinationDoseDataBean> doseList)
  {
    this.doseList = doseList;
  }

  public void setDue(DateTime due)
  {
    this.due = due;
  }

  public void setDueReason(String dueReason)
  {
    this.dueReason = dueReason;
  }

  public void setEarly(DateTime early)
  {
    this.early = early;
  }

  public void setEvent(Event event)
  {
    this.event = event;
  }

  public void setEventList(List<Event> eventList)
  {
    this.eventList = eventList;
  }

  public void setEventPosition(int eventPosition)
  {
    this.eventPosition = eventPosition;
  }

  public void setFinished(DateTime finished)
  {
    this.finished = finished;
  }

  public void setForecast(VaccineForecastDataBean forecast)
  {
    this.forecast = forecast;
  }

  public void setForecastCode(String forecastCode)
  {
    this.forecastCode = forecastCode;
  }

  public void setForecastDate(Date forecastDate)
  {
    this.forecastDate = forecastDate;
  }

  public void setForecastManager(VaccineForecastManagerInterface forecastManager)
  {
    this.forecastManager = forecastManager;
  }

  public void setHasHistoryOfVaricella(boolean hasHistoryOfVaricella)
  {
    this.hasHistoryOfVaricella = hasHistoryOfVaricella;
  }

  public void setIndicates(VaccineForecastDataBean.Indicate[] indicates)
  {
    this.indicates = indicates;
  }

  public void setIndicatesPos(int indicatesPos)
  {
    this.indicatesPos = indicatesPos;
  }

  public void setNextAction(String nextAction)
  {
    this.nextAction = nextAction;
  }

  public void setOriginalEventList(List<Event> originalEventList)
  {
    this.originalEventList = originalEventList;
  }

  public void setOverdue(DateTime overdue)
  {
    this.overdue = overdue;
  }

  public void setPatient(PatientForecastRecordDataBean patient)
  {
    this.patient = patient;
  }

  public void setPreviousAfterInvalidInterval(TimePeriod previousAfterInvalidInterval)
  {
    this.previousAfterInvalidInterval = previousAfterInvalidInterval;
  }

  public void setPreviousEventDate(DateTime previousEventDate)
  {
    this.previousEventDate = previousEventDate;
  }

  public void setPreviousEventDateValid(DateTime previousEventDateValid)
  {
    this.previousEventDateValid = previousEventDateValid;
  }

  public void setPreviousEventWasContra(boolean previousEventWasContra)
  {
    this.previousEventWasContra = previousEventWasContra;
  }

  public void setPreviousVaccineIdHistory(Set<Integer> previousVaccineIdHistory)
  {
    this.previousVaccineIdHistory = previousVaccineIdHistory;
  }

  public void setPreviousVaccineIdList(List<Integer> previousVaccineIdList)
  {
    this.previousVaccineIdList = previousVaccineIdList;
  }

  public void setResultList(List<ImmunizationForecastDataBean> resultList)
  {
    this.resultList = resultList;
  }

  public void setSchedule(VaccineForecastDataBean.Schedule schedule)
  {
    this.schedule = schedule;
  }

  public void setScheduleList(List<Schedule> scheduleList)
  {
    this.scheduleList = scheduleList;
  }

  public void setScheduleListPos(int scheduleListPos)
  {
    this.scheduleListPos = scheduleListPos;
  }

  public void setSeasonal(Seasonal seasonal)
  {
    this.seasonal = seasonal;
  }

  public void setSeasonCompleted(boolean seasonCompleted)
  {
    this.seasonCompleted = seasonCompleted;
  }

  public void setSeasonEnd(DateTime seasonEnd)
  {
    this.seasonEnd = seasonEnd;
  }

  public void setSeasonStart(DateTime seasonStart)
  {
    this.seasonStart = seasonStart;
  }

  public void setToday(DateTime today)
  {
    this.today = today;
  }

  public void setTrace(Trace trace)
  {
    this.trace = trace;
  }

  public void setTraceBuffer(StringBuffer traceBuffer)
  {
    this.traceBuffer = traceBuffer;
  }

  public void setTraceList(TraceList traceList)
  {
    this.traceList = traceList;
  }

  public void setTraces(Map<String, List<Trace>> traces)
  {
    this.traces = traces;
  }

  public void setVaccinations(List<ImmunizationInterface> vaccList)
  {
    this.vaccinations = new ArrayList<ImmunizationInterface>(vaccList);
    vaccList = new ArrayList<ImmunizationInterface>(vaccList);
    Collections.sort(vaccList, new Comparator<ImmunizationInterface>() {
      public int compare(ImmunizationInterface imm1, ImmunizationInterface imm2)
      {
        return imm1.getDateOfShot().compareTo(imm2.getDateOfShot());
      }
    });
    eventList = new ArrayList<Event>();
    Event event = null;
    hasHistoryOfVaricella = false;
    for (Iterator<ImmunizationInterface> it = vaccList.iterator(); it.hasNext();)
    {
      ImmunizationInterface imm = it.next();
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

  public void setValid(DateTime valid)
  {
    this.valid = valid;
  }

  public void setValidDoseCount(int validDoseCount)
  {
    this.validDoseCount = validDoseCount;
  }

  public void setValidGrace(TimePeriod validGrace)
  {
    this.validGrace = validGrace;
  }

}
