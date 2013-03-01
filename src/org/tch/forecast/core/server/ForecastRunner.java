package org.tch.forecast.core.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.Forecaster;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.api.impl.ForecastHandlerCore;
import org.tch.forecast.core.api.impl.VaccineForecastManager;
import org.tch.forecast.core.model.PatientRecordDataBean;

public class ForecastRunner {
  private VaccineForecastManager vaccineForecastManager;

  private List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();

  public List<VaccinationDoseDataBean> getDoseList() {
    return doseList;
  }

  public void setDoseList(List<VaccinationDoseDataBean> doseList) {
    this.doseList = doseList;
  }

  public PatientRecordDataBean getPatient() {
    return patient;
  }

  public void setPatient(PatientRecordDataBean patient) {
    this.patient = patient;
  }

  public List<ImmunizationInterface> getImms() {
    return imms;
  }

  public void setImms(List<ImmunizationInterface> imms) {
    this.imms = imms;
  }

  public Date getForecastDate() {
    return forecastDate;
  }

  public void setForecastDate(Date forecastDate) {
    this.forecastDate = forecastDate;
  }

  public List<ImmunizationForecastDataBean> getResultList() {
    return resultList;
  }

  public void setResultList(List<ImmunizationForecastDataBean> resultList) {
    this.resultList = resultList;
  }

  public Map getTraceMap() {
    return traceMap;
  }

  public void setTraceMap(Map traceMap) {
    this.traceMap = traceMap;
  }

  private PatientRecordDataBean patient = new PatientRecordDataBean();
  private List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();
  private Date forecastDate = null;
  private List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
  private Map traceMap = new HashMap();
  private List<ImmunizationForecastDataBean> forecastListDueToday;
  private List<ImmunizationForecastDataBean> forecastListDueLater;
  private List<TraceList> traceListListDontGive;

  public List<ImmunizationForecastDataBean> getForecastListDueToday() {
    return forecastListDueToday;
  }

  public void setForecastListDueToday(List<ImmunizationForecastDataBean> forecastListDueToday) {
    this.forecastListDueToday = forecastListDueToday;
  }

  public List<ImmunizationForecastDataBean> getForecastListDueLater() {
    return forecastListDueLater;
  }

  public void setForecastListDueLater(List<ImmunizationForecastDataBean> forecastListDueLater) {
    this.forecastListDueLater = forecastListDueLater;
  }

  public List<TraceList> getTraceListListDontGive() {
    return traceListListDontGive;
  }

  public void setTraceListListDontGive(List<TraceList> traceListListDontGive) {
    this.traceListListDontGive = traceListListDontGive;
  }

  public ForecastRunner(VaccineForecastManager vaccineForecastManager) throws Exception {
    this.vaccineForecastManager = vaccineForecastManager;
  }

  public void forecast() throws Exception {
    StringBuffer traceBuffer = new StringBuffer();
    Forecaster forecaster = new Forecaster(vaccineForecastManager);
    forecaster.setPatient(patient);
    forecaster.setVaccinations(imms);
    forecaster.setForecastDate(forecastDate);
    forecaster.forecast(resultList, doseList, traceBuffer, traceMap);

    DateTime forecastDateTime = new DateTime(forecastDate);
    forecastListDueToday = new ArrayList<ImmunizationForecastDataBean>();
    forecastListDueLater = new ArrayList<ImmunizationForecastDataBean>();
    traceListListDontGive = new ArrayList<TraceList>();
    traceMap.remove(ImmunizationForecastDataBean.PERTUSSIS);
    for (Iterator<ImmunizationForecastDataBean> it = resultList.iterator(); it.hasNext();) {
      ImmunizationForecastDataBean forecastExamine = it.next();
      DateTime validDate = new DateTime(forecastExamine.getValid());
      DateTime dueDate = new DateTime(forecastExamine.getDue());
      DateTime overdueDate = new DateTime(forecastExamine.getOverdue());
      DateTime finishedDate = new DateTime(forecastExamine.getFinished());
      String statusDescription = ""; 
      if (forecastDateTime.isLessThan(dueDate)) {
        statusDescription = "";
      } else if (forecastDateTime.isLessThan(overdueDate)) {
        statusDescription = "due";
      } else if (forecastDateTime.isLessThan(finishedDate)) {
        statusDescription = "overdue";
      } 
      forecastExamine.setStatusDescription(statusDescription);
      
      if (forecastExamine.getForecastName().equals(ImmunizationForecastDataBean.MMR)) {
        traceMap.remove(ImmunizationForecastDataBean.MEASLES);
        traceMap.remove(ImmunizationForecastDataBean.MUMPS);
        traceMap.remove(ImmunizationForecastDataBean.RUBELLA);
      }
      if (forecastExamine.getForecastName().equals(ImmunizationForecastDataBean.DTAP)
          || forecastExamine.getForecastName().equals(ImmunizationForecastDataBean.TDAP)
          || forecastExamine.getForecastName().equals(ImmunizationForecastDataBean.TD)) {
        traceMap.remove(ImmunizationForecastDataBean.DIPHTHERIA);
      }
      if (!forecastDateTime.isLessThan(new DateTime(forecastExamine.getDue()))) {
        if (!forecastDateTime.isLessThan(new DateTime(forecastExamine.getFinished()))) {
          TraceList traceList = (TraceList) traceMap.get(forecastExamine.getForecastName());
          if (traceList != null) {
            DateTime dt = new DateTime(forecastExamine.getFinished());
            traceList.setStatusDescription("Too late to complete. Next dose was expected before "
                + dt.toString("M/D/Y") + ".");
          }
        } else {
          traceMap.remove(forecastExamine.getForecastName());
          forecastListDueToday.add(forecastExamine);
        }
        it.remove();
      } else {
        traceMap.remove(forecastExamine.getForecastName());
        forecastListDueLater.add(forecastExamine);
      }
    }
    ForecastHandlerCore.sort(forecastListDueToday);
    ForecastHandlerCore.sort(resultList);

    for (Iterator it = traceMap.keySet().iterator(); it.hasNext();) {
      String key = (String) it.next();
      TraceList traceList = (TraceList) traceMap.get(key);
      if (traceList.getStatusDescription().equals("")) {
        traceList.setStatusDescription("Vaccination series complete.");
      }
    }

    for (Iterator it = traceMap.keySet().iterator(); it.hasNext();) {
      String key = (String) it.next();
      TraceList traceList = (TraceList) traceMap.get(key);
      traceListListDontGive.add(traceList);
    }

  }
}
