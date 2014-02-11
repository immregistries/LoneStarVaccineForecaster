<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="org.tch.forecast.validator.login.Login"%>
<%@page import="org.tch.forecast.validator.login.UserAuthenticationInfoModel"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>

<%@page import="java.net.URLEncoder"%><html>
  <head>
  <title>Forecaster Validator</title>
  </head>
  <body>
    <h1>Forecaster Validator</h1>
    <% 
    boolean validationDone = false;
	UserAuthenticationInfoModel authModel = new UserAuthenticationInfoModel(request.getParameter("userName"));
	Login login = new Login(request);
	boolean success = login.authenticate(authModel,true);
	if(authModel.username != null && !"".equals(authModel.username)){
		validationDone = true;
	}
	if(success){
        RequestDispatcher dispatcher =request.getRequestDispatcher("main.jsp");
		dispatcher.forward(request, response); 
		//response.sendRedirect("main.jsp"); 
	}

	%>
    <h3>Login</h3>
    <form>
	<%
	if(validationDone && !success){
	%>
	<b><font color='red'>Invalid Username</font></b></font><br>
	<%
	}
	%>
    Your Name
    <input type="text" name="userName" value="<%= request.getParameter("userName") == null ? "" : request.getParameter("userName") %>"/>
    <input type="submit" name="action" value="Login" />
    </form>
    
  </body>
</html>