package org.tch.forecast.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.tch.forecast.validator.DataSourceUnavailableException;
import org.tch.forecast.validator.db.DatabasePool;

public class ForecastComparisonSaver {
	
	public static void saveForecast(HttpServletRequest request) throws SQLException, DataSourceUnavailableException{
	      String dose 			= request.getParameter("dose");
	      String validateDate 	= request.getParameter("valid_date");
	      String dueDate 		= request.getParameter("due_date");
	      String overdueDate 	= request.getParameter("overdue_date");
	      String caseID 		= request.getParameter("caseId");
	      String userName 		= request.getParameter("userName");
	      String action 		= request.getParameter("action");
	      String lineCode 		= request.getParameter("line_code");
	      String tableName = "";
	      String whereClause = "";
	      String updateClms = " SET dose_number = ?, " +
	      		"valid_date = str_to_date(?, '%m/%d/%Y'), " +
	      		"due_date = str_to_date(?, '%m/%d/%Y'), " +
	      		"overdue_date = str_to_date(?, '%m/%d/%Y') ";
	      if("CT_EXPECTED".equalsIgnoreCase(action)){
	    	  tableName = " expected_result ";
	    	  whereClause = " where case_id = ? and  entity_id <> 2 and line_code = ? ";
	      }else if("TCH_EXPECTED".equalsIgnoreCase(action)){
	    	  tableName = " expected_result ";
	    	  whereClause = " where case_id = ? and  entity_id = 2 and line_code = ? ";
	      }else if("TCH_ACTUAL".equalsIgnoreCase(action)){
	    	  //not supported
	      }else if("MCIR_ACTUAL".equalsIgnoreCase(action)){
	    	  tableName = " actual_result ";
	    	  whereClause = " where case_id = ? and  software_id <> 1  and line_code = ? ";
	      }else{
	    	  return;
	      }
	      Connection conn = null;
	      PreparedStatement pstmt = null;
	      try{
		      conn = DatabasePool.getConnection();
		      String sql = " UPDATE " + tableName + updateClms + whereClause;
		      pstmt = conn.prepareStatement(sql);
		      pstmt.setString(1, dose);
		      pstmt.setString(2, validateDate);
		      pstmt.setString(3, dueDate);
		      pstmt.setString(4, overdueDate);
		      pstmt.setString(5, caseID);
		      pstmt.setString(6, lineCode);
		      pstmt.executeUpdate();
		  }finally{
	    	  if(pstmt != null){
	    		  pstmt.close();
	    	  }
	    	  if(conn != null){
	    		  conn.close();
	    	  }
	      }		
	}
	
}
