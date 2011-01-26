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
import org.tch.forecast.model.VaccineModel;
import org.tch.forecast.validator.db.DatabasePool;

public class VaccineAddAction
{
  public void add(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    VaccineModel model = new VaccineModel();
    model.cvx_code = req.getParameter("cvx_code");
    model.cvx_label = req.getParameter("cvx_label");
    if (model.cvx_code == null)
    {
      req.setAttribute("error_message", "Enter valid CVX Code.");
      RequestDispatcher dispatcher = req.getRequestDispatcher("addVaccine.jsp");
      dispatcher.forward(req, resp);
      return;
    }
    if (model.cvx_label == null)
    {
      req.setAttribute("error_message", "Enter valid CVX Name.");
      RequestDispatcher dispatcher = req.getRequestDispatcher("addVaccine.jsp");
      dispatcher.forward(req, resp);
      return;
    }
    if (req.getParameter("vaccine_id") == null)
    {
      req.setAttribute("error_message", "Enter vaccine ID.");
      RequestDispatcher dispatcher = req.getRequestDispatcher("addVaccine.jsp");
      dispatcher.forward(req, resp);
      return;
    }

    try
    {
      model.vaccine_id = new Integer(req.getParameter("vaccine_id"));
    } catch (NumberFormatException ex)
    {
      req.setAttribute("error_message", "Enter valid vaccine ID.");
      RequestDispatcher dispatcher = req.getRequestDispatcher("addVaccine.jsp");
      dispatcher.forward(req, resp);
      return;
    }
    Connection conn = null;
    PreparedStatement pstmt = null, pstmt2 = null, pstmt3 = null;
    ResultSet rs = null, rs2 = null;
    try
    {
      conn = DatabasePool.getConnection();
      String sql = "SELECT 1 FROM vaccine_cvx where cvx_code = '" + model.cvx_code + "'";
      pstmt2 = conn.prepareStatement(sql);
      rs2 = pstmt2.executeQuery();
      if (rs2.next())
      {
        req.setAttribute("error_message", "Enter unique CVX Code.");
        RequestDispatcher dispatcher = req.getRequestDispatcher("editTestCase.jsp");
        dispatcher.forward(req, resp);
        return;
      }
      sql = "SELECT 1 FROM vaccine_cvx where vaccine_id = " + model.vaccine_id;
      pstmt3 = conn.prepareStatement(sql);
      rs = pstmt3.executeQuery();
      if (rs.next())
      {
        req.setAttribute("error_message", "Enter unique vaccine.");
        RequestDispatcher dispatcher = req.getRequestDispatcher("addVaccine.jsp");
        dispatcher.forward(req, resp);
        return;
      }
      pstmt = StmtHelper.genInsert(conn, "vaccine_cvx", model, null);
      pstmt.execute();
      RequestDispatcher dispatcher = req.getRequestDispatcher("index.jsp");
      dispatcher.forward(req, resp);
    } catch (Exception ex)
    {
      throw new ServletException(ex);
    } finally
    {
      if (rs != null)
      {
        try
        {
          rs.close();
        } catch (SQLException ex)
        {
        }
      }
      if (rs2 != null)
      {
        try
        {
          rs2.close();
        } catch (SQLException ex)
        {
        }
      }
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
