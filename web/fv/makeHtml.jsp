<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.net.URLEncoder"%>

<html>
  <head>
  <title>Download HL7 Batch</title>
  </head>
  <body>
    <h1>Download HL7 Batch</h1>

    <% 
    Connection conn = DatabasePool.getConnection();
    PreparedStatement pstmt = null;
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

	   %>
		<pre><%
	  String editurl = null;	
      String sql = "select tc.case_id, tc.patient_last, tc.patient_first, date_format(patient_dob, '%Y%m%d'), patient_sex, date_format(now(), '%Y%m%d%H%i%S')  \n" + 
        "from test_case tc \n" + 
        "order by tc.patient_last, tc.patient_first";
      pstmt = conn.prepareStatement(sql);
      ResultSet rset = pstmt.executeQuery();
      boolean first = true;
      while (rset.next()) {
        if (first)
        {
          %>FHS|^~\&|IZ REGISTER|RSB|||<%= rset.getString(6) %>-0500
BHS|^~\&|IZ REGISTER|RSB|||<%= rset.getString(6) %>-0500<%
          first = false;
        }
        %>
MSH|^~\&|TCH FORECAST TESTER|TCH.FT|IIS|TEST ONLY|<%= rset.getString(6) %>-0500||VXU^V04|<%= rset.getString(6) %>.<%= rset.getInt(1) %>|P|2.4|||AL|AL|
PID|1||<%= rset.getInt(1) %>^^^TCH.FT^MR||<%= rset.getString(2) %>^<%= rset.getString(3) %>||<%= rset.getString(4) %>|<%= rset.getString(5) %>|<%
     PreparedStatement pstmt2 = null;
     try {
       sql = "select date_format(admin_date, '%Y%m%d'), cvx_code from test_vaccine where case_id = ?";
       pstmt2 = conn.prepareStatement(sql);
       pstmt2.setInt(1, rset.getInt(1));
       ResultSet rset2 = pstmt2.executeQuery();
       while (rset2.next()) { 
       %>
RXA|0|1|<%= rset2.getString(1) %>|<%= rset2.getString(1) %>|<%= rset2.getString(2) %>^^CVX|999|||01^HISTORICAL INFORMATION - SOURCE UNSPECIF^NIP001|<%
       }
       rset2.close();
     }
     finally {
       if (pstmt2 != null)
       {
         pstmt2.close();
       }
     }
      }
    %>
BTS|
FTS|1 </pre>
    <br>
    <%
      }
    } finally {DatabasePool.close(conn);} %>
  </body>
</html>