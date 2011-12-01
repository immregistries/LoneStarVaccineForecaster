<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.net.URLEncoder"%>


<% 
boolean isEdit = request.getParameter("case_id") != null && !"".equals(request.getParameter("case_id"));
%>
<html>
<head>
<title>
	<%=isEdit ? "Edit Test Case" : "Add Test Case" %>
</title>
  <link rel="stylesheet" type="text/css" href="index.css" />
</head>
<body>
<h1><%=isEdit ? "Edit Test Case" : "Add Test Case" %></h1>

<%
String errorMsg = (String)request.getAttribute("error_message");
if(errorMsg != null && !"".equals(errorMsg)){
%>
<font color="red"><b>Error:<%=errorMsg%></b></font>
<%}%>

<%
	Connection conn = DatabasePool.getConnection();
	PreparedStatement pstmt = null;
	ResultSet rset = null;
	String lastName = "", firstName = "", sex = "", dob = "", forecastDate = "", name = "", descr="" , groupCode="";
	forecastDate = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
	
	if(errorMsg != null && !"".equals(errorMsg)){
		name	= request.getParameter("case_label");
		descr	= request.getParameter("case_description");
		groupCode	= request.getParameter("group_code");
		lastName	= request.getParameter("patient_last");
		firstName	= request.getParameter("patient_first");
		sex			= request.getParameter("patient_sex");
		dob			= request.getParameter("patient_dob");	
		forecastDate= request.getParameter("forecast_date");
	}else if(isEdit){
		pstmt = conn.prepareStatement("select case_label,case_description, group_code,patient_first,patient_last,patient_sex,"
		+ "date_format(patient_dob, '%m/%d/%Y') as patient_dob, date_format(forecast_date, '%m/%d/%Y') as forecast_date  from test_case where case_id = ? ");
		pstmt.setInt(1,new Integer(request.getParameter("case_id")).intValue());
		rset = pstmt.executeQuery();
		if(rset.next()){
			name		= rset.getString("case_label");
			descr		= rset.getString("case_description");
			groupCode	= rset.getString("group_code");
			lastName	= rset.getString("patient_last");
			firstName	= rset.getString("patient_first");
			sex			= rset.getString("patient_sex");
			dob			= rset.getString("patient_dob");
			forecastDate= rset.getString("forecast_date");
		}
		rset.close();
		pstmt.close();
	}
	
	pstmt = conn.prepareStatement("select group_code,group_label from test_group");
	rset = pstmt.executeQuery();
	HashMap groupCodeMap = new HashMap();
	while(rset.next()){
		groupCodeMap.put(rset.getString("group_code"),rset.getString("group_label"));
	}
	rset.close();
	pstmt.close();
	conn.close();

	if(name == null){
		name = "";
	}
	if(descr == null){
		descr = "";
	}
	if(groupCode == null){
		groupCode = "";
	}
	if(lastName == null){
		lastName = "";
	}
	if(firstName == null){
		firstName = "";
	}
	if(sex == null){
		sex = "";
	}
	if(dob == null){
		dob = "";
	}
	if(forecastDate == null){
		forecastDate = "";
	}
%>

<form method="post" action="editTest.action">
<table>
		<input type="hidden" name="action" value="<%=isEdit ? "Edit Test Case" : "Add Test Case" %>"/>		
		<input type="hidden" name="userName" value="<%=request.getParameter("userName")==null ? "" : request.getParameter("userName") %>"/>
		<% if(isEdit){ %>
		<input type="hidden" name="case_id" value="<%=request.getParameter("case_id")==null ? "" : request.getParameter("case_id") %>"/>
		<input type="hidden" name="caseId" value="<%=request.getParameter("case_id")==null ? "" : request.getParameter("case_id") %>"/>
		<% } %>

		<tr>
			<td align="right">Name:</td>
			<td align="left">
				<input type="text" name="case_label" id="case_label" size="100" maxlength="100" 
				value="<%=name%>"/>
			</td>
		</tr>
		<tr>
			<td align="right">Description:</td>
			<td align="left">
				<textarea name="case_description" id="case_description" rows=5 cols="100" size="4000" maxlength="4000" ><%=descr.trim() %></textarea>
			</td>
		</tr> 
		<tr>
			<td align="right">Category:</td>
			<td align="left">
			 <select name="group_code">
					<%
					boolean isValueSet = false;
					for(Object testGroupCode : groupCodeMap.keySet()){ 
						if(testGroupCode.equals(groupCode)){
							
					%>
							<option value="<%=testGroupCode%>" selected="selected" ><%= groupCodeMap.get(testGroupCode) %></option>
						<%}else{%>
							<option value="<%=testGroupCode%>"><%=groupCodeMap.get(testGroupCode) %></option>
						<%}
					}%>
			</select>
			</td>
		</tr>
		<tr>
			<td align="right">Last Name:</td>
			<td align="left">
				<input type="text" name="patient_last" id="patient_last" size="30" maxlength="30" value="<%=lastName%>"/>
			</td>
		</tr>
		<tr>
			<td align="right">First Name:</td>
			<td align="left">
				<input type="text" name="patient_first" id="patient_first" size="30" maxlength="30" value="<%=firstName%>"/>
			</td>
		</tr>
		<tr>
			<td align="right">DOB<font size="-2">(mm/dd/yyyy)</font>:</td>
			<td align="left">
				<input type="text" name="patient_dob" id="patient_dob" size="10" maxlength="10" value="<%=dob%>"/>
			</td>
		</tr> 
		<tr>
			<td align="right">Sex:</td>
			<td align="left">
			 <select name="patient_sex">
					<option value="M" <%= "M".equals(sex) ? "selected" : "" %> >Male</option>
					<option value="F" <%= "F".equals(sex) ? "selected" : "" %> >Female</option>
			</select>
			</td>
		</tr>
		<tr>
			<td align="right">Forecast Date<font size="-2">(mm/dd/yyyy)</font>:</td>
			<td align="left">
				<input type="text" name="forecast_date" id="forecast_date" size="10" maxlength="10" value="<%=forecastDate%>"/>
			</td>
		</tr> 
		<tr>
			<td align="right">													 			
				<input type="submit" name="submit" id="submit" value="Save"/>
			</td>	
			<td align="left">
				<input type="button" value="Cancel" onclick="windowClose();"/>				
			</td>
		</tr>
</table>
<br>
<% if(isEdit){ 
	  String deleteurl = new String("deleteTestCase.action?");
		   deleteurl = deleteurl + "action=" + ( URLEncoder.encode("Delete Test Case", "UTF-8") );
		   deleteurl = deleteurl + "&case_id=" + ( request.getParameter("case_id")==null ? "" : request.getParameter("case_id") );
		   deleteurl = deleteurl + "&userName=" + ( request.getParameter("userName")==null ? "" : request.getParameter("userName") ); %>
	[<a href=" <%= deleteurl %>" onclick="return confirm('Are you sure you want to delete this Test Case?');">Delete Test Case</a>]
<% } %>
</form>
</body>
</html>