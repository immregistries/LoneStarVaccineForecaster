package org.immregistries.lonestar;

import java.util.HashMap;
import java.util.Map;
import org.immregistries.lonestar.core.api.impl.ForecastHandlerCore;
import org.immregistries.lonestar.core.api.impl.VaccineForecastManager;

public class ForecastManagerSingleton {

  private static ForecastManagerSingleton singleton = null;

  public static ForecastManagerSingleton getForecastManagerSingleton() {
    if (singleton == null) {
      singleton = new ForecastManagerSingleton();
    }
    return singleton;
  }

  protected static final String SCHEDULE_NAME_DEFAULT = "default";

  private static Map<String, ForecastHandlerCore> forecastHandlerCoreMap =
      new HashMap<String, ForecastHandlerCore>();

  public ForecastHandlerCore getForecastHandlerCore() {
    return getForecastHandlerCore(SCHEDULE_NAME_DEFAULT);
  }

  protected ForecastHandlerCore getForecastHandlerCore(String scheduleName) {
    ForecastHandlerCore forecastHandlerCore = forecastHandlerCoreMap.get(scheduleName);
    if (forecastHandlerCore == null) {
      VaccineForecastManager forecastManager;
      try {
        if (scheduleName.equals(SCHEDULE_NAME_DEFAULT)) {
          forecastManager = new VaccineForecastManager();
        } else {
          forecastManager = new VaccineForecastManager(scheduleName + ".xml");
        }
      } catch (Exception e) {
        throw new IllegalArgumentException("Unable to initialize forecaster", e);
      }
      forecastHandlerCore = new ForecastHandlerCore(forecastManager);
      forecastHandlerCoreMap.put(scheduleName, forecastHandlerCore);
    }
    return forecastHandlerCore;
  }
}
