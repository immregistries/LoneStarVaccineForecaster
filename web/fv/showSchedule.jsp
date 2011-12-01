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
<%@page import="org.tch.forecast.support.VaccineForecastManager"%>
<%@page import="org.tch.forecast.core.VaccineForecastDataBean.Schedule"%>
<%@page import="org.tch.forecast.core.VaccineForecastDataBean"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.tch.forecast.core.Trace"%>
<%@page import="org.tch.forecast.core.VaccinationDoseDataBean"%>
<%@page import="org.tch.forecast.core.TimePeriod"%><html>
<head>
<title>Forecast Schedule</title>
  <link rel="stylesheet" type="text/css" href="index.css" />
</head>
<body>
<% 
String caseId = request.getParameter("caseId");
String action = request.getParameter("action");
if (action == null)
{
  action = "";
}

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
  
  Forecaster forecaster = new Forecaster(new VaccineForecastManager());
  StringBuffer traceBuffer = new StringBuffer();
  List resultList = new ArrayList();
  List doseList = new ArrayList();
  Map traces = new HashMap();
  PatientRecordDataBean patient = new PatientRecordDataBean();
  DateTime eventDate = null;
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
    <td><%= rset.getString(6) %>, <%= rset.getString(5) %> (<%= rset.getString(8) %>) dob <%= rset.getString(7) %></td>
  </tr>
</table>
<% } %>
<p>[<a href="index.jsp?userName=<%= URLEncoder.encode(userName, "UTF-8") %>">Back to Home</a>]</p>
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
        </tr>
        <% }
        rset.close();
        pstmt.close();
        forecaster.setPatient(patient);
        forecaster.setVaccinations(imms);
        forecaster.forecast(resultList, doseList, traceBuffer, traces);
        
        
        %>
      </table>
<% 
  Trace trace = null; 
  for (Iterator it = traces.keySet().iterator(); it.hasNext();)
  {
    String key = (String) it.next();
    List traceList = (List) traces.get(key);
    %>
    <p><a name="trace.<%= key %>"></a>&nbsp;</p>
    <hr>
    <h2><%= key %> Trace</h2>
    <%
    for (Iterator traceIt = traceList.iterator(); traceIt.hasNext();)
    {
      trace = (Trace) traceIt.next();
      List doses = trace.getDoses();
      Schedule schedule = trace.getSchedule();
      trace.getReason();
      trace.isComplete();
      trace.isContraindicated();
      trace.isFinished();
      trace.isInvalid();
      if (schedule != null) { 
      %>
        <h3><a name="trace.<%= schedule.getVaccineForecast().getForecastCode() %>.<%= schedule.getScheduleName() %>"></a><%= schedule.getLabel() %></h3>
        <% 
        if (doses != null) {
          %>
          <table border="1" cellspacing="0">
            <tr>
              <th>Vaccine Id</th>
              <th>Date</th>
              <th>Status</th>
              <th>Reason</th>
            </tr>
          <%
          eventDate = null;
          for (Iterator doseIt = doses.iterator(); doseIt.hasNext();) 
          {
            VaccinationDoseDataBean dose = (VaccinationDoseDataBean) doseIt.next();
            eventDate = new DateTime(dose.getAdminDate());
            %>
            <tr>
              <td><%= dose.getVaccineId() %></td>
              <td><%= eventDate.toString("M/D/Y") %></td>
              <td><%= dose.getStatusCode() %></td>
              <td><%= dose.getReason()%>&nbsp;</td>
            </tr>
            <%
          }
          %>
          </table>
          <br>
          <%
        }
        trace.getValidDate();
        trace.getDueDate();
        trace.getOverdueDate();
        trace.getFinishedDate();
        trace.getValidReason();
        %>
        <%@ include file="showScheduleTable.jspf"%>
        <br>
      <%
      }
    } %><%
  }
  trace = null;

%>
<%
  String[] indicationTypes = {"BIRTH",  "NO-VAR-HIS", "FEMALE"};
  for (int i = 0; i < indicationTypes.length; i++) 
  { 
    List indications = new VaccineForecastManager().getIndications(indicationTypes[i]);
    for (Iterator it = indications.iterator(); it.hasNext();)
    {
      VaccineForecastDataBean.Schedule schedule = (VaccineForecastDataBean.Schedule) it.next();
      %>
        <p>&nbsp;</p>
        <hr>
        <h2><%= schedule.getVaccineForecast().getForecastCode() %> Forecast</h2>
        <table border="1" cellspacing="0">
          <tr>
            <th>Vaccine</th>
            <th>Vaccine Ids</th>
          </tr>
            <% Map vaccines = schedule.getVaccines();
            for (Iterator vit = vaccines.keySet().iterator(); vit.hasNext(); )
            {
              String key = (String) vit.next();
            %>
          <tr>
            <td><%= key %></td>
            <td>
            <%= vaccines.get(key) %></td>
          </tr>
            <% } %>
        </table>
      <%
      Map schedules = schedule.getVaccineForecast().getSchedules();
      for (Iterator mit = schedules.keySet().iterator(); mit.hasNext(); )
      {
        String scheduleName = (String) mit.next();
        schedule = (Schedule) schedules.get(scheduleName);
        %>
        <h3><%= schedule.getLabel() %></h3>
        <%@ include file="showScheduleTable.jspf"%>
        <%
      }
    }
  }
%>

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
                <td style="border-top-width: 1px; border-top-style: solid;"><%= forecast.getForecastName() %></td>
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
    <% } else { %>
    <p>[<a href="index.jsp">Back to Home</a>]</p>
    <% }} finally {DatabasePool.close(conn); } %>

</body>
</html>