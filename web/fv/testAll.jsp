<%@page import="org.tch.forecast.core.VaccinationDoseDataBean"%>
<%@page import="org.tch.forecast.core.ImmunizationInterface"%>
<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.tch.forecast.core.Forecaster"%>
<%@page import="org.tch.hl7.immunizations.databeans.PatientDataBean"%>
<%@page import="org.tch.forecast.core.model.PatientRecordDataBean"%>
<%@page import="org.tch.forecast.core.DateTime"%>
<%@page import="org.tch.forecast.core.model.Immunization"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.tch.forecast.core.ImmunizationForecastDataBean"%>
<%@page import="org.tch.forecast.core.VaccineForecastManagerInterface"%>
<%@page import="org.tch.forecast.support.VaccineForecastManager"%><html>

<html>
  <head>
  <title>FV Test All</title>
  <link rel="stylesheet" type="text/css" href="index.css" />
  </head>
  <body>
    <h1>Test All</h1>

    <% 
    Connection conn = DatabasePool.getConnection();
    Connection conn2 = DatabasePool.getConnection();
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

	  String editurl = null;
	  String sql = "SELECT software_id, software_label FROM forecasting_software ORDER BY software_id";
	  pstmt = conn.prepareStatement(sql);
	  ResultSet rset = pstmt.executeQuery();
	  List<String> softwareLabels = new ArrayList<String>();
	  while (rset.next())
	  {
	    softwareLabels.add(rset.getString(2));
	  }
	  pstmt.close();
      sql = "select tg.group_label, tc.case_id, tc.case_label, tc.case_description, ts.status_label \n" + 
        "from test_case tc, test_group tg, test_status ts \n" + 
        "where tc.group_code = tg.group_code \n" +
        "  and tc.status_code = ts.status_code \n" +
        "order by tg.group_code, tc.case_id";
      pstmt = conn.prepareStatement(sql);
      rset = pstmt.executeQuery();
      String lastGroupLabel = "";
      while (rset.next()) {
        String caseId = rset.getString(2);
        String caseLabel = rset.getString(3);
        String caseDescription = rset.getString(4);
        String statusLabel = rset.getString(5);
      
        if (!lastGroupLabel.equals(rset.getString(1))) {
          if (!lastGroupLabel.equals("")) {
            %>
            </table> <%
          }
          %>
           <h3><%= rset.getString(1) %></h3>
           <table border="1" cellspacing="0">
             <tr>
               <th>Test Case</th>
               <th>Status</th>
               <th>Test Results</th>
               <% for (String softwareLabel : softwareLabels) { if (false) {%>
               <th><%= softwareLabel%></th>
               <% } } %>
             </tr>
          <%
          lastGroupLabel = rset.getString(1);
        }
        int testCount = 0;
        int testPass = 0;
        
        Forecaster forecaster = new Forecaster(new VaccineForecastManager());
        List<ImmunizationForecastDataBean> resultList = new ArrayList<ImmunizationForecastDataBean>();
        List<VaccinationDoseDataBean> doseList = new ArrayList<VaccinationDoseDataBean>();
        PatientRecordDataBean patient = new PatientRecordDataBean();
        Date forecastDate = null;
        List<ImmunizationInterface> imms = new ArrayList<ImmunizationInterface>();
        sql = "SELECT tc.case_label, tc.case_description, tc.case_source, tc.group_code, tc.patient_first, \n" + 
          "tc.patient_last, date_format(tc.patient_dob, '%m/%d/%Y'), tc.patient_sex, tc.status_code, ts.status_label, \n" +
          "tc.forecast_date "+
          "FROM test_case tc, test_status ts\n" +
          "WHERE tc.case_id =" + caseId + " \n" +
          "  AND tc.status_code = ts.status_code\n";
        PreparedStatement pstmt2 = conn2.prepareStatement(sql);
        ResultSet rset2 = pstmt2.executeQuery();
        if (rset2.next())
        {
          patient.setSex(rset2.getString(8));
          patient.setDob(new DateTime(rset2.getString(7)));
          forecastDate = rset2.getDate(11);
        
        sql = "SELECT tv.cvx_code, cvx.cvx_label, date_format(admin_date, '%m/%d/%Y'), mvx_code, cvx.vaccine_id \n" + 
          "FROM test_vaccine tv, vaccine_cvx cvx \n" +
          "WHERE tv.cvx_code = cvx.cvx_code \n" +
          "  AND tv.case_id = ? \n";
        rset2.close();
        pstmt2.close();
        pstmt2 = conn2.prepareStatement(sql);
        pstmt2.setString(1, caseId);
        rset2 = pstmt2.executeQuery();
         while (rset2.next()) { 
                Immunization imm = new Immunization();
                imm.setDateOfShot(new DateTime(rset2.getString(3)).getDate());
                imm.setVaccineId(rset2.getInt(5));
                imms.add(imm);
         } 
         }
              rset2.close();
              pstmt2.close();
              // actually does the forecasting
              forecaster.setPatient(patient);
              forecaster.setVaccinations(imms);
              forecaster.setForecastDate(forecastDate);
              try 
              {
                forecaster.forecast(resultList, doseList, null, null);
              }
              catch (Exception e)
              {
                out.println("<pre>Vaccination Forecast Failed: " + e.getMessage());
                e.printStackTrace();
                out.println("</pre>");
              }
              int entityId = 2; // default to TCH
              
              sql = "SELECT er.line_code, fl.line_label, er.dose_number, date_format(er.valid_date, '%m/%d/%Y'), date_format(er.due_date, '%m/%d/%Y'), date_format(er.overdue_date, '%m/%d/%Y'), ee.entity_label \n" +
              "FROM expected_result er, forecast_line fl, expecting_entity ee \n" +
              "WHERE case_id = " + caseId + " \n" +
              "  AND er.entity_id = " + entityId + " \n" +
              "  AND er.line_code = fl.line_code \n" +
              "  AND er.entity_id = ee.entity_id \n";
              pstmt2 = conn2.prepareStatement(sql);
              rset2 = pstmt2.executeQuery();
              while (rset2.next()) {
                testCount++;
                String lineCode = rset2.getString(1);
                String lineLabel = rset2.getString(2);
                String doseNumberExpected = rset2.getString(3);
                String validDateExpected = rset2.getString(4);
                String dueDateExpected = rset2.getString(5);
                String overdueDateExpected = rset2.getString(6);
                if (validDateExpected == null || validDateExpected.equals("00/00/0000"))
                {
                  validDateExpected = "";
                }
                if (dueDateExpected == null || dueDateExpected.equals("00/00/0000"))
                {
                  dueDateExpected = "";
                }
                if (overdueDateExpected == null || overdueDateExpected.equals("00/00/0000"))
                {
                  overdueDateExpected = "";
                }
                String entityLabel = rset2.getString(7);

              
          
                String doseNumberActual = "COMP";
                String validDateActual = "";
                String dueDateActual = "";
                String overdueDateActual = "";
                DateTime today = new DateTime(forecastDate);
                for (ImmunizationForecastDataBean forecast : resultList)
                {  
                  String forecastLabel = forecast.getForecastLabel();
                  if (forecastLabel.equals("DTaP/Tdap"))
                  {
                    forecastLabel = "DTaP";
                  }
                  else if (forecastLabel.equals("Varicella"))
                  {
                    forecastLabel = "Var";
                  }
                  else if (forecastLabel.equals("IPV"))
                  {
                    forecastLabel = "Polio";
                  }
                  else if (forecastLabel.equals("MCV4"))
                  {
                    forecastLabel = "Meni";
                  }
                  else if (forecastLabel.equals("Measles"))
                  {
                    forecastLabel = "MMR";
                  }
                  else if (forecastLabel.equals("PCV7"))
                  {
                    forecastLabel = "Pneu";
                  }
                  else if (forecastLabel.equals("Rotavirus"))
                  {
                    forecastLabel = "Rota";
                  }
                  else if (forecastLabel.equals("Influenza"))
                  {
                    forecastLabel = "Flu";
                  }
                  if (forecastLabel.equals(lineCode))
                  {
                    DateTime finishedActual = new DateTime(forecast.getFinished());
                    if (today.isLessThan(finishedActual))
                    {
                      validDateActual = new DateTime(forecast.getValid()).toString("M/D/Y");
                      dueDateActual = new DateTime(forecast.getDue()).toString("M/D/Y");
                      overdueDateActual = new DateTime(forecast.getOverdue()).toString("M/D/Y");
                      doseNumberActual = forecast.getDose();
                    }
                  }
                }
                if (doseNumberActual.equals(doseNumberExpected))
                {
                  if (doseNumberActual.equals("COMP"))
                  {
                    testPass++;
                  }
                  else if (validDateActual.equals(validDateExpected) && dueDateActual.equals(dueDateExpected) && overdueDateActual.equals(overdueDateExpected))
                  {
                    testPass++;
                  }
                }
              }
        %>
        <tr>
          <td><a href="testCase.jsp?caseId=<%= caseId %>&userName=<%= URLEncoder.encode(userName, "UTF-8") %>"><%= caseLabel %></a></td>
          <td bgcolor="<%= statusLabel.equals("Fail") ? "#CC3333" : ((statusLabel.equals("Pass") || statusLabel.equals("Accept")) ? "#99FF99" : (statusLabel.equals("Fixed") ? "#FF9933" : "#FFFFFF")) %>"><%= statusLabel %></td>
          <td bgcolor="<%= (testCount > 0 && testCount != testPass) ? "#CC3333" : ((testCount > 0 && testCount == testPass) ? "#99FF99" : ((testCount == 0) ? "#FF9933" : "#FFFFFF")) %>"><%= testPass %> / <%= testCount %></td>
	    </tr>
        <%
      }
    %>
    </table>
    <br>
    <%
	String  addURL = new String("editTestCase.jsp?");
		addURL = addURL + "userName=" + URLEncoder.encode(userName, "UTF-8");
	%>
	<% if (!viewOnly) { %>
	[<a href="<%= addURL %>" title="Add Test Case">Add Test Case</a>]
	<%  String addvaccine = new String("addVaccine.jsp?");
		addvaccine = addvaccine + "&userName=" + URLEncoder.encode(userName, "UTF-8");
	%>
	[<a href="<%= addvaccine %>" title="Add Vaccine" >Add Vaccine</a>]
	<% } %>
	[<a href="batchEvaluate.jsp" title="Batch Evaluate" >Batch Evaluate</a>]
	<% 
    }
    %>
    <br>
	<h3>Login</h3>
    <form action="login.jsp">
    Your Name
    <input type="text" name="userName" value="<%= userName %>"/>
    <input type="submit" name="action" value="Login"/>
    </form>
    <%
    } finally {DatabasePool.close(conn); DatabasePool.close(conn2);} %>
  </body>
</html>