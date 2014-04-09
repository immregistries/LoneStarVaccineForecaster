package org.tch.forecast.core.api.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.ForecastSchedule;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.VaccineForecastManagerInterface;

public class VaccineForecastManager implements VaccineForecastManagerInterface
{
  private boolean initialized = false;
  private Map<String, List<Schedule>> indications = new HashMap<String, List<Schedule>>();
  private ForecastSchedule forecastSchedule = null;
  private String forecastScheduleLocation = "ForecastSchedule.xml";
  private String forecastScheduleText = null;

  public String getForecastScheduleLocation() {
    return forecastScheduleLocation;
  }

  public void setForecastScheduleLocation(String forecastScheduleLocation) {
    this.forecastScheduleLocation = forecastScheduleLocation;
  }


  public String getForecastScheduleText() {
    return forecastScheduleText;
  }

  public void setForecastScheduleText(String forecastScheduleText) {
    this.forecastScheduleText = forecastScheduleText;
  }

  public VaccineForecastManager() throws Exception {
    // default;
  }

  public VaccineForecastManager(String forecastScheduleLocation) throws Exception {
    this.forecastScheduleLocation = forecastScheduleLocation;
  }

  public void reset() {
    initialized = false;
    indications = new HashMap<String, List<Schedule>>();
  }

  public Schedule getSchedule(String lineCode) throws Exception {
    init();
    for (List<Schedule> scheduleList : indications.values()) {
      for (Schedule schedule : scheduleList) {
        if (schedule.getForecastCode().equals(lineCode)) {
          return schedule;
        }
      }
    }
    return null;
  }

  public ForecastSchedule getForecastSchedule() {
    return forecastSchedule;
  }

  public Map<String, List<Schedule>> getIndicationsMap() {
    return indications;
  }

  public List<Schedule> getIndications(String indication) throws Exception {
    init();
    return indications.get(indication);
  }

  public String getVaccineName(int id) {
    if (vaccineIdToLabelMap.containsKey(id)) {
      return vaccineIdToLabelMap.get(id);
    } else {
      return "Unknown vaccine " + id;
    }
  }

  private static HashMap<Integer, String> vaccineIdToLabelMap = null;

  private void init() throws Exception {
    if (!initialized) {
      if (forecastScheduleText != null) {
        initFromText();
      } else {
        initFromResource();
      }
      initialized = true;
    }
    initVaccineIdToLabelMap();
  }

  private void initVaccineIdToLabelMap() throws Exception {
    if (vaccineIdToLabelMap == null) {
      try {
        vaccineIdToLabelMap = new HashMap<Integer, String>();
        BufferedReader in = new BufferedReader(new InputStreamReader(
            VaccineForecastManager.class.getResourceAsStream("vaccineIdToLabel.txt")));
        String line;
        while ((line = in.readLine()) != null) {
          int pos = line.indexOf("=");
          if (line.length() >= 3 && pos != -1) {
            String vaccineId = line.substring(0, pos).trim();
            String vaccineLabel = line.substring(pos + 1);
            vaccineIdToLabelMap.put(Integer.parseInt(vaccineId), vaccineLabel);
          }
        }
      } catch (Exception e) {
        throw new Exception("Unable to read from vaccineIdToLabel.txt", e);
      }
    }
  }

  private void initFromResource() throws Exception {
    forecastSchedule = new ForecastSchedule();
    InputStream is = VaccineForecastManager.class.getResourceAsStream(forecastScheduleLocation);
    if (is == null) {
      is = VaccineForecastManager.class.getResourceAsStream("/" + forecastScheduleLocation);
    }
    forecastSchedule.init(is, this);
    getVaccineForecasts();
  }

  private void initFromText() throws Exception {
    forecastSchedule = new ForecastSchedule();
    InputStream is = VaccineForecastManager.class.getResourceAsStream(forecastScheduleLocation);
    if (is == null) {
      is = VaccineForecastManager.class.getResourceAsStream("/" + forecastScheduleLocation);
    }
    forecastSchedule.initFromText(forecastScheduleText, this);
    getVaccineForecasts();
  }

  public static class ForecastAntigen implements Comparable<ForecastAntigen>
  {
    private String forecastCode = "";
    private String forecastLabel = "";
    private int sortOrder = 0;

    @Override
    public int compareTo(ForecastAntigen o) {
      return this.getSortOrder() - o.getSortOrder();
    }

    public String getForecastCode() {
      return forecastCode;
    }

    public void setForecastCode(String forecastCode) {
      this.forecastCode = forecastCode;
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

    private ForecastAntigen(String forecastCode, String forecastLabel, int sortOrder) {
      this.forecastCode = forecastCode;
      this.forecastLabel = forecastLabel;
      this.sortOrder = sortOrder;
    }
  }

  private static List<ForecastAntigen> forecastAntigenList = new ArrayList<ForecastAntigen>();

  public List<ForecastAntigen> getForecastAntigenList() {
    return forecastAntigenList;
  }

  static {
    forecastAntigenList.add(new ForecastAntigen("Influenza", "Influenza", 16));
    forecastAntigenList.add(new ForecastAntigen("HepB", "HepB", 1));
    forecastAntigenList.add(new ForecastAntigen("Diphtheria", "DTaP/Tdap", 2));
    forecastAntigenList.add(new ForecastAntigen("Pertussis", "Pertussis", 4));
    forecastAntigenList.add(new ForecastAntigen("Hib", "Hib", 5));
    forecastAntigenList.add(new ForecastAntigen("Pneumo", "PCV13", 6));
    forecastAntigenList.add(new ForecastAntigen("Polio", "IPV", 7));
    forecastAntigenList.add(new ForecastAntigen("Rotavirus", "Rota", 8));
    forecastAntigenList.add(new ForecastAntigen("Measles", "Measles", 9));
    forecastAntigenList.add(new ForecastAntigen("Mumps", "Mumps", 10));
    forecastAntigenList.add(new ForecastAntigen("Rubella", "Rubella", 11));
    forecastAntigenList.add(new ForecastAntigen("Varicella", "Var", 12));
    forecastAntigenList.add(new ForecastAntigen("Mening", "MCV4", 13));
    forecastAntigenList.add(new ForecastAntigen("HepA", "HepA", 14));
    forecastAntigenList.add(new ForecastAntigen("HPV", "HPV", 15));
    forecastAntigenList.add(new ForecastAntigen("Zoster", "Zoster", 16));
    forecastAntigenList.add(new ForecastAntigen("PPSV", "PPSV", 18));
    Collections.sort(forecastAntigenList);
  }

  protected void getVaccineForecasts() throws Exception {
    for (ForecastAntigen forecastAntigen : forecastAntigenList) {
      for (VaccineForecastDataBean vaccineForecast : forecastSchedule.getVaccineForecastList()) {
        if (vaccineForecast.getForecastCode().equals(forecastAntigen.forecastCode)) {
          vaccineForecast.setForecastLabel(forecastAntigen.forecastLabel);
          vaccineForecast.setSortOrder(forecastAntigen.sortOrder);
          break;
        }
      }
    }

  }

}
