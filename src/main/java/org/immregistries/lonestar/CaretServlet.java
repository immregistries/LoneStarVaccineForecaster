package org.immregistries.lonestar;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.immregistries.lonestar.core.api.impl.CvxCode;
import org.immregistries.lonestar.core.api.impl.CvxCodes;
import org.immregistries.lonestar.core.api.impl.VaccineForecastManager;
import org.immregistries.lonestar.core.server.CaretForecaster;

public class CaretServlet extends HttpServlet {

  private static final String EXAMPLE1 =
      "20150924^0^0^0^0^CREYG,ARLIE  Chart#: 00-00-31^31^19830215^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^10/01%03/01^0^0^0^0^~~~3484^110^19830215^0^0^0|||";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
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
        out.println("    <form action=\"caret\">");
        out.println(
            "      <input type=\"text\" name=\"r\" size=\"60\" /><input type=\"submit\" value=\"Forecast\" name=\"action\"/>");
        out.println("    </form>");
        out.println("   <h2>Examples</h2>");
        out.println("     <ul>");
        out.println("       <li><a href=\"caret?r=" + URLEncoder.encode(EXAMPLE1, "UTF-8")
            + "\">Adult flu not due any more </li>");
        out.println("     </ul>");
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
        String result = cf.forecast(vaccineForecastManager, cvxToVaccineIdMap);
        out.println(result);
        out.println();
        out.println(result.replaceAll("\\Q|||\\E", "\n"));
      }
    } catch (Exception e) {
      throw new ServletException("Unable to forecast", e);
    } finally {
      out.close();
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

  }

}
