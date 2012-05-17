<%@page import="org.tch.forecast.support.VaccineForecastManager"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.StringWriter"%>
<%@page import="java.util.Map"%>
<%@page import="org.tch.forecast.core.VaccineForecastDataBean.Schedule"%>
<%@page import="org.tch.forecast.StepServlet"%>
<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>

<html>
  <head>
  <title>FV Print Schedule</title>
  <link rel="stylesheet" type="text/css" href="step.css" />
  </head>
  <body>

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
        
        if (request.getParameter("lineCode") == null)
        {
          ResultSet rset = null;
          %><h1>Show Schedules</h1>
          <form action="printSchedule.jsp" method="GET">
          <table>
          <%
          String sql = "SELECT line_code, line_label FROM forecast_line";
          pstmt = conn.prepareStatement(sql);
          rset = pstmt.executeQuery();
          %>
            <tr>
              <td>Forecast Line</td>
              <td>
                <select name="lineCode"> <%
                while (rset.next())
                { %>
                 <option value="<%= rset.getString(1)%>"><%= rset.getString(2) %></option><% 
                }%>
                </select>
              </td>
            </tr>
          </table>
          <input type="submit" value="Submit" name="action"/>
          </form> <%
        }
        else {
          String lineCode = StepServlet.convertLineCode(request.getParameter("lineCode"));
        %>
          <h1><%= lineCode %> Forecast Schedules</h1> <%
        
          VaccineForecastManager vaccineForecastManager = new VaccineForecastManager();
          Schedule startingSchedule = vaccineForecastManager.getSchedule(lineCode);
          Map<String, Schedule> schedules = startingSchedule.getVaccineForecast().getSchedules();
          StringWriter stringWriter = new StringWriter();
          PrintWriter stringWriterOut = new PrintWriter(stringWriter);
          StepServlet.printSchedules(stringWriterOut, startingSchedule);
          out.print(stringWriter);
          for (Schedule schedule : schedules.values())
          {
            %>
            <h2><%= schedule.getScheduleName() %> <%= schedule.getLabel() %></h2>
            <%
            stringWriter = new StringWriter();
            stringWriterOut = new PrintWriter(stringWriter);
            StepServlet.printSchedule(stringWriterOut, schedule);
            out.print(stringWriter);
          }
        } 
      }
    } finally {DatabasePool.close(conn); } %>
  </body>
</html>