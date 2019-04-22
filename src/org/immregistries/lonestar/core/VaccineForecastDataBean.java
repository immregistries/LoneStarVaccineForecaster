package org.immregistries.lonestar.core;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.immregistries.lonestar.core.api.impl.ForecastAntigen;
import org.immregistries.lonestar.core.decisionLogic.DecisionLogic;
import org.immregistries.lonestar.core.decisionLogic.DecisionLogicFactory;
import org.immregistries.lonestar.core.logic.Event;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class VaccineForecastDataBean
{
  // private static Map<String, List<Schedule>> indications = new HashMap<String, List<Schedule>>();

  private String forecastCode = "";
  private String forecastLabel = "";
  private int sortOrder = 0;
  private Map<String, NamedVaccine> vaccines = new HashMap<String, NamedVaccine>();
  private Map<String, Schedule> schedules = new HashMap<String, Schedule>();
  private Seasonal seasonal = null;
  private List<Transition> transitionList = new ArrayList<Transition>();
  private Map<String, DecisionLogic> decisionLogicMap = new HashMap<String, DecisionLogic>();
  private List<InvalidateSameDay> invalidateSameDayList = new ArrayList<InvalidateSameDay>();

  public List<InvalidateSameDay> getInvalidateSameDayList() {
    return invalidateSameDayList;
  }

  public void setInvalidateSameDayList(List<InvalidateSameDay> invalidateSameDayList) {
    this.invalidateSameDayList = invalidateSameDayList;
  }

  protected VaccineForecastDataBean() {
    // default for ForecastSchedule to build object
  }

  public VaccineForecastDataBean(String source, VaccineForecastManagerInterface forecastManager) throws Exception {
    try {
      DocumentBuilderFactory factory;
      DocumentBuilder builder;
      factory = DocumentBuilderFactory.newInstance();
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(true);
      factory.setNamespaceAware(true);
      builder = factory.newDocumentBuilder();
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(source);
      processDocument(builder.parse(new InputSource(is)), forecastManager);
    } catch (Exception exception) {
      throw new Exception("Unable to read XML definition " + source, exception);
    }
  }

  public DecisionLogic getDecisionLogic(String name) {
    return decisionLogicMap.get(name);
  }

  protected Object processDocument(Document node, VaccineForecastManagerInterface forecastManager) throws Exception {
    Node n = node.getFirstChild();
    if (n != null) {
      processNode(n, forecastManager);
    }
    return null;
  }

  protected void processNode(Node n, VaccineForecastManagerInterface forecastManager) throws Exception {
    String name = n.getNodeName();
    if (!name.equals("forecast")) {
      throw new Exception("Root node in definition xml should be 'forecast', instead found '" + name + "'");
    }
    forecastCode = DomUtils.getAttributeValue(n, "seriesName");
    if (forecastCode == null || forecastCode.equals("")) {
      throw new Exception("forecastCode attribute is missing on forecast tag");
    }
    ForecastAntigen forecastAntigen = ForecastAntigen.getForecastAntigen(forecastCode);
    if (forecastAntigen != null) {
      forecastLabel = forecastAntigen.getForecastLabel();
      sortOrder = forecastAntigen.getSortOrder();
    }
    String completesString = DomUtils.getAttributeValue(n, "completes");
    if (completesString.equals("")) {
      completesString = forecastCode;
    }
    String[] completes = completesString.split("\\,");
    if (completes.length == 0 || completes[0] == null || completes[0].equals("")) {
      throw new Exception("completes must indicate at least one completion for forecastCode " + forecastCode
          + ", trying to read '" + completesString + "'");
    }
    List<ForecastAntigen> completesList = new ArrayList<ForecastAntigen>();
    for (String complete : completes) {
      ForecastAntigen fa = ForecastAntigen.getForecastAntigen(complete);
      if (fa == null) {
        throw new Exception("forecastCode '" + complete + "' in completes string is not recognized for forecastCode "
            + forecastCode);
      }
      completesList.add(fa);
    }
    NodeList l = n.getChildNodes();
    for (int i = 0, icount = l.getLength(); i < icount; i++) {
      n = l.item(i);
      name = n.getNodeName();
      processScheduleOrVaccine(n, name, completesList, forecastCode, forecastManager);
    }
  }

  private void processScheduleOrVaccine(Node n, String name, List<ForecastAntigen> completesList, String forecastCode,
      VaccineForecastManagerInterface forecastManager) throws Exception {
    if (name != null) {
      if (name.equals("schedule")) {
        Schedule schedule = new Schedule();
        schedule.setForecastCode(forecastCode);
        schedule.setCompletesList(completesList);
        schedule.setScheduleName(DomUtils.getAttributeValue(n, "scheduleName"));
        schedule.setLabel(DomUtils.getAttributeValue(n, "label"));
        schedule.setDose(DomUtils.getAttributeValue(n, "dose"));
        schedule.setIndication(DomUtils.getAttributeValue(n, "indication"));
        if (schedule.getIndication().toUpperCase().startsWith("AGE ")) {
          schedule.setIndicationAge(new TimePeriod(schedule.getIndication().substring(4)));
          schedule.setIndication("AGE");
        }
        String indicationEndString = DomUtils.getAttributeValue(n, "indicationEnd");
        if (!indicationEndString.equals("")) {
          schedule.setIndicationEndAge(new TimePeriod(indicationEndString));
        }
        addToIndicationList(schedule, forecastManager);
        schedules.put(schedule.getScheduleName(), schedule);
        NodeList l = n.getChildNodes();
        for (int i = 0, icount = l.getLength(); i < icount; i++) {
          n = l.item(i);
          name = n.getNodeName();
          processScheduleItem(n, name, schedule);
          if (schedule.getIndicates() == null) {
            throw new Exception("No indicates defined for schedule " + schedule.getScheduleName());
          }
        }
        schedule.convertIndicateFromListToArray();
        schedule.convertContraindicateFromListToArray();
        schedule.checkForConsistency();
      } else if (name.equals("vaccine")) {
        NamedVaccine namedVaccine = new NamedVaccine();
        String vaccineName = DomUtils.getAttributeValue(n, "vaccineName");
        if (vaccineName == null || vaccineName.equals("")) {
          throw new Exception("vaccineName attribute is missing on vaccine tag");
        }
        String vaccineIds = DomUtils.getAttributeValue(n, "vaccineIds");
        if (vaccineIds == null || vaccineIds.equals("")) {
          throw new Exception("vaccineIds attribute is missing on vaccine tag");
        }
        namedVaccine.setVaccineIds(vaccineIds);
        namedVaccine.setVaccineName(vaccineName);
        String validStartDate = DomUtils.getAttributeValue(n, "validStartDate");
        if (validStartDate != null && validStartDate.length() > 0) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          namedVaccine.setValidStartDate(sdf.parse(validStartDate));
        }
        String validAge = DomUtils.getAttributeValue(n, "validAge");
        if (validAge != null && validAge.length() > 0) {
          namedVaccine.setValidAge(new TimePeriod(validAge));
        }
        vaccines.put(vaccineName.toUpperCase(), namedVaccine);
      } else if (name.equals("invalidateSameDay")) {
        InvalidateSameDay invalidateSameDay = new InvalidateSameDay();
        String vaccineName = DomUtils.getAttributeValue(n, "vaccineName");
        invalidateSameDay.setInvalidateVaccineName(vaccineName);
        invalidateSameDay.setInvalidateVaccines(convertToVaccineIds(vaccineName));
        vaccineName = DomUtils.getAttributeValue(n, "ifGiven");
        invalidateSameDay.setIfGivenVaccineName(vaccineName);
        invalidateSameDay.setIfGivenVaccines(convertToVaccineIds(vaccineName));
        invalidateSameDayList.add(invalidateSameDay);
      } else if (name.equals("seasonal")) {
        seasonal = new Seasonal();
        String seasonalDue = DomUtils.getAttributeValue(n, "due");
        String seasonalOverdue = DomUtils.getAttributeValue(n, "overdue");
        String seasonalEnd = DomUtils.getAttributeValue(n, "end");
        String seasonalFinished = DomUtils.getAttributeValue(n, "finished");
        seasonal.setDue(new TimePeriod(seasonalDue));
        seasonal.setOverdue(new TimePeriod(seasonalOverdue));
        seasonal.setEnd(new TimePeriod(seasonalEnd));
        if (seasonalFinished != null && !seasonalFinished.equals("")) {
          seasonal.setFinished(new TimePeriod(seasonalFinished));
        }
      } else if (name.equals("transition")) {
        Transition transition = new Transition();
        String transitionName = DomUtils.getAttributeValue(n, "name");
        String transitionAge = DomUtils.getAttributeValue(n, "age");
        int transitionVaccineId = DomUtils.getAttributeValueInt(n, "vaccineId");
        transition.setName(transitionName);
        transition.setAge(new TimePeriod(transitionAge));
        transition.setVaccineId(transitionVaccineId);
        transitionList.add(transition);
      } else if (name.equals("decisionLogic")) {
        String decisionLogicName = DomUtils.getAttributeValue(n, "name");
        DecisionLogic decisionLogic = DecisionLogicFactory.getDecisionLogic(decisionLogicName);
        if (decisionLogic != null) {
          NodeList l = n.getChildNodes();
          for (int i = 0, icount = l.getLength(); i < icount; i++) {
            n = l.item(i);
            name = n.getNodeName();
            processDecisionLogicItem(n, name, decisionLogic);
          }
          decisionLogicMap.put(decisionLogicName, decisionLogic);
        }
      }
    }
  }

  private void addToIndicationList(Schedule schedule, VaccineForecastManagerInterface forecastManager) throws Exception {
    if (schedule.getIndication() != null && !schedule.getIndication().equals("")) {
      Map<String, List<Schedule>> indicationsMap = forecastManager.getIndicationsMap();
      List<Schedule> indicationList = indicationsMap.get(schedule.getIndication());
      if (indicationList == null) {
        indicationList = new ArrayList<Schedule>();
        indicationsMap.put(schedule.getIndication(), indicationList);
      }
      indicationList.add(schedule);
    }
  }

  private void processDecisionLogicItem(Node n, String name, DecisionLogic decisionLogic) throws Exception {
    if (name != null) {
      String mapName = DomUtils.getAttributeValue(n, "name");
      String mapValue = DomUtils.getAttributeValue(n, "value");
      if (name.equals("constant")) {
        decisionLogic.getConstantMap().put(mapName, mapValue);
      } else if (name.equals("transition")) {
        decisionLogic.getTransitionMap().put(mapName, mapValue);
      }
    }
  }

  private void processScheduleItem(Node n, String name, Schedule schedule) throws Exception {
    if (name != null) {
      if (name.equals("indicationCriteria")) {
        IndicationCriteria indicationCriteria = new IndicationCriteria();
        schedule.setIndicationCriteria(indicationCriteria);
        indicationCriteria.setAfterAge(new TimePeriod(DomUtils.getAttributeValue(n, "afterAge")));
        indicationCriteria.setBeforeAge(new TimePeriod(DomUtils.getAttributeValue(n, "beforeAge")));
        String vaccineName = DomUtils.getAttributeValue(n, "vaccineName");
        indicationCriteria.setVaccineName(vaccineName);
        indicationCriteria.setVaccines(convertToVaccineIds(vaccineName));
      } else if (name.equals("valid")) {
        schedule.setValidAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setValidInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
        schedule.setValidGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
        String intervalGraceString = DomUtils.getAttributeValue(n, "intervalGrace");
        if (!intervalGraceString.equals("")) {
          schedule.setValidIntervalGrace(new TimePeriod(intervalGraceString));
        }
      } else if (name.equals("early")) {
        schedule.setEarlyAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setEarlyInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("due")) {
        schedule.setDueAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setDueInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("earlyOverdue")) {
        schedule.setEarlyOverdueAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setEarlyOverdueInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("overdue")) {
        schedule.setOverdueAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setOverdueInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("finished")) {
        schedule.setFinishedAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setFinishedInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
      } else if (name.equals("assumeComplete")) {
        schedule.setAssumeCompleteAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        schedule.setAssumeCompleteReason(DomUtils.getAttributeValue(n, "reason"));
      } else if (name.equals("after-invalid")) {
        schedule.setAfterInvalidInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
        schedule.setAfterInvalidGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
      } else if (name.equals("after-contra")) {
        schedule.setAfterContraInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
        schedule.setAfterContraGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
      } else if (name.equals("before-previous")) {
        schedule.setBeforePreviousInterval(new TimePeriod(DomUtils.getAttributeValue(n, "interval")));
        schedule.setBeforePreviousGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
      } else if (name.equals("pos")) {
        schedule.setPosColumn(DomUtils.getAttributeValueInt(n, "column"));
        schedule.setPosRow(DomUtils.getAttributeValueInt(n, "row"));
      } else if (name.equals("recommend")) {
        String recommendString = DomUtils.getAttributeValue(n, "seriesName");
        if (!recommendString.equals("")) {
          ForecastAntigen fa = ForecastAntigen.getForecastAntigen(recommendString);
          schedule.setRecommend(fa);
        }
      } else if (name.equals("completed")) {
        String completedString = DomUtils.getAttributeValue(n, "seriesName");
        if (!completedString.equals("")) {
          ForecastAntigen fa = ForecastAntigen.getForecastAntigen(completedString);
          schedule.setCompleted(fa);
        }
      } else if (name.equals("contraindicate")) {
        Contraindicate contraindicate = new Contraindicate();
        String vaccineName = DomUtils.getAttributeValue(n, "vaccineName");
        contraindicate.setVaccineName(vaccineName);
        contraindicate.setVaccines(convertToVaccineIds(vaccineName));
        contraindicate.setAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        contraindicate.setAfterInterval(new TimePeriod(DomUtils.getAttributeValue(n, "afterInterval")));
        contraindicate.setReason(DomUtils.getAttributeValue(n, "reason"));
        contraindicate.setGrace(new TimePeriod(DomUtils.getAttributeValue(n, "grace")));
        contraindicate.setAgainst(DomUtils.getAttributeValue(n, "against"));
        if (contraindicate.hasAgainst()) {
          contraindicate.setAgainstVaccines(convertToVaccineIds(contraindicate.getAgainst()));
          contraindicate.setAgainstContra(DomUtils.getAttributeValue(n, "contra"));
          contraindicate.setAgainstAllowed(DomUtils.getAttributeValue(n, "allowed"));
        }
        schedule.getContraindicateList().add(contraindicate);
      } else if (name.equals("indicate")) {
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
        indicate.setHistoryOfVaccineValidAge(readValidAge(historyOfVaccineName));
        indicate.setScheduleName(DomUtils.getAttributeValue(n, "schedule"));
        indicate.setAge(new TimePeriod(DomUtils.getAttributeValue(n, "age")));
        indicate.setMinInterval(new TimePeriod(DomUtils.getAttributeValue(n, "minInterval")));
        indicate.setMaxInterval(new TimePeriod(DomUtils.getAttributeValue(n, "maxInterval")));
        indicate.setReason(DomUtils.getAttributeValue(n, "reason"));
        indicate.setSeasonCompleted("Yes".equalsIgnoreCase(DomUtils.getAttributeValue(n, "seasonCompleted")));
        indicate.setHasHad(DomUtils.getAttributeValue(n, "hasHad"));
        if (indicate.isHashHad()) {
          indicate.setHasHadVaccines(convertToVaccineIds(indicate.getHasHad()));
        }
        schedule.getIndicateList().add(indicate);
      }
    }
  }

  public TimePeriod readValidAge(String vaccineName) throws Exception {
    if (vaccineName == null || vaccineName.equals("")) {
      return null;
    }
    NamedVaccine namedVaccine = vaccines.get(vaccineName.toUpperCase());
    if (namedVaccine == null) {
      throw new Exception("Unrecognized vaccine name '" + vaccineName + "'");
    }
    return namedVaccine.getValidAge();

  }

  public ValidVaccine[] convertToVaccineIds(String vaccineName) throws Exception {
    if (vaccineName == null || vaccineName.length() == 0) {
      return new ValidVaccine[0];
    }
    NamedVaccine namedVaccine = vaccines.get(vaccineName.toUpperCase());
    if (namedVaccine == null) {
      throw new Exception("Unrecognized vaccine name '" + vaccineName + "'");
    }
    String vaccineString = namedVaccine.getVaccineIds();
    if (vaccineString == null) {
      throw new Exception("Unrecognized vaccine name '" + vaccineName + "'");
    }
    String[] vaccNames = vaccineString.split("\\,");
    ValidVaccine[] validVaccines = new ValidVaccine[vaccNames.length];
    for (int i = 0; i < vaccNames.length; i++) {
      ValidVaccine validVaccine = new ValidVaccine();
      validVaccines[i] = validVaccine;
      String vaccName = vaccNames[i].trim();
      try {
        validVaccine.setVaccineId(Integer.parseInt(vaccName));
      } catch (NumberFormatException nfe) {
        validVaccine.setVaccineId(0);
      }
      if (validVaccine.getVaccineId() == 0) {
        throw new IllegalArgumentException("Unrecognized vaccine '" + vaccName + "', must be vaccine id");
      }
      validVaccine.setValidStartDate(namedVaccine.getValidStartDate());
      validVaccine.setValidAge(namedVaccine.getValidAge());
    }
    return validVaccines;
  }

  public class Schedule
  {
    private String forecastCode = "";
    private List<ForecastAntigen> completesList = new ArrayList<ForecastAntigen>();
    private String scheduleName = "";
    private String label = "";
    private TimePeriod validAge = null;
    private TimePeriod validInterval = null;
    private TimePeriod validGrace = null;
    private TimePeriod validIntervalGrace = null;
    private TimePeriod earlyAge = null;
    private TimePeriod earlyInterval = null;
    private TimePeriod earlyOverdueAge = null;
    private TimePeriod earlyOverdueInterval = null;
    private TimePeriod dueAge = null;
    private TimePeriod dueInterval = null;
    private TimePeriod overdueAge = null;
    private TimePeriod overdueInterval = null;
    private TimePeriod finishedAge = null;
    private TimePeriod assumeCompleteAge = null;
    private String assumeCompleteReason = "";
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
    private ForecastAntigen recommend = null;
    private ForecastAntigen completed = null;
    private String dose = "";
    private String indication = "";
    private TimePeriod indicationAge = null;
    private TimePeriod indicationEndAge = null;
    private int posColumn = 0;
    private int posRow = 0;
    private IndicationCriteria indicationCriteria = null;

    public List<ForecastAntigen> getCompletesList() {
      return completesList;
    }

    public void setCompletesList(List<ForecastAntigen> completesList) {
      this.completesList = completesList;
    }

    public TimePeriod getIndicationEndAge() {
      return indicationEndAge;
    }

    public void setIndicationEndAge(TimePeriod indicationEndAge) {
      this.indicationEndAge = indicationEndAge;
    }

    public TimePeriod getIndicationAge() {
      return indicationAge;
    }

    public void setIndicationAge(TimePeriod indicationAge) {
      this.indicationAge = indicationAge;
    }

    public ForecastAntigen getRecommend() {
      return recommend;
    }

    public void setRecommend(ForecastAntigen recommend) {
      this.recommend = recommend;
    }

    public TimePeriod getAssumeCompleteAge() {
      return assumeCompleteAge;
    }

    public ForecastAntigen getCompleted() {
      return completed;
    }

    public void setCompleted(ForecastAntigen completed) {
      this.completed = completed;
    }

    public void setAssumeCompleteAge(TimePeriod assumeCompleteAge) {
      this.assumeCompleteAge = assumeCompleteAge;
    }

    public String getAssumeCompleteReason() {
      return assumeCompleteReason;
    }

    public void setAssumeCompleteReason(String assumeCompleteReason) {
      this.assumeCompleteReason = assumeCompleteReason;
    }

    public TimePeriod getEarlyOverdueAge() {
      return earlyOverdueAge;
    }

    public void setEarlyOverdueAge(TimePeriod earlyOverdueAge) {
      this.earlyOverdueAge = earlyOverdueAge;
    }

    public TimePeriod getEarlyOverdueInterval() {
      return earlyOverdueInterval;
    }

    public void setEarlyOverdueInterval(TimePeriod earlyOverdueInterval) {
      this.earlyOverdueInterval = earlyOverdueInterval;
    }

    public IndicationCriteria getIndicationCriteria() {
      return indicationCriteria;
    }

    public void setIndicationCriteria(IndicationCriteria indicateCriteria) {
      this.indicationCriteria = indicateCriteria;
    }

    public int getPosColumn() {
      return posColumn;
    }

    public void setPosColumn(int posColumn) {
      this.posColumn = posColumn;
    }

    public int getPosRow() {
      return posRow;
    }

    public void setPosRow(int posRow) {
      this.posRow = posRow;
    }

    private void checkForConsistency() throws Exception {
      if (validAge.isEmpty() && validInterval.isEmpty()) {
        throw new Exception("Valid age or valid interval must be defined");
      }
      if (dueAge.isEmpty() && dueInterval.isEmpty()) {
        throw new Exception("Due age or due interval must be defined");
      }
      if (overdueAge.isEmpty() && overdueInterval.isEmpty()) {
        throw new Exception("Overdue age or overdue interval must be defined");
      }
    }

    public String getForecastCode() {
      return forecastCode;
    }

    public void setForecastCode(String forecastCode) {
      this.forecastCode = forecastCode;
    }

    public VaccineForecastDataBean getVaccineForecast() {
      return VaccineForecastDataBean.this;
    }

    public String getScheduleName() {
      return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
      this.scheduleName = scheduleName;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public TimePeriod getValidAge() {
      return validAge;
    }

    public void setValidAge(TimePeriod validAge) {
      this.validAge = validAge;
    }

    public TimePeriod getValidInterval() {
      return validInterval;
    }

    public void setValidInterval(TimePeriod validInterval) {
      this.validInterval = validInterval;
    }

    public TimePeriod getDueAge() {
      return dueAge;
    }

    public void setDueAge(TimePeriod dueAge) {
      this.dueAge = dueAge;
    }

    public TimePeriod getDueInterval() {
      return dueInterval;
    }

    public void setDueInterval(TimePeriod dueInterval) {
      this.dueInterval = dueInterval;
    }

    public TimePeriod getOverdueAge() {
      return overdueAge;
    }

    public void setOverdueAge(TimePeriod overdueAge) {
      this.overdueAge = overdueAge;
    }

    public TimePeriod getOverdueInterval() {
      return overdueInterval;
    }

    public void setOverdueInterval(TimePeriod overdueInterval) {
      this.overdueInterval = overdueInterval;
    }

    public TimePeriod getFinishedAge() {
      return finishedAge;
    }

    public void setFinishedAge(TimePeriod finishedAge) {
      this.finishedAge = finishedAge;
    }

    public TimePeriod getFinishedInterval() {
      return finishedInterval;
    }

    public void setFinishedInterval(TimePeriod finishedInterval) {
      this.finishedInterval = finishedInterval;
    }

    public TimePeriod getAfterInvalidInterval() {
      return afterInvalidInterval;
    }

    public void setAfterInvalidInterval(TimePeriod afterInvalidInterval) {
      this.afterInvalidInterval = afterInvalidInterval;
    }

    public TimePeriod getAfterContraInterval() {
      return afterContraInterval;
    }

    public void setAfterContraInterval(TimePeriod afterContraInterval) {
      this.afterContraInterval = afterContraInterval;
    }

    public Indicate[] getIndicates() {
      return indicates;
    }

    public void setIndicates(Indicate[] indicates) {
      this.indicates = indicates;
    }

    public Contraindicate[] getContraindicates() {
      return contraindicates;
    }

    public void setContraindicates(Contraindicate[] contraindicates) {
      this.contraindicates = contraindicates;
    }

    private List<Indicate> getIndicateList() {
      return indicateList;
    }

    private void convertIndicateFromListToArray() {
      indicates = indicateList.toArray(new Indicate[0]);
      indicateList = null;
    }

    private List<Contraindicate> getContraindicateList() {
      return contraindicateList;
    }

    private void convertContraindicateFromListToArray() {
      contraindicates = contraindicateList.toArray(new Contraindicate[0]);
      contraindicateList = null;
    }

    public String getDose() {
      return dose;
    }

    public void setDose(String dose) {
      this.dose = dose;
    }

    public String getIndication() {
      return indication;
    }

    public void setIndication(String indication) {
      this.indication = indication;
    }

    public TimePeriod getEarlyAge() {
      return earlyAge;
    }

    public void setEarlyAge(TimePeriod earlyAge) {
      this.earlyAge = earlyAge;
    }

    public TimePeriod getEarlyInterval() {
      return earlyInterval;
    }

    public void setEarlyInterval(TimePeriod earlyInterval) {
      this.earlyInterval = earlyInterval;
    }

    public TimePeriod getBeforePreviousInterval() {
      return beforePreviousInterval;
    }

    public void setBeforePreviousInterval(TimePeriod beforePreviousInterval) {
      this.beforePreviousInterval = beforePreviousInterval;
    }

    public TimePeriod getValidIntervalGrace() {
      return validIntervalGrace;
    }

    public void setValidIntervalGrace(TimePeriod validIntervalGrace) {
      this.validIntervalGrace = validIntervalGrace;
    }

    public TimePeriod getValidGrace() {
      return validGrace;
    }

    public void setValidGrace(TimePeriod validGrace) {
      this.validGrace = validGrace;
    }

    public TimePeriod getAfterInvalidGrace() {
      return afterInvalidGrace;
    }

    public void setAfterInvalidGrace(TimePeriod afterInvalidGrace) {
      this.afterInvalidGrace = afterInvalidGrace;
    }

    public TimePeriod getAfterContraGrace() {
      return afterContraGrace;
    }

    public void setAfterContraGrace(TimePeriod afterContraGrace) {
      this.afterContraGrace = afterContraGrace;
    }

    public TimePeriod getBeforePreviousGrace() {
      return beforePreviousGrace;
    }

    public void setBeforePreviousGrace(TimePeriod beforePreviousGrace) {
      this.beforePreviousGrace = beforePreviousGrace;
    }

    public void setIndicateList(List<Indicate> indicateList) {
      this.indicateList = indicateList;
    }

    public void setContraindicateList(List<Contraindicate> contraindicateList) {
      this.contraindicateList = contraindicateList;
    }

    public Map<String, NamedVaccine> getVaccines() {
      return vaccines;
    }

  }

  public class Contraindicate
  {
    private ValidVaccine[] vaccines = new ValidVaccine[0];
    private String vaccineName = "";
    private TimePeriod age = null;
    private TimePeriod afterInterval = null;
    private TimePeriod grace = null;
    private String reason = "";
    private String against = "";
    private ValidVaccine[] againstVaccines = new ValidVaccine[0];
    private String againstContra = "";
    private String againstAllowed = "";

    public String getAgainstContra() {
      return againstContra;
    }

    public void setAgainstContra(String againstContra) {
      this.againstContra = againstContra;
    }

    public String getAgainstAllowed() {
      return againstAllowed;
    }

    public void setAgainstAllowed(String againstAllowed) {
      this.againstAllowed = againstAllowed;
    }

    public ValidVaccine[] getAgainstVaccines() {
      return againstVaccines;
    }

    public void setAgainstVaccines(ValidVaccine[] againstVaccines) {
      this.againstVaccines = againstVaccines;
    }

    public String getAgainst() {
      return against;
    }

    public boolean hasAgainst() {
      return against != null && against.length() > 0;
    }

    public void setAgainst(String against) {
      this.against = against;
    }

    public TimePeriod getGrace() {
      return grace;
    }

    public void setGrace(TimePeriod grace) {
      this.grace = grace;
    }

    public String getVaccineName() {
      return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
      this.vaccineName = vaccineName;
    }

    public ValidVaccine[] getVaccines() {
      return vaccines;
    }

    public void setVaccines(ValidVaccine[] vaccines) {
      this.vaccines = vaccines;
    }

    public TimePeriod getAge() {
      return age;
    }

    public void setAge(TimePeriod age) {
      this.age = age;
    }

    public TimePeriod getAfterInterval() {
      return afterInterval;
    }

    public void setAfterInterval(TimePeriod afterInterval) {
      this.afterInterval = afterInterval;
    }

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }

  }

  public class IndicationCriteria
  {
    private TimePeriod afterAge = null;
    private TimePeriod beforeAge = null;
    private String vaccineName = "";
    private ValidVaccine[] vaccines = new ValidVaccine[0];

    public String getVaccineName() {
      return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
      this.vaccineName = vaccineName;
    }

    public TimePeriod getAfterAge() {
      return afterAge;
    }

    public void setAfterAge(TimePeriod afterAge) {
      this.afterAge = afterAge;
    }

    public TimePeriod getBeforeAge() {
      return beforeAge;
    }

    public void setBeforeAge(TimePeriod beforeAge) {
      this.beforeAge = beforeAge;
    }

    public ValidVaccine[] getVaccines() {
      return vaccines;
    }

    public void setVaccines(ValidVaccine[] vaccines) {
      this.vaccines = vaccines;
    }

  }

  public class Indicate
  {
    private ValidVaccine[] vaccines = new ValidVaccine[0];
    private String scheduleName = "";
    private TimePeriod age = null;
    private TimePeriod minInterval = null;
    private TimePeriod maxInterval = null;
    private String vaccineName = "";
    private String previousVaccineName = "";
    private String historyOfVaccineName = "";
    private TimePeriod historyOfVaccineValidAge = null;
    private ValidVaccine[] previousVaccines = new ValidVaccine[0];
    private ValidVaccine[] historyOfVaccines = new ValidVaccine[0];
    private String reason = "";
    private boolean seasonCompleted = false;
    private String hasHad = "";
    public ValidVaccine[] hasHadVaccines = new ValidVaccine[0];

    public String SCHEDULE_INVALID = "INVALID";
    public String SCHEDULE_CONTRA = "CONTRA";
    public String SCHEDULE_COMPLETE = "COMPLETE";
    public String SCHEDULE_FINISHED = "FINISHED";

    public TimePeriod getHistoryOfVaccineValidAge() {
      return historyOfVaccineValidAge;
    }

    public void setHistoryOfVaccineValidAge(TimePeriod historyOfVaccineValidAge) {
      this.historyOfVaccineValidAge = historyOfVaccineValidAge;
    }

    public ValidVaccine[] getHasHadVaccines() {
      return hasHadVaccines;
    }

    public void setHasHadVaccines(ValidVaccine[] hasHadVaccines) {
      this.hasHadVaccines = hasHadVaccines;
    }

    public String getReason() {
      return reason;
    }

    public void setReason(String reason) {
      this.reason = reason;
    }

    public boolean isInvalid() {
      return scheduleName != null && scheduleName.equalsIgnoreCase(SCHEDULE_INVALID);
    }

    public boolean isContra() {
      return scheduleName != null && scheduleName.equalsIgnoreCase(SCHEDULE_CONTRA);
    }

    public boolean isComplete() {
      return scheduleName != null && scheduleName.equalsIgnoreCase(SCHEDULE_COMPLETE);
    }

    public ValidVaccine[] getVaccines() {
      return vaccines;
    }

    public void setVaccines(ValidVaccine[] vaccines) {
      this.vaccines = vaccines;
    }

    public ValidVaccine[] getPreviousVaccines() {
      return previousVaccines;
    }

    public void setPreviousVaccines(ValidVaccine[] previousVaccines) {
      this.previousVaccines = previousVaccines;
    }

    public ValidVaccine[] getHistoryOfVaccines() {
      return historyOfVaccines;
    }

    public void setHistoryOfVaccines(ValidVaccine[] historyOfVaccines) {
      this.historyOfVaccines = historyOfVaccines;
    }

    public String getScheduleName() {
      return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
      this.scheduleName = scheduleName;
    }

    public TimePeriod getAge() {
      return age;
    }

    public void setAge(TimePeriod age) {
      this.age = age;
    }

    public TimePeriod getMinInterval() {
      return minInterval;
    }

    public String getHasHad() {
      return hasHad;
    }

    public boolean isHashHad() {
      return hasHad != null && !hasHad.equals("");
    }

    public void setHasHad(String hasHad) {
      this.hasHad = hasHad;
    }

    public void setMinInterval(TimePeriod minInterval) {
      this.minInterval = minInterval;
    }

    public TimePeriod getMaxInterval() {
      return maxInterval;
    }

    public void setMaxInterval(TimePeriod maxInterval) {
      this.maxInterval = maxInterval;
    }

    public String getVaccineName() {
      return vaccineName;
    }

    public String getPreviousVaccineName() {
      return previousVaccineName;
    }

    public String getHistoryOfVaccineName() {
      return historyOfVaccineName;
    }

    public void setVaccineName(String vaccineName) {
      this.vaccineName = vaccineName;
    }

    public void setPreviousVaccineName(String previousVaccineName) {
      this.previousVaccineName = previousVaccineName;
    }

    public void setHistoryOfVaccineName(String historyOfVaccineName) {
      this.historyOfVaccineName = historyOfVaccineName;
    }

    public boolean isSeasonCompleted() {
      return seasonCompleted;
    }

    public void setSeasonCompleted(boolean seasonCompleted) {
      this.seasonCompleted = seasonCompleted;
    }

  }

  public String getForecastCode() {
    return forecastCode;
  }

  public void setForecastCode(String forecastCode) {
    this.forecastCode = forecastCode;
  }

  public Map<String, Schedule> getSchedules() {
    return schedules;
  }

  public void setSchedules(Map<String, Schedule> schedules) {
    this.schedules = schedules;
  }

  public Seasonal getSeasonal() {
    return seasonal;
  }

  public List<Transition> getTransitionList() {
    return transitionList;
  }

  public String getForecastLabel() {
    return forecastLabel;
  }

  public void setForecastLabel(String forecastLabel) {
    this.forecastLabel = forecastLabel;
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    this.sortOrder = sortOrder;
  }

  public Map<String, NamedVaccine> getVaccines() {
    return vaccines;
  }

  /**
   * Determines if the specified vaccineId appears in the list of the vaccines for this forecast
   */
  public boolean isVaccinePresent(String vaccineId) {
    boolean isVaccinePresent = false;
    for ( NamedVaccine namedVaccine : vaccines.values() ) {
      isVaccinePresent |= namedVaccine.getVaccineIds().contains(vaccineId);
    }
    return isVaccinePresent;
  }
  
  public class NamedVaccine
  {
    private String vaccineIds = "";
    private String vaccineName = "";
    private Date validStartDate = null;
    private TimePeriod validAge = null;

    public TimePeriod getValidAge() {
      return validAge;
    }

    public void setValidAge(TimePeriod validAge) {
      this.validAge = validAge;
    }

    public String getVaccineIds() {
      return vaccineIds;
    }

    public void setVaccineIds(String vaccineIds) {
      this.vaccineIds = vaccineIds;
    }

    public String getVaccineName() {
      return vaccineName;
    }

    public void setVaccineName(String vaccineName) {
      this.vaccineName = vaccineName;
    }

    public Date getValidStartDate() {
      return validStartDate;
    }

    public void setValidStartDate(Date validStartDate) {
      this.validStartDate = validStartDate;
    }
  }

  public class InvalidateSameDay
  {
    private String invalidateVaccineName = "";
    private ValidVaccine[] invalidateVaccines = new ValidVaccine[0];
    private String ifGivenVaccineName = "";
    private ValidVaccine[] ifGivenVaccines = new ValidVaccine[0];

    public String getInvalidateVaccineName() {
      return invalidateVaccineName;
    }

    public void setInvalidateVaccineName(String invalidateVaccineName) {
      this.invalidateVaccineName = invalidateVaccineName;
    }

    public ValidVaccine[] getInvalidateVaccines() {
      return invalidateVaccines;
    }

    public void setInvalidateVaccines(ValidVaccine[] invalidateVaccines) {
      this.invalidateVaccines = invalidateVaccines;
    }

    public String getIfGivenVaccineName() {
      return ifGivenVaccineName;
    }

    public void setIfGivenVaccineName(String ifGivenVaccineName) {
      this.ifGivenVaccineName = ifGivenVaccineName;
    }

    public ValidVaccine[] getIfGivenVaccines() {
      return ifGivenVaccines;
    }

    public void setIfGivenVaccines(ValidVaccine[] ifGivenVaccines) {
      this.ifGivenVaccines = ifGivenVaccines;
    }

  }

  public class ValidVaccine
  {
    private int vaccineId = 0;
    private Date validStartDate = null;
    private Date validEndDate = null;
    private TimePeriod validAge = null;

    public TimePeriod getValidAge() {
      return validAge;
    }

    public void setValidAge(TimePeriod validAge) {
      this.validAge = validAge;
    }

    @Override
    public String toString() {
      if (validStartDate != null) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return Integer.toString(vaccineId) + " given before " + sdf.format(validStartDate);
      }
      return Integer.toString(vaccineId);
    }

    public int getVaccineId() {
      return vaccineId;
    }

    public void setVaccineId(int vaccineId) {
      this.vaccineId = vaccineId;
    }

    public Date getValidStartDate() {
      return validStartDate;
    }

    public void setValidStartDate(Date validStartDate) {
      this.validStartDate = validStartDate;
    }

    public Date getValidEndDate() {
      return validEndDate;
    }

    public void setValidEndDate(Date validEndDate) {
      this.validEndDate = validEndDate;
    }

    public boolean isSame(ImmunizationInterface imm, Event event) {
      return isSame(imm.getVaccineId(), event == null ? null : event.getEventDate());
    }

    public boolean isSame(int vaccineId, Date adminDate) {
      if (this.vaccineId != vaccineId) {
        return false;
      }
      if (adminDate != null) {
        if (validStartDate != null) {
          if (adminDate.before(validStartDate)) {
            return false;
          }
        }
        if (validEndDate != null) {
          if (validEndDate.before(adminDate)) {
            return false;
          }
        }
      }
      return true;
    }
  }

}
