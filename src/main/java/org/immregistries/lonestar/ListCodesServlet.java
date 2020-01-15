package org.immregistries.lonestar;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.immregistries.lonestar.core.api.impl.CvxCode;
import org.immregistries.lonestar.core.api.test.ListCodes;

public class ListCodesServlet extends HttpServlet {

  private static Map<String, String> scheduleNameMap = new HashMap<String, String>();

  static {
    scheduleNameMap.put("Diphtheria", "Diphtheria");
    scheduleNameMap.put("HepA", "HepA");
    scheduleNameMap.put("HepB", "HepB");
    scheduleNameMap.put("Hib", "HIB");
    scheduleNameMap.put("HPV", "HPV");
    scheduleNameMap.put("Influenza", "Influenza");
    scheduleNameMap.put("Mening", "Mening");
    scheduleNameMap.put("MeningococcalB", "MeningococcalB");
    scheduleNameMap.put("Measles", "MMR");
    scheduleNameMap.put("Mumps", "MMR");
    scheduleNameMap.put("Rubella", "MMR");
    scheduleNameMap.put("Pertussis", "Pertussis");
    scheduleNameMap.put("Pneumo", "Pneumo");
    scheduleNameMap.put("Pneumo65", "Pneumo65");
    scheduleNameMap.put("Polio", "Polio");
    // scheduleNameMap.put("PPSV", "PPSV");
    scheduleNameMap.put("Rotavirus", "Rotavirus");
    scheduleNameMap.put("Varicella", "Varicella");
    scheduleNameMap.put("Zoster", "Zoster");
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    resp.setContentType("text/html");

    String cvxCodeSelected = req.getParameter("cvxCode");
    out.println("<html>");
    out.println("  <head>");
    out.println("    <title>Forecaster Codes</title>");
    out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"index.css\" />");
    out.println("  </head>");
    out.println("  <body>");
    ListCodes listCodes;
    try {
      listCodes = new ListCodes();
    } catch (Exception e) {
      throw new ServletException("Unable to list codes", e);
    }

    out.println("<h1>Lone Star Vaccine Forecaster Codes</h1>");

    out.println("<form action=\"listCodes\" method=\"GET\">");
    out.println("  CVX Code <input type=\"text\" name=\"cvxCode\" value=\""
        + (cvxCodeSelected == null ? "" : cvxCodeSelected) + "\" size=\"3\">");
    out.println("  <input type=\"submit\" name=\"submit\" value=\"Search\"/>");
    out.println("</form>");

