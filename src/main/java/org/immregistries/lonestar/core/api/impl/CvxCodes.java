package org.immregistries.lonestar.core.api.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CvxCodes
{
  public static Map<String, CvxCode> getCvxToCvxCodeMap() throws Exception {
    Map<String, CvxCode> cvxToVaccineIdMap = null;
    try {
      cvxToVaccineIdMap = new HashMap<String, CvxCode>();
      BufferedReader in = new BufferedReader(new InputStreamReader(CvxCodes.class.getResourceAsStream("cvxCodes.txt")));
      String line = in.readLine();
      if (line != null) {
        while ((line = in.readLine()) != null) {
          String[] fields = line.split("\\t");
          if (fields.length >= 5) {
            CvxCode cvxCode = new CvxCode();
            cvxCode.setCvxLabel(clean(fields[0]));
            cvxCode.setCvxCode(clean(fields[1]));
            cvxCode.setVaccineLabel(clean(fields[2]));
            String vaccineId = clean(fields[3]);
            if (vaccineId.length() > 0) {
              cvxCode.setVaccineId(Integer.parseInt(vaccineId));
            } else {
              cvxCode.setVaccineId(0);
            }
            String useStatus = clean(fields[4]);
            if (useStatus.equalsIgnoreCase("SUPPORTED")) {
              cvxCode.setUseStatus(CvxCode.UseStatus.SUPPORTED);
            } else if (useStatus.equalsIgnoreCase("NOT SUPPORTED")) {
              cvxCode.setUseStatus(CvxCode.UseStatus.NOT_SUPPORTED);
            } else if (useStatus.equalsIgnoreCase("PENDING")) {
              cvxCode.setUseStatus(CvxCode.UseStatus.PENDING);
            } else {
              cvxCode.setUseStatus(CvxCode.UseStatus.NOT_SUPPORTED);
            }

            cvxToVaccineIdMap.put(cvxCode.getCvxCode(), cvxCode);
          }
          else {
            throw new Exception("Problem parsing cvxCodes.txt. Each line needs at least five fields. Only found "+line.length()+" fields.\nAre there tabs in this line: "+line);
          }
        }
      }
    } catch (Exception e) {
      throw new Exception("Unable to connect to load vaccine ids from cvxCodes.txt", e);
    }
    return cvxToVaccineIdMap;
  }

  private static String clean(String s) {
    if (s == null) {
      return "";
    } else if (s.startsWith("\"") && s.endsWith("\"")) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }
}
