package org.immregistries.lonestar.core.logic;

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

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.PatientForecastRecordDataBean;
import org.immregistries.lonestar.core.Seasonal;
import org.immregistries.lonestar.core.TimePeriod;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.TraceList;
import org.immregistries.lonestar.core.Transition;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.VaccineForecastDataBean;
import org.immregistries.lonestar.core.VaccineForecastManagerInterface;
import org.immregistries.lonestar.core.VaccineForecastDataBean.Schedule;
import org.immregistries.lonestar.core.VaccineForecastDataBean.ValidVaccine;
import org.immregistries.lonestar.core.api.impl.ForecastAntigen;
import org.immregistries.lonestar.core.api.impl.ForecastOptions;
import org.immregistries.lonestar.core.model.Assumption;

public class DataStore
{

  public static final TimePeriod NO_GRACE_PERIOD = new TimePeriod("");
  
  protected DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

  public static final int VARICELLA_HISTORY = 378;

  public DateFormat getDateFormat()
  {
    return dateFormat;
  }

  public void setDateFormat(DateFormat dateFormat)
  {
    this.dateFormat = dateFormat;
  }

  protected DateTime beforePreviousEventDate;
  protected List<BlackOut> blackOutDates = null;
  protected StringBuffer detailLog = null;
  protected List<VaccinationDoseDataBean> doseList = null;
  protected List<Assumption> assumptionList = null;
  protected DateTime due = null;
  protected String dueReason = "";
  protected Event event = null;
  protected List<Event> eventList = null;
  protected int eventPosition = 0;
  protected DateTime finished = null;
  protected VaccineForecastDataBean forecast = null;
  protected String forecastCode = null;
  protected DateTime forecastDateTime = new DateTime(new Date());
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
  protected DateTime previousEventDateValidNotBirth;
  protected boolean previousEventWasContra = false;
  protected Set<Integer> previousVaccineIdHistory = new HashSet<Integer>();
  protected List<Integer> previousVaccineIdList = null;
  protected List<ImmunizationForecastDataBean> resultList = null;
  protected VaccineForecastDataBean.Schedule schedule;
  protected List<Schedule> scheduleList;
  protected int scheduleListPos = -1;
  protected Seasonal seasonal = null;
  protected List<Transition> transitionList = null;
  protected DateTime seasonEndDateTime = null;
  protected DateTime seasonStartDateTime = null;
  protected Trace trace = null;
  protected TraceList traceList = null;
  protected Map<String, List<Trace>> traces = null;
  protected List<ImmunizationInterface> vaccinations;
  protected DateTime valid = null;
  protected DateTime validGrace = null;
  protected int validDoseCount = 0;
  protected String whenValidText = null;
  protected ForecastOptions forecastOptions = null;
  protected Map<Integer, String> invalidatedSameDayVaccineIdMapToReason = null;

  public Map<Integer, String> getInvalidatedSameDayVaccineIdMapToReason() {
    return invalidatedSameDayVaccineIdMapToReason;
  }

  public List<Assumption> getAssumptionList() {
    return assumptionList;
  }

  public DateTime getPreviousEventDateValidNotBirth() {
    return previousEventDateValidNotBirth;
  }

  public void setPreviousEventDateValidNotBirth(DateTime previousEventDateValidNotBirth) {
    this.previousEventDateValidNotBirth = previousEventDateValidNotBirth;
  }

  public ForecastOptions getForecastOptions() {
    return forecastOptions;
  }

  public void setForecastOptions(ForecastOptions forecastOptions) {
    this.forecastOptions = forecastOptions;
  }

  public DataStore(VaccineForecastManagerInterface forecastManager) {
    this.forecastManager = forecastManager;
  }

  public DateTime getBeforePreviousEventDate()
  {
    return beforePreviousEventDate;
  }

  public List<BlackOut> getBlackOutDates()
  {
    return blackOutDates;
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
    return forecastDateTime.getDate();
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

  public DateTime getSeasonEndDateTime()
  {
    return seasonEndDateTime;
  }

  public DateTime getSeasonStartDateTime()
  {
    return seasonStartDateTime;
  }

  public Trace getTrace()
  {
    return trace;
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

  public DateTime getValidGrace()
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

  public void setBlackOutDates(List<BlackOut> blackOutDates)
  {
    this.blackOutDates = blackOutDates;
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
    this.forecastDateTime = new DateTime(forecastDate);
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

  public void setSeasonEndDateTime(DateTime seasonEnd)
  {
    this.seasonEndDateTime = seasonEnd;
  }

  public void setSeasonStartDateTime(DateTime seasonStart)
  {
    this.seasonStartDateTime = seasonStart;
  }

  public void setTrace(Trace trace)
  {
    this.trace = trace;
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

  public void setValidGrace(DateTime validGrace)
  {
    this.validGrace = validGrace;
  }

}
