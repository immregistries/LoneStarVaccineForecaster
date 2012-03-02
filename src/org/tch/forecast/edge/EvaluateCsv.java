package org.tch.forecast.edge;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tch.forecast.core.Forecaster;
import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.support.Immunization;
import org.tch.forecast.support.PatientRecordDataBean;
import org.tch.forecast.support.VaccineForecastManager;
import org.tch.forecast.validator.DataSourceUnavailableException;
import org.tch.forecast.validator.db.DatabasePool;
import org.tch.hl7.core.util.DateTime;

public class EvaluateCsv
{
  private static boolean DEBUG_CVX_READER = false;

  /*
   * Based on this, here is what the format of the CVS file I plan to give back
   * to you:
   * 
   * + Patient ID - echo of what you sent
   * 
   * + DOB - echo of what you sent
   * 
   * + VacDate - echo of what you sent
   * 
   * + CVX - echo of what you sent
   * 
   * + Forecast Code - The type of evaluation done on this immunization
   * 
   * + Schedule Code - This is the particular dose state the forecaster was on
   * at this point. For example if there are two states P1 and P2 standing for
   * two states in a two dose schedule then this will be P1, P2 or COMPLETED
   * 
   * + Dose Code - This is the dose number and usually numeric as in 1, 2, 3,
   * etc. But in some cases is non-number for example B for Booster and S for
   * Supplemental
   * 
   * + Status Code - This indicates whether this vaccination was valid for the
   * given forecast code, possible values: V : Valid, I : Invalid: M : Missed.
   * (Do you want to get Missed immunization opportunities? If not then there
   * will be no M.)
   * 
   * + Reason - The human readable reason for the status code
   * 
   * A few points I can see now:
   * 
   * You will need to ensure that all patients have their vaccinations together
   * in the file. (I'm sure you're already doing this.) I will process line by
   * line until the patient id changes then do the forecast. Then I will print
   * the results out. So the patients in the results will be in the same order
   * as they were sent. This will help me process efficiently. Do you want to
   * get notice of missed opportunities? This would be were I indicate that when
   * a HepB was given the child was due for a DTaP. What I can do is add a line
   * indicating the Vaccine and the missed date with a status code of M. This
   * would mean that the vaccination should have been given on the same date as
   * another vaccination give but was not. This would not match when it comes
   * back on your side.
   */

  public static final int FIELD_1_PATIENT_ID = 0;
  public static final int FIELD_2_DOB = 1;
  public static final int FIELD_3_VAX_DATE = 2;
  public static final int FIELD_4_CVX = 3;
  public static final int FIELD_5_FORECAST_CODE = 4;
  public static final int FIELD_6_SCHEDULE_CODE = 5;
  public static final int FIELD_7_DOSE_CODE = 6;
  public static final int FIELD_8_STATUS_CODE = 7;
  public static final int FIELD_9_REASON = 8;

  public static final String[] OUTPUT_HEADERS = { "Patient ID", "DOB", "VacDate", "CVX", "Forecast Code",
      "Schedule Code", "Dose Code", "Status Code", "Reason" };

  public static String evaluateCsv(String inputCsv)
  {
    StringWriter stringOut = new StringWriter();
    PrintWriter out = new PrintWriter(stringOut);
    try
    {
      Map<String, String> cvxCodeMap = new HashMap<String, String>();
      initCvxMap(cvxCodeMap);

      BufferedReader in = new BufferedReader(new StringReader(inputCsv));
      String line = "";
      String[] firstLine = null;
      String patientId = "";
      String dob = "";
      List<String> vaxDateList = new ArrayList<String>();
      List<String> cvxList = new ArrayList<String>();

      while ((line = in.readLine()) != null)
      {
        line = line.trim();
        if (line.length() > 0)
        {
          if (firstLine == null)
          {
            firstLine = readFirstLine(line);
            debugFirstLine(out, firstLine);
            printLine(out, OUTPUT_HEADERS);
          } else
          {
            String[] lineFields = readLine(line, firstLine);
            debugLineFields(out, lineFields);
            if (!lineFields[FIELD_1_PATIENT_ID].equals(patientId))
            {
              forecast(out, cvxCodeMap, patientId, dob, vaxDateList, cvxList);
              patientId = lineFields[FIELD_1_PATIENT_ID];
              dob = lineFields[FIELD_2_DOB];
              vaxDateList.add(lineFields[FIELD_3_VAX_DATE]);
              cvxList.add(lineFields[FIELD_4_CVX]);
            }
            else
            {
              vaxDateList.add(lineFields[FIELD_3_VAX_DATE]);
              cvxList.add(lineFields[FIELD_4_CVX]);              
            }
          }
        }
      }
      forecast(out, cvxCodeMap, patientId, dob, vaxDateList, cvxList);

    } catch (Exception e)
    {
      e.printStackTrace(out);
    }
    return stringOut.toString();
  }

