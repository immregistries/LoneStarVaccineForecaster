package org.immregistries.lonestar.core.api.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.Forecaster;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.VaccineForecastManagerInterface;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;

public class ForecastHandlerCore {
  private static final String[] MMR_FORECASTS = {ImmunizationForecastDataBean.MEASLES,
      ImmunizationForecastDataBean.MUMPS, ImmunizationForecastDataBean.RUBELLA};

  public static boolean consolidate(List<ImmunizationForecastDataBean> forecastList, String[] f,
      String label) {
    boolean same = true;
    ImmunizationForecastDataBean forecastA = getForecast(forecastList, f[0]);
    for (int i = 1; i < f.length; i++) {
      ImmunizationForecastDataBean forecastB = getForecast(forecastList, f[i]);
      if (forecastA == null && forecastB != null) {
        same = false;
        break;
      }
      if (forecastB != null) {
        if (!sameDate(forecastB.getValid(), forecastA.getValid())) {
          same = false;
          break;
        }
        if (!sameDate(forecastB.getDateDue(), forecastA.getDateDue())) {
          same = false;
          break;
        }
        if (!sameDate(forecastB.getOverdue(), forecastA.getOverdue())) {
          same = false;
          break;
        }
      }
      forecastA = forecastB;
    }
    if (same) {
      ImmunizationForecastDataBean forecast = getForecast(forecastList, f[0]);
      if (forecast != null) {
        forecast.setForecastName(label);
        forecast.setForecastLabel(label);
        for (int i = 1; i < f.length; i++) {
          removeForecast(forecastList, f[i]);
        }
      }
    }
    return same;
  }

