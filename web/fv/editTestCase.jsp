<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.net.URLEncoder"%>


<html>
<head>
<title>Edit Actuals</title>
</head>
<body>

<h1><%= "Edit Test Case"%></h1>
<%
String errorMsg = (String)request.getAttribute("error_message");
if(errorMsg != null && !"".equals(errorMsg)){
%>
<font color="red"><b>Error:<%=errorMsg%></b></font>
<%}%>

<%
	String lastName = "", firstName = "", sex = "", dob = "";
	if(errorMsg != null && !"".equals(errorMsg)){
		lastName	= request.getParameter("patient_last");
		firstName	= request.getParameter("patient_first");
		sex			= request.getParameter("patient_sex");
		dob			= request.getParameter("patient_dob");	
	}else{
		Connection conn = DatabasePool.getConnection();
		PreparedStatement pstmt = null;
		pstmt = conn.prepareStatement("select patient_first,patient_last,patient_sex,date_format(patient_dob, '%m/%d/%Y') as patient_dob " 
		+ "from test_case where case_id = ? ");
		pstmt.setInt(1,new Integer(request.getParameter("case_id")).intValue());
		ResultSet rset = pstmt.executeQuery();
		if(rset.next()){
			lastName	= rset.getString("patient_last");
			firstName	= rset.getString("patient_first");
			sex			= rset.getString("patient_sex");
			dob			= rset.getString("patient_dob");
		}
		rset.close();
		pstmt.close();
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
%>

<form method="post" action="editTest.action">
<table>
		<input type="hidden" name="action" value="<%="Edit Test Case" %>"/>		
		<input type="hidden" name="userName" value="<%=request.getParameter("userName")==null ? "" : request.getParameter("userName") %>"/>
		<input type="hidden" name="case_id" value="<%=request.getParameter("case_id")==null ? "" : request.getParameter("case_id") %>"/>
		<input type="hidden" name="caseId" value="<%=request.getParameter("case_id")==null ? "" : request.getParameter("case_id") %>"/>

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
			<td align="right">													 			
				<input type="submit" name="submit" id="submit" value="Save"/>
			</td>	
			<td align="left">
				<input type="button" value="Cancel" onclick="windowClose();"/>				
			</td>
		</tr>
</table>
</body>
</html>