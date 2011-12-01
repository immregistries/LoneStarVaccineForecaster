<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>

<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>

<%
	boolean isEdit = request.getParameter("edit") != null && "y".equals(request.getParameter("edit"));
	Connection conn = DatabasePool.getConnection();
	PreparedStatement pstmt = null;
	pstmt = conn.prepareStatement("select cvx_code,  cvx_label,  vaccine_id from vaccine_cvx");
	ResultSet rset = pstmt.executeQuery();
	HashMap codeMap = new HashMap();
	while(rset.next()){
		codeMap.put(rset.getString("cvx_code"),new Object[]{
					rset.getString("cvx_code"),
					rset.getString("cvx_label"),
					new Integer(rset.getInt("vaccine_id"))
		});
	}
	rset.close();
	pstmt.close();
	conn.close();
%>
<html>
<head>
<title>
<%= isEdit ? "Edit Vaccine" : "Add Vaccine" %>
</title>
  <link rel="stylesheet" type="text/css" href="index.css" />
</head>
<body>
<script type="text/JavaScript">

</script>


<h1><%= isEdit ? "Edit Vaccine" : "Add Vaccine" %></h1>
<%
String errorMsg = (String)request.getAttribute("error_message");
if(errorMsg != null && !"".equals(errorMsg)){
%>
<font color="red"><b>Error:<%=errorMsg%></b></font>
<%}%>
<form method="post" action="addVaccine.action">
<table>
		<%if(isEdit){%>
		<input type="hidden" name="action" value="<%="Edit VaccineTest" %>"/>		
		<%}else{%>
		<input type="hidden" name="action" value="<%="Add VaccineTest" %>"/>		
		<%}%>
		<input type="hidden" name="userName" value="<%=request.getParameter("userName")==null ? "" : request.getParameter("userName") %>"/>
		<input type="hidden" name="case_id" value="<%=request.getParameter("case_id")==null ? "" : request.getParameter("case_id") %>"/>
		<input type="hidden" name="caseId" value="<%=request.getParameter("case_id")==null ? "" : request.getParameter("case_id") %>"/>
		<input type="hidden" name="old_cvx_code" value="<%=request.getParameter("mvx_code")==null ? "" : request.getParameter("cvx_code") %>"/>
		<input type="hidden" name="old_mvx_code" value="<%=request.getParameter("mvx_code")==null ? "" : request.getParameter("mvx_code") %>"/>
		<input type="hidden" name="old_admin_date" value="<%=request.getParameter("admin_date")==null ? "" : request.getParameter("admin_date") %>"/>
		
		<tr>
			<td align="right">CVX:</td>
			<td align="left">
			 <select name="cvx_code">
					<%
					boolean isValueSet = false;
					for(Object testGroupCode : codeMap.keySet()){ 
						if(testGroupCode.equals(request.getParameter("cvx_code"))){
							
					%>
							<option value="<%=testGroupCode%>" selected="selected" ><%= ((Object[])codeMap.get(testGroupCode))[1] %></option>
						<%}else{%>
							<option value="<%=testGroupCode%>"><%= ((Object[])codeMap.get(testGroupCode))[1] %></option>
						<%}
					}%>
			</select>
			</td>
		</tr>
		<!--<tr>
			<td align="right">TCH:</td>
			<td align="left">
				<input type="text" name="vaccine_id" id="vaccine_id" size="11" maxlength="11" 
				value="<%=request.getParameter("vaccine_id")==null ? "" : request.getParameter("vaccine_id") %>" readonly/>
			</td>
		</tr> -->
		<tr>
			<td align="right">MVX:</td>
			<td align="left">
				<input type="text" name="mvx_code" id="mvx_code" size="5" maxlength="5" 
				value="<%=request.getParameter("mvx_code")==null ? "" : request.getParameter("mvx_code") %>"/>
			</td>
		</tr>
		<tr>
			<td align="right">Admin Date<font size="-2">(mm/dd/yyyy)</font>:</td>
			<td align="left">
				<input type="text" name="admin_date" id="admin_date" size="10" maxlength="10" value="<%=request.getParameter("admin_date")==null ? "" : request.getParameter("admin_date") %>"/>
			</td>
		</tr> 
		<tr>
			<td align="right">													 			
				<input type="submit" name="submit" id="submit" value="Save"/>
			</td>	
			<td align="left">
				<input type="button" value="Cancel"/>				
			</td>
		</tr>
</table>
<%if(isEdit){%>
<%  String deleteVaccineUrl = new String("deleteTestVaccine.action?");
	deleteVaccineUrl = deleteVaccineUrl + "action=" +  URLEncoder.encode("Delete VaccineTest", "UTF-8");
	deleteVaccineUrl = deleteVaccineUrl + "&userName=" + (request.getParameter("userName")==null ? "" : request.getParameter("userName"));
	deleteVaccineUrl = deleteVaccineUrl + "&case_id=" + (request.getParameter("case_id")==null ? "" : request.getParameter("case_id"));
	deleteVaccineUrl = deleteVaccineUrl + "&cvx_code=" +  request.getParameter("cvx_code");
	deleteVaccineUrl = deleteVaccineUrl + "&admin_date="  +  URLEncoder.encode(
	(request.getParameter("admin_date")==null ? "" : request.getParameter("admin_date")), "UTF-8");
%>
<a href="<%= deleteVaccineUrl %>" title=">Delete Vaccine" onclick="return confirm('Are you sure you want to delete this Vaccine?');" >Delete Vaccine</a>
<%}%>
</body>
</html>