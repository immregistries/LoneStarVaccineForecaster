package org.tch.forecast;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tch.forecast.core.api.impl.CvxCode;
import org.tch.forecast.core.api.impl.CvxCodes;
import org.tch.forecast.core.api.impl.VaccineForecastManager;
import org.tch.forecast.core.server.CaretForecaster;

public class CaretServlet extends HttpServlet
{

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String caretString = req.getParameter("r");

    PrintWriter out = new PrintWriter(resp.getOutputStream());

    try {
      if (caretString == null) {
        resp.setContentType("text/html");
        out.println("<html>");
        out.println("  <head>");
        out.println("    <title>Caret Format</title>");
        out.println("  </head>");
        out.println("  <body>");
        out.println("    <form>");
        out.println("      <input type=\"text\" name=\"r\" /><input type=\"submit\" value=\"Forecast\" name=\"action\"");
        out.println("    </form>");
        out.println("  </body>");
        out.println("</html>");
      } else {
        resp.setContentType("text/plain");
        VaccineForecastManager vaccineForecastManager = new VaccineForecastManager();
        Map<String, CvxCode> cvxToCvxCodeMap = CvxCodes.getCvxToCvxCodeMap();
        Map<String, Integer> cvxToVaccineIdMap = new HashMap<String, Integer>();
        for (CvxCode cvxCode : cvxToCvxCodeMap.values()) {
          cvxToVaccineIdMap.put(cvxCode.getCvxCode(), cvxCode.getVaccineId());
        }
        CaretForecaster cf = new CaretForecaster(caretString);
        out.println(cf.forecast(vaccineForecastManager, cvxToVaccineIdMap));
      }
    } catch (Exception e) {
      throw new ServletException("Unable to forecast", e);
    } finally {
      out.close();
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

  }

}
