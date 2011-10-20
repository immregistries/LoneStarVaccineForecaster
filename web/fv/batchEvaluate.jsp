<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>

<html>
  <head>
  <title>Forecaster Validator</title>
  <link rel="stylesheet" type="text/css" href="index.css" />
  </head>
  <body>
    <h1>Batch Evaluate</h1>

    <% 
    Connection conn = DatabasePool.getConnection();
    PreparedStatement pstmt = null;
    boolean viewOnly = true;
    try { 
    
      String userName = (String)session.getAttribute("userName");
      if (userName == null || userName.equals(""))
      {
		System.out.println("being redirected..");
		RequestDispatcher dispatcher =request.getRequestDispatcher("login.jsp");
		dispatcher.forward(request, response);
		/*response.sendRedirect("login.jsp");*/
      }else if (!"".equals(userName)) 
      {
        viewOnly = userName.equals("View Only");
        String inputCsv = request.getParameter("inputCsv");
        if (inputCsv == null)
        {
          inputCsv = "";
        }
        String outputCsv = "";
        if (!inputCsv.equals(""))
        {
          outputCsv = org.tch.forecast.edge.EvaluateCsv.evaluateCsv(inputCsv);
        }
        
	   %>
    <form action="batchEvaluate.jsp" method="POST">
    <table>
      <tr><td>Input CSV</td><td><textarea name="inputCsv" cols="40" rows="10"><%= inputCsv %></textarea></td></tr>
      <tr><td colspan="2" align="right"><input type="submit" name="action" value="Evaluate"/></td></tr>
    </table>
    </form>
    <%
    if (!outputCsv.equals("")) {
     %>
       <h2>Output</h2>
       <pre><%= outputCsv %></pre>
     <%
    }
    %>
	<% 
    }
    } finally {DatabasePool.close(conn); } %>
  </body>
</html>