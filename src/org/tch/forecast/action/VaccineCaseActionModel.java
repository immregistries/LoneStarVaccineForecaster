package org.tch.forecast.action;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tch.forecast.db.StmtField;
import org.tch.forecast.db.StmtHelper;
import org.tch.forecast.model.VaccineCaseModel;
import org.tch.forecast.validator.db.DatabasePool;

public class VaccineCaseActionModel
{
  public void add(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    VaccineCaseModel model = new VaccineCaseModel();
    model.case_id = new Integer(req.getParameter("case_id"));
    model.cvx_code = req.getParameter("cvx_code");
    String dobEntered = req.getParameter("admin_date");
    StringTokenizer tokenizer = new StringTokenizer(dobEntered, "/");
    boolean validDateEntered = true;
    Calendar cal = Calendar.getInstance();
    try
    {
      String month = tokenizer.nextToken();
      String day = tokenizer.nextToken();
      String year = tokenizer.nextToken();
      if (month == null || month.length() != 2)
      {
        validDateEntered = false;
      }
      if (day == null || day.length() != 2)
      {
        validDateEntered = false;
      }
      if (year == null || year.length() != 4)
      {
        validDateEntered = false;
      }
      cal.set(Calendar.YEAR, new Integer(year).intValue());
      cal.set(Calendar.MONTH, new Integer(month).intValue() - 1);
      cal.set(Calendar.DAY_OF_MONTH, new Integer(day).intValue());
    } catch (NoSuchElementException ex)
    {
      validDateEntered = false;
    } catch (NumberFormatException nex)
    {
      validDateEntered = false;
    }
    if (!validDateEntered)
    {
      req.setAttribute("error_message", "Invalid Date");
      RequestDispatcher dispatcher = req.getRequestDispatcher("addVaccineToCase.jsp");
      dispatcher.forward(req, resp);
      return;
    }

    model.admin_date = new java.sql.Date(cal.getTimeInMillis());
    model.mvx_code = req.getParameter("mvx_code");

    Connection conn = null;
    PreparedStatement pstmt = null, pstmt2 = null, pstmt3 = null;
    ResultSet rs = null, rs2 = null;
    try
    {
      conn = DatabasePool.getConnection();
      if ("Add VaccineTest".equals(req.getParameter("action")))
      {
        String sql = "select 1 from test_vaccine where case_id = ? and cvx_code = ? and admin_date = ? ";
        pstmt2 = conn.prepareStatement(sql);
        pstmt2.setInt(1, model.case_id.intValue());
        pstmt2.setString(2, model.cvx_code);
        pstmt2.setDate(3, model.admin_date);
        rs2 = pstmt2.executeQuery();
        if (rs2.next())
        {
          req.setAttribute("error_message", "Vaccine already exist.");
          RequestDispatcher dispatcher = req.getRequestDispatcher("addVaccineToCase.jsp");
          dispatcher.forward(req, resp);
          return;
        }

        pstmt = StmtHelper.genInsert(conn, "test_vaccine", model, null);
        pstmt.execute();
      } else if ("Edit VaccineTest".equals(req.getParameter("action")))
      {
        if (!req.getParameter("old_cvx_code").equals(req.getParameter("cvx_code"))
            || !req.getParameter("old_admin_date").equals(req.getParameter("admin_date")))
        {
          String sql = "select 1 from test_vaccine where case_id = ? and cvx_code = ? and admin_date = ? ";
          pstmt2 = conn.prepareStatement(sql);
          pstmt2.setInt(1, model.case_id.intValue());
          pstmt2.setString(2, model.cvx_code);
          pstmt2.setDate(3, model.admin_date);
          rs2 = pstmt2.executeQuery();
          if (rs2.next())
          {
            req.setAttribute("error_message", "Vaccine already exist.");
            RequestDispatcher dispatcher = req.getRequestDispatcher("addVaccineToCase.jsp");
            dispatcher.forward(req, resp);
            return;
          }
        }
        ArrayList whereList = new ArrayList();
        StmtField field = new StmtField();
        field.fieldname = "case_id";
        field.value = model.case_id;
        whereList.add(field);
        field = new StmtField();
        field.fieldname = "cvx_code";
        field.value = req.getParameter("old_cvx_code");
        whereList.add(field);
        field = new StmtField();
        field.fieldname = "admin_date";
        field.value = getDate(req.getParameter("old_admin_date"));
        whereList.add(field);
        pstmt = StmtHelper.genUpdate(conn, "test_vaccine", model, whereList);
        pstmt.execute();
      }
      RequestDispatcher dispatcher = req.getRequestDispatcher("testCase.jsp?caseId=" + req.getParameter("case_id"));
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

  public void delete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    Connection conn = null;
    PreparedStatement pstmt = null;
    try
    {
      conn = DatabasePool.getConnection();
      pstmt = conn.prepareStatement(" DELETE FROM test_vaccine where case_id = ? and cvx_code = ? and admin_date = ? ");
      pstmt.setInt(1, new Integer(req.getParameter("case_id")).intValue());
      pstmt.setString(2, req.getParameter("cvx_code"));
      pstmt.setDate(3, getDate(req.getParameter("admin_date")));
      pstmt.execute();
      RequestDispatcher dispatcher = req.getRequestDispatcher("testCase.jsp?caseId=" + req.getParameter("case_id"));
      dispatcher.forward(req, resp);
    } catch (Exception ex)
    {
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

  public static Date getDate(String dobEntered)
  {
    StringTokenizer tokenizer = new StringTokenizer(dobEntered, "/");
    Calendar cal = Calendar.getInstance();
    try
    {
      String month = tokenizer.nextToken();
      String day = tokenizer.nextToken();
      String year = tokenizer.nextToken();
      if (month == null || month.length() != 2)
      {
        return null;
      }
      if (day == null || day.length() != 2)
      {
        return null;
      }
      if (year == null || year.length() != 4)
      {
        return null;
      }
      cal.set(Calendar.YEAR, new Integer(year).intValue());
      cal.set(Calendar.MONTH, new Integer(month).intValue() - 1);
      cal.set(Calendar.DAY_OF_MONTH, new Integer(day).intValue());
    } catch (NoSuchElementException ex)
    {
      return null;
    } catch (NumberFormatException nex)
    {
      return null;
    }
    return new java.sql.Date(cal.getTimeInMillis());
  }
}