package org.tch.forecast.core.api.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CvxCodes {
  public static Map<String, Integer> getCvxToVaccineIdMap() throws Exception {
    Map<String, Integer> cvxToVaccineIdMap = null;
    try {
      cvxToVaccineIdMap = new HashMap<String, Integer>();
      BufferedReader in = new BufferedReader(new InputStreamReader(
          CvxCodes.class.getResourceAsStream("cvxToVaccineId.txt")));
      String line;
      while ((line = in.readLine()) != null) {
        int pos = line.indexOf("=");
        if (line.length() >= 3 && pos != -1) {
          String cvxCode = line.substring(0, pos).trim();
          String vaccineId = line.substring(pos + 1);
          cvxToVaccineIdMap.put(cvxCode, Integer.parseInt(vaccineId));
        }
      }
    } catch (Exception e) {
      throw new Exception("Unable to connect to load vaccine ids from cvxToVaccineId.txt", e);
    }
    return cvxToVaccineIdMap;
  }
}
