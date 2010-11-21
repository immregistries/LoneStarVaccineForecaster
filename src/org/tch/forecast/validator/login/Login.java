package org.tch.forecast.validator.login;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.tch.forecast.validator.DataSourceUnavailableException;
import org.tch.forecast.validator.db.DBUtill;
import org.tch.forecast.validator.db.DatabasePool;

public class Login {
	private final static String LOGIN_SQL = "select * from test_user where upper(user_name) = ?";
	
	private HttpServletRequest request;
	
	public Login(HttpServletRequest request){
		this.request = request;
	}
	
	public boolean authenticate(UserAuthenticationInfoModel model, boolean updateSession) throws SQLException, DataSourceUnavailableException{
	    if(model.username == null || "".equals(model.username) ){
	    	return false;
	    }
		Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    int i = 1;
	    try{
	    conn = DatabasePool.getConnection();
	    pstmt = conn.prepareStatement(LOGIN_SQL);
	    pstmt.setString(i++, model.username.toUpperCase());
	    rs = pstmt.executeQuery();
	    if(rs.next()){
	    	model.username = rs.getString("user_name"); //set actual name
	    	if(updateSession){
	    		updateSession(model);
	    	}
	    	return true;
	    }
	    }finally{
	    	DBUtill.close(rs, pstmt, conn);
	    }
	    return false;
	}
	
	private void updateSession(UserAuthenticationInfoModel model){
		HttpSession session = request.getSession();
		session.setAttribute("userName", model.username);
	}
	
	
	
}
