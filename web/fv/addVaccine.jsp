<html>
<head>
<title>Add Vaccine</title>
  <link rel="stylesheet" type="text/css" href="index.css" />
</head>
<body>

<script type="text/JavaScript">
</script>


<h1>Add Vaccine</h1>
<%
String errorMsg = (String)request.getAttribute("error_message");
if(errorMsg != null && !"".equals(errorMsg)){
%>
<font color="red"><b>Error:<%=errorMsg%></b></font>
<%}%>
<form method="post" action="addVaccine.action">
<table>
		<input type="hidden" name="action" value="<%="Add Vaccine" %>"/>		
		<input type="hidden" name="userName" value="<%=request.getParameter("userName")==null ? "" : request.getParameter("userName") %>"/>
		<input type="hidden" name="case_id" value="<%=request.getParameter("case_id")==null ? "" : request.getParameter("case_id") %>"/>
		<input type="hidden" name="caseId" value="<%=request.getParameter("case_id")==null ? "" : request.getParameter("case_id") %>"/>
		<tr>
			<td align="right">CVX:</td>
			<td align="left">
				<input type="text" name="cvx_code" id="cvx_code" size="5" maxlength="5" 
				value="<%=request.getParameter("cvx_code")==null ? "" : request.getParameter("cvx_code") %>"/>
			</td>
		</tr>
		<tr>
			<td align="right">Name:</td>
			<td align="left">
				<input type="text" name="cvx_label" id="cvx_label" size="50" maxlength="50" 
				value="<%=request.getParameter("cvx_label")==null ? "" : request.getParameter("cvx_label") %>"/>
			</td>
		</tr> 
		<tr>
			<td align="right">TCH:</td>
			<td align="left">
				<input type="text" name="vaccine_id" id="vaccine_id" size="11" maxlength="11" 
				value="<%=request.getParameter("vaccine_id")==null ? "" : request.getParameter("vaccine_id") %>"/>
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
</body>
</html>