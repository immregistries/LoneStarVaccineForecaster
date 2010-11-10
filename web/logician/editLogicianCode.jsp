<%@page import="java.sql.Connection"%>
<%@page import="org.tch.forecast.validator.db.DatabasePool"%>
<%@page import="java.sql.PreparedStatement"%>
<%@page import="java.sql.ResultSet"%>

<%@page import="java.net.URLEncoder"%>
<%@page import="java.io.BufferedReader"%>
<%@page import="java.io.StringReader"%><html>
  <head>
  <title>Logician Code</title>
  </head>
  <body>
    <h1>Edit Logician Code</h1>
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
      String obsTerm = request.getParameter("obsTerm");
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
      String action = request.getParameter("action");
      if (action != null)
      {
        String cptCode = request.getParameter("cptCode");
        String doseNumber = request.getParameter("doseNumber");
        String obsCode = request.getParameter("obsCode");
        String dataType = request.getParameter("dataType");
        String includeStatus = request.getParameter("includeStatus");
        if (!includeStatus.equals(""))
        {
          sql = "REPLACE INTO logician_flowsheet (obs_term, entity_id, include_status) VALUES (?, ?, ?)";
          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, obsTerm);
          pstmt.setInt(2, entityId);
          pstmt.setString(3, includeStatus);
          pstmt.executeUpdate();
          pstmt.close();
        }
        boolean problem = false;
        if (cptCode.equals(""))
        {
          problem = true;
          %>
            <p><font color="#FF0000">Problem! CPT code must be specified</font></p>
          <%
        }
        if (doseNumber.equals(""))
        {
          problem = true;
          %>
            <p><font color="#FF0000">Problem! Dose number must be specified</font></p>
          <%
        }
        if (obsCode.equals(""))
        {
          problem = true;
          %>
            <p><font color="#FF0000">Problem! Obs code must be specified</font></p>
          <%
        }
        if (dataType.equals(""))
        {
          problem = true;
          %>
            <p><font color="#FF0000">Problem! Data type must be specified</font></p>
          <%
        }
        if (!problem)
        {
          sql = "UPDATE logician_code SET obs_code = ?, hd_id = ?, data_type = ?, cpt_code = ?, dose_number = ?, description = ? WHERE obs_term = ?";
          pstmt = conn.prepareStatement(sql);
          pstmt.setString(1, obsCode);
          pstmt.setString(2, request.getParameter("hdId"));
          pstmt.setString(3, dataType);
          pstmt.setString(4, cptCode);
          pstmt.setString(5, doseNumber);
          pstmt.setString(6, request.getParameter("description"));
          pstmt.setString(7, obsTerm);
          pstmt.executeUpdate();
          pstmt.close();
          %>
          <script type="text/javascript">
<!--
window.location = "index.jsp?entityId=<%= entityId%>"
//-->
</script>
          
          <%
        }
      }
      {
        %>
        <h3><%= registryName %> <%= entityName %></h3>
        <%
          sql = "SELECT \n " + 
            "  lc.obs_term, lc.obs_code, lc.hd_id, lc.data_type, lc.cpt_code, \n " + 
            "  lc.dose_number, lc.description, lf.include_status \n" +
            "FROM logician_code lc \n " + 
            "     LEFT JOIN logician_flowsheet lf \n " + 
            "            ON lc.obs_term = lf.obs_term AND entity_id = ? \n " + 
            "WHERE lc.obs_term = ? \n";
          pstmt = conn.prepareStatement(sql);
          pstmt.setInt(1, entityId);
          pstmt.setString(2, obsTerm);
          rset = pstmt.executeQuery();
          if (rset.next())
          {
            %>
            <form action="editLogicianCode.jsp" method="GET">
            <input type="hidden" name="obsTerm" value="<%= obsTerm %>">
            <input type="hidden" name="entityId" value="<%= entityId %>">
            <table>
            <tr>
              <td>Obs Term</td>
              <td><input type="text" name="obsTerm" value="<%= rset.getString(1) %>" size="20"></td>
            </tr>
            <tr>
              <td>Obs Code</td>
              <td><input type="text" name="obsCode" value="<%= rset.getString(2) %>" size="15"></td>
            </tr>
            <tr>
              <td>HD ID</td>
              <td><input type="text" name="hdId" value="<%= rset.getString(3) %>" size="4"></td>
            </tr>
            <tr>
              <td>Data Type</td>
              <% String dataType = rset.getString(4); if (dataType == null) {dataType = "";}%>
              <td><select name="dataType">
                <option value="">--select--</option>
                <option value="DATE" <%= dataType.equals("DATE") ? "selected" : ""%>>Date/Given</option>
                <option value="BYID" <%= dataType.equals("BYID") ? "selected" : "" %>>Given By</option>
                <option value="DOSE" <%= dataType.equals("DOSE") ? "selected" : "" %>>Dose</option>
                <option value="DUE" <%= dataType.equals("DUE") ? "selected" : "" %>>Due</option>
                <option value="EXP" <%= dataType.equals("EXP") ? "selected" : "" %>>Expiration Date</option>
                <option value="HIST" <%= dataType.equals("HIST") ? "selected" : "" %>>Historical</option>
                <option value="LOT" <%= dataType.equals("LOT") ? "selected" : "" %>>Lot Number</option>
                <option value="MFR" <%= dataType.equals("MFR") ? "selected" : "" %>>Manufacturer</option>
                <option value="RNG" <%= dataType.equals("RNG") ? "selected" : "" %>>Reason Not Given</option>
                <option value="ROUTE" <%= dataType.equals("ROUTE") ? "selected" : "" %>>Route</option>
                <option value="SITE" <%= dataType.equals("SITE") ? "selected" : "" %>>Site (on body)</option>
                <option value="SRC" <%= dataType.equals("SRC") ? "selected" : "" %>>Source</option>
                <option value="VACCVFC"> <%= dataType.equals("VACCVFC") ? "selected" : "" %>Vaccination VFC Status</option>
                <option value="VIS" <%= dataType.equals("VIS") ? "selected" : "" %>>VIS Date</option>
              </select>
              </td>
           </tr>
            <tr>
              <td>CPT Code</td>
              <td><input type="text" name="cptCode" value="<%= rset.getString(5) %>" size="5"></td>
            </tr>
            <tr>
              <td>Dose Number</td>
              <td><input type="text" name="doseNumber" value="<%= rset.getString(6) %>" size="3"></td>
            </tr>
            <tr>
              <td>Description</td>
              <td><input type="text" name="description" value="<%= rset.getString(7) %>" size="40"></td>
            </tr>
            <tr>
              <td>Include Status</td>
              <td>
              <% String includeStatus = rset.getString(8); if (includeStatus == null) { includeStatus = ""; }%>
              <select name="includeStatus">
                <option value="">--select--</option>
                <option value="E" <%= includeStatus.equals("E") ? "selected" : ""%>>Expected</option>
                <option value="S" <%= includeStatus.equals("S") ? "selected" : ""%>>Sent</option>
                <option value="D" <%= includeStatus.equals("D") ? "selected" : ""%>>Don't Send</option>
                <option value="U" <%= includeStatus.equals("U") ? "selected" : ""%>>Unknown</option>
              </select>
              </td>
            </tr>
            <tr>
              <td colspan="2" align="right"><input type="submit" name="action" value="SAVE"></td>
            </tr>
            </table>
            </form>
            <%
          }
          rset.close();
          pstmt.close();
          %>
        <%
      }
      boolean foundRoot = false;
      if (obsTerm.indexOf("#") != -1)
      {
        obsTerm = obsTerm.substring(0, obsTerm.indexOf("#"));
        foundRoot = true;
      }
      else
      {
        for (int i = 0; i < obsTerm.length(); i++)
        {
          if (obsTerm.charAt(i) >= '1' && obsTerm.charAt(i) <= '9') 
          {
            obsTerm = obsTerm.substring(0, i);
            foundRoot = true;
            break;
          }
        }
      }
      if (!foundRoot)
      {
        String[] endingRoots = {"SITE", "DOSE", "MFR", "LOT", "VIS", "EX", "STE", "BY", "RNG", " IMMU", "HX", "DOS", "SIT", "LO", "MF", "VC", "VI", "SI"};
        for (int i = 0; i < endingRoots.length; i++)
        {
          if (obsTerm.endsWith(endingRoots[i]))
          {
            obsTerm = obsTerm.substring(0, obsTerm.length() - endingRoots[i].length());
            foundRoot = true;
          }
        }
      }
      %>
      <p>[<a href="index.jsp?entityId=<%= entityId %>">Cancel and Return</a>]</p>
      <h3>Similar Codes</h3>
      <p>This is a list of codes that may or may not be related. There may be missing entries as the Logician system using non-standard naming conventions.</p>
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
            "FROM logician_code lc \n " + 
            "     LEFT JOIN logician_flowsheet lf \n " + 
            "            ON lc.obs_term = lf.obs_term AND entity_id = ? \n " + 
            "WHERE lc.obs_term LIKE ? " + 
            "ORDER BY lc.cpt_code, lc.obs_term, lc.dose_number, lc.data_type";
          pstmt = conn.prepareStatement(sql);
          pstmt.setInt(1, entityId);
          pstmt.setString(2, obsTerm.trim() + "%");
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
      <%
    } finally {DatabasePool.close(conn); } %>
    
    
  </body>
</html>