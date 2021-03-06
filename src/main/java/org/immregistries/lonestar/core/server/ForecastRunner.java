package org.immregistries.lonestar.core.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.api.impl.ForecastHandlerCore;
import org.immregistries.lonestar.core.api.impl.ForecastOptions;
import org.immregistries.lonestar.core.api.impl.VaccineForecastManager;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;

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
  private List<ImmunizationForecastDataBean> resultList =
      new ArrayList<ImmunizationForecastDataBean>();
  private Map traceMap = new HashMap();
  private List<ImmunizationForecastDataBean> forecastListDueToday;
  private List<ImmunizationForecastDataBean> forecastListDueLater;
  private String forecasterScheduleName = "";

  public String getForecasterScheduleName() {
    return forecasterScheduleName;
  }

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

  public ForecastRunner(VaccineForecastManager vaccineForecastManager) throws Exception {
    this.vaccineForecastManager = vaccineForecastManager;
  }

  public String getTextReport(boolean dueUseEarly) {
    StringWriter stringOut = new StringWriter();
    PrintWriter out = new PrintWriter(stringOut);
    ForecastReportPrinter forecastReportPrinter = new ForecastReportPrinter(vaccineForecastManager);
    forecastReportPrinter.printNarrowTextVersionOfForecast(resultList, imms, forecasterScheduleName,
        new DateTime(forecastDate), doseList, out, true, patient.getDob());
    out.close();
    return stringOut.toString();
  }

  public void printTextReport(PrintWriter out) {
    ForecastReportPrinter forecastReportPrinter = new ForecastReportPrinter(vaccineForecastManager);
    forecastReportPrinter.printNarrowTextVersionOfForecast(resultList, imms, forecasterScheduleName,
        new DateTime(forecastDate), doseList, out, true, patient.getDob());
    out.close();
  }

  private ForecastOptions forecastOptions = new ForecastOptions();

  public ForecastOptions getForecastOptions() {
    return forecastOptions;
  }

  public void setForecastOptions(ForecastOptions forecastOptions) {
    this.forecastOptions = forecastOptions;
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

  public void forecast() throws Exception {

    ForecastHandlerCore forecastHandlerCore = new ForecastHandlerCore(vaccineForecastManager);
    forecastHandlerCore.setKeepDetailLog(keepDetailLog);
    forecasterScheduleName = forecastHandlerCore.forecast(doseList, patient, imms,
        new DateTime(forecastDate), traceMap, resultList, forecastOptions);
    if (keepDetailLog) {
      detailLog = forecastHandlerCore.getDetailLog();
    }

    forecastListDueToday = new ArrayList<ImmunizationForecastDataBean>();
    forecastListDueLater = new ArrayList<ImmunizationForecastDataBean>();
    traceMap.remove(ImmunizationForecastDataBean.PERTUSSIS);
    for (ImmunizationForecastDataBean forecastExamine : resultList) {

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

      if (forecastExamine.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE)
          || forecastExamine.getStatusDescriptionInternal()
              .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_OVERDUE)) {
        forecastListDueToday.add(forecastExamine);
        traceMap.remove(forecastExamine.getForecastName());
      } else if (forecastExamine.getStatusDescriptionInternal()
          .equals(ImmunizationForecastDataBean.STATUS_DESCRIPTION_DUE_LATER)) {
        forecastListDueLater.add(forecastExamine);
        traceMap.remove(forecastExamine.getForecastName());
      }
    }
    ForecastHandlerCore.sort(forecastListDueToday);
    ForecastHandlerCore.sort(resultList);

  }
}
