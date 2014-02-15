package org.tch.forecast.core.api.test;

import java.io.PrintStream;
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

  public static enum Format {
    SPACED, STANDARD
  }

  // java -classpath deploy/tch-forecaster.jar org.tch.forecast.core.api.test.ListCodes
  public static void main(String[] args) throws Exception {

    PrintStream out = System.out;
    Format format = Format.STANDARD;
    if (args != null && args.length > 0) {
      if (args[0].equals("spaced")) {
        format = Format.SPACED;
      }
    }
    ListCodes listCodes = new ListCodes();
    listCodes.printCodes(format, out);
  }

  private List<CvxCode> cvxCodeList = null;
  private List<String> forecastCodeList = null;

  public List<CvxCode> getCvxCodeList() {
    return cvxCodeList;
  }

  public void setCvxCodeList(List<CvxCode> cvxCodeList) {
    this.cvxCodeList = cvxCodeList;
  }

  public List<String> getForecastCodeList() {
    return forecastCodeList;
  }

  public void setForecastCodeList(List<String> forecastCodeList) {
    this.forecastCodeList = forecastCodeList;
  }

  public ListCodes() throws Exception {
    Map<String, CvxCode> cvxToVaccineIdMap = ForecastHandler.getCvxToVaccineIdMap();
    VaccineForecastManager vaccineForecastManager = new VaccineForecastManager();
    cvxCodeList = new ArrayList<CvxCode>(cvxToVaccineIdMap.values());

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

    forecastCodeList = new ArrayList<String>(forecastCodeSet);
    Collections.sort(forecastCodeList);

    for (CvxCode cvxCode : cvxCodeList) {
      boolean hasLocationSet = false;
      for (String forecastCode : forecastCodeList) {
        Set<String> locationSet = cvxCode.getLocationMapSet().get(forecastCode);
        if (locationSet != null && !locationSet.isEmpty()) {
          hasLocationSet = true;
          break;
        }
      }
      cvxCode.setLocationSet(hasLocationSet);
    }

  }

  public void printCodes(Format format, PrintStream out) throws Exception {

    if (format == Format.SPACED) {
      out.println("TCH Forecaster - List Codes");
      out.println();
      out.print("Label              CVX  TCH  ");
      for (String forecastCode : forecastCodeList) {
        out.print(pad(forecastCode, 10));
      }
      out.println();
      out.println("-----------------------------------------------------------------");
      for (CvxCode cvxCode : cvxCodeList) {
        out.print(pad(cvxCode.getVaccineLabel(), 18) + " " + pad(cvxCode.getCvxCode(), 4) + " "
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
          out.print(pad(sb.toString(), 10));
        }
        out.println();
      }
    } else if (format == Format.STANDARD) {

      out.print("CVX Label\tCVX\tTCH Label\tTCH\tUse Status\tProblem Comment");
      for (String forecastCode : forecastCodeList) {
        out.print("\t" + forecastCode);
      }
      out.println();
      for (CvxCode cvxCode : cvxCodeList) {
        out.print("\"" + cvxCode.getCvxLabel() + "\"\t");
        out.print("\"" + cvxCode.getCvxCode() + "\"\t");
        out.print("\"" + cvxCode.getVaccineLabel() + "\"\t");
        out.print("" + (cvxCode.getVaccineId() == 0 ? "" : "" + cvxCode.getVaccineId()) + "\t");
        if (cvxCode.getUseStatus() == CvxCode.UseStatus.NOT_SUPPORTED) {
          out.print("\"NOT SUPPORTED\"");
        } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.SUPPORTED) {
          out.print("\"SUPPORTED\"");
        } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.PENDING) {
          out.print("\"PENDING\"");
        } else {
          out.print("\"\"");
        }

        
        if (cvxCode.isLocationSet()) {
          out.print("\t");
        } else {
          if (cvxCode.getUseStatus() == CvxCode.UseStatus.SUPPORTED) {
            out.print("\t\"Problem: No forecast series references this code\"");
          } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.PENDING) {
            out.print("\t\"Code is being added to the forecaster\"");
          } else {
            out.print("\t");
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
            out.print("\t");
          } else {
            out.print("\t\"" + sb.toString() + "\"");
          }
        }
        out.println();
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
