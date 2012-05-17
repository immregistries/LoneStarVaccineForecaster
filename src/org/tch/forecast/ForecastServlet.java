package org.tch.forecast;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tch.forecast.core.Forecaster;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.TraceList;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.support.Immunization;
import org.tch.forecast.support.PatientRecordDataBean;
import org.tch.forecast.support.VaccineForecastManager;
import org.tch.forecast.validator.DataSourceUnavailableException;
import org.tch.forecast.validator.db.DatabasePool;
import org.tch.hl7.core.util.DateTime;

public class ForecastServlet extends HttpServlet
{
  public static final String RESULT_FORMAT_TEXT = "text";
  public static final String RESULT_FORMAT_HTML = "html";

  private static final String[] MMR_FORECASTS = { ImmunizationForecastDataBean.MEASLES,
      ImmunizationForecastDataBean.MUMPS, ImmunizationForecastDataBean.RUBELLA };

  @Override
  public void init() throws ServletException
  {

    super.init();
  }

  private static Map<String, Integer> cvxToVaccineIdMap = null;

  private void initCvxCodes() throws ServletException
  {
    if (cvxToVaccineIdMap == null)
    {
      String url;

      try
      {
        Connection conn = DatabasePool.getConnection();
        try
        {
          cvxToVaccineIdMap = new HashMap<String, Integer>();
          PreparedStatement pstmt = conn.prepareStatement("SELECT cvx_code, vaccine_id FROM vaccine_cvx");
          ResultSet rset = pstmt.executeQuery();
          while (rset.next())
          {
            cvxToVaccineIdMap.put(rset.getString(1), rset.getInt(2));
          }
          rset.close();
          pstmt.close();
        } catch (Exception e)
        {
          throw new ServletException("Unable to query for CVX codes", e);
        } finally
        {
          DatabasePool.close(conn);
        }
      } catch (DataSourceUnavailableException e)
      {
        cvxToVaccineIdMap = null;
        throw new ServletException("Unable to connect to database", e);
      }
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    initCvxCodes();
    List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
    PatientRecordDataBean patient = new PatientRecordDataBean();
    List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();

    String evalDateString = req.getParameter("evalDate");
    if (evalDateString != null && evalDateString.length() != 8)
    {
      throw new ServletException("Parameter 'evalDate' is optional, but if sent must be in YYYYMMDD format. ");
    }
    DateTime forecastDate = new DateTime(evalDateString == null ? "today" : evalDateString);
    String evalSchedule = req.getParameter("evalSchedule");
    if (evalSchedule == null)
    {
      evalSchedule = "";
    }
    String resultFormat = req.getParameter("resultFormat");
    if (resultFormat == null || resultFormat.equals(""))
    {
      throw new ServletException("Parameter 'resultFormat' is required. ");
    }
    String patientDobString = req.getParameter("patientDob");
    if (patientDobString == null || patientDobString.length() != 8)
    {
      throw new ServletException("Parameter 'patientDob' is required and must be in YYYYMMDD format. ");
    }
    patient.setDob(new DateTime(patientDobString));
    String patientSex = req.getParameter("patientSex");
    if (patientSex == null || (!patientSex.equalsIgnoreCase("M") && !patientSex.equalsIgnoreCase("F")))
    {
      throw new ServletException("Parameter 'patientSex' is required and must have a value of 'M' or 'F'. ");
    }
    patient.setSex(patientSex.toUpperCase());
    int n = 1;
    while (req.getParameter("vaccineDate" + n) != null)
    {
      String vaccineDateString = req.getParameter("vaccineDate" + n);
      if (vaccineDateString.length() != 8)
      {
        throw new ServletException("Parameter 'vaccineDate" + n + "' must be in YYYYMMDD format.");
      }
      String vaccineCvx = req.getParameter("vaccineCvx" + n);
      String vaccineMvx = req.getParameter("vaccineMvx" + n);
      int vaccineId = 0;
      if (vaccineCvx == null)
      {
        throw new ServletException("Parameter 'vaccineCvx" + n + "' is required.");
      } else
      {
        vaccineId = cvxToVaccineIdMap.get(vaccineCvx);
        if (vaccineId == 0)
        {
          vaccineId = cvxToVaccineIdMap.get("0" + vaccineCvx);
        }
        if (vaccineId == 0)
        {
          throw new ServletException("Parameter 'vaccineCvx" + n + "' must be a recognized CVX code.");
        }
      }
      Immunization imm = new Immunization();
      imm.setCvx(vaccineCvx);
      imm.setDateOfShot(new DateTime(vaccineDateString).getDate());
      imm.setVaccineId(vaccineId);
      imms.add(imm);
      n++;
    }

    DateTime today = new DateTime(forecastDate.getDate());
    StringBuffer traceBuffer = new StringBuffer();
    Map traceMap = new HashMap();
    List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
    Forecaster forecaster = new Forecaster(new VaccineForecastManager());
    forecaster.setPatient(patient);
    forecaster.setVaccinations(imms);
    forecaster.setForecastDate(forecastDate.getDate());
    try
    {
      forecaster.forecast(resultList, doseList, traceBuffer, traceMap);

      DateTime sevenYearsAgo = new DateTime(today);
      sevenYearsAgo.addYears(-7);
      DateTime dob = new DateTime(patient.getDob());
      consolidate(resultList, MMR_FORECASTS, "MMR");
      String label;
      ImmunizationForecastDataBean forecastDiphtheria = getForecast(resultList, ImmunizationForecastDataBean.DIPHTHERIA);
      if (forecastDiphtheria != null)
      {
        DateTime nextGiveTime = new DateTime(forecastDiphtheria.getDue());
        if (nextGiveTime.isLessThan(today))
        {
          nextGiveTime = today;
        }
        DateTime age7 = new DateTime(dob);
        age7.addYears(7);
        DateTime moveTo = null;
        if (nextGiveTime.isLessThan(age7))
        {
          label = "DTaP";
        } else
        {
          ImmunizationForecastDataBean forecastPertussis = getForecast(resultList,
              ImmunizationForecastDataBean.PERTUSSIS);
          label = forecastPertussis == null || forecastPertussis.getDose().equals("1") ? "Tdap" : "Td";
          moveTo = age7;
        }

        if (moveTo != null)
        {
          if (new DateTime(forecastDiphtheria.getDue()).isLessThan(moveTo))
          {
            forecastDiphtheria.setDue(moveTo.getDate());
          }
          if (new DateTime(forecastDiphtheria.getValid()).isLessThan(moveTo))
          {
            forecastDiphtheria.setValid(moveTo.getDate());
          }
          if (new DateTime(forecastDiphtheria.getEarly()).isLessThan(moveTo))
          {
            forecastDiphtheria.setEarly(moveTo.getDate());
          }
          if (new DateTime(forecastDiphtheria.getOverdue()).isLessThan(moveTo))
          {
            forecastDiphtheria.setOverdue(moveTo.getDate());
          }
          if (new DateTime(forecastDiphtheria.getFinished()).isLessThan(moveTo))
          {
            forecastDiphtheria.setFinished(moveTo.getDate());
          }

        }
        forecastDiphtheria.setForecastName(label);
        forecastDiphtheria.setForecastLabel(label);
      }
      remove(resultList, ImmunizationForecastDataBean.PERTUSSIS);
      comment(resultList, ImmunizationForecastDataBean.PNEUMO, "S",
          "Supplementary dose of PCV13 is needed. Please refer to the Forecaster Reference Tool and MMWR 59(09) March 12, 2010.");
      alterInfluenza(resultList, today);
      sort(resultList);

    } catch (Exception e)
    {
      throw new ServletException("Unable to forecast, enexpected exception", e);
    }

    if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_HTML))
    {
      resp.setContentType("text/html");
    } else if (resultFormat.equalsIgnoreCase(RESULT_FORMAT_TEXT))
    {
      resp.setContentType("text/plain");
      PrintWriter out = new PrintWriter(resp.getOutputStream());

      List<ImmunizationForecastDataBean> forecastListDueToday = new ArrayList<ImmunizationForecastDataBean>();
      traceMap.remove(ImmunizationForecastDataBean.PERTUSSIS);
      for (Iterator<ImmunizationForecastDataBean> it = resultList.iterator(); it.hasNext();)
      {
        ImmunizationForecastDataBean forecastExamine = it.next();
        if (forecastExamine.getForecastName().equals("MMR"))
        {
          traceMap.remove(ImmunizationForecastDataBean.MEASLES);
          traceMap.remove(ImmunizationForecastDataBean.MUMPS);
          traceMap.remove(ImmunizationForecastDataBean.RUBELLA);
        }
        if (forecastExamine.getForecastName().equals("DTaP") || forecastExamine.getForecastName().equals("Tdap")
            || forecastExamine.getForecastName().equals("Td"))
        {
          traceMap.remove(ImmunizationForecastDataBean.DIPHTHERIA);
        }
        if (!forecastDate.isLessThan(new DateTime(forecastExamine.getDue())))
        {
          if (!forecastDate.isLessThan(new DateTime(forecastExamine.getFinished())))
          {
            TraceList traceList = (TraceList) traceMap.get(forecastExamine.getForecastName());
            if (traceList != null)
            {
              DateTime dt = new DateTime(forecastExamine.getFinished());
              traceList.setStatusDescription("Too late to complete. Next dose was expected before "
                  + dt.toString("M/D/Y") + ".");
            }
          } else
          {
            traceMap.remove(forecastExamine.getForecastName());
            forecastListDueToday.add(forecastExamine);
          }
          it.remove();
        } else
        {
          traceMap.remove(forecastExamine.getForecastName());
        }
      }
      sort(forecastListDueToday);
      sort(resultList);

      for (Iterator it = traceMap.keySet().iterator(); it.hasNext();)
      {
        String key = (String) it.next();
        TraceList traceList = (TraceList) traceMap.get(key);
        if (traceList.getStatusDescription().equals(""))
        {
          traceList.setStatusDescription("Vaccination series complete.");
        }
      }

      out.println("TCH Immunization Forecaster");
      out.println();
      out.println("VACCINATIONS RECOMMENDED " + new DateTime(forecastDate.getDate()).toString("M/D/Y"));

      List<ImmunizationForecastDataBean> forecastList = forecastListDueToday;
      boolean vaccinesDueToday = false;
      for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();)
      {
        ImmunizationForecastDataBean forecast = it.next();
        String statusDescription;
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue());
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());
        if (today.isLessThan(dueDate))
        {
          statusDescription = "";
        } else if (today.isLessThan(overdueDate))
        {
          statusDescription = "due";
        } else if (today.isLessThan(finishedDate))
        {
          statusDescription = "overdue";
        } else
        {
          continue;
        }
        vaccinesDueToday = true;

        String forecastDose = forecast.getDose();
        out.print("Forecasting " + forecast.getForecastLabel());
        out.print(" dose " + forecastDose);
        out.print(" due " + dueDate.toString("M/D/Y"));
        out.print(" valid " + validDate.toString("M/D/Y"));
        out.print(" overdue " + overdueDate.toString("M/D/Y"));
        out.print(" finished " + finishedDate.toString("M/D/Y"));
        out.println(" status " + statusDescription);

      }
      out.println();
      out.println("VACCCINATIONS RECOMMENDED AFTER " + new DateTime(forecastDate.getDate()).toString("M/D/Y"));

      forecastList = resultList;
      for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();)
      {
        ImmunizationForecastDataBean forecast = it.next();
        DateTime validDate = new DateTime(forecast.getValid());
        DateTime dueDate = new DateTime(forecast.getDue());
        DateTime overdueDate = new DateTime(forecast.getOverdue());
        DateTime finishedDate = new DateTime(forecast.getFinished());
        String forecastDose = forecast.getDose();
        out.print("Forecasting " + forecast.getForecastLabel());
        out.print(" dose " + forecastDose);
        out.print(" due " + dueDate.toString("M/D/Y"));
        out.print(" valid " + validDate.toString("M/D/Y"));
        out.print(" overdue " + overdueDate.toString("M/D/Y"));
        out.println(" finished " + finishedDate.toString("M/D/Y"));
      }
      out.println();
      out.println("VACCINATIONS COMPLETED OR NOT RECOMMENDED");
  
      for (Iterator it = traceMap.keySet().iterator(); it.hasNext();)
      {
        String key = (String) it.next();
        TraceList traceList = (TraceList) traceMap.get(key);
        out.println("Forecasting " + traceList.getForecastLabel() + " complete");
      }

      out.println();
      out.println("IMMUNIZATION HISTORY");
      for (ImmunizationInterface imm : imms)
      {
        out.print("Vaccine " + imm.getCvx() + " (TCH " + imm.getVaccineId() + ")");
        out.println(" given " + new DateTime(imm.getDateOfShot()).toString("M/D/Y"));
      }
      out.close();

    } else
    {
      throw new ServletException("Unrecognized result format '" + resultFormat + "'");
    }
  }

  private boolean consolidate(List<ImmunizationForecastDataBean> forecastList, String[] f, String label)
  {
    boolean same = true;
    ImmunizationForecastDataBean forecastA = getForecast(forecastList, f[0]);
    for (int i = 1; i < f.length; i++)
    {
      ImmunizationForecastDataBean forecastB = getForecast(forecastList, f[i]);
      if (forecastA == null && forecastB != null)
      {
        same = false;
        break;
      }
      if (forecastB != null)
      {
        if (!sameDate(forecastB.getValid(), forecastA.getValid()))
        {
          same = false;
          break;
        }
        if (!sameDate(forecastB.getDateDue(), forecastA.getDateDue()))
        {
          same = false;
          break;
        }
        if (!sameDate(forecastB.getOverdue(), forecastA.getOverdue()))
        {
          same = false;
          break;
        }
      }
      forecastA = forecastB;
    }
    if (same)
    {
      ImmunizationForecastDataBean forecast = getForecast(forecastList, f[0]);
      if (forecast != null)
      {
        forecast.setForecastName(label);
        forecast.setForecastLabel(label);
        for (int i = 1; i < f.length; i++)
        {
          removeForecast(forecastList, f[i]);
        }
      }
    }
    return same;
  }

  private boolean sameDate(Date date1, Date date2)
  {
    DateTime d1 = new DateTime(date1);
    DateTime d2 = new DateTime(date2);
    return d1.equals(d2);
  }

  private ImmunizationForecastDataBean getForecast(List<ImmunizationForecastDataBean> forecastList, String forecastName)
  {
    for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();)
    {
      ImmunizationForecastDataBean forecastExamine = it.next();
      if (forecastExamine.getForecastName().equals(forecastName))
      {
        return forecastExamine;
      }
    }
    return null;
  }

  private ImmunizationForecastDataBean removeForecast(List<ImmunizationForecastDataBean> forecastList,
      String forecastName)
  {
    for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();)
    {
      ImmunizationForecastDataBean forecastExamine = it.next();
      if (forecastExamine.getForecastName().equals(forecastName))
      {
        it.remove();
        return forecastExamine;
      }
    }
    return null;
  }

  private void remove(List<ImmunizationForecastDataBean> forecastList, String forecastName)
  {
    for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();)
    {
      ImmunizationForecastDataBean forecastExamine = it.next();
      if (forecastExamine.getForecastName().equals(forecastName))
      {
        it.remove();
      }
    }
  }

  private void comment(List forecastList, String forecastName, String dose, String comment)
  {
    for (Iterator it = forecastList.iterator(); it.hasNext();)
    {
      ImmunizationForecastDataBean forecastExamine = (ImmunizationForecastDataBean) it.next();
      if (forecastExamine.getForecastName().equals(forecastName))
      {
        if (dose == null || dose.equals(forecastExamine.getDose()))
        {
          forecastExamine.setComment(comment);
        }
      }
    }
  }

  private void alterInfluenza(List<ImmunizationForecastDataBean> forecastList, DateTime today)
  {
    ImmunizationForecastDataBean forecastExamine = null;
    for (Iterator<ImmunizationForecastDataBean> it = forecastList.iterator(); it.hasNext();)
    {
      forecastExamine = it.next();
      if (forecastExamine.getForecastName().equals(ImmunizationForecastDataBean.INFLUENZA))
      {
        break;
      }
      forecastExamine = null;
    }
    if (forecastExamine != null)
    {
      String start = "08/01";
      String end = "07/01";
      DateTime startDate = new DateTime(start + "/" + today.getYear());
      DateTime endDate = null;
      if (today.isLessThan(startDate))
      {
        // today is before start of next season
        endDate = new DateTime(end + "/" + today.getYear());
        if (today.isGreaterThanOrEquals(endDate))
        {
          // today is after the end of previous season
          // send end date of the next season, which is next year
          endDate = new DateTime(end + "/" + (today.getYear() + 1));
        } else
        {
          // today is before end of current season
          // change startDate to last year
          startDate = new DateTime(start + "/" + (today.getYear() - 1));
        }
      } else
      {
        // today is in season
        // send end date to next year
        endDate = new DateTime(end + "/" + (today.getYear() + 1));
      }
      DateTime dateDue = new DateTime(forecastExamine.getDateDue());
      if (dateDue.isLessThan(startDate))
      {
        // Forecast is before the start of this or next season
        forecastExamine.setDateDue(startDate.getDate());
      } else if (dateDue.isGreaterThanOrEquals(endDate))
      {
        // Patient is up-to-date, forecast is for next season, go ahead and
        // remove
        remove(forecastList, ImmunizationForecastDataBean.INFLUENZA);
      }
    }
  }

  private void sort(List<ImmunizationForecastDataBean> forecastList)
  {
    Collections.sort(forecastList, new Comparator<ImmunizationForecastDataBean>() {
      public int compare(ImmunizationForecastDataBean forecast1, ImmunizationForecastDataBean forecast2)
      {
        if (forecast1.getSortOrder() < forecast2.getSortOrder())
        {
          return -1;
        } else if (forecast1.getSortOrder() > forecast2.getSortOrder())
        {
          return 1;
        }
        return 0;
      }
    });
  }

}
