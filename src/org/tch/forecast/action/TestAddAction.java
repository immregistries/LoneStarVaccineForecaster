package org.tch.forecast.action;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tch.forecast.db.StmtHelper;
import org.tch.forecast.model.TestCaseModel;
import org.tch.forecast.validator.db.DatabasePool;

public class TestAddAction
{
  public void add(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    TestCaseModel model = new TestCaseModel();
    model.case_label = req.getParameter("case_label");
    model.case_description = req.getParameter("case_description");
    model.group_code = req.getParameter("group_code");
    Connection conn = null;
    PreparedStatement pstmt = null, pstmt2 = null,pstmt3=null;
    ResultSet rs = null,rs2=null;
    try
    {
      conn = DatabasePool.getConnection();
      if (model.case_label == null || "".equals(model.case_label))
      {
        req.setAttribute("error_message", "Please Enter Case Name.");
        RequestDispatcher dispatcher = req.getRequestDispatcher("addTestCase.jsp");
        dispatcher.forward(req, resp);
        return;
      }
      boolean caseExist = false;
      pstmt3 = conn.prepareStatement("SELECT 1 FROM test_case where case_label = ?");
      pstmt3.setString(1, model.case_label);
      rs2 = pstmt3.executeQuery();
      if(rs2.next()){
        caseExist = true;
      }
      if (caseExist)
      {
        req.setAttribute("error_message", "Case with given Name already exist.");
        RequestDispatcher dispatcher = req.getRequestDispatcher("addTestCase.jsp");
        dispatcher.forward(req, resp);
        return;
      }
      
      pstmt2 = conn.prepareStatement("SELECT max(case_id) FROM test_case");
      rs = pstmt2.executeQuery();
      if(rs.next()){
        model.case_id = new Integer(rs.getInt(1) + 1);
      }else{
        model.case_id = new Integer(1);
      }
      pstmt = StmtHelper.genInsert(conn, "test_case", model, null);
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
      if (pstmt2 != null)
      {
        try
        {
          pstmt2.close();
        } catch (SQLException ex)
        {
        }
      }
      if (pstmt3 != null)
      {
        try
        {
          pstmt3.close();
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
