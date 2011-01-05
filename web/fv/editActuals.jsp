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
<%@page import="org.tch.forecast.support.VaccineForecastManager"%>

<html>
<head>
<title>Edit Actuals</title>
</head>
<body>

<script type="text/JavaScript">

function popupPage(vheight,vwidth,varpage) { //open a new popup window
  var page = "" + varpage;
  windowprops = "height="+vheight+",width="+vwidth+",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";//360,680
  var popup=window.open(page, "groupno", windowprops);
  if (popup != null) {
    if (popup.opener == null) {
      popup.opener = self;
    }
    popup.focus();
  }
}

	function windowClose() {
		window.close(); 
	}

</script>

<% 
String caseId = request.getParameter("case_id");
%>
<h1>

<%= request.getParameter("header") %></h1>
<form method="post" action="testCase.jsp">
<table>
		<input type="hidden" name="caseId" value="<%=request.getParameter("caseId")==null ? "" : request.getParameter("caseId") %>"/>
		<input type="hidden" name="line_code" value="<%=request.getParameter("line_code")==null ? "" : request.getParameter("line_code") %>"/>
		<input type="hidden" name="userName" value="<%=request.getParameter("userName")==null ? "" : request.getParameter("userName") %>"/>		
		<input type="hidden" name="action" value="<%=request.getParameter("action")==null ? "" : request.getParameter("action") %>"/>		
		<tr>
			<td>Dose</td>
			<td>
				<input type="text" name="dose" id="dose" size="10" maxlength="10" value="<%=request.getParameter("dose")==null ? "" : request.getParameter("dose")%>"/>
			</td>
		</tr>
		<tr>
			<td>Valid</td>
			<td>
				<input type="text" name="valid_date" id="valid_date" size="10" maxlength="10" value="<%=request.getParameter("valid_date")==null ? "" : request.getParameter("valid_date")%>"/>
			</td>
		</tr>
		<tr>
			<td>Due</td>
			<td>
				<input type="text" name="due_date" id="dose" size="10" maxlength="10" value="<%=request.getParameter("due_date")==null ? "" : request.getParameter("due_date")%>"/>
			</td>
		</tr>
		<tr>
			<td>Overdue</td>
			<td>
				<input type="text" name="overdue_date" id="overdue_date" size="10" maxlength="10" value="<%=request.getParameter("overdue_date")==null ? "" : request.getParameter("overdue_date")%>"/>
			</td>
		</tr>
		<tr>
			<td>													 			
				<input type="submit" name="submit" id="submit" value="Save"/>			
				<input type="button" value="Cancel" onclick="windowClose();"/>				
			</td>
		</tr>
</table>
</body>
</html>