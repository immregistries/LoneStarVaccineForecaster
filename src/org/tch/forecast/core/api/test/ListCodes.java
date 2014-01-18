package org.tch.forecast.core.api.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tch.forecast.core.VaccineForecastDataBean.NamedVaccine;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.VaccineForecastDataBean.ValidVaccine;
import org.tch.forecast.core.api.impl.CvxCode;
import org.tch.forecast.core.api.impl.ForecastHandler;
import org.tch.forecast.core.api.impl.VaccineForecastManager;

public class ListCodes
{

  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.api.test.ListCodes
  public static void main(String[] args) throws Exception {

    Map<String, CvxCode> cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
    VaccineForecastManager vaccineForecastManager = new VaccineForecastManager();
    List<CvxCode> cvxCodeList = new ArrayList<CvxCode>(cvxToVaccineIdMap.values());

    Collections.sort(cvxCodeList, new Comparator<CvxCode>() {
      @Override
      public int compare(CvxCode o1, CvxCode o2) {
        return o1.getCvxLabel().compareTo(o2.getCvxLabel());
      }
    });

    for (CvxCode cvxCode : cvxCodeList) {
      cvxCode.setLocationMapSet(new HashMap<String, Set<String>>());
    }

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
            for (CvxCode cvxCode : cvxCodeList) {
              if (cvxCode.getVaccineId() == vaccineId) {
                forecastCodeSet.add(schedule.getForecastCode());
                Set<String> locationSet = cvxCode.getLocationMapSet().get(schedule.getForecastCode());
                if (locationSet == null) {
                  locationSet = new HashSet<String>();
                  cvxCode.getLocationMapSet().put(schedule.getForecastCode(), locationSet);
                }
                locationSet.add(vaccineName);
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
      for (CvxCode cvxCode : cvxCodeList) {
        System.out.print(pad(cvxCode.getVaccineLabel(), 18) + " " + pad(cvxCode.getCvxCode(), 4) + " "
            + pad(cvxCode.getVaccineId() == 0 ? "" : ("" + cvxCode.getVaccineId()), 4) + " ");
        for (String forecastCode : forecastCodeList) {
          Set<String> locationSet = cvxCode.getLocationMapSet().get(forecastCode);
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
    }
    if (args.length > 0 && args[0].equals("html")) {
      // list of all codes by CVX name
      // list of all codes by CVX id
      // list of all vaccine group types supported with associated vaccine codes
    } else {

      System.out.print("CVX Label\tCVX\tTCH Label\tTCH\tUse Status\tProblem Comment");
      for (String forecastCode : forecastCodeList) {
        System.out.print("\t" + forecastCode);
      }
      System.out.println();
      for (CvxCode cvxCode : cvxCodeList) {
        System.out.print("\"" + cvxCode.getCvxLabel() + "\"\t");
        System.out.print("\"" + cvxCode.getCvxCode() + "\"\t");
        System.out.print("\"" + cvxCode.getVaccineLabel() + "\"\t");
        System.out.print("" + (cvxCode.getVaccineId() == 0 ? "" : "" + cvxCode.getVaccineId()) + "\t");
        if (cvxCode.getUseStatus() == CvxCode.UseStatus.NOT_SUPPORTED) {
          System.out.print("\"NOT SUPPORTED\"");
        } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.SUPPORTED) {
          System.out.print("\"SUPPORTED\"");
        } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.PENDING) {
          System.out.print("\"PENDING\"");
        } else {
          System.out.print("\"\"");
        }

        boolean hasLocationSet = false;
        for (String forecastCode : forecastCodeList) {
          Set<String> locationSet = cvxCode.getLocationMapSet().get(forecastCode);
          if (locationSet != null && !locationSet.isEmpty()) {
            hasLocationSet = true;
            break;
          }
        }
        if (hasLocationSet) {
          System.out.print("\t");
        } else {
          if (cvxCode.getUseStatus() == CvxCode.UseStatus.SUPPORTED) {
            System.out.print("\t\"Problem: No forecast series references this code\"");
          } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.PENDING) {
            System.out.print("\t\"Code is being added to the forecaster\"");
          } else {
            System.out.print("\t");
          }
        }
        for (String forecastCode : forecastCodeList) {
          Set<String> locationSet = cvxCode.getLocationMapSet().get(forecastCode);
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
            System.out.print("\t");
          } else {
            System.out.print("\t\"" + sb.toString() + "\"");
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

}
