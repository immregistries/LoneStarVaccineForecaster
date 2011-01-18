package org.tch.forecast.action;

import java.io.IOException;
import java.sql.Connection;
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
import org.tch.forecast.model.TestCaseModel;
import org.tch.forecast.validator.db.DatabasePool;

public class TestCaseUpdateAction
{
  public void update(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    TestCaseModel model = new TestCaseModel();
    model.case_label = req.getParameter("case_label");
    model.case_description = req.getParameter("case_description");
    model.group_code = req.getParameter("group_code");
    if (req.getParameter("case_id") != null)
    {
      model.case_id = new Integer(req.getParameter("case_id"));
    }
    model.patient_first = req.getParameter("patient_first");
    model.patient_last = req.getParameter("patient_last");
    model.patient_sex = req.getParameter("patient_sex");
    String dobEntered = req.getParameter("patient_dob");
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
      RequestDispatcher dispatcher = req.getRequestDispatcher("editTestCase.jsp");
      dispatcher.forward(req, resp);
      return;
    }

    model.patient_dob = new java.sql.Date(cal.getTimeInMillis());

    Connection conn = null;
    PreparedStatement pstmt = null, pstmt2 = null, pstmt3 = null;
    ResultSet rs = null, rs2 = null;
    try
    {
      conn = DatabasePool.getConnection();

      if (model.case_label == null || "".equals(model.case_label))
      {
        req.setAttribute("error_message", "Please Enter Case Name.");
        RequestDispatcher dispatcher = req.getRequestDispatcher("editTestCase.jsp");
        dispatcher.forward(req, resp);
        return;
      }
      boolean caseExist = false;
      String sql = "SELECT 1 FROM test_case where case_label = ?";
      if (model.case_id != null)
      {
        sql = sql + " and case_id <> " + model.case_id;
      }
      pstmt3 = conn.prepareStatement(sql);
      pstmt3.setString(1, model.case_label);
      rs2 = pstmt3.executeQuery();
      if (rs2.next())
      {
        caseExist = true;
      }
      if (caseExist)
      {
        req.setAttribute("error_message", "Case with given Name already exist.");
        RequestDispatcher dispatcher = req.getRequestDispatcher("editTestCase.jsp");
        dispatcher.forward(req, resp);
        return;
      }
      boolean insert = false;
      if (model.case_id == null)
      {
        insert = true;
        pstmt2 = conn.prepareStatement("SELECT max(case_id) FROM test_case");
        rs = pstmt2.executeQuery();
        if (rs.next())
        {
          model.case_id = new Integer(rs.getInt(1) + 1);
        } else
        {
          model.case_id = new Integer(1);
        }
      }
      StmtField caseIdField = new StmtField();
      caseIdField.fieldname = "case_id";
      caseIdField.value = model.case_id;
      ArrayList whereList = new ArrayList();
      whereList.add(caseIdField);
      if (!insert)
      {
        pstmt = StmtHelper
            .genUpdate(conn, "test_case", model, whereList, new String[] { "case_source", "status_code" });
      } else
      {
        pstmt = StmtHelper.genInsert(conn, "test_case", model, null);
      }
      pstmt.execute();
      RequestDispatcher dispatcher = req.getRequestDispatcher("testCase.jsp?caseId=" + model.case_id);
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
}
