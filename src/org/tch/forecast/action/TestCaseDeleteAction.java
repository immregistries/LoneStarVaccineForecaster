package org.tch.forecast.action;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tch.forecast.validator.db.DatabasePool;

public class TestCaseDeleteAction
{
  public void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    Connection conn = null;
    PreparedStatement pstmt=null;
    try
    {
      conn = DatabasePool.getConnection();
      pstmt = conn.prepareStatement(" DELETE FROM test_case where case_id = ? ");
      pstmt.setInt(1, new Integer(req.getParameter("case_id")).intValue());
      pstmt.execute();
      RequestDispatcher dispatcher = req.getRequestDispatcher("main.jsp");
      dispatcher.forward(req, resp);
    }catch(Exception ex){
      throw new ServletException(ex);
    } finally
    {
      if (pstmt != null)
      {
        try
        {
          pstmt.close();
        } catch (SQLException ex)
        {
        }
      }
      if (conn != null)
      {
        try
        {
          conn.close();
        } catch (SQLException ex)
        {
        }
      }
    }

  }
}