<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>

<%@page import="java.net.URLEncoder"%>
<%@page import="org.tch.forecast.core.Forecaster"%>
<%@page import="org.tch.hl7.immunizations.databeans.PatientDataBean"%>
<%@page import="org.tch.forecast.support.PatientRecordDataBean"%>
<%@page import="org.tch.hl7.core.util.DateTime"%>
<%@page import="org.tch.forecast.support.Immunization"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.tch.forecast.core.ImmunizationForecastDataBean"%>
<%@page import="org.tch.forecast.core.VaccineForecastManagerInterface"%>
<%@page import="org.tch.forecast.support.VaccineForecastManager"%><html>
<head>
<title>Forecaster Validator</title>
</head>
<body>
<% 
String caseId = request.getParameter("caseId");
String action = request.getParameter("action");
if (action == null)
{
  action = "";
}
String url;
Connection conn = DatabasePool.getConnection();
PreparedStatement pstmt = null;
try {
  String sql = null;
  String userName = request.getParameter("userName");
  if (userName == null)
  {
    userName = "";
  }
  if (!userName.equals("")) {
    String noteText = request.getParameter("noteText");
    if (action.equals("changeStatus"))
    {
      sql = "UPDATE test_case SET status_code = ? WHERE case_id = ? ";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, request.getParameter("statusCode"));
      pstmt.setString(2, caseId);
      pstmt.executeUpdate();
      pstmt.close();
      noteText = "Changed test status to " + request.getParameter("statusCode");
    }
    else if (action.equals("Change Expected"))
    {
      sql = "UPDATE expected_result SET dose_number = ?, valid_date = str_to_date(?, '%m/%d/%Y'), due_date = str_to_date(?, '%m/%d/%Y'), overdue_date = str_to_date(?, '%m/%d/%Y') WHERE case_id = ? AND entity_id = 2 AND line_code = ?";
    
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, request.getParameter("doseNumberExpected"));
      pstmt.setString(2, request.getParameter("validDateExpected"));
      pstmt.setString(3, request.getParameter("dueDateExpected"));
      pstmt.setString(4, request.getParameter("overdueDateExpected"));
      pstmt.setString(5, caseId);
      pstmt.setString(6, request.getParameter("lineCode"));
      pstmt.executeUpdate();
      pstmt.close();
      noteText = "Changed expected result. Dose number = " +  request.getParameter("doseNumberExpected") + 
      ". Valid date = " + request.getParameter("validDateExpected") +
      ". Due date = " + request.getParameter("dueDateExpected") +
      ". Overdue date = " + request.getParameter("overdueDateExpected") + ".";
    }
    else if (action.equals("Age Test in Years") && request.getParameter("confirm") != null)
    {
      int yearAdd = Integer.parseInt(request.getParameter("yearAdd"));
      sql = "UPDATE test_case SET patient_dob = date_add(patient_dob, INTERVAL ? YEAR) where case_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, yearAdd);
      pstmt.setString(2, caseId);
      pstmt.executeUpdate();
      pstmt.close();
      sql = "UPDATE expected_result \n" +
      "  SET  \n" +
      "  valid_date = date_add(valid_date, INTERVAL ? YEAR), \n" +
      "  due_date = date_add(due_date, INTERVAL ? YEAR), \n" +
      "  overdue_date = date_add(overdue_date, INTERVAL ? YEAR) \n" +
      "WHERE case_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, yearAdd);
      pstmt.setInt(2, yearAdd);
      pstmt.setInt(3, yearAdd);
      pstmt.setString(4, caseId);
      pstmt.executeUpdate();
      pstmt.close();
      sql = "UPDATE actual_result \n" +
      "  SET  \n" +
      "  valid_date = date_add(valid_date, INTERVAL ? YEAR), \n" +
      "  due_date = date_add(due_date, INTERVAL ? YEAR), \n" +
      "  overdue_date = date_add(overdue_date, INTERVAL ? YEAR) \n" +
      "WHERE case_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, yearAdd);
      pstmt.setInt(2, yearAdd);
      pstmt.setInt(3, yearAdd);
      pstmt.setString(4, caseId);
      pstmt.executeUpdate();
      pstmt.close();      
      sql = "UPDATE test_vaccine SET admin_date = date_add(admin_date, INTERVAL ? YEAR) WHERE case_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, yearAdd);
      pstmt.setString(2, caseId);
      pstmt.executeUpdate();
      pstmt.close();
    }
    else if (action.equals("Age Test in Months") && request.getParameter("confirm") != null)
    {
      int monthAdd = Integer.parseInt(request.getParameter("monthAdd"));
      sql = "UPDATE test_case SET patient_dob = date_add(patient_dob, INTERVAL ? MONTH) where case_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, monthAdd);
      pstmt.setString(2, caseId);
      pstmt.executeUpdate();
      pstmt.close();
      sql = "UPDATE expected_result \n" +
      "  SET  \n" +
      "  valid_date = date_add(valid_date, INTERVAL ? MONTH), \n" +
      "  due_date = date_add(due_date, INTERVAL ? MONTH), \n" +
      "  overdue_date = date_add(overdue_date, INTERVAL ? MONTH) \n" +
      "WHERE case_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, monthAdd);
      pstmt.setInt(2, monthAdd);
      pstmt.setInt(3, monthAdd);
      pstmt.setString(4, caseId);
      pstmt.executeUpdate();
      pstmt.close();
      sql = "UPDATE actual_result \n" +
      "  SET  \n" +
      "  valid_date = date_add(valid_date, INTERVAL ? MONTH), \n" +
      "  due_date = date_add(due_date, INTERVAL ? MONTH), \n" +
      "  overdue_date = date_add(overdue_date, INTERVAL ? MONTH) \n" +
      "WHERE case_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, monthAdd);
      pstmt.setInt(2, monthAdd);
      pstmt.setInt(3, monthAdd);
      pstmt.setString(4, caseId);
      pstmt.executeUpdate();
      pstmt.close();      
      sql = "UPDATE test_vaccine SET admin_date = date_add(admin_date, INTERVAL ? MONTH) WHERE case_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, monthAdd);
      pstmt.setString(2, caseId);
      pstmt.executeUpdate();
      pstmt.close();
    }else if (action.equals("CT_EXPECTED")||
					action.equals("TCH_EXPECTED") ||
					action.equals("TCH_ACTUAL") ||
					action.equals("MCIR_ACTUAL")
		){
		org.tch.forecast.validator.ForecastComparisonSaver.saveForecast(request);
	}
    if (noteText != null && !noteText.equals("")) 
    {
      sql = "INSERT INTO test_note (case_id, entity_id, user_name, note_text, note_date) VALUES (?, 2, ?, ?, NOW())";
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, caseId);
      pstmt.setString(2, userName);
      pstmt.setString(3, noteText);
      pstmt.executeUpdate();
      pstmt.close();
    }
  
  Forecaster forecaster = new Forecaster(new VaccineForecastManager());
  StringBuffer traceBuffer = new StringBuffer();
  List resultList = new ArrayList();
  List doseList = new ArrayList();
  PatientRecordDataBean patient = new PatientRecordDataBean();
  List imms = new ArrayList();
  sql = "SELECT tc.case_label, tc.case_description, tc.case_source, tc.group_code, tc.patient_first, \n" + 
    "tc.patient_last, date_format(tc.patient_dob, '%m/%d/%Y'), tc.patient_sex, tc.status_code, ts.status_label\n" +
    "FROM test_case tc, test_status ts\n" +
    "WHERE tc.case_id =" + caseId + " \n" +
    "  AND tc.status_code = ts.status_code\n";
  pstmt = conn.prepareStatement(sql);
  ResultSet rset = pstmt.executeQuery();
  if (rset.next())
  {
    patient.setSex(rset.getString(8));
    patient.setDob(new DateTime(rset.getString(7)));
  
%>
<h1><%= rset.getString(1) %></h1>
<table border="0">
  <tr>
    <th align="left">Description&nbsp;</th>
    <td><%= rset.getString(2) %></td>
  </tr>
  <tr>
    <th align="left">Patient&nbsp;</th>
    <td><%= rset.getString(6) == null ? "" : rset.getString(6) %>, <%= rset.getString(5) == null ? "" : rset.getString(5) %>
	(<%= rset.getString(8) == null ? "" : rset.getString(8) %>) dob <%= rset.getString(7) == null ? "":rset.getString(7) %>
	</td>
  </tr>
  <tr>
    <th align="left">Status&nbsp;</th>
    <td><%= rset.getString(10) %> ... change status to: <a href="testCase.jsp?caseId=<%= caseId %>&userName=<%= URLEncoder.encode(userName, "UTF-8") %>&action=changeStatus&statusCode=PASS">pass</a> <a href="testCase.jsp?caseId=<%= caseId %>&userName=<%= URLEncoder.encode(userName, "UTF-8") %>&action=changeStatus&statusCode=ACC">accept</a> <a href="testCase.jsp?caseId=<%= caseId %>&userName=<%= URLEncoder.encode(userName, "UTF-8") %>&action=changeStatus&statusCode=RES">research</a> <a href="testCase.jsp?caseId=<%= caseId %>&userName=<%= URLEncoder.encode(userName, "UTF-8") %>&action=changeStatus&statusCode=FAIL">fail</a> <a href="testCase.jsp?caseId=<%= caseId %>&userName=<%= URLEncoder.encode(userName, "UTF-8") %>&action=changeStatus&statusCode=FIX">fixed</a></td>
  </tr>
</table>
<% String editTesturl = new String("editTestCase.jsp?");
				editTesturl = editTesturl + "case_id=" + caseId;
				editTesturl = editTesturl + "&userName=" + URLEncoder.encode(userName, "UTF-8");
%>
<p>
[<a href="index.jsp?userName=<%= URLEncoder.encode(userName, "UTF-8") %>">Back to Home</a>] 
[<a href="showSchedule.jsp?caseId=<%= caseId %>&userName=<%= URLEncoder.encode(userName, "UTF-8") %>">Forecast Trace</a>]
[<a href=" <%= editTesturl %>">Edit Test Case</a>]
</p>
  <%
  sql = "SELECT tv.cvx_code, cvx.cvx_label, date_format(admin_date, '%m/%d/%Y'), mvx_code, cvx.vaccine_id \n" + 
    "FROM test_vaccine tv, vaccine_cvx cvx \n" +
    "WHERE tv.cvx_code = cvx.cvx_code \n" +
    "  AND tv.case_id = ? \n";
  rset.close();
  pstmt.close();
  pstmt = conn.prepareStatement(sql);
  pstmt.setString(1, caseId);
  rset = pstmt.executeQuery();
  %>
    <h2>Vaccinations</h2>
      <table border="1" cellspacing="0">
        <tr>
          <th>Vaccine</th>
          <th>Date</th>
          <th>MVX</th>
          <th>CVX</th>
          <th>TCH</th>
		  <th>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
         </tr>
        <% while (rset.next()) { 
          Immunization imm = new Immunization();
          imm.setDateOfShot(new DateTime(rset.getString(3)).getDate());
          imm.setVaccineId(rset.getInt(5));
          imms.add(imm);
        %>
        <tr>
          <td><%= rset.getString(2) %>&nbsp;</td>
          <td><%= rset.getString(3) %>&nbsp;</td>
          <td><%= rset.getString(4) %>&nbsp;</td>
          <td><%= rset.getString(1) %>&nbsp;</td>
          <td><%= rset.getString(5) %>&nbsp;</td>
		   <td>
				<%  String editVaccineUrl = new String("addVaccineToCase.jsp?");
				editVaccineUrl = editVaccineUrl + "userName=" + URLEncoder.encode(userName, "UTF-8");
				editVaccineUrl = editVaccineUrl + "&case_id=" + caseId;
				editVaccineUrl = editVaccineUrl + "&cvx_code=" +  URLEncoder.encode(rset.getString(1), "UTF-8");
				editVaccineUrl = editVaccineUrl + "&vaccine_id=" +  URLEncoder.encode(rset.getString(5), "UTF-8");
				editVaccineUrl = editVaccineUrl + "&mvx_code=" +  URLEncoder.encode(rset.getString(4), "UTF-8");
				editVaccineUrl = editVaccineUrl + "&admin_date=" +  URLEncoder.encode(rset.getString(3), "UTF-8");
				editVaccineUrl = editVaccineUrl + "&edit=y";
				%>
			   <a href="<%= editVaccineUrl %>" title="Edit" >Edit</a>&nbsp;
			</td>
        </tr>
        <% }
        rset.close();
        pstmt.close();
        forecaster.setPatient(patient);
        forecaster.setVaccinations(imms);
        forecaster.forecast(resultList, doseList, traceBuffer, null);
        
        
        %>
      </table>
<br>
<%  String addcasevaccine = new String("addVaccineToCase.jsp?");
	addcasevaccine = addcasevaccine + "&case_id=" + caseId;
	addcasevaccine = addcasevaccine + "&userName=" + URLEncoder.encode(userName, "UTF-8");
%>
[<a href="<%= addcasevaccine %>" title="Add Vaccine to Case" >Add Vaccine to Case</a>]

<h2>Forecast Comparison</h2>
<% 
sql = "SELECT er.line_code, fl.line_label, er.dose_number, date_format(er.valid_date, '%m/%d/%Y'), date_format(er.due_date, '%m/%d/%Y'), date_format(er.overdue_date, '%m/%d/%Y') \n" +
"FROM expected_result er, forecast_line fl \n" +
"WHERE case_id = " + caseId + " \n" +
"  AND entity_id = 2 \n" +
"  AND er.line_code = fl.line_code \n";
pstmt = conn.prepareStatement(sql);
rset = pstmt.executeQuery();
while (rset.next()) {
  String lineCode = rset.getString(1);
  String lineLabel = rset.getString(2);
  String doseNumberExpected = rset.getString(3);
  String validDateExpected = rset.getString(4);
  String dueDateExpected = rset.getString(5);
  String overdueDateExpected = rset.getString(6);
  if (validDateExpected == null || validDateExpected.equals("00/00/0000"))
  {
    validDateExpected = "";
  }
  if (dueDateExpected == null || dueDateExpected.equals("00/00/0000"))
  {
    dueDateExpected = "";
  }
  if (overdueDateExpected == null || overdueDateExpected.equals("00/00/0000"))
  {
    overdueDateExpected = "";
  }
  
%>
<h3><%= lineLabel %></h3>
      <table border="1" cellspacing="0">
        <tr>
          <th>Source</th>
          <th>Dose</th>
          <th>Valid</th>
          <th>Due</th>
          <th>Overdue</th>
		  <th>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
        </tr>
        <%
        String doseNumberActual = "COMP";
        String validDateActual = "";
        String dueDateActual = "";
        String overdueDateActual = "";
        DateTime today = new DateTime("today");
		ImmunizationForecastDataBean forecast = null;
        for (Iterator it = resultList.iterator(); it.hasNext(); )
        {  
          forecast = (ImmunizationForecastDataBean) it.next();
          String forecastLabel = forecast.getForecastLabel();
          if (forecastLabel.equals("DTaP/Tdap"))
          {
            forecastLabel = "DTaP";
          }
          else if (forecastLabel.equals("Varicella"))
          {
            forecastLabel = "Var";
          }
          else if (forecastLabel.equals("IPV"))
          {
            forecastLabel = "Polio";
          }
          else if (forecastLabel.equals("MCV4"))
          {
            forecastLabel = "Meni";
          }
          else if (forecastLabel.equals("Measles"))
          {
            forecastLabel = "MMR";
          }
          else if (forecastLabel.equals("PCV7"))
          {
            forecastLabel = "Pneu";
          }
          else if (forecastLabel.equals("Rotavirus"))
          {
            forecastLabel = "Rota";
          }
          else if (forecastLabel.equals("Influenza"))
          {
            forecastLabel = "Flu";
          }
          if (forecastLabel.equals(lineCode))
          {
            DateTime finishedActual = new DateTime(forecast.getFinished());
            if (today.isLessThan(finishedActual))
            {
              validDateActual = new DateTime(forecast.getValid()).toString("M/D/Y");
              dueDateActual = new DateTime(forecast.getDue()).toString("M/D/Y");
              overdueDateActual = new DateTime(forecast.getOverdue()).toString("M/D/Y");
              doseNumberActual = forecast.getDose();
            }
          }
        }
        
        String sql2 = "SELECT er.entity_id, ee.entity_label, er.dose_number, date_format(er.valid_date, '%m/%d/%Y'), date_format(er.due_date, '%m/%d/%Y'), date_format(er.overdue_date, '%m/%d/%Y') \n" +
        "FROM expected_result er, expecting_entity ee \n" +
        "WHERE case_id = " + caseId + " \n" +
        "  AND er.entity_id <> 2 \n" +
        "  AND er.entity_id = ee.entity_id \n" + 
        "  AND er.line_code = '" + lineCode + "' \n";
        PreparedStatement pstmt2 = conn.prepareStatement(sql2);
        ResultSet rset2 = pstmt2.executeQuery();
        while (rset2.next()) 
        {
          String doseNumberCompare = rset2.getString(3);
          String validDateCompare = rset2.getString(4);
          String dueDateCompare = rset2.getString(5);
          String overdueDateCompare = rset2.getString(6);
          if (validDateCompare == null)
          {
            validDateCompare = "";
          }
          if (dueDateCompare == null)
          {
            dueDateCompare = "";
          }
          if (overdueDateCompare == null)
          {
            overdueDateCompare = "";
          }
        %>
        <tr>
          <td><%= rset2.getString(2) %> Expected&nbsp;</td>
          <td bgcolor="<%= doseNumberCompare.equals(doseNumberActual) ? "#FFFFFF" : "#FF9933" %>"><%= doseNumberCompare %>&nbsp;</td>
          <td bgcolor="<%= validDateCompare.equals(validDateActual) ? "#FFFFFF" : "#FF9933" %>"><%= validDateCompare %>&nbsp;</td>
          <td bgcolor="<%= dueDateCompare.equals(dueDateActual) ? "#FFFFFF" : "#FF9933" %>"><%= dueDateCompare %>&nbsp;</td>
          <td bgcolor="<%= overdueDateCompare.equals(overdueDateActual) ? "#FFFFFF" : "#FF9933" %>"><%= overdueDateCompare %>&nbsp;</td>
		  <td>
				<%  url = new String("editActuals.jsp?");
		
				url = url + "action=CT_EXPECTED";
				url = url + "&caseId=" + caseId;
				url = url + "&dose=" + doseNumberCompare;
				url = url + "&valid_date=" + validDateCompare;
				url = url + "&due_date=" + dueDateCompare;
				url = url + "&overdue_date=" + overdueDateCompare;
				url = url + "&userName=" + URLEncoder.encode(userName, "UTF-8");
				url = url + "&header=" + URLEncoder.encode(rset2.getString(2) + " Expected", "UTF-8");
				url = url + "&line_code=" + URLEncoder.encode(lineCode, "UTF-8");

				%>
			  <a href="<%= url %>" title="Edit" >Edit</a>
		  </td>
        </tr>
        <% } %>
        <tr>
          <td bgcolor="#FFFF99">TCH Expected&nbsp;</td>
          <td bgcolor="<%= doseNumberExpected.equals(doseNumberActual) ? "#FFFF99" : "#FF9933" %>"><%= doseNumberExpected %>&nbsp;</td>
          <td bgcolor="<%= validDateExpected.equals(validDateActual) ? "#FFFF99" : "#FF9933" %>"><%= validDateExpected %>&nbsp;</td>
          <td bgcolor="<%= dueDateExpected.equals(dueDateActual) ? "#FFFF99" : "#FF9933" %>"><%= dueDateExpected %>&nbsp;</td>
          <td bgcolor="<%= overdueDateExpected.equals(overdueDateActual) ? "#FFFF99" : "#FF9933" %>"><%= overdueDateExpected %>&nbsp;</td>
		   <td>
				<%  url = new String("editActuals.jsp?");
		
				url = url + "action=TCH_EXPECTED";
				url = url + "&caseId=" + caseId;
				url = url + "&dose=" + doseNumberExpected;
				url = url + "&valid_date=" + validDateExpected;
				url = url + "&due_date=" + dueDateExpected;
				url = url + "&overdue_date=" + overdueDateExpected;
				url = url + "&userName=" + URLEncoder.encode(userName, "UTF-8");
				url = url + "&header=" + URLEncoder.encode("TCH Expected", "UTF-8");
				url = url + "&line_code=" + URLEncoder.encode(lineCode, "UTF-8");

				%>
			  <a href="<%= url %>" title="Edit" >Edit</a>
		  </td>
        </tr>
        <tr>
          <td bgcolor="#FFFF99">TCH Actual&nbsp;</td>
          <td bgcolor="#FFFF99"><%= doseNumberActual %>&nbsp;</td>
          <td bgcolor="#FFFF99"><%= validDateActual %>&nbsp;</td>
          <td bgcolor="#FFFF99"><%= dueDateActual %>&nbsp;</td>
          <td bgcolor="#FFFF99"><%= overdueDateActual %>&nbsp;</td>
		<!--  <td>
				<%  url = new String("editActuals.jsp?");
			  validDateActual = new DateTime(forecast.getValid()).toString("M/D/Y");
              dueDateActual = new DateTime(forecast.getDue()).toString("M/D/Y");
              overdueDateActual = new DateTime(forecast.getOverdue()).toString("M/D/Y");
              doseNumberActual = forecast.getDose();

				url = url + "action=TCH_ACTUAL";
				url = url + "&caseId=" + caseId;
				url = url + "&dose=" + doseNumberActual;
				url = url + "&valid_date=" + validDateActual;
				url = url + "&due_date=" + dueDateActual;
				url = url + "&overdue_date=" + overdueDateActual;
				url = url + "&userName=" + URLEncoder.encode(userName, "UTF-8");
				url = url + "&header=" + URLEncoder.encode("TCH Actual", "UTF-8");
				url = url + "&line_code=" + URLEncoder.encode(lineCode, "UTF-8");


				%>
			  <a href="<%= url %>" title="Edit" target="_blank" >Edit</a>
		  </td> -->
        </tr>
        <%
        rset2.close();
        pstmt2.close();
        sql2 = "SELECT ar.software_id, fs.software_label, ar.dose_number, date_format(ar.valid_date, '%m/%d/%Y'), date_format(ar.due_date, '%m/%d/%Y'), date_format(ar.overdue_date, '%m/%d/%Y') \n" +
        "FROM actual_result ar, forecasting_software fs \n" +
        "WHERE ar.case_id = " + caseId + " \n" +
        "  AND ar.software_id <> 1 \n" + 
        "  AND ar.software_id = fs.software_id \n" + 
        "  AND ar.line_code = '" + lineCode + "' \n";
        pstmt2 = conn.prepareStatement(sql2);
        rset2 = pstmt2.executeQuery();
        while (rset2.next()) 
        {
          String doseNumberCompare = rset2.getString(3);
          String validDateCompare = rset2.getString(4);
          String dueDateCompare = rset2.getString(5);
          String overdueDateCompare = rset2.getString(6);
          if (validDateCompare == null)
          {
            validDateCompare = "";
          }
          if (dueDateCompare == null)
          {
            dueDateCompare = "";
          }
          if (overdueDateCompare == null)
          {
            overdueDateCompare = "";
          }
        %>
        <tr>
          <td><%= rset2.getString(2) %> Actual&nbsp;</td>
          <td bgcolor="<%= doseNumberCompare.equals(doseNumberActual) ? "#FFFFFF" : "#FF9933" %>"><%= doseNumberCompare %>&nbsp;</td>
          <td bgcolor="<%= validDateCompare.equals(validDateActual) ? "#FFFFFF" : "#FF9933" %>"><%= validDateCompare %>&nbsp;</td>
          <td bgcolor="<%= dueDateCompare.equals(dueDateActual) ? "#FFFFFF" : "#FF9933" %>"><%= dueDateCompare %>&nbsp;</td>
          <td bgcolor="<%= overdueDateCompare.equals(overdueDateActual) ? "#FFFFFF" : "#FF9933" %>"><%= overdueDateCompare %>&nbsp;</td>
		  	  <td>
				<%  url = new String("editActuals.jsp?");
				url = url + "action=MCIR_ACTUAL";
				url = url + "&caseId=" + caseId;
				url = url + "&dose=" + doseNumberCompare;
				url = url + "&valid_date=" + validDateCompare;
				url = url + "&due_date=" + dueDateCompare;
				url = url + "&overdue_date=" + overdueDateCompare;
				url = url + "&userName=" + URLEncoder.encode(userName, "UTF-8");
				url = url + "&header=" + URLEncoder.encode(rset2.getString(2) + "Actual", "UTF-8");
				url = url + "&line_code=" + URLEncoder.encode(lineCode, "UTF-8");

				%>
			  <a href="<%= url %>" title="Edit" >Edit</a>
		  </td>
        </tr>
        <% } %>
        
      </table>
      <!--
      <form>
        <table>
        <tr>
          <th>Change</th>
          <th>Dose</th>
          <th>Valid</th>
          <th>Due</th>
          <th>Overdue</th>
        </tr>
        <tr>
          <td>TCH Expected</td>
          <td><input type="text" size="1" name="doseNumberExpected" value="<%= doseNumberExpected %>"></td>
          <td><input type="text" size="8" name="validDateExpected" value="<%= validDateExpected %>"></td>
          <td><input type="text" size="8" name="dueDateExpected" value="<%= dueDateExpected %>"></td>
          <td><input type="text" size="8" name="overdueDateExpected" value="<%= overdueDateExpected %>"></td>
          <input type="hidden" name="caseId" value="<%= caseId %>">
          <input type="hidden" name="lineCode" value="<%= lineCode %>">
          <input type="hidden" name="userName" value="<%= userName %>">
          </tr>
        </table>
        <input type="submit" name="action" value="Change Expected">
      </form> -->

      
    <% } %>
<h2>Notes</h2>
<%
pstmt.close();
sql = "SELECT ee.entity_label, tn.note_text, date_format(tn.note_date, '%m/%d/%Y %r'), tn.user_name \n" +
"FROM test_note tn, expecting_entity ee \n" +
"WHERE tn.case_id = ? \n" +
"  AND ee.entity_id = tn.entity_id \n" +
"ORDER BY tn.note_date";
pstmt = conn.prepareStatement(sql);
pstmt.setString(1, caseId);
rset = pstmt.executeQuery();
while (rset.next()) {
  String entityLabel = rset.getString(1);
  noteText = rset.getString(2);
  String noteDate = rset.getString(3);
  String noteUserName = rset.getString(4);
%>
  <p><b>Explanation note from <%= entityLabel %> <%= noteDate %></b>
  <br><font color="#CC3333"><%= noteUserName %></font>: <%= noteText %></p>
    <% } %> 
    <form>
      <table>
        <tr> 
          <td valign="top">Name</td>
          <td><input type="text" name="userName" value="<%= userName %>"></td>
        </tr>
        <tr>
          <td valign="top">Note</td>
          <td> 
            <textarea name="noteText" cols="30" rows="3"></textarea>
            <input type="hidden" name="caseId" value="<%= caseId %>">
          </td>
        </tr>
        <tr>
          <td colspan="2" align="right">
            <input type="submit" name="action" value="Add Note">
          </td>
        </tr>
      </table>
    </form>
    <%} %>
    <h2>TCH Forecast - Complete Results</h2>
    
    <table bgColor="#ffffff" cellPadding="3" cellSpacing="0" style="border-width: 2px; border-style: solid; border-color: #006699; margin-top: 5; margin-bottom: 5;"><%
           %>
            <tr>
              <th style="font-size: smaller; color: #FFFFFF; background-color: #006699;">Forecast</th>
              <th style="font-size: smaller; color: #FFFFFF; background-color: #006699;">Dose</th>
              <th style="font-size: smaller; color: #FFFFFF; background-color: #006699;">Valid</th>
              <th style="font-size: smaller; color: #FFFFFF; background-color: #006699;">Due</th>
              <th style="font-size: smaller; color: #FFFFFF; background-color: #006699;">Overdue</th>
              <th style="font-size: smaller; color: #FFFFFF; background-color: #006699;">Finished</th>
            </tr><%
            List commentList = new ArrayList();  
            for (Iterator it = resultList.iterator(); it.hasNext(); )
            {  
              ImmunizationForecastDataBean forecast = (ImmunizationForecastDataBean) it.next();
              DateTime today = new DateTime("today");
              DateTime validDate = new DateTime(forecast.getValid());
              DateTime dueDate = new DateTime(forecast.getDue());
              DateTime overdueDate = new DateTime(forecast.getOverdue());
              DateTime finishedDate = new DateTime(forecast.getFinished());
              String forecastDose = forecast.getDose();
              if (!forecast.getComment().equals(""))
              {
                for (int starCount = 0; starCount <= commentList.size(); starCount++) 
                {
                  forecastDose = forecastDose + "*";                  
                }
                commentList.add(forecast.getComment());
              }
              %>
              <tr>
                <td style="border-top-width: 1px; border-top-style: solid;"><a href="showSchedule.jsp?caseId=<%= caseId %>&userName=<%= URLEncoder.encode(userName, "UTF-8") %>#trace.<%= forecast.getForecastName() %>"><%= forecast.getForecastName() %></a></td>
                <td style="border-top-width: 1px; border-top-style: solid;"><%= forecastDose %></td>
                <td style="border-top-width: 1px; border-top-style: solid;"><%= validDate.toString("M/D/Y") %></td>
                <td style="border-top-width: 1px; border-top-style: solid;"><%= dueDate.toString("M/D/Y") %></td>
                <td style="border-top-width: 1px; border-top-style: solid;"><%= overdueDate.toString("M/D/Y") %></td>
                <td style="border-top-width: 1px; border-top-style: solid;"><%= finishedDate.toString("M/D/Y") %></td>
              </tr><%
            }
          
          int starPosition = 0;
          for (Iterator cit = commentList.iterator(); cit.hasNext(); )
          {
            String comment = " " + (String) cit.next();
            starPosition++;
            for (int starCount = 0; starCount < starPosition; starCount++) 
            {
              comment = "*" + comment;
            }
            %>
            <font size="-1"><em><%= comment %></em></font><br><%
          }
          %>
        </table>
    <font size="-1"><%= traceBuffer %></font>
    <h2>Age Test Case</h2>
    <p>Warning! This will change the test case dob and all of the expected and actual values. This
    is for moving a test case forward to a new date where the forecast will perform as expected. The
    recommended value is 4 years, as this avoids problems with leap years making a difference to 
    recommendations. A value of -4 years is acceptable to move a test case back 4 years.</p>
    <form action="testCase.jsp" method="POST">
          <input type="hidden" name="caseId" value="<%= caseId %>">
          <input type="hidden" name="userName" value="<%= userName %>">
          Age by <input type="text" name="yearAdd" value="4" size="3">
          years. <input type="checkbox" name="confirm" value="true"> confirm.
          <input type="submit" name="action" value="Age Test in Years">
    </form>
    <form action="testCase.jsp" method="POST">
          <input type="hidden" name="caseId" value="<%= caseId %>">
          <input type="hidden" name="userName" value="<%= userName %>">
          Age by <input type="text" name="monthAdd" value="4" size="3">
          months. <input type="checkbox" name="confirm" value="true"> confirm.
          <input type="submit" name="action" value="Age Test in Months">
    </form>
    <% } else { %>
    <p>[<a href="index.jsp">Back to Home</a>]</p>
    <% }} finally {DatabasePool.close(conn); } %>

</body>
</html>