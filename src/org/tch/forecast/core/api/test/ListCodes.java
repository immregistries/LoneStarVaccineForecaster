package org.tch.forecast.core.api.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tch.forecast.core.VaccineForecastDataBean.IndicationCriteria;
import org.tch.forecast.core.VaccineForecastDataBean.NamedVaccine;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.VaccineForecastDataBean.ValidVaccine;
import org.tch.forecast.core.api.impl.ForecastHandler;
import org.tch.forecast.core.api.impl.VaccineForecastManager;

public class ListCodes
{
  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.api.test.ListCodes
  public static void main(String[] args) throws Exception {

    Map<String, Integer> cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
    VaccineForecastManager vaccineForecastManager = new VaccineForecastManager();
    List<VaccineEntry> vaccineEntryList = new ArrayList<VaccineEntry>();

    for (String cvxCode : cvxToVaccineIdMap.keySet()) {
      VaccineEntry vaccineEntry = new VaccineEntry();
      vaccineEntry.cvxCode = cvxCode;
      vaccineEntry.vaccineId = cvxToVaccineIdMap.get(cvxCode);
      vaccineEntry.label = vaccineForecastManager.getVaccineName(vaccineEntry.vaccineId);
      vaccineEntryList.add(vaccineEntry);
    }

    Collections.sort(vaccineEntryList, new Comparator<VaccineEntry>() {
      @Override
      public int compare(VaccineEntry o1, VaccineEntry o2) {
        return o1.label.compareTo(o2.label);
      }
    });

    Set<String> forecastCodeSet = new HashSet<String>();

    VaccineForecastManager vacccineForecastManager = new VaccineForecastManager();

    Map<String, List<Schedule>> indicationsMap = vaccineForecastManager.getIndicationsMap();

    for (String key : indicationsMap.keySet()) {
      List<Schedule> scheduleList = indicationsMap.get(key);
      for (Schedule schedule : scheduleList) {
        for (String vaccineName : schedule.getVaccines().keySet()) {
          NamedVaccine namedVaccine = schedule.getVaccines().get(vaccineName);
          String vaccineString = namedVaccine.getVaccineIds();
          if (vaccineString == null) {
            throw new Exception("Unrecognized vaccine name '" + vaccineName + "'");
          }
          String[] vaccNames = vaccineString.split("\\,");
          ValidVaccine[] validVaccines = new ValidVaccine[vaccNames.length];
          for (int i = 0; i < vaccNames.length; i++) {
            String vaccName = vaccNames[i].trim();
            int vaccineId = 0;
            try {
              vaccineId = Integer.parseInt(vaccName);
            } catch (NumberFormatException nfe) {
              continue;
            }
            if (vaccineId == 0) {
              continue;
            }
            for (VaccineEntry vaccineEntry : vaccineEntryList) {
              if (vaccineEntry.vaccineId == vaccineId) {
                forecastCodeSet.add(schedule.getForecastCode());
                Set<String> locationSet = vaccineEntry.locationMapSet.get(schedule.getForecastCode());
                if (locationSet == null) {
                  locationSet = new HashSet<String>();
                  vaccineEntry.locationMapSet.put(schedule.getForecastCode(), locationSet);
                }
                locationSet.add(vaccineName);
                break;
              }
            }
          }
        }
      }
    }

    List<String> forecastCodeList = new ArrayList<String>(forecastCodeSet);
    Collections.sort(forecastCodeList);

    if (args.length > 0 && args[0].equals("spaced")) {
      System.out.println("TCH Forecaster - List Codes");
      System.out.println();
      System.out.print("Label              CVX  TCH  ");
      for (String forecastCode : forecastCodeList) {
        System.out.print(pad(forecastCode, 10));
      }
      System.out.println();
      System.out.println("-----------------------------------------------------------------");
      for (VaccineEntry vaccineEntry : vaccineEntryList) {
        System.out.print(pad(vaccineEntry.label, 18) + " " + pad(vaccineEntry.cvxCode, 4) + " "
            + pad("" + vaccineEntry.vaccineId, 4) + " ");
        for (String forecastCode : forecastCodeList) {
          Set<String> locationSet = vaccineEntry.locationMapSet.get(forecastCode);
          StringBuilder sb = new StringBuilder();
          if (locationSet != null) {
            boolean first = true;
            for (String location : locationSet) {
              if (!first) {
                sb.append(",");
              }
              first = false;
              sb.append(location);
            }
          }
          System.out.print(pad(sb.toString(), 10));
        }
        System.out.println();
      }
    } else {

      System.out.print("Label,CVX,TCH");
      for (String forecastCode : forecastCodeList) {
        System.out.print("," + forecastCode);
      }
      System.out.println();
      for (VaccineEntry vaccineEntry : vaccineEntryList) {
        System.out.print("\"" + vaccineEntry.label + "\"," + vaccineEntry.cvxCode + "," + +vaccineEntry.vaccineId);
        for (String forecastCode : forecastCodeList) {
          Set<String> locationSet = vaccineEntry.locationMapSet.get(forecastCode);
          StringBuilder sb = new StringBuilder();
          if (locationSet != null) {
            boolean first = true;
            for (String location : locationSet) {
              if (!first) {
                sb.append(" & ");
              }
              first = false;
              sb.append(location);
            }
          }
          if (sb.length() == 0) {
            System.out.print(",");
          } else {
            System.out.print(",\"" + sb.toString() + "\"");
          }
        }
        System.out.println();
      }
    }
  }

  private static String pad(String s, int size) {
    if (s.length() > size) {
      return s.substring(0, size);
    }
    s = s + "                                                                ";
    return s.substring(0, size);
  }

  private static class VaccineEntry
  {
    private String cvxCode = "";
    private int vaccineId = 0;
    private String label = "";
    private Map<String, Set<String>> locationMapSet = new HashMap<String, Set<String>>();
  }
}
