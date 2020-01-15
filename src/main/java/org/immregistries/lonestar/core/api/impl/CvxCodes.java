package org.immregistries.lonestar.core.api.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.generated.Code;
import org.immregistries.codebase.client.reference.CodesetType;

public class CvxCodes {
  public static Map<String, CvxCode> getCvxToCvxCodeMap() throws Exception {
    Map<String, CvxCode> cvxToVaccineIdMap = null;
    try {
      cvxToVaccineIdMap = new HashMap<String, CvxCode>();


      CodeMap codeMap = CodeMapManager.getCodeMap();

      Collection<Code> codeCollection = codeMap.getCodesForTable(CodesetType.VACCINATION_CVX_CODE);

      for (Code code : codeCollection) {
        CvxCode cvxCode = new CvxCode();
        cvxCode.setCvxCode(code.getValue());
        cvxCode.setCvxLabel(code.getLabel());
        cvxCode.setVaccineLabel(code.getLabel());
        cvxCode.setUseStatus(CvxCode.UseStatus.SUPPORTED);
        try {
          cvxCode.setVaccineId(Integer.parseInt(code.getValue()));
        } catch (NumberFormatException nfe) {
          // continue
        }
        cvxToVaccineIdMap.put(cvxCode.getCvxCode(), cvxCode);
      }

    } catch (Exception e) {
      throw new Exception("Unable to connect to load vaccine ids from codeset", e);
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
