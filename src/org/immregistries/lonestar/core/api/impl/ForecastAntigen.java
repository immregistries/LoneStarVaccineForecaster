package org.immregistries.lonestar.core.api.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForecastAntigen implements Comparable<ForecastAntigen>
{
  private String forecastCode = "";
  private String forecastLabel = "";
  private int sortOrder = 0;
  private String[] alternateNames = new String[0];

  // JDK 1.5 Override is not applicable to interfaces
  //@Override
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

  private ForecastAntigen(String forecastCode, String forecastLabel, int sortOrder, String[] alternateNames) {
    this.forecastCode = forecastCode;
    this.forecastLabel = forecastLabel;
    this.sortOrder = sortOrder;
    this.alternateNames = alternateNames;
  }

  private ForecastAntigen(String forecastCode, String forecastLabel, int sortOrder) {
    this.forecastCode = forecastCode;
    this.forecastLabel = forecastLabel;
    this.sortOrder = sortOrder;
  }

  private static List<ForecastAntigen> forecastAntigenList = new ArrayList<ForecastAntigen>();
  private static Map<String, ForecastAntigen> forecastAntigenMap = new HashMap<String, ForecastAntigen>();

  public static ForecastAntigen getForecastAntigen(String forecastCode) {
    return forecastAntigenMap.get(forecastCode);
  }

  public static List<ForecastAntigen> getForecastAntigenList() {
    return forecastAntigenList;
  }

  static {
    forecastAntigenList.add(new ForecastAntigen("Influenza", "Influenza", 16));
    forecastAntigenList.add(new ForecastAntigen("HepB", "HepB", 1));
    forecastAntigenList.add(new ForecastAntigen("Diphtheria", "DTaP/Tdap", 2));
    forecastAntigenList.add(new ForecastAntigen("Pertussis", "Pertussis", 4));
    forecastAntigenList.add(new ForecastAntigen("Hib", "Hib", 5));
    forecastAntigenList.add(new ForecastAntigen("Pneumo", "PCV13", 6, new String[] { "PCV", "PCV-13", "PCV13" }));
    forecastAntigenList.add(new ForecastAntigen("Polio", "IPV", 8));
    forecastAntigenList.add(new ForecastAntigen("Rotavirus", "Rota", 9));
    forecastAntigenList.add(new ForecastAntigen("Measles", "Measles", 10));
    forecastAntigenList.add(new ForecastAntigen("Mumps", "Mumps", 11));
    forecastAntigenList.add(new ForecastAntigen("Rubella", "Rubella", 12));
    forecastAntigenList.add(new ForecastAntigen("Varicella", "Var", 13));
    forecastAntigenList.add(new ForecastAntigen("Mening", "MCV4", 14));
    forecastAntigenList.add(new ForecastAntigen("HepA", "HepA", 15));
    forecastAntigenList.add(new ForecastAntigen("HPV", "HPV", 16));
    forecastAntigenList.add(new ForecastAntigen("Zoster", "RZV (Shingrix)", 17));
    forecastAntigenList.add(new ForecastAntigen("Pneumo65", "Pneumo for 65+", 18));
    forecastAntigenList.add(new ForecastAntigen("MeningococcalB", "MeningococcalB", 19));
    forecastAntigenList.add(new ForecastAntigen("MeningBexsero", "Bexsero", 20));
    forecastAntigenList.add(new ForecastAntigen("MeningTrumenba", "Trumenba", 21));
    Collections.sort(forecastAntigenList);
    for (ForecastAntigen forecastAntigen : forecastAntigenList) {
      forecastAntigenMap.put(forecastAntigen.getForecastCode(), forecastAntigen);
      for (String alternateName : forecastAntigen.alternateNames)
      {
        forecastAntigenMap.put(alternateName, forecastAntigen);
      }
    }
    // addition antigens that have not schedule, but may be forecasted
    {
      ForecastAntigen forecastAntigen = new ForecastAntigen("PPSV", "PPSV", 7); // replaced by Pneumo65
      forecastAntigenMap.put(forecastAntigen.getForecastCode(), forecastAntigen);
    }

  }
}
