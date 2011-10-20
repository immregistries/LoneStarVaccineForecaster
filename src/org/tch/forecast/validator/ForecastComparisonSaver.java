package org.tch.forecast.validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.tch.forecast.validator.DataSourceUnavailableException;
import org.tch.forecast.validator.db.DatabasePool;

public class ForecastComparisonSaver
{

  public static void saveForecast(HttpServletRequest request) throws SQLException, DataSourceUnavailableException
  {
    String dose = request.getParameter("dose");
    String validateDate = request.getParameter("valid_date");
    String dueDate = request.getParameter("due_date");
    String overdueDate = request.getParameter("overdue_date");
    String caseID = request.getParameter("caseId");
    String userName = request.getParameter("userName");
    String action = request.getParameter("action");
    String lineCode = request.getParameter("line_code");
    String softwareId = request.getParameter("software_id");
    String entityId = request.getParameter("entity_id");
    String tableName = "";
    String whereClause = "";
    String updateClms = " SET dose_number = ?, " 
        + "valid_date = str_to_date(?, '%m/%d/%Y'), "
        + "due_date = str_to_date(?, '%m/%d/%Y'), " 
        + "overdue_date = str_to_date(?, '%m/%d/%Y') ";
    
    if (softwareId != null)
    {
      tableName = " actual_result ";
      whereClause = " where case_id = ? and software_id = ? and line_code = ? ";
    }
    else if (entityId != null)
    {
      tableName = " expected_result ";
      whereClause = " where case_id = ? and entity_id = ? and line_code = ? ";
    }
    else
    {
      throw new IllegalArgumentException("Software Id or Entity Id must be defined");
    }
    Connection conn = null;
    PreparedStatement pstmt = null;
    try
    {
      conn = DatabasePool.getConnection();
      String sql = " UPDATE " + tableName + updateClms + whereClause;
      pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, dose);
      pstmt.setString(2, validateDate);
      pstmt.setString(3, dueDate);
      pstmt.setString(4, overdueDate);
      pstmt.setString(5, caseID);
      pstmt.setString(6, softwareId == null ? entityId : softwareId);
      pstmt.setString(7, lineCode);
      pstmt.executeUpdate();
    } finally
    {
      if (pstmt != null)
      {
        pstmt.close();
      }
      if (conn != null)
      {
        conn.close();
      }
    }
  }

}