    if (cvxCodeSelected != null && !cvxCodeSelected.equals("")) {
      boolean found = false;
      for (CvxCode cvxCode : listCodes.getCvxCodeList()) {
        if (cvxCode.getCvxCode().equals(cvxCodeSelected)) {
          found = true;
          out.println("<h2>" + cvxCode.getCvxLabel() + "</h2>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>CVX Label</th>");
          out.println("    <td>" + cvxCode.getCvxLabel() + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>CVX</th>");
          out.println("    <td>" + cvxCode.getCvxCode() + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Internal Label</th>");
          out.println("    <td>" + cvxCode.getVaccineLabel() + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Internal Id</th>");
          out.println("    <td>"
              + (cvxCode.getVaccineId() == 0 ? "&nbsp;" : "" + cvxCode.getVaccineId()) + "</td>");
          out.println("  </tr>");
          out.println("  <tr>");
          out.println("    <th>Use Status</th>");
          out.println("    <td>" + getUseStatusDisplay(cvxCode) + "</td>");
          out.println("  </tr>");

          if (!cvxCode.isLocationSet()) {
            if (cvxCode.getUseStatus() == CvxCode.UseStatus.SUPPORTED) {
              out.println("  <tr>");
              out.println("    <th>Comment</th>");
              out.println("    <td>No forecast series references this code</td>");
              out.println("  </tr>");
            } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.PENDING) {
              out.println("  <tr>");
              out.println("    <th>Comment</th>");
              out.println("    <td>Code is being added to the forecaster</td>");
              out.println("  </tr>");
            }
          }
          out.println("</table>");
          out.println("</br>");

          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>Schedule</th>");
          out.println("    <th>Vaccine Name(s)</th>");
          out.println("    <th>Details</th>");
          out.println("  </tr>");
          for (String forecastCode : listCodes.getForecastCodeList()) {
            Set<String> locationSet = cvxCode.getLocationMapSet().get(forecastCode);
            StringBuilder sb = new StringBuilder();
            if (locationSet != null) {
              boolean first = true;
              for (String location : locationSet) {
                if (!first) {
                  sb.append(" & ");
                }
                first = false;
                sb.append(location);
              }
            }
            if (sb.length() != 0) {
              out.println("  <tr>");
              out.println("    <td>" + forecastCode + "</td>");
              out.println("    <td>" + sb.toString() + "</td>");
              String scheduleName = scheduleNameMap.get(forecastCode);
              if (scheduleName != null) {
                out.println("  <td><a href=\"schedules/" + scheduleName
                    + ".pdf\">PDF</a> <a href=\"schedules/" + scheduleName
                    + ".xlsx\">Excel</a></td>");
              } else {
                out.println("  <td>&nbsp;</td>");
              }
              out.println("  </tr>");
            }
          }
          out.println("</table>");

        }
      }
      if (!found) {
        out.println("<p class=\"fail\">CVX code not found.</p>");
      }
    }
    out.println("<h2>All CVX Codes</h2>");
    out.println("<table>");
    out.println("  <tr>");
    out.println("    <th>CVX</th>");
    out.println("    <th>CVX Label</th>");
    out.println("    <th>Use Status</th>");
    out.println("    <th>Comment</th>");
    out.println("  </tr>");
    for (CvxCode cvxCode : listCodes.getCvxCodeList()) {
      String displayClass = "";
      if (cvxCode.getUseStatus() == CvxCode.UseStatus.SUPPORTED) {
        displayClass = "pass";
      } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.PENDING) {
        displayClass = "fail";
      } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.NOT_SUPPORTED) {
        displayClass = "grey";
      }
      out.println("  <tr>");
      out.println("    <td class=\"" + displayClass + "\">" + cvxCode.getCvxCode() + "</td>");
      if (cvxCode.getUseStatus() != CvxCode.UseStatus.NOT_SUPPORTED) {
        out.println("    <td class=\"" + displayClass + "\"><a href=\"listCodes?cvxCode="
            + cvxCode.getCvxCode() + "\">" + cvxCode.getCvxLabel() + "</a></td>");
      } else {
        out.println("    <td class=\"" + displayClass + "\">" + cvxCode.getCvxLabel() + "</td>");
      }
      out.println(
          "    <td class=\"" + displayClass + "\">" + getUseStatusDisplay(cvxCode) + "</td>");
      if (cvxCode.isLocationSet()) {
        out.println("    <td class=\"" + displayClass + "\">&nbsp;</td>");
      } else {
        if (cvxCode.getUseStatus() == CvxCode.UseStatus.SUPPORTED) {
          out.println(
              "    <td class=\"fail\">Problem: No forecast series references this code</td>");
        } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.PENDING) {
          out.println("    <td class=\"fail\">Code is being added to the forecaster</td>");
        } else {
          out.println("    <td class=\"" + displayClass + "\">&nbsp;</td>");
        }
      }
      out.println("  </tr>");
    }
    out.println("</table>");

    out.println("  </body>");
    out.println("</html>");

    out.close();
  }

  public String getUseStatusDisplay(CvxCode cvxCode) {
    if (cvxCode.getUseStatus() == CvxCode.UseStatus.NOT_SUPPORTED) {
      return "NOT SUPPORTED";
    } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.SUPPORTED) {
      return "SUPPORTED";
    } else if (cvxCode.getUseStatus() == CvxCode.UseStatus.PENDING) {
      return "PENDING";
    } else {
      return "&nbsp;";
    }
  }
}
