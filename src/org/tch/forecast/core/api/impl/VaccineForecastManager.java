package org.tch.forecast.core.api.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.ForecastSchedule;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.VaccineForecastManagerInterface;

public class VaccineForecastManager implements VaccineForecastManagerInterface {
  private static Map<String, List<Schedule>> indications = null;
  private static ForecastSchedule forecastSchedule = null;

  public VaccineForecastManager() throws Exception {
    init();
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

  private static void init() throws Exception {
    if (indications == null) {
      String forecastScheduleLocation = "ForecastSchedule.xml";
      init(forecastScheduleLocation);

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

  public static void init(String forecastScheduleLocation) throws Exception {
    forecastSchedule = new ForecastSchedule();
    forecastSchedule.init(VaccineForecastManager.class.getResourceAsStream(forecastScheduleLocation));
    getVaccineForecasts();
    indications = VaccineForecastDataBean.getIndications();
  }

  private static class ForecastAntigen {
    private String forecastCode = "";
    private String forecastLabel = "";
    private int sortOrder = 0;

    private ForecastAntigen(String forecastCode, String forecastLabel, int sortOrder) {
      this.forecastCode = forecastCode;
      this.forecastLabel = forecastLabel;
      this.sortOrder = sortOrder;
    }
  }

  private static List<ForecastAntigen> forecastAntigenList = new ArrayList<ForecastAntigen>();

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
  }

  private static void getVaccineForecasts() throws Exception {
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
