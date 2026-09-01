package org.immregistries.lonestar.core.api.impl;

import java.io.InputStream;
import org.immregistries.codebase.client.CodeMap;
import org.immregistries.codebase.client.CodeMapBuilder;

public class CodeMapManager {

  private static CodeMapManager singleton = null;

  public static CodeMap getCodeMap() {
    if (singleton == null) {
      singleton = new CodeMapManager();
    }
    return singleton.codeMap;
  }

  CodeMapBuilder builder = CodeMapBuilder.INSTANCE;
  CodeMap codeMap = null;

  private static final String CODESET_LOCATION = "Compiled.xml";

  public CodeMapManager() {
    InputStream is = this.getClass().getResourceAsStream(CODESET_LOCATION);
    if (is == null) {
      is = VaccineForecastManager.class.getResourceAsStream("/" + CODESET_LOCATION);
    }
    if (is == null) {

      System.err.println("Unable to find Compiled.xml!");
    }
    codeMap = builder.getCodeMap(is);
  }
}
