package org.immregistries.lonestar.core.api.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.immregistries.lonestar.core.ForecastSchedule;
import org.immregistries.lonestar.core.VaccineForecastDataBean;
import org.immregistries.lonestar.core.VaccineForecastManagerInterface;
import org.immregistries.lonestar.core.VaccineForecastDataBean.Schedule;

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

  public void init() throws Exception {
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
  }

  private void initFromText() throws Exception {
    forecastSchedule = new ForecastSchedule();
    InputStream is = VaccineForecastManager.class.getResourceAsStream(forecastScheduleLocation);
    if (is == null) {
      is = VaccineForecastManager.class.getResourceAsStream("/" + forecastScheduleLocation);
    }
    forecastSchedule.initFromText(forecastScheduleText, this);
  }

}
