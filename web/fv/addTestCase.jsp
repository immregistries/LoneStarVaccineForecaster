<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>

<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Iterator"%>

<html>
<head>
<title>Add Test Case</title>
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
	Connection conn = DatabasePool.getConnection();
	PreparedStatement pstmt = null;
	pstmt = conn.prepareStatement("select group_code,group_label from test_group");
	ResultSet rset = pstmt.executeQuery();
	HashMap groupCodeMap = new HashMap();
	while(rset.next()){
		groupCodeMap.put(rset.getString("group_code"),rset.getString("group_label"));
	}
	rset.close();
	pstmt.close();
%>
<h1><%= "Add Test Case"%></h1>
<%
String errorMsg = (String)request.getAttribute("error_message");
if(errorMsg != null && !"".equals(errorMsg)){
%>
<font color="red"><b>Error:<%=errorMsg%></b></font>
<%}%>
<form method="post" action="addTestCase.action">
<table>
		<input type="hidden" name="action" value="<%="Add Test Case" %>"/>		
		<input type="hidden" name="userName" value="<%=request.getParameter("userName")==null ? "" : request.getParameter("userName") %>"/>
		<tr>
			<td align="right">Name:</td>
			<td align="left">
				<input type="text" name="case_label" id="case_label" size="100" maxlength="100" 
				value="<%=request.getParameter("case_label")==null ? "" : request.getParameter("case_label") %>"/>
			</td>
		</tr>
		<tr>
			<td align="right">Description:</td>
			<td align="left">
				<textarea name="case_description" id="case_description" rows=5 cols="100" size="4000" maxlength="4000" ><%=request.getParameter("case_description")==null ? "" : request.getParameter("case_description").trim() %></textarea>
			</td>
		</tr> 
		<tr>
			<td align="right">Category:</td>
			<td align="left">
			 <select name="group_code">
					<%
					boolean isValueSet = false;
					for(Object testGroupCode : groupCodeMap.keySet()){ 
						if(testGroupCode.equals(request.getParameter("group_code"))){
							
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