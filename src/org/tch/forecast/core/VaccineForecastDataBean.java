package org.tch.forecast.core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class VaccineForecastDataBean
{
  private static Map<String, List<Schedule>> indications = new HashMap<String, List<Schedule>>();

  private String forecastCode = "";
  private String forecastLabel = "";
  private int sortOrder = 0;
  private Map<String, String> vaccines = new HashMap<String, String>();
  private Map<String, Schedule> schedules = new HashMap<String, Schedule>();
  private Seasonal seasonal = null;
  
  protected VaccineForecastDataBean() 
  {
    // default for ForecastSchedule to build object
  }

  public VaccineForecastDataBean(String source) throws Exception {
    try
    {
      DocumentBuilderFactory factory;
      DocumentBuilder builder;
      factory = DocumentBuilderFactory.newInstance();
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(true);
      factory.setNamespaceAware(true);
      builder = factory.newDocumentBuilder();
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(source);
      processDocument(builder.parse(new InputSource(is)));
    } catch (Exception exception)
    {
      throw new Exception("Unable to read XML definition " + source, exception);
    }
  }

  protected Object processDocument(Document node) throws Exception
  {
    Node n = node.getFirstChild();
    if (n != null)
    {
      processNode(n);
    }
    return null;
  }

  protected void processNode(Node n) throws Exception
  {
    String name = n.getNodeName();
    if (!name.equals("forecast"))
    {
      throw new Exception("Root node in definition xml should be 'forecast', instead found '" + name + "'");
    }
    forecastCode = DomUtils.getAttributeValue(n, "seriesName");
    if (forecastCode == null || forecastCode.equals(""))
    {
      throw new Exception("forecastCode attribute is missing on forecast tag");
    }
    NodeList l = n.getChildNodes();
    for (int i = 0, icount = l.getLength(); i < icount; i++)
    {
      n = l.item(i);
      name = n.getNodeName();
      processScheduleOrVaccine(n, name, forecastCode);
    }
  }

  private void processScheduleOrVaccine(Node n, String name, String forecastCode) throws Exception
  {
    if (name != null)
    {
      if (name.equals("schedule"))
      {
        Schedule schedule = new Schedule();
        schedule.setForecastCode(forecastCode);
        schedule.setScheduleName(DomUtils.getAttributeValue(n, "scheduleName"));
        schedule.setLabel(DomUtils.getAttributeValue(n, "label"));
        schedule.setDose(DomUtils.getAttributeValue(n, "dose"));
        schedule.setIndication(DomUtils.getAttributeValue(n, "indication"));
        addToIndicationList(schedule);
        schedules.put(schedule.getScheduleName(), schedule);
        NodeList l = n.getChildNodes();
        for (int i = 0, icount = l.getLength(); i < icount; i++)
        {
          n = l.item(i);
          name = n.getNodeName();
          processScheduleItem(n, name, schedule);
          if (schedule.getIndicates() == null)
          {
            throw new Exception("No indicates defined for schedule " + schedule.getScheduleName());
          }
        }
        schedule.convertIndicateFromListToArray();
        schedule.convertContraindicateFromListToArray();
        schedule.checkForConsistency();
      } else if (name.equals("vaccine"))
      {
        String vaccineName = DomUtils.getAttributeValue(n, "vaccineName");
        if (vaccineName == null || vaccineName.equals(""))
        {
          throw new Exception("vaccineName attribute is missing on vaccine tag");
        }
        String vaccineIds = DomUtils.getAttributeValue(n, "vaccineIds");
        if (vaccineIds == null || vaccineIds.equals(""))
        {
          throw new Exception("vaccineIds attribute is missing on vaccine tag");
        }
        vaccines.put(vaccineName.toUpperCase(), vaccineIds);
      } else if (name.equals("seasonal"))
      {
        seasonal = new Seasonal();
        String seasonalStart = DomUtils.getAttributeValue(n, "start");
        String seasonalDue = DomUtils.getAttributeValue(n, "due");
        String seasonalOverdue = DomUtils.getAttributeValue(n, "overdue");
        String seasonalEnd = DomUtils.getAttributeValue(n, "end");
        seasonal.setStart(new TimePeriod(seasonalStart));
        seasonal.setDue(new TimePeriod(seasonalDue));
        seasonal.setOverdue(new TimePeriod(seasonalOverdue));
        seasonal.setEnd(new TimePeriod(seasonalEnd));
      }
    }
  }

  private void addToIndicationList(Schedule schedule)
  {
    if (schedule.getIndication() != null && !schedule.getIndication().equals(""))
    {
      List<Schedule> indicationList = indications.get(schedule.getIndication());
      if (indicationList == null)
      {
        indicationList = new ArrayList<Schedule>();
        indications.put(schedule.getIndication(), indicationList);
      }
      indicationList.add(schedule);
    }
  }

  private void processScheduleItem(Node n, String name, Schedule schedule) throws Exception
  {
    if (name != null)
    {
      if (name.equals("valid"))
      {
        schedule.setValidAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setValidInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
        schedule.setValidGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
      } else if (name.equals("early"))
      {
        schedule.setEarlyAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setEarlyInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("due"))
      {
        schedule.setDueAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setDueInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("overdue"))
      {
        schedule.setOverdueAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setOverdueInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("finished"))
      {
        schedule.setFinishedAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setFinishedInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("after-invalid"))
      {
        schedule.setAfterInvalidInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
        schedule.setAfterInvalidGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
      } else if (name.equals("after-contra"))
      {
        schedule.setAfterContraInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
        schedule.setAfterContraGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
      } else if (name.equals("before-previous"))
      {
        schedule.setBeforePreviousInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
        schedule.setBeforePreviousGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
      } else if (name.equals("pos"))
      {
        schedule.setPosColumn(DomUtils.getAttributeValueInt(n, "column"));
        schedule.setPosRow(DomUtils.getAttributeValueInt(n, "row"));
      } else if (name.equals("contraindicate"))
      {
        Contraindicate contraindicate = new Contraindicate();
        String vaccineName = DomUtils.getAttributeValue(n, "vaccineName");
        contraindicate.setVaccineName(vaccineName);
        contraindicate.setVaccines(convertToVaccineIds(vaccineName));
        contraindicate.setAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        contraindicate.setAfterInterval(new TimePeriod(DomUtils.getAttributeValue(n, "afterInterval")));
        contraindicate.setReason(DomUtils.getAttributeValue(n, "reason"));
        schedule.getContraindicateList().add(contraindicate);
      } else if (name.equals("indicate"))
      {
        Indicate indicate = new Indicate();
        String vaccineName = DomUtils.getAttributeValue(n, "vaccineName");
        indicate.setVaccineName(vaccineName);
        indicate.setVaccines(convertToVaccineIds(vaccineName));
        String previousVaccineName = DomUtils.getAttributeValue(n, "previousVaccineName");
        indicate.setPreviousVaccineName(previousVaccineName);
        indicate.setPreviousVaccines(convertToVaccineIds(previousVaccineName));
        String historyOfVaccineName = DomUtils.getAttributeValue(n, "historyOfVaccineName");
        indicate.setHistoryOfVaccines(convertToVaccineIds(historyOfVaccineName));
        indicate.setHistoryOfVaccineName(historyOfVaccineName);
        indicate.setScheduleName(DomUtils.getAttributeValue(n, "schedule"));
        indicate.setAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        indicate.setMinInterval(new TimePeriod(DomUtils.getAttributeValue(n, "minInterval")));
        indicate.setMaxInterval(new TimePeriod(DomUtils.getAttributeValue(n, "maxInterval")));
        indicate.setReason(DomUtils.getAttributeValue(n, "reason"));
        indicate.setSeasonCompleted("Yes".equalsIgnoreCase(DomUtils.getAttributeValue(n, "seasonCompleted")));
        schedule.getIndicateList().add(indicate);
      }
    }
  }

  private int[] convertToVaccineIds(String vaccineName) throws Exception
  {
    if (vaccineName == null || vaccineName.length() == 0)
    {
      return new int[0];
    }
    String vaccineString = vaccines.get(vaccineName.toUpperCase());
    if (vaccineString == null)
    {
      throw new Exception("Unrecognized vaccine name '" + vaccineName + "'");
    }
    String[] vaccNames = vaccineString.split("\\,");
    int[] vaccineIds = new int[vaccNames.length];
    for (int i = 0; i < vaccNames.length; i++)
    {
      String vaccName = vaccNames[i].trim();
      try
      {
        vaccineIds[i] = Integer.parseInt(vaccName);
      } catch (NumberFormatException nfe)
      {
        vaccineIds[i] = 0;
      }
      if (vaccineIds[i] == 0)
      {
        throw new IllegalArgumentException("Unrecognized vaccine '" + vaccName + "', must be vaccine id");
      }
    }
    return vaccineIds;
  }

  public class Schedule
  {
    private String forecastCode = "";
    private String scheduleName = "";
    private String label = "";
    private TimePeriod validAge = null;
    private TimePeriod validInterval = null;
    private TimePeriod validGrace = null;
    private TimePeriod earlyAge = null;
    private TimePeriod earlyInterval = null;
    private TimePeriod dueAge = null;
    private TimePeriod dueInterval = null;
    private TimePeriod overdueAge = null;
    private TimePeriod overdueInterval = null;
    private TimePeriod finishedAge = null;
    private TimePeriod finishedInterval = null;
    private TimePeriod afterInvalidInterval = null;
    private TimePeriod afterInvalidGrace = null;
    private TimePeriod afterContraInterval = null;
    private TimePeriod afterContraGrace = null;
    private TimePeriod beforePreviousInterval = null;
    private TimePeriod beforePreviousGrace = null;
    private Indicate[] indicates = new Indicate[0];
    private Contraindicate[] contraindicates = new Contraindicate[0];
    private List<Indicate> indicateList = new ArrayList<Indicate>();
    private List<Contraindicate> contraindicateList = new ArrayList<Contraindicate>();
    private String dose = "";
    private String indication = "";
    private int posColumn = 0;
    private int posRow = 0;

    public int getPosColumn()
    {
      return posColumn;
    }

    public void setPosColumn(int posColumn)
    {
      this.posColumn = posColumn;
    }

    public int getPosRow()
    {
      return posRow;
    }

    public void setPosRow(int posRow)
    {
      this.posRow = posRow;
    }

    private void checkForConsistency() throws Exception
    {
      if (validAge.isEmpty() && validInterval.isEmpty())
      {
        throw new Exception("Valid age or valid interval must be defined");
      }
      if (dueAge.isEmpty() && dueInterval.isEmpty())
      {
        throw new Exception("Due age or due interval must be defined");
      }
      if (overdueAge.isEmpty() && overdueInterval.isEmpty())
      {
        throw new Exception("Overdue age or overdue interval must be defined");
      }
    }

    public String getForecastCode()
    {
      return forecastCode;
    }

    public void setForecastCode(String forecastCode)
    {
      this.forecastCode = forecastCode;
    }

    public VaccineForecastDataBean getVaccineForecast()
    {
      return VaccineForecastDataBean.this;
    }

    public String getScheduleName()
    {
      return scheduleName;
    }

    public void setScheduleName(String scheduleName)
    {
      this.scheduleName = scheduleName;
    }

    public String getLabel()
    {
      return label;
    }

    public void setLabel(String label)
    {
      this.label = label;
    }

    public TimePeriod getValidAge()
    {
      return validAge;
    }

    public void setValidAge(TimePeriod validAge)
    {
      this.validAge = validAge;
    }

    public TimePeriod getValidInterval()
    {
      return validInterval;
    }

    public void setValidInterval(TimePeriod validInterval)
    {
      this.validInterval = validInterval;
    }

    public TimePeriod getDueAge()
    {
      return dueAge;
    }

    public void setDueAge(TimePeriod dueAge)
    {
      this.dueAge = dueAge;
    }

    public TimePeriod getDueInterval()
    {
      return dueInterval;
    }

    public void setDueInterval(TimePeriod dueInterval)
    {
      this.dueInterval = dueInterval;
    }

    public TimePeriod getOverdueAge()
    {
      return overdueAge;
    }

    public void setOverdueAge(TimePeriod overdueAge)
    {
      this.overdueAge = overdueAge;
    }

    public TimePeriod getOverdueInterval()
    {
      return overdueInterval;
    }

    public void setOverdueInterval(TimePeriod overdueInterval)
    {
      this.overdueInterval = overdueInterval;
    }

    public TimePeriod getFinishedAge()
    {
      return finishedAge;
    }

    public void setFinishedAge(TimePeriod finishedAge)
    {
      this.finishedAge = finishedAge;
    }

    public TimePeriod getFinishedInterval()
    {
      return finishedInterval;
    }

    public void setFinishedInterval(TimePeriod finishedInterval)
    {
      this.finishedInterval = finishedInterval;
    }

    public TimePeriod getAfterInvalidInterval()
    {
      return afterInvalidInterval;
    }

    public void setAfterInvalidInterval(TimePeriod afterInvalidInterval)
    {
      this.afterInvalidInterval = afterInvalidInterval;
    }

    public TimePeriod getAfterContraInterval()
    {
      return afterContraInterval;
    }

    public void setAfterContraInterval(TimePeriod afterContraInterval)
    {
      this.afterContraInterval = afterContraInterval;
    }

    public Indicate[] getIndicates()
    {
      return indicates;
    }

    public void setIndicates(Indicate[] indicates)
    {
      this.indicates = indicates;
    }

    public Contraindicate[] getContraindicates()
    {
      return contraindicates;
    }

    public void setContraindicates(Contraindicate[] contraindicates)
    {
      this.contraindicates = contraindicates;
    }

    private List<Indicate> getIndicateList()
    {
      return indicateList;
    }

    private void convertIndicateFromListToArray()
    {
      indicates = indicateList.toArray(new Indicate[0]);
      indicateList = null;
    }

    private List<Contraindicate> getContraindicateList()
    {
      return contraindicateList;
    }

    private void convertContraindicateFromListToArray()
    {
      contraindicates = contraindicateList.toArray(new Contraindicate[0]);
      contraindicateList = null;
    }

    public String getDose()
    {
      return dose;
    }

    public void setDose(String dose)
    {
      this.dose = dose;
    }

    public String getIndication()
    {
      return indication;
    }

    public void setIndication(String indication)
    {
      this.indication = indication;
    }

    public TimePeriod getEarlyAge()
    {
      return earlyAge;
    }

    public void setEarlyAge(TimePeriod earlyAge)
    {
      this.earlyAge = earlyAge;
    }

    public TimePeriod getEarlyInterval()
    {
      return earlyInterval;
    }

    public void setEarlyInterval(TimePeriod earlyInterval)
    {
      this.earlyInterval = earlyInterval;
    }

    public TimePeriod getBeforePreviousInterval()
    {
      return beforePreviousInterval;
    }

    public void setBeforePreviousInterval(TimePeriod beforePreviousInterval)
    {
      this.beforePreviousInterval = beforePreviousInterval;
    }

    public TimePeriod getValidGrace()
    {
      return validGrace;
    }

    public void setValidGrace(TimePeriod validGrace)
    {
      this.validGrace = validGrace;
    }

    public TimePeriod getAfterInvalidGrace()
    {
      return afterInvalidGrace;
    }

    public void setAfterInvalidGrace(TimePeriod afterInvalidGrace)
    {
      this.afterInvalidGrace = afterInvalidGrace;
    }

    public TimePeriod getAfterContraGrace()
    {
      return afterContraGrace;
    }

    public void setAfterContraGrace(TimePeriod afterContraGrace)
    {
      this.afterContraGrace = afterContraGrace;
    }

    public TimePeriod getBeforePreviousGrace()
    {
      return beforePreviousGrace;
    }

    public void setBeforePreviousGrace(TimePeriod beforePreviousGrace)
    {
      this.beforePreviousGrace = beforePreviousGrace;
    }

    public void setIndicateList(List<Indicate> indicateList)
    {
      this.indicateList = indicateList;
    }

    public void setContraindicateList(List<Contraindicate> contraindicateList)
    {
      this.contraindicateList = contraindicateList;
    }

    public Map<String, String> getVaccines()
    {
      return vaccines;
    }

  }

  public class Contraindicate
  {
    private int[] vaccines = new int[0];
    private String vaccineName = "";
    private TimePeriod age = null;
    private TimePeriod afterInterval = null;
    private String reason = "";

    public String getVaccineName()
    {
      return vaccineName;
    }

    public void setVaccineName(String vaccineName)
    {
      this.vaccineName = vaccineName;
    }

    public int[] getVaccines()
    {
      return vaccines;
    }

    public void setVaccines(int[] vaccines)
    {
      this.vaccines = vaccines;
    }

    public TimePeriod getAge()
    {
      return age;
    }

    public void setAge(TimePeriod age)
    {
      this.age = age;
    }

    public TimePeriod getAfterInterval()
    {
      return afterInterval;
    }

    public void setAfterInterval(TimePeriod afterInterval)
    {
      this.afterInterval = afterInterval;
    }

    public String getReason()
    {
      return reason;
    }

    public void setReason(String reason)
    {
      this.reason = reason;
    }

  }

  public class Indicate
  {
    private int[] vaccines = new int[0];
    private String scheduleName = "";
    private TimePeriod age = null;
    private TimePeriod minInterval = null;
    private TimePeriod maxInterval = null;
    private String vaccineName = "";
    private String previousVaccineName = "";
    private String historyOfVaccineName = "";
    private int[] previousVaccines = new int[0];
    private int[] historyOfVaccines = new int[0];
    private String reason = "";
    private boolean seasonCompleted = false;

    public String SCHEDULE_INVALID = "INVALID";
    public String SCHEDULE_CONTRA = "CONTRA";
    public String SCHEDULE_COMPLETE = "COMPLETE";

    public String getReason()
    {
      return reason;
    }

    public void setReason(String reason)
    {
      this.reason = reason;
    }

    public boolean isInvalid()
    {
      return scheduleName != null && scheduleName.equalsIgnoreCase(SCHEDULE_INVALID);
    }

    public boolean isContra()
    {
      return scheduleName != null && scheduleName.equalsIgnoreCase(SCHEDULE_CONTRA);
    }

    public boolean isComplete()
    {
      return scheduleName != null && scheduleName.equalsIgnoreCase(SCHEDULE_COMPLETE);
    }

    public int[] getVaccines()
    {
      return vaccines;
    }

    public void setVaccines(int[] vaccines)
    {
      this.vaccines = vaccines;
    }

    public int[] getPreviousVaccines()
    {
      return previousVaccines;
    }

    public void setPreviousVaccines(int[] previousVaccines)
    {
      this.previousVaccines = previousVaccines;
    }

    public int[] getHistoryOfVaccines()
    {
      return historyOfVaccines;
    }

    public void setHistoryOfVaccines(int[] historyOfVaccines)
    {
      this.historyOfVaccines = historyOfVaccines;
    }

    public String getScheduleName()
    {
      return scheduleName;
    }

    public void setScheduleName(String scheduleName)
    {
      this.scheduleName = scheduleName;
    }

    public TimePeriod getAge()
    {
      return age;
    }

    public void setAge(TimePeriod age)
    {
      this.age = age;
    }

    public TimePeriod getMinInterval()
    {
      return minInterval;
    }

    public void setMinInterval(TimePeriod minInterval)
    {
      this.minInterval = minInterval;
    }

    public TimePeriod getMaxInterval()
    {
      return maxInterval;
    }

    public void setMaxInterval(TimePeriod maxInterval)
    {
      this.maxInterval = maxInterval;
    }

    public String getVaccineName()
    {
      return vaccineName;
    }

    public String getPreviousVaccineName()
    {
      return previousVaccineName;
    }

    public String getHistoryOfVaccineName()
    {
      return historyOfVaccineName;
    }

    public void setVaccineName(String vaccineName)
    {
      this.vaccineName = vaccineName;
    }

    public void setPreviousVaccineName(String previousVaccineName)
    {
      this.previousVaccineName = previousVaccineName;
    }

    public void setHistoryOfVaccineName(String historyOfVaccineName)
    {
      this.historyOfVaccineName = historyOfVaccineName;
    }

    public boolean isSeasonCompleted()
    {
      return seasonCompleted;
    }

    public void setSeasonCompleted(boolean seasonCompleted)
    {
      this.seasonCompleted = seasonCompleted;
    }

  }

  public String getForecastCode()
  {
    return forecastCode;
  }

  public void setForecastCode(String forecastCode)
  {
    this.forecastCode = forecastCode;
  }

  public Map<String, Schedule> getSchedules()
  {
    return schedules;
  }

  public void setSchedules(Map<String, Schedule> schedules)
  {
    this.schedules = schedules;
  }

  public Seasonal getSeasonal()
  {
    return seasonal;
  }

  public String getForecastLabel()
  {
    return forecastLabel;
  }

  public void setForecastLabel(String forecastLabel)
  {
    this.forecastLabel = forecastLabel;
  }

  public static Map<String, List<Schedule>> getIndications()
  {
    return indications;
  }

  public int getSortOrder()
  {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder)
  {
    this.sortOrder = sortOrder;
  }

  public Map<String, String> getVaccines()
  {
    return vaccines;
  }

  public class Seasonal
  {
    private TimePeriod start = null;
    private TimePeriod due = null;
    private TimePeriod overdue = null;
    private TimePeriod end = null;

    public TimePeriod getStart()
    {
      return start;
    }

    public void setStart(TimePeriod start)
    {
      this.start = start;
    }

    public TimePeriod getDue()
    {
      return due;
    }

    public void setDue(TimePeriod due)
    {
      this.due = due;
    }

    public TimePeriod getOverdue()
    {
      return overdue;
    }

    public void setOverdue(TimePeriod overdue)
    {
      this.overdue = overdue;
    }

    public TimePeriod getEnd()
    {
      return end;
    }

    public void setEnd(TimePeriod end)
    {
      this.end = end;
    }

  }

}
