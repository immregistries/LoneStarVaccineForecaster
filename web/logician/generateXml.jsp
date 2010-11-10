<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>

<%@page import="java.net.URLEncoder"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.StringReader"%><html>
  <head>
  <title>Logician XML</title>
  </head>
  <body>
    <h1>Logician XML</h1>
    <% 
    Connection conn = DatabasePool.getConnection();
    PreparedStatement pstmt = null;
    try { 
      int entityId = 0;
      String entityIdString = request.getParameter("entityId");
      if (entityIdString != null)
      {
        entityId = Integer.parseInt(entityIdString);
      }
      String sql;
      ResultSet rset;
      String registryName = "";
      String entityName = "";
      sql = "SELECT registry_name, entity_name FROM logician_entity WHERE entity_id = ?";
      pstmt = conn.prepareStatement(sql);
      pstmt.setInt(1, entityId);
      rset = pstmt.executeQuery();
      if (rset.next())
      {
        registryName = rset.getString(1);
        entityName = rset.getString(2);
      }
      rset.close();
      pstmt.close();
      {
        %>
        <h3><%= registryName %> <%= entityName %></h3>
        <p>The following codes can not be exported. These values will not be accepted by IWeb until they are properly configured.</p>
        <table border="1" cellpadding="3" cellspacing="0"> 
          <tr>
            <th>CPT</th>
            <th>Obs Term</th>
            <th>Dose</th>
            <th>Type</th>
            <th>Obs Code</th>
            <th>Status</th>
            <th>Edit</th>
          </tr>
          <%
          sql = "SELECT \n " + 
            "  lc.obs_term, lc.obs_code, lc.hd_id, lc.data_type, lc.cpt_code, \n " + 
            "  lc.dose_number, lc.description, lf.include_status \n" +
            "FROM logician_code lc, logician_flowsheet lf \n " + 
            "WHERE lc.obs_term = lf.obs_term AND lf.entity_id = ? \n" + 
            "  AND (lf.include_status = 'E' OR lf.include_status = 'S' ) \n " +
            "  AND lc.cpt_code = '' " + 
            "ORDER BY lc.cpt_code, lc.obs_term, lc.dose_number, lc.data_type";
          pstmt = conn.prepareStatement(sql);
          pstmt.setInt(1, entityId);
          rset = pstmt.executeQuery();
          while (rset.next())
          {
            String status = rset.getString(8);
            if (status == null)
            {
              status = "";
            }
            String color = "#FFFFFF";
            if (status.equals("E"))
            {
              color = "#CCFFCC";
            }
            else if (status.equals("S"))
            {
              color = "#33CC66";
            }
            else if (status.equals("D"))
            {
              color = "#CC6600";
            }
            %>
            <tr>
              <td bgcolor="<%= color %>"><%= rset.getString(5) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(1) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(6) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(4) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(2) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= status %>&nbsp;</td>
              <td bgcolor="<%= color %>"><a href="editLogicianCode.jsp?entityId=<%= entityId %>&obsTerm=<%= URLEncoder.encode(rset.getString(1), "UTF-8") %>">edit</a></td>
            </tr>
            <%
          }
          rset.close();
          pstmt.close();
          %>
        </table>
        <br>
                <p>The following codes should not be sent. Please communicate these to the sender. IWeb may generate errors if these are sent.</p>
        <table border="1" cellpadding="3" cellspacing="0"> 
          <tr>
            <th>CPT</th>
            <th>Obs Term</th>
            <th>Dose</th>
            <th>Type</th>
            <th>Obs Code</th>
            <th>Status</th>
            <th>Edit</th>
          </tr>
          <%
          sql = "SELECT \n " + 
            "  lc.obs_term, lc.obs_code, lc.hd_id, lc.data_type, lc.cpt_code, \n " + 
            "  lc.dose_number, lc.description, lf.include_status \n" +
            "FROM logician_code lc, logician_flowsheet lf \n " + 
            "WHERE lc.obs_term = lf.obs_term AND lf.entity_id = ? \n" + 
            "  AND lf.include_status = 'D' " + 
            "ORDER BY lc.cpt_code, lc.obs_term, lc.dose_number, lc.data_type";
          pstmt = conn.prepareStatement(sql);
          pstmt.setInt(1, entityId);
          rset = pstmt.executeQuery();
          while (rset.next())
          {
            String status = rset.getString(8);
            if (status == null)
            {
              status = "";
            }
            String color = "#FFFFFF";
            if (status.equals("E"))
            {
              color = "#CCFFCC";
            }
            else if (status.equals("S"))
            {
              color = "#33CC66";
            }
            else if (status.equals("D"))
            {
              color = "#CC6600";
            }
            %>
            <tr>
              <td bgcolor="<%= color %>"><%= rset.getString(5) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(1) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(6) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(4) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(2) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= status %>&nbsp;</td>
              <td bgcolor="<%= color %>"><a href="editLogicianCode.jsp?entityId=<%= entityId %>&obsTerm=<%= URLEncoder.encode(rset.getString(1), "UTF-8") %>">edit</a></td>
            </tr>
            <%
          }
          rset.close();
          pstmt.close();
          %>
        </table>
        <br>
        
        
        
        <%
        if (request.getParameter("sendAll") == null) {
        sql = "SELECT \n " + 
          "  lc.obs_term, lc.obs_code, lc.hd_id, lc.data_type, lc.cpt_code, \n " + 
          "  lc.dose_number, lc.description, lf.include_status \n" +
          "FROM logician_code lc, logician_flowsheet lf \n " + 
          "WHERE lc.obs_term = lf.obs_term AND lf.entity_id = ? \n" + 
          "  AND (lf.include_status = 'E' OR lf.include_status = 'S' ) \n " +
          "  AND lc.cpt_code <> '' " + 
          "ORDER BY lc.cpt_code, lc.obs_term, lc.dose_number, lc.data_type";
        }
        else {
          sql = "SELECT \n " + 
          "  lc.obs_term, lc.obs_code, lc.hd_id, lc.data_type, lc.cpt_code, \n " + 
          "  lc.dose_number, lc.description, lf.include_status \n" +
          "FROM logician_code lc \n " + 
          "     LEFT JOIN logician_flowsheet lf \n " + 
          "            ON lc.obs_term = lf.obs_term AND entity_id = ? \n " + 
          "WHERE (lf.include_status IS NULL OR lf.include_status <> 'D' ) \n " +
          "  AND lc.cpt_code <> '' " + 
          "ORDER BY lc.cpt_code, lc.obs_term, lc.dose_number, lc.data_type";
        }

        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, entityId);
        rset = pstmt.executeQuery();
        %>
        <p>Copy this XML into IWeb:</p>
        <pre>
&lt;observation&gt;<%
          while (rset.next())
          {
            // <identifier code="MLI-90821" cpt="90715" dose="1" type="DATE"/>
            %>
  &lt;identifier code="<%= rset.getString(2) %>" cpt="<%= rset.getString(5) %>" dose="<%= rset.getString(6) %>" type="<%= rset.getString(4) %>"/&gt;<%
          }
          %>
&lt;/observation&gt;
        </pre>
        <%
      }
    } finally {DatabasePool.close(conn); } %>
  </body>
</html>