  private static void forecast(PrintWriter out, Map<String, String> cvxCodeMap, String patientId, String dob, List<String> vaxDateList,
      List<String> cvxList) throws Exception
  {
    if (!patientId.equals(""))
    {
      List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();
      for (int i = 0; i < vaxDateList.size(); i++)
      {
        if (!vaxDateList.get(i).equals("") && !cvxList.get(i).equals(""))
        {
          String vaccineId = cvxCodeMap.get(cvxList.get(i));
          if (vaccineId != null)
          {
            Immunization imm = new Immunization();
            imm.setDateOfShot(new DateTime(vaxDateList.get(i)).getDate());
            imm.setVaccineId(Integer.parseInt(vaccineId));
            imms.add(imm);
          }
        }
      }
      Forecaster forecaster = new Forecaster(new VaccineForecastManager());
      PatientRecordDataBean patient = new PatientRecordDataBean();
      patient.setSex("F");
      patient.setDob(new DateTime(dob));

      forecaster.setPatient(patient);
      forecaster.setVaccinations(imms);
      forecaster.setForecastDate(new Date());
      List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
      List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
      forecaster.forecast(resultList, doseList, null, null);

      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
      for (int i = 0; i < vaxDateList.size(); i++)
      {
        boolean found = false;
        String reasonNotEvaluated = "Not Evaluated: Forecaster did not evaluate";
        if (vaxDateList.get(i).equals(""))
        {
          reasonNotEvaluated = "Not Evaluated: Vax Date not given";
        }
        else if (cvxList.get(i).equals(""))
        {
          reasonNotEvaluated = "Not Evaluated: CVX not given";
        }
        else if (cvxCodeMap.get(cvxList.get(i)) == null)
        {
          reasonNotEvaluated = "Not Evaluated: CVX not recognized";
        } else
        {
          int vaccineId = Integer.parseInt(cvxCodeMap.get(cvxList.get(i)));
          String vaxDate = vaxDateList.get(i);
          for (int j = 0; j < doseList.size(); j++)
          {
            VaccinationDoseDataBean dose = doseList.get(j);
            if (dose.getVaccineId() == vaccineId && vaxDate.equals(sdf.format(dose.getAdminDate())))
            {
              found = true;
              String[] outputLine = createOutputLine();
              outputLine[FIELD_1_PATIENT_ID] = patientId;
              outputLine[FIELD_2_DOB] = dob;
              outputLine[FIELD_3_VAX_DATE] = vaxDateList.get(i);
              outputLine[FIELD_4_CVX] = vaxDate;
              outputLine[FIELD_5_FORECAST_CODE] = dose.getForecastCode();
              outputLine[FIELD_6_SCHEDULE_CODE] = dose.getScheduleCode();
              outputLine[FIELD_7_DOSE_CODE] = dose.getDoseCode();
              outputLine[FIELD_8_STATUS_CODE] = dose.getStatusCode();
              outputLine[FIELD_9_REASON] = dose.getReason();
              printLine(out, outputLine);
            }
          }
        }
        if (!found)
        {
          String[] outputLine = createOutputLine();
          outputLine[FIELD_1_PATIENT_ID] = patientId;
          outputLine[FIELD_2_DOB] = dob;
          outputLine[FIELD_3_VAX_DATE] = vaxDateList.get(i);
          outputLine[FIELD_4_CVX] = cvxList.get(i);
          outputLine[FIELD_9_REASON] = reasonNotEvaluated;
          printLine(out, outputLine);
        }
      }
      vaxDateList.clear();
      cvxList.clear();
    }
  }

  private static void initCvxMap(Map<String, String> cvxCodeMap) throws DataSourceUnavailableException, SQLException
  {
    Connection conn = DatabasePool.getConnection();
    try
    {
      PreparedStatement pstmt = conn.prepareStatement("SELECT cvx_code, vaccine_id FROM vaccine_cvx ");
      ResultSet rset = pstmt.executeQuery();
      while (rset.next())
      {
        String cvxCode = rset.getString(1);
        String vaccineId = rset.getString(2);
        cvxCodeMap.put(cvxCode, vaccineId);
        // solve the problem of the pesky 03 code being sent in as only 3
        if (cvxCode.startsWith("0") && cvxCode.length() == 2)
        {
          cvxCodeMap.put(cvxCode.substring(1), vaccineId);
        }
      }
    } finally
    {
      DatabasePool.close(conn);
    }
  }

  private static String[] createOutputLine()
  {
    String[] outputLine = new String[OUTPUT_HEADERS.length];
    for (int i = 0; i < outputLine.length; i++)
    {
      outputLine[i] = "";
    }
    return outputLine;
  }

  private static void printLine(PrintWriter out, String[] printLine)
  {
    for (int i = 0; i < printLine.length; i++)
    {
      if (i > 0)
      {
        out.print(",");
      }
      out.print("\"");
      out.print(printLine[i]);
      out.print("\"");
    }
    out.println();
  }

  private static void debugFirstLine(PrintWriter out, String[] firstLine)
  {
    if (DEBUG_CVX_READER)
    {
      if (firstLine != null)
      {
        out.print("First Line: ");
        for (int i = 0; i < firstLine.length; i++)
        {
          out.print(firstLine[i] + ",");
        }
        out.println();
      } else
      {
        out.println("No First Line");
      }
    }
  }

  private static void debugLineFields(PrintWriter out, String[] lineFields)
  {
    if (DEBUG_CVX_READER)
    {
      if (lineFields != null)
      {
        out.print("Line: ");
        for (int i = 0; i < lineFields.length; i++)
        {
          out.print(lineFields[i] + ",");
        }
        out.println();
      } else
      {
        out.println("No Line");
      }
    }
  }

  private static String[] readFirstLine(String line)
  {
    String[] firstLine;
    // read first line
    List<String> tokenList = new ArrayList<String>();
    char[] lineChars = line.toCharArray();
    String token = "";
    boolean quoted = false;
    for (int i = 0; i < lineChars.length; i++)
    {
      if (lineChars[i] == '"')
      {
        if (quoted)
        {
          quoted = false;
        } else
        {
          quoted = true;
        }
      } else if (lineChars[i] == ',')
      {
        if (quoted)
        {
          token += lineChars[i];
        } else
        {
          tokenList.add(token);
          token = "";
        }
      } else
      {
        token += lineChars[i];
      }
    }
    if (token.length() > 0)
    {
      tokenList.add(token);
    }
    firstLine = new String[tokenList.size()];
    for (int i = 0; i < tokenList.size(); i++)
    {
      firstLine[i] = tokenList.get(i);
    }
    return firstLine;
  }

  private static String[] readLine(String line, String[] firstLine)
  {
    String[] lineFields = new String[firstLine.length];
    // read first line
    char[] lineChars = line.toCharArray();
    String value = "";
    boolean quoted = false;
    int pos = 0;
    for (int i = 0; i < lineChars.length && pos < firstLine.length; i++)
    {
      if (lineChars[i] == '"')
      {
        if (quoted)
        {
          quoted = false;
        } else
        {
          quoted = true;
        }
      } else if (lineChars[i] == ',')
      {
        if (quoted)
        {
          value += lineChars[i];
        } else
        {
          lineFields[pos++] = value;
          value = "";
        }
      } else
      {
        value += lineChars[i];
      }
    }
    if (value.length() > 0 && pos < lineFields.length)
    {
      lineFields[pos++] = value;
    }
    for (int i = pos; i < lineFields.length; i++)
    {
      lineFields[i] = "";
    }
    return lineFields;
  }

}
