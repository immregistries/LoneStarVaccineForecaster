package org.tch.forecast.action;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ActionServlet extends HttpServlet
{

  public ActionServlet() {
    super();
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res) throws javax.servlet.ServletException,
      java.io.IOException
  {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest req, HttpServletResponse res) throws javax.servlet.ServletException,
      java.io.IOException
  {
    String action = req.getParameter("action");
    if("Edit Test Case".equals(action) ||
        "Add Test Case".equals(action)){
      TestCaseUpdateAction testUpdateAction = new TestCaseUpdateAction();
      testUpdateAction.update(req,res);
    }else if("Delete Test Case".equals(action)){
      TestCaseDeleteAction testCaseDeleteAction = new TestCaseDeleteAction();
      testCaseDeleteAction.delete(req,res);
    }
  }

}
