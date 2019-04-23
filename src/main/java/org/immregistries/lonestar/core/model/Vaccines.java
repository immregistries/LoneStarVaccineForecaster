package org.immregistries.lonestar.core.model;

import java.util.HashMap;
import java.util.Map;

public class Vaccines {

  private Map<Integer, String> vaccineIdToCvx = new HashMap<Integer, String>();
  private Map<String, Integer> cvxToVaccineId = new HashMap<String, Integer>();

  private void setCvxToVaccineId(int vaccineId, String cvxCode) {
    vaccineIdToCvx.put(vaccineId, cvxCode);
    cvxToVaccineId.put(cvxCode, vaccineId);
  }
  
  public String mapToCvx(int vaccineid)
  {
    return vaccineIdToCvx.get(vaccineid);
  }
  
  public Integer mapToVaccineid(String cvxCode)
  {
    return cvxToVaccineId.get(cvxCode);
  }
  
  public boolean isRecognizedCvxCode(String cvxCode)
  {
    return cvxToVaccineId.containsKey(cvxCode);
  }

  public Vaccines() {

    setCvxToVaccineId(123, "115");
    setCvxToVaccineId(208, "116");
    setCvxToVaccineId(206, "119");
    setCvxToVaccineId(115, "120");
    setCvxToVaccineId(1000, "54");
    setCvxToVaccineId(1010, "55");
    setCvxToVaccineId(1020, "82");
    setCvxToVaccineId(1030, "24");
    setCvxToVaccineId(211, "19");
    setCvxToVaccineId(1050, "27");
    setCvxToVaccineId(1060, "26");
    setCvxToVaccineId(1070, "29");
    setCvxToVaccineId(1080, "56");
    setCvxToVaccineId(1090, "12");
    setCvxToVaccineId(112, "28");
    setCvxToVaccineId(110, "20");
    setCvxToVaccineId(113, "50");
    setCvxToVaccineId(111, "01");
    setCvxToVaccineId(121, "22");
    setCvxToVaccineId(1150, "102");
    setCvxToVaccineId(1160, "57");
    setCvxToVaccineId(1170, "52");
    setCvxToVaccineId(1180, "83");
    setCvxToVaccineId(1190, "84");
    setCvxToVaccineId(1200, "31");
    setCvxToVaccineId(145, "85");
    setCvxToVaccineId(146, "104");
    setCvxToVaccineId(1230, "30");
    setCvxToVaccineId(1240, "08");
    setCvxToVaccineId(1250, "42");
    setCvxToVaccineId(1260, "43");
    setCvxToVaccineId(1270, "44");
    setCvxToVaccineId(137, "45");
    setCvxToVaccineId(1290, "58");
    setCvxToVaccineId(1300, "59");
    setCvxToVaccineId(1310, "60");
    setCvxToVaccineId(1320, "46");
    setCvxToVaccineId(1330, "47");
    setCvxToVaccineId(1340, "48");
    setCvxToVaccineId(1350, "49");
    setCvxToVaccineId(128, "17");
    setCvxToVaccineId(136, "51");
    setCvxToVaccineId(1380, "61");
    setCvxToVaccineId(1390, "62");
    setCvxToVaccineId(1400, "86");
    setCvxToVaccineId(1410, "87");
    setCvxToVaccineId(1420, "14");
    setCvxToVaccineId(181, "15");
    setCvxToVaccineId(1440, "16");
    setCvxToVaccineId(179, "88");
    setCvxToVaccineId(147, "10");
    setCvxToVaccineId(148, "02");
    setCvxToVaccineId(153, "89");
    setCvxToVaccineId(1490, "39");
    setCvxToVaccineId(1500, "63");
    setCvxToVaccineId(1510, "64");
    setCvxToVaccineId(1520, "65");
    setCvxToVaccineId(209, "66");
    setCvxToVaccineId(158, "03");
    setCvxToVaccineId(160, "04");
    setCvxToVaccineId(1560, "94");
    setCvxToVaccineId(1570, "67");
    setCvxToVaccineId(161, "05");
    setCvxToVaccineId(1590, "68");
    setCvxToVaccineId(182, "32");
    setCvxToVaccineId(197, "103");
    setCvxToVaccineId(162, "07");
    setCvxToVaccineId(1630, "69");
    setCvxToVaccineId(1640, "11");
    setCvxToVaccineId(1650, "23");
    setCvxToVaccineId(155, "33");
    setCvxToVaccineId(154, "100");
    setCvxToVaccineId(1680, "70");
    setCvxToVaccineId(1690, "18");
    setCvxToVaccineId(1700, "40");
    setCvxToVaccineId(210, "90");
    setCvxToVaccineId(1720, "72");
    setCvxToVaccineId(1730, "73");
    setCvxToVaccineId(1740, "34");
    setCvxToVaccineId(208, "74");
    setCvxToVaccineId(1760, "71");
    setCvxToVaccineId(1770, "93");
    setCvxToVaccineId(171, "06");
    setCvxToVaccineId(175, "38");
    setCvxToVaccineId(1800, "75");
    setCvxToVaccineId(1810, "76");
    setCvxToVaccineId(122, "09");
    setCvxToVaccineId(1830, "35");
    setCvxToVaccineId(1840, "77");
    setCvxToVaccineId(1850, "13");
    setCvxToVaccineId(1860, "95");
    setCvxToVaccineId(1870, "96");
    setCvxToVaccineId(1880, "97");
    setCvxToVaccineId(1890, "98");
    setCvxToVaccineId(1900, "78");
    setCvxToVaccineId(1910, "25");
    setCvxToVaccineId(1920, "41");
    setCvxToVaccineId(1930, "53");
    setCvxToVaccineId(1940, "101");
    setCvxToVaccineId(1950, "91");
    setCvxToVaccineId(1960, "79");
    setCvxToVaccineId(178, "21");
    setCvxToVaccineId(1980, "81");
    setCvxToVaccineId(1990, "80");
    setCvxToVaccineId(2000, "92");
    setCvxToVaccineId(2010, "36");
    setCvxToVaccineId(2020, "37");
    setCvxToVaccineId(999, "999");
    setCvxToVaccineId(180, "111");
    setCvxToVaccineId(114, "110");
  }
}
