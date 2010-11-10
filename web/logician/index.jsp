<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>

<%@page import="java.net.URLEncoder"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.StringReader"%><html>
  <head>
  <title>Logician Codes</title>
  </head>
  <body>
    <h1>Logician Codes</h1>
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
      if (entityId != 0) 
      {
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
      }
      String action = request.getParameter("action");
      if (action == null)
      {
        action = "";
      }
      if (action.equals("Start"))
      {
        sql = "INSERT INTO logician_entity (registry_name, entity_name) VALUES (?, ?)";
        pstmt = conn.prepareStatement(sql);
        registryName = request.getParameter("registryName");
        entityName = request.getParameter("entityName");
        pstmt.setString(1, registryName);
        pstmt.setString(2, entityName);
        pstmt.executeUpdate();
        pstmt.close();
        sql = "SELECT last_insert_id()";
        pstmt = conn.prepareStatement(sql);
        rset = pstmt.executeQuery();
        if (rset.next())
        {
          entityId = rset.getInt(1);
        }
        rset.close();
        pstmt.close();
      }
      else if (action.equals("Save Expected Values"))
      {
        %>
        <p>Importing expected codes</p>
        <table border="1" cellpadding="3" cellspacing="0">
          <tr>
            <th>Value</th>
            <th>Action</th>
          </tr>
          <%
          String expectedValues = request.getParameter("expectedValues");
          boolean addUnrecognizedCodes = request.getParameter("addUnrecognizedCodes") != null;
          BufferedReader br = new BufferedReader(new StringReader(expectedValues));
          String line ;
          while ((line = br.readLine()) != null)
          {
            line = line.trim();
            if (line.equals(""))
            {
              continue;
            }
            String obsTerm = "";
            String obsCode = "";
            String actionTaken = "";
            String parts[] = line.split("\\^");
            if (parts.length == 2)
            {
              obsCode = parts[0];
              obsTerm = parts[1];
            }
            else
            {
              obsTerm = parts[0];
            }
            // pull logician_code
            sql = "SELECT lc.obs_term, lf.include_status \n " + 
                  "FROM logician_code lc \n " + 
                  "     LEFT JOIN logician_flowsheet lf \n " + 
                  "            ON lc.obs_term = lf.obs_term AND entity_id = ? \n " + 
                  "WHERE lc.obs_term = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, entityId);
            pstmt.setString(2, obsTerm);
            rset = pstmt.executeQuery();
            boolean obsTermRecognized = false;
            String includeStatus = null;
            if (rset.next())
            {
              obsTermRecognized = true;
              includeStatus = rset.getString(2);
            }
            rset.close();
            pstmt.close();
             
            String color = "#FFFFFF";
            if (!obsTermRecognized)
            {
              if (addUnrecognizedCodes) 
              {
                actionTaken = "New observation added. ";
                color = "#CCCCFF";
                sql = "INSERT INTO logician_code (obs_term, obs_code, hd_id, data_type, cpt_code, dose_number, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, obsTerm);
                pstmt.setString(2, obsCode);
                pstmt.setString(3, "");
                pstmt.setString(4, "");
                pstmt.setString(5, "");
                pstmt.setString(6, "");
                pstmt.setString(7, "");
                pstmt.executeUpdate();
                pstmt.close();
                obsTermRecognized = true;
              }
              else
              {
                actionTaken = "Observation was not recognized. ";
                color = "#FF9999";
              }
            }
            if (includeStatus == null && obsTermRecognized)
            {
              actionTaken += "Observation marked as expected. ";
              if (color.equals("#FFFFFF"))
              {
                color = "#CCCCFF";
              }
              sql = "REPLACE INTO logician_flowsheet (obs_term, entity_id, include_status) VALUES (?, ?, ?)";
              pstmt = conn.prepareStatement(sql);
              pstmt.setString(1, obsTerm);
              pstmt.setInt(2, entityId);
              pstmt.setString(3, "E");
              pstmt.executeUpdate();
              pstmt.close();
            }
            %>
            <tr>
              <td bgcolor="<%= color %>"><%= obsTerm %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= actionTaken %>&nbsp;</td>
            </tr>
            <%
          }
          %>
        </table>
        <%
      }
      else if (action.equals("Save Sent Values"))
      {
        %>
        <p>Importing sent codes</p>
        <table border="1" cellpadding="3" cellspacing="0">
          <tr>
            <th>Value</th>
            <th>Action</th>
          </tr>
          <%
          String sentValues = request.getParameter("sentValues");
          BufferedReader br = new BufferedReader(new StringReader(sentValues));
          String line ;
          while ((line = br.readLine()) != null)
          {
            line = line.trim();
            if (line.equals(""))
            {
              continue;
            }
            String obsTerm = "";
            String obsCode = "";
            String actionTaken = "";
            String parts[] = line.split("\\^");
            if (parts.length == 2)
            {
              obsCode = parts[0];
              obsTerm = parts[1];
            }
            else
            {
              obsTerm = parts[0];
            }
            // pull logician_code
            sql = "SELECT lc.obs_term, lf.include_status \n " + 
                  "FROM logician_code lc \n " + 
                  "     LEFT JOIN logician_flowsheet lf \n " + 
                  "            ON lc.obs_term = lf.obs_term AND entity_id = ? \n " + 
                  "WHERE lc.obs_term = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, entityId);
            pstmt.setString(2, obsTerm);
            rset = pstmt.executeQuery();
            boolean obsTermRecognized = false;
            String includeStatus = null;
            if (rset.next())
            {
              obsTermRecognized = true;
              includeStatus = rset.getString(2);
            }
            rset.close();
            pstmt.close();
             
            if (!obsTermRecognized)
            {
              actionTaken = "New observation added. ";
              sql = "INSERT INTO logician_code (obs_term, obs_code, hd_id, data_type, cpt_code, dose_number, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
              pstmt = conn.prepareStatement(sql);
              pstmt.setString(1, obsTerm);
              pstmt.setString(2, obsCode);
              pstmt.setString(3, "");
              pstmt.setString(4, "");
              pstmt.setString(5, "");
              pstmt.setString(6, "");
              pstmt.setString(7, "");
              pstmt.executeUpdate();
              pstmt.close();
            }
            if (includeStatus == null || includeStatus.equals("E"))
            {
              actionTaken += "Observation marked as sent. ";
              sql = "REPLACE INTO logician_flowsheet (obs_term, entity_id, include_status) VALUES (?, ?, ?)";
              pstmt = conn.prepareStatement(sql);
              pstmt.setString(1, obsTerm);
              pstmt.setInt(2, entityId);
              pstmt.setString(3, "S");
              pstmt.executeUpdate();
              pstmt.close();
            }
            String color = actionTaken.equals("") ? "#FFFFFF" : "#CCCCFF";
            if (includeStatus != null && includeStatus.equals("D"))
            {
              actionTaken += "Observation should not have been sent. ";
              color = "#FF9999";
            }
            %>
            <tr>
              <td bgcolor="<%= color %>"><%= obsTerm %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= actionTaken %>&nbsp;</td>
            </tr>
            <%
          }
          %>
        </table>
        <%
      }
      if (entityId == 0) 
      {
        sql = "SELECT entity_id, registry_name, entity_name FROM logician_entity ORDER BY registry_name, entity_name";
        pstmt = conn.prepareStatement(sql);
        rset = pstmt.executeQuery(); 
        %>
        <p>This web page is the central location for XML definition documents that are to be loaded into IWeb. This 
        site will help coordinate the hundreds of codes for each site. If configuring an HL7 interface click the 
        'all' link next to the clinic you are configuring. <p>
        <table> 
          <tr>
            <th>Registry</th>
            <th>Entity</th>
            <th>Generate XML for IWeb</th>
          </tr>
          <%
          while (rset.next()) {
            %>
            <tr>
              <td><a href="index.jsp?entityId=<%= rset.getString(1) %>"><%= rset.getString(2) %></a></td>
              <td><a href="index.jsp?entityId=<%= rset.getString(1) %>"><%= rset.getString(3) %></a></td>
              <td align="center">
                <a href="generateXml.jsp?entityId=<%= rset.getString(1) %>&sendAll=true">all</a>
                <a href="generateXml.jsp?entityId=<%= rset.getString(1) %>">only expected</a>              </td>
            </tr>
            <%
          }
          %>
        </table>    
        <h3>New Logician Entity</h3>
        <form method="POST" action="index.jsp">
          Registry Name <input type="text" name="registryName"/>
          Entity Name <input type="text" name="entityName"/>
          <input type="submit" name="action" value="Start"/>
        </form>
        <%
      }
      else
      {
        %>
        <h3><%= registryName %> <%= entityName %></h3>
        <p>[<a href="index.jsp">home</a>]</p>
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
        
        <hr>
        <h2>Please Send</h2>
          <%
          sql = "SELECT cpt.cpt_code, cpt.cpt_label, lc.obs_term, lc.dose_number, lc.data_type, lc.obs_code, lf.include_status \n" + 
          "  FROM logician_cpt cpt, logician_code lc  \n" + 
          "  LEFT JOIN logician_flowsheet lf ON lc.obs_term = lf.obs_term AND entity_id = ? \n" + 
          "WHERE cpt.accept_status = 'E' \n" + 
          "  AND cpt.cpt_code = lc.cpt_code \n" + 
          "ORDER BY cpt.cpt_label, lc.obs_term \n";
          pstmt = conn.prepareStatement(sql);
          pstmt.setInt(1, entityId);
          rset = pstmt.executeQuery();
          String cptCode = null;
          while (rset.next())
          {
            if (!rset.getString(1).equals(cptCode)) {
              if (cptCode != null) {
                %>
                </table>
                <%
               
              }
              cptCode = rset.getString(1);
              %>
              
              <p>Please send vaccine <b><%= rset.getString(2) %></b> (<%= cptCode %>)</p>
              <table border="1" cellpadding="3" cellspacing="0"> 
                <tr>
                  <th>Obs Term</th>
                  <th>Dose</th>
                  <th>Type</th>
                  <th>Obs Code</th>
                  <th>Status</th>
                  <th>Edit</th>
                </tr>
              <%
              
            }
            String status = rset.getString(7);
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
              <td bgcolor="<%= color %>"><%= rset.getString(3) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(4) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(5) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(6) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= status %>&nbsp;</td>
              <td bgcolor="<%= color %>"><a href="editLogicianCode.jsp?entityId=<%= entityId %>&obsTerm=<%= URLEncoder.encode(rset.getString(3), "UTF-8") %>">edit</a></td>
            </tr>
            <%
          }
          rset.close();
          pstmt.close();
        if (cptCode != null) 
        {  %>
          </table><br><% 
        } %>
        <hr>
        <h2>Should NOT Be Sent</h2>
          <%
          sql = "SELECT cpt.cpt_code, cpt.cpt_label, lc.obs_term, lc.dose_number, lc.data_type, lc.obs_code, lf.include_status \n" + 
          "  FROM logician_cpt cpt, logician_code lc, logician_flowsheet lf \n" + 
          "WHERE cpt.accept_status = 'N' \n" + 
          "  AND cpt.cpt_code = lc.cpt_code \n" +
          "  AND lc.obs_term = lf.obs_term \n" + 
          "  AND (lf.include_status = 'E'  OR lf.include_status = 'S' ) \n" + 
          "  AND entity_id = ? \n" + 
          "ORDER BY cpt.cpt_label, lc.obs_term \n";
          pstmt = conn.prepareStatement(sql);
          pstmt.setInt(1, entityId);
          rset = pstmt.executeQuery();
          cptCode = null;
          while (rset.next())
          {
            if (!rset.getString(1).equals(cptCode)) {
              if (cptCode != null) {
                %>
                </table>
                <%
               
              }
              cptCode = rset.getString(1);
              %>
              
              <p>Should NOT send vaccine <b><%= rset.getString(2) %></b> (<%= cptCode %>):</p>
              <table border="1" cellpadding="3" cellspacing="0"> 
                <tr>
                  <th>Obs Term</th>
                  <th>Dose</th>
                  <th>Type</th>
                  <th>Obs Code</th>
                  <th>Status</th>
                  <th>Edit</th>
                </tr>
              <%
              
            }
            String status = rset.getString(7);
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
              <td bgcolor="<%= color %>"><%= rset.getString(3) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(4) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(5) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= rset.getString(6) %>&nbsp;</td>
              <td bgcolor="<%= color %>"><%= status %>&nbsp;</td>
              <td bgcolor="<%= color %>"><a href="editLogicianCode.jsp?entityId=<%= entityId %>&obsTerm=<%= URLEncoder.encode(rset.getString(3), "UTF-8") %>">edit</a></td>
            </tr>
            <%
          }
          rset.close();
          pstmt.close();
        if (cptCode != null) 
        {  %>
          </table><br><% 
        } %>
        
        <hr>
        <h2>List of all observation codes</h2>
        <%
        sql = "SELECT \n " + 
          "  lc.obs_term, lc.obs_code, lc.hd_id, lc.data_type, lc.cpt_code, \n " + 
          "  lc.dose_number, lc.description, lf.include_status \n" +
          "FROM logician_code lc \n " + 
          "     LEFT JOIN logician_flowsheet lf \n " + 
          "            ON lc.obs_term = lf.obs_term AND entity_id = ? \n " + 
          "ORDER BY lc.cpt_code, lc.obs_term, lc.dose_number, lc.data_type";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, entityId);
        rset = pstmt.executeQuery();
        %>        
        <table border="1" cellspacing="0" cellpadding="4">
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
          %>
        </table>
        <h3>Record Values Expected</h3>
        <form method="POST" action="index.jsp">
          <input type="checkbox" name="addUnrecognizedCodes" value="true"> Add unrecognized values
          <textarea name="expectedValues" cols="20" rows="10"></textarea>
          <input type="hidden" name="entityId" value="<%= entityId %>">
          <input type="submit" name="action" value="Save Expected Values">
        </form>
        <h3>Record Values Sent</h3>
        <form method="POST" action="index.jsp">
          <textarea name="sentValues" cols="20" rows="10"></textarea>
          <input type="hidden" name="entityId" value="<%= entityId %>">
          <input type="submit" name="action" value="Save Sent Values">
        </form>
        Generate XML: <a href="generateXml.jsp?entityId=<%= entityId %>&sendAll=true">all</a>
        <a href="generateXml.jsp?entityId=<%= entityId %>">only expected</a>
        <%
      }
    } finally {DatabasePool.close(conn); } %>
  </body>
</html>