import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class IHSTestGenerator
{

  private static Map forecastCode = new HashMap();

  static
  {
    forecastCode.put("HepB", "HEPB");
    forecastCode.put("Var", "Var");
    forecastCode.put("Hib", "HIB");
    forecastCode.put("DTaP", "DTP");
    forecastCode.put("HepA", "HEPA");
    forecastCode.put("Polio", "OORIPV");
    forecastCode.put("Meni", "MENING");
    forecastCode.put("MMR", "MMR");
    forecastCode.put("Pneu", "SPn");
    forecastCode.put("Rota", "Rv");
    forecastCode.put("HPV", "HPV");
    forecastCode.put("Flu", "Influenza");
  }
  private static SimpleDateFormat sdfOut = new SimpleDateFormat("MM/dd/yyyy");

  public static void main(String[] args) throws Exception
  {
    File expectedResultsFile = new File(args[0]);
    loadTestVaccines(args[1]);
    loadTestCases(args[2]);
    File outFile = new File(args[3]);
    PrintWriter out = new PrintWriter(new FileWriter(outFile));
    try
    {
      printHeader(out);
      BufferedReader in = new BufferedReader(new FileReader(expectedResultsFile));
      String line = in.readLine(); // read header row
      while ((line = in.readLine()) != null)
      {
        String[] fields = line.split("\\,");
        if (fields.length < 7)
        {
          continue;
        }
        String caseId = readString(fields, 1);
        System.out.println(" + " + caseId);
        String lineCode = (String) forecastCode.get(readString(fields, 3));
        Date overdueDate = readDate(fields, 7);
        TestCase testCase = (TestCase) testCases.get(caseId);
        List testVaccines = (List) vaccinesByCase.get(caseId);
        if (testCase == null)
        {
          throw new Exception("Case " + caseId + " was not found in test case file");
        }
        Date latestEventDate = testCase.patientDob;
        if (testVaccines != null)
        {
          for (Iterator it = testVaccines.iterator(); it.hasNext();)
          {
            TestVaccine testVaccine = (TestVaccine) it.next();
            if (testVaccine.adminDate.after(latestEventDate))
            {
              latestEventDate = testVaccine.adminDate;
            }
          }
        }
        Calendar dayAfterLastEvent = Calendar.getInstance();
        dayAfterLastEvent.setTime(latestEventDate);
        dayAfterLastEvent.add(Calendar.DAY_OF_MONTH, 1);
        printForecast(out, caseId, lineCode, testCase, testVaccines, dayAfterLastEvent.getTime(), "Due");
        if (overdueDate != null)
        {
          Calendar forecastOverdue = Calendar.getInstance();
          forecastOverdue.setTime(overdueDate);
          forecastOverdue.add(Calendar.DAY_OF_MONTH, 14);

          Date forecastDate = forecastOverdue.getTime();
          printForecast(out, caseId, lineCode, testCase, testVaccines, forecastDate, "Overdue");
        }
      }
    } finally
    {
      out.close();
    }
  }

  private static void printForecast(PrintWriter out, String caseId, String lineCode, TestCase testCase,
      List testVaccines, Date forecastDate, String description)
  {
    out.println("Case: " + testCase.caseLabel + " (" + caseId + ") " + description);
    out.println("Active: " + lineCode);
    out.println("ndp:i");
    out.println("Date Used for Forecast: " + sdfOut.format(forecastDate));
    out.println("Date of birth: " + sdfOut.format(testCase.patientDob));
    out.println("Gender: " + (testCase.patientSex.equals("F") ? "Female" : "Male"));
    out.println("Contraindicated vaccines: ");
    out.println("Other facts:");
    if (testVaccines != null)
    {
      for (Iterator it = testVaccines.iterator(); it.hasNext();)
      {
        TestVaccine testVaccine = (TestVaccine) it.next();
        out.println(testVaccine.cvxCode + ": " + sdfOut.format(testVaccine.adminDate));
      }
    }
    out.println("");
  }

  private static Map vaccinesByCase = new HashMap();

  private static class TestVaccine
  {
    String cvxCode = "";
    Date adminDate = null;
  }

  private static void loadTestVaccines(String filename) throws Exception
  {
    File file = new File(filename);
    BufferedReader in = new BufferedReader(new FileReader(file));
    String line = in.readLine(); // read header row
    while ((line = in.readLine()) != null)
    {
      String[] fields = line.split("\\,");
      if (fields.length < 3)
      {
        continue;
      }
      String caseId = readString(fields, 1);
      TestVaccine testVaccine = new TestVaccine();
      testVaccine.cvxCode = readString(fields, 2);
      testVaccine.adminDate = readDate(fields, 3);
      if (testVaccine.adminDate == null)
      {
        throw new Exception("Date was null on this line: " + line);
      }
      List vaccines = (List) vaccinesByCase.get(caseId);
      if (vaccines == null)
      {
        vaccines = new ArrayList();
        vaccinesByCase.put(caseId, vaccines);
      }
      vaccines.add(testVaccine);
    }
  }

  private static Map testCases = new HashMap();

  private static class TestCase
  {
    String caseLabel = "";
    String groupCode = "";
    String patientLast = "";
    String patientFirst = "";
    Date patientDob = null;
    String patientSex = "";
  }

  private static void loadTestCases(String filename) throws Exception
  {
    File file = new File(filename);
    BufferedReader in = new BufferedReader(new FileReader(file));
    String line = in.readLine(); // read header row
    while ((line = in.readLine()) != null)
    {
      String[] fields = line.split("\\,");
      if (fields.length < 6)
      {
        continue;
      }
      String caseId = readString(fields, 1);
      TestCase testCase = new TestCase();
      testCase.caseLabel = readString(fields, 2);
      testCase.groupCode = readString(fields, 3);
      testCase.patientLast = readString(fields, 4);
      testCase.patientFirst = readString(fields, 5);
      testCase.patientDob = readDate(fields, 6);
      testCase.patientSex = readString(fields, 7);
      testCases.put(caseId, testCase);
    }

  }

  private static void printHeader(PrintWriter out)
  {
    out.println("Version: OHD_1 ");
    out.println("Forecasting_Mode: Acceptable");
    out.println("Forecasting_Interval: Invalid");
    out.println("");
    out.println("");
  }

  private static String readString(String[] fields, int pos)
  {
    pos--;
    if (fields.length > pos)
    {
      String field = fields[pos];
      if (field.startsWith("\""))
      {
        field = field.substring(1);
        if (field.endsWith("\""))
        {
          field = field.substring(0, field.length() - 1);
        }
      }
      return field;
    }
    return "";
  }

  private static SimpleDateFormat sdfIn = new SimpleDateFormat("yyyy-MM-dd");

  private static Date readDate(String[] fields, int pos) throws ParseException
  {
    String field = readString(fields, pos);
    if (field.equals("NULL") || field.equals(""))
    {
      return null;
    }
    return sdfIn.parse(field);
  }
}