  public static ImmunizationForecastDataBean getForecast(
      List<ImmunizationForecastDataBean> forecastList, String forecastName) {
    for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();) {
      ImmunizationForecastDataBean forecastExamine = it.next();
      if (forecastExamine.getForecastName().equals(forecastName)) {
        return forecastExamine;
      }
    }
    return null;
  }

  public static ImmunizationForecastDataBean removeForecast(
      List<ImmunizationForecastDataBean> forecastList, String forecastName) {
    for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();) {
      ImmunizationForecastDataBean forecastExamine = it.next();
      if (forecastExamine.getForecastName().equals(forecastName)) {
        it.remove();
        return forecastExamine;
      }
    }
    return null;
  }

  public static boolean sameDate(Date date1, Date date2) {
    if (date1 == null && date2 == null) {
      return true;
    } else if (date1 == null || date2 == null) {
      return false;
    }
    DateTime d1 = new DateTime(date1);
    DateTime d2 = new DateTime(date2);
    return d1.equals(d2);
  }

  public static void remove(List<ImmunizationForecastDataBean> forecastList, String forecastName) {
    for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();) {
      ImmunizationForecastDataBean forecastExamine = it.next();
      if (forecastExamine.getForecastName().equals(forecastName)) {
        it.remove();
      }
    }
  }

  private static void comment(List forecastList, String forecastName, String dose, String comment) {
    for (Iterator it = forecastList.iterator(); it.hasNext();) {
      ImmunizationForecastDataBean forecastExamine = (ImmunizationForecastDataBean) it.next();
      if (forecastExamine.getForecastName().equals(forecastName)) {
        if (dose == null || dose.equals(forecastExamine.getDose())) {
          forecastExamine.setComment(comment);
        }
      }
    }
  }

  public static void sort(List<ImmunizationForecastDataBean> forecastList) {
    Collections.sort(forecastList, new Comparator<ImmunizationForecastDataBean>() {
      public int compare(ImmunizationForecastDataBean forecast1,
          ImmunizationForecastDataBean forecast2) {
        if (forecast1.getSortOrder() < forecast2.getSortOrder()) {
          return -1;
        } else if (forecast1.getSortOrder() > forecast2.getSortOrder()) {
          return 1;
        }
        return 0;
      }
    });
  }

  private VaccineForecastManagerInterface vaccineForecastManager = null;

  public VaccineForecastManagerInterface getVaccineForecastManager() {
    return vaccineForecastManager;
  }

  public ForecastHandlerCore(VaccineForecastManagerInterface vaccineForecastManager) {
    this.vaccineForecastManager = vaccineForecastManager;
  }

  private boolean keepDetailLog = false;
  private StringBuffer detailLog = null;

  public boolean isKeepDetailLog() {
    return keepDetailLog;
  }

  public void setKeepDetailLog(boolean keepDetailLog) {
    this.keepDetailLog = keepDetailLog;
  }

  public StringBuffer getDetailLog() {
    return detailLog;
  }

  public void setDetailLog(StringBuffer detailLog) {
    this.detailLog = detailLog;
  }

  public String forecast(List<VaccinationDoseDataBean> doseList, PatientRecordDataBean patient,
      List<ImmunizationInterface> imms, DateTime forecastDate, Map<String, List<Trace>> traceMap,
      List<ImmunizationForecastDataBean> resultList, ForecastOptions forecastOptions)
      throws Exception {
    String forecasterScheduleName = null;
    {

      DateTime today = new DateTime(forecastDate.getDate());

      Forecaster forecaster = new Forecaster(vaccineForecastManager);
      forecaster.setPatient(patient);
      forecaster.setVaccinations(imms);
      forecaster.setForecastDate(forecastDate.getDate());
      forecaster.setForecastOptions(forecastOptions);
      if (keepDetailLog) {
        detailLog = new StringBuffer();
        forecaster.setDetailLog(detailLog);
      }
      try {
        forecaster.forecast(resultList, doseList, traceMap);
        forecasterScheduleName = forecaster.getForecastSchedule().getScheduleName();

        DateTime sevenYearsAgo = new DateTime(today);
        sevenYearsAgo.addYears(-7);
        DateTime dob = new DateTime(patient.getDob());
        consolidate(resultList, MMR_FORECASTS, "MMR");
        String label;
        ImmunizationForecastDataBean forecastDiphtheria =
            getForecast(resultList, ImmunizationForecastDataBean.DIPHTHERIA);
        if (forecastDiphtheria != null) {
          DateTime nextGiveTime = new DateTime(forecastDiphtheria.getDue());
          if (nextGiveTime.isLessThan(today)) {
            nextGiveTime = today;
          }
          DateTime age7 = new DateTime(dob);
          age7.addYears(7);
          DateTime moveTo = null;
          if (nextGiveTime.isLessThan(age7)) {
            label = "DTaP";
          } else {
            ImmunizationForecastDataBean forecastPertussis =
                getForecast(resultList, ImmunizationForecastDataBean.PERTUSSIS);
            label = forecastPertussis == null || forecastPertussis.getDose().equals("1") ? "Tdap"
                : "Td";
            moveTo = age7;
          }

          if (moveTo != null) {
            if (new DateTime(forecastDiphtheria.getDue()).isLessThan(moveTo)) {
              forecastDiphtheria.setDue(moveTo.getDate());
            }
            if (new DateTime(forecastDiphtheria.getValid()).isLessThan(moveTo)) {
              forecastDiphtheria.setValid(moveTo.getDate());
            }
            if (new DateTime(forecastDiphtheria.getOverdue()).isLessThan(moveTo)) {
              forecastDiphtheria.setOverdue(moveTo.getDate());
            }
            if (new DateTime(forecastDiphtheria.getFinished()).isLessThan(moveTo)) {
              forecastDiphtheria.setFinished(moveTo.getDate());
            }

          }
          forecastDiphtheria.setForecastName(label);
          forecastDiphtheria.setForecastLabel(label);
        }

        remove(resultList, ImmunizationForecastDataBean.PERTUSSIS);

        comment(resultList, ImmunizationForecastDataBean.PNEUMO, "S",
            "Supplementary dose of PCV13 is needed. Please refer to the Forecaster Reference Tool and MMWR 59(09) March 12, 2010.");
        sort(resultList);

      } catch (Exception e) {
        throw new Exception("Unable to forecast, enexpected exception", e);
      }
    }
    return forecasterScheduleName;
  }
}
