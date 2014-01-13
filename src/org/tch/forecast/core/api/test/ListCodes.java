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
import org.tch.forecast.core.api.impl.ForecastHandler;
import org.tch.forecast.core.api.impl.VaccineForecastManager;

public class ListCodes
{

  private static Map<String, String> fullCvxIdToNameMap = new HashMap<String, String>();
  static {
    // To recreate this follow these steps:
    // 1.  Visit http://www2a.cdc.gov/vaccines/iis/iisstandards/vaccines.asp?rpt=cvx
    // 2.  Download Excel format
    // 3.  Open in Excel and use this formula:
    //     ="fullCvxIdToNameMap.put("&CHAR(34)&A2&CHAR(34)&", "&CHAR(34)&B2&CHAR(34)&");"
    // 4. Copy down all the rows and then copy into here
    fullCvxIdToNameMap.put("998", "no vaccine administered");
    fullCvxIdToNameMap.put("99", "RESERVED - do not use");
    fullCvxIdToNameMap.put("999", "unknown");
    fullCvxIdToNameMap.put("143", "Adenovirus types 4 and 7");
    fullCvxIdToNameMap.put("54", "adenovirus, type 4");
    fullCvxIdToNameMap.put("55", "adenovirus, type 7");
    fullCvxIdToNameMap.put("82", "adenovirus, unspecified formulation");
    fullCvxIdToNameMap.put("24", "anthrax");
    fullCvxIdToNameMap.put("19", "BCG");
    fullCvxIdToNameMap.put("27", "botulinum antitoxin");
    fullCvxIdToNameMap.put("26", "cholera");
    fullCvxIdToNameMap.put("29", "CMVIG");
    fullCvxIdToNameMap.put("56", "dengue fever");
    fullCvxIdToNameMap.put("12", "diphtheria antitoxin");
    fullCvxIdToNameMap.put("28", "DT (pediatric)");
    fullCvxIdToNameMap.put("20", "DTaP");
    fullCvxIdToNameMap.put("106", "DTaP, 5 pertussis antigens");
    fullCvxIdToNameMap.put("107", "DTaP, unspecified formulation");
    fullCvxIdToNameMap.put("146", "DTaP,IPV,Hib,HepB");
    fullCvxIdToNameMap.put("110", "DTaP-Hep B-IPV");
    fullCvxIdToNameMap.put("50", "DTaP-Hib");
    fullCvxIdToNameMap.put("120", "DTaP-Hib-IPV");
    fullCvxIdToNameMap.put("130", "DTaP-IPV");
    fullCvxIdToNameMap.put("132", "DTaP-IPV-HIB-HEP B, historical");
    fullCvxIdToNameMap.put("01", "DTP");
    fullCvxIdToNameMap.put("22", "DTP-Hib");
    fullCvxIdToNameMap.put("102", "DTP-Hib-Hep B");
    fullCvxIdToNameMap.put("57", "hantavirus");
    fullCvxIdToNameMap.put("30", "HBIG");
    fullCvxIdToNameMap.put("52", "Hep A, adult");
    fullCvxIdToNameMap.put("154", "Hep A, IG");
    fullCvxIdToNameMap.put("83", "Hep A, ped/adol, 2 dose");
    fullCvxIdToNameMap.put("84", "Hep A, ped/adol, 3 dose");
    fullCvxIdToNameMap.put("31", "Hep A, pediatric, unspecified formulation");
    fullCvxIdToNameMap.put("85", "Hep A, unspecified formulation");
    fullCvxIdToNameMap.put("104", "Hep A-Hep B");
    fullCvxIdToNameMap.put("08", "Hep B, adolescent or pediatric");
    fullCvxIdToNameMap.put("42", "Hep B, adolescent/high risk infant");
    fullCvxIdToNameMap.put("43", "Hep B, adult");
    fullCvxIdToNameMap.put("44", "Hep B, dialysis");
    fullCvxIdToNameMap.put("45", "Hep B, unspecified formulation");
    fullCvxIdToNameMap.put("58", "Hep C");
    fullCvxIdToNameMap.put("59", "Hep E");
    fullCvxIdToNameMap.put("60", "herpes simplex 2");
    fullCvxIdToNameMap.put("47", "Hib (HbOC)");
    fullCvxIdToNameMap.put("46", "Hib (PRP-D)");
    fullCvxIdToNameMap.put("49", "Hib (PRP-OMP)");
    fullCvxIdToNameMap.put("48", "Hib (PRP-T)");
    fullCvxIdToNameMap.put("17", "Hib, unspecified formulation");
    fullCvxIdToNameMap.put("51", "Hib-Hep B");
    fullCvxIdToNameMap.put("61", "HIV");
    fullCvxIdToNameMap.put("118", "HPV, bivalent");
    fullCvxIdToNameMap.put("62", "HPV, quadrivalent");
    fullCvxIdToNameMap.put("137", "HPV, unspecified formulation");
    fullCvxIdToNameMap.put("86", "IG");
    fullCvxIdToNameMap.put("14", "IG, unspecified formulation");
    fullCvxIdToNameMap.put("87", "IGIV");
    fullCvxIdToNameMap.put("151", "influenza nasal, unspecified formulation");
    fullCvxIdToNameMap.put("123", "influenza, H5N1-1203");
    fullCvxIdToNameMap.put("135", "Influenza, high dose seasonal");
    fullCvxIdToNameMap.put("153", "Influenza, injectable, MDCK, preservative free");
    fullCvxIdToNameMap.put("158", "influenza, injectable, quadrivalent");
    fullCvxIdToNameMap.put("150", "influenza, injectable, quadrivalent, preservative free");
    fullCvxIdToNameMap.put("111", "influenza, live, intranasal");
    fullCvxIdToNameMap.put("149", "influenza, live, intranasal, quadrivalent");
    fullCvxIdToNameMap.put("155", "influenza, recombinant, injectable, preservative free");
    fullCvxIdToNameMap.put("141", "Influenza, seasonal, injectable");
    fullCvxIdToNameMap.put("140", "Influenza, seasonal, injectable, preservative free");
    fullCvxIdToNameMap.put("144", "influenza, seasonal, intradermal, preservative free");
    fullCvxIdToNameMap.put("15", "influenza, split (incl. purified surface antigen)");
    fullCvxIdToNameMap.put("88", "influenza, unspecified formulation");
    fullCvxIdToNameMap.put("16", "influenza, whole");
    fullCvxIdToNameMap.put("10", "IPV");
    fullCvxIdToNameMap.put("134", "Japanese Encephalitis IM");
    fullCvxIdToNameMap.put("39", "Japanese encephalitis SC");
    fullCvxIdToNameMap.put("129", "Japanese Encephalitis, unspecified formulation");
    fullCvxIdToNameMap.put("63", "Junin virus");
    fullCvxIdToNameMap.put("64", "leishmaniasis");
    fullCvxIdToNameMap.put("65", "leprosy");
    fullCvxIdToNameMap.put("66", "Lyme disease");
    fullCvxIdToNameMap.put("04", "M/R");
    fullCvxIdToNameMap.put("67", "malaria");
    fullCvxIdToNameMap.put("05", "measles");
    fullCvxIdToNameMap.put("68", "melanoma");
    fullCvxIdToNameMap.put("103", "meningococcal C conjugate");
    fullCvxIdToNameMap.put("148", "Meningococcal C/Y-HIB PRP");
    fullCvxIdToNameMap.put("147", "meningococcal MCV4, unspecified formulation");
    fullCvxIdToNameMap.put("136", "Meningococcal MCV4O");
    fullCvxIdToNameMap.put("114", "meningococcal MCV4P");
    fullCvxIdToNameMap.put("32", "meningococcal MPSV4");
    fullCvxIdToNameMap.put("108", "meningococcal, unspecified formulation");
    fullCvxIdToNameMap.put("03", "MMR");
    fullCvxIdToNameMap.put("94", "MMRV");
    fullCvxIdToNameMap.put("07", "mumps");
    fullCvxIdToNameMap.put("127", "Novel influenza-H1N1-09");
    fullCvxIdToNameMap.put("128", "Novel Influenza-H1N1-09, all formulations");
    fullCvxIdToNameMap.put("125", "Novel Influenza-H1N1-09, nasal");
    fullCvxIdToNameMap.put("126", "Novel influenza-H1N1-09, preservative-free");
    fullCvxIdToNameMap.put("02", "OPV");
    fullCvxIdToNameMap.put("69", "parainfluenza-3");
    fullCvxIdToNameMap.put("11", "pertussis");
    fullCvxIdToNameMap.put("23", "plague");
    fullCvxIdToNameMap.put("133", "Pneumococcal conjugate PCV 13");
    fullCvxIdToNameMap.put("100", "pneumococcal conjugate PCV 7");
    fullCvxIdToNameMap.put("152", "Pneumococcal Conjugate, unspecified formulation");
    fullCvxIdToNameMap.put("33", "pneumococcal polysaccharide PPV23");
    fullCvxIdToNameMap.put("109", "pneumococcal, unspecified formulation");
    fullCvxIdToNameMap.put("89", "polio, unspecified formulation");
    fullCvxIdToNameMap.put("70", "Q fever");
    fullCvxIdToNameMap.put("40", "rabies, intradermal injection");
    fullCvxIdToNameMap.put("18", "rabies, intramuscular injection");
    fullCvxIdToNameMap.put("90", "rabies, unspecified formulation");
    fullCvxIdToNameMap.put("72", "rheumatic fever");
    fullCvxIdToNameMap.put("159", "Rho(D) - Unspecified formulation");
    fullCvxIdToNameMap.put("157", "Rho(D) -IG IM");
    fullCvxIdToNameMap.put("156", "Rho(D)-IG");
    fullCvxIdToNameMap.put("73", "Rift Valley fever");
    fullCvxIdToNameMap.put("34", "RIG");
    fullCvxIdToNameMap.put("119", "rotavirus, monovalent");
    fullCvxIdToNameMap.put("116", "rotavirus, pentavalent");
    fullCvxIdToNameMap.put("74", "rotavirus, tetravalent");
    fullCvxIdToNameMap.put("122", "rotavirus, unspecified formulation");
    fullCvxIdToNameMap.put("71", "RSV-IGIV");
    fullCvxIdToNameMap.put("93", "RSV-MAb");
    fullCvxIdToNameMap.put("145", "RSV-MAb (new)");
    fullCvxIdToNameMap.put("06", "rubella");
    fullCvxIdToNameMap.put("38", "rubella/mumps");
    fullCvxIdToNameMap.put("76", "Staphylococcus bacterio lysate");
    fullCvxIdToNameMap.put("138", "Td (adult)");
    fullCvxIdToNameMap.put("113", "Td (adult) preservative free");
    fullCvxIdToNameMap.put("09", "Td (adult), adsorbed");
    fullCvxIdToNameMap.put("139", "Td(adult) unspecified formulation");
    fullCvxIdToNameMap.put("115", "Tdap");
    fullCvxIdToNameMap.put("35", "tetanus toxoid, adsorbed");
    fullCvxIdToNameMap.put("142", "tetanus toxoid, not adsorbed");
    fullCvxIdToNameMap.put("112", "tetanus toxoid, unspecified formulation");
    fullCvxIdToNameMap.put("77", "tick-borne encephalitis");
    fullCvxIdToNameMap.put("13", "TIG");
    fullCvxIdToNameMap.put("98", "TST, unspecified formulation");
    fullCvxIdToNameMap.put("95", "TST-OT tine test");
    fullCvxIdToNameMap.put("96", "TST-PPD intradermal");
    fullCvxIdToNameMap.put("97", "TST-PPD tine test");
    fullCvxIdToNameMap.put("78", "tularemia vaccine");
    fullCvxIdToNameMap.put("25", "typhoid, oral");
    fullCvxIdToNameMap.put("41", "typhoid, parenteral");
    fullCvxIdToNameMap.put("53", "typhoid, parenteral, AKD (U.S. military)");
    fullCvxIdToNameMap.put("91", "typhoid, unspecified formulation");
    fullCvxIdToNameMap.put("101", "typhoid, ViCPs");
    fullCvxIdToNameMap.put("131", "typhus, historical");
    fullCvxIdToNameMap.put("75", "vaccinia (smallpox)");
    fullCvxIdToNameMap.put("105", "vaccinia (smallpox) diluted");
    fullCvxIdToNameMap.put("79", "vaccinia immune globulin");
    fullCvxIdToNameMap.put("21", "varicella");
    fullCvxIdToNameMap.put("81", "VEE, inactivated");
    fullCvxIdToNameMap.put("80", "VEE, live");
    fullCvxIdToNameMap.put("92", "VEE, unspecified formulation");
    fullCvxIdToNameMap.put("36", "VZIG");
    fullCvxIdToNameMap.put("117", "VZIG (IND)");
    fullCvxIdToNameMap.put("37", "yellow fever");
    fullCvxIdToNameMap.put("121", "zoster");

  }

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

    for (String cvxCode : fullCvxIdToNameMap.keySet()) {
      if (!cvxToVaccineIdMap.containsKey(cvxCode)) {
        VaccineEntry vaccineEntry = new VaccineEntry();
        vaccineEntry.cvxCode = cvxCode;
        vaccineEntry.label = fullCvxIdToNameMap.get(cvxCode);
        vaccineEntryList.add(vaccineEntry);
      }
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
            + pad(vaccineEntry.vaccineId == 0 ? "" : ("" + vaccineEntry.vaccineId), 4) + " ");
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
        System.out.print("\"" + vaccineEntry.label + "\"," + vaccineEntry.cvxCode + ","
            + (vaccineEntry.vaccineId == 0 ? "" : "" + vaccineEntry.vaccineId));
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
