package org.tch.forecast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FhirTestServlet extends FhirServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    out.println("<html><head><title>TCH Forecaster FHIR Server</title></head><body>");
    out.println("<h1>FHIR Server</h1>");
    out.println("<p>This is the end point for the DSTU2 FHIR end-point. This is experimental and may change at any time. </p>");
    out.println("<form method=\"POST\" action=\"fhirTest\">");
    out.println("  <textarea name=\"xml\"></textarea><br/>");
    out.println("  <input type=\"submit\" name=\"submit\" value=\"submit\"/>");
    out.println("</form>");
    out.println("</body></html>");
    out.close();
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String xml = req.getParameter("xml");
    InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
    ForecastInput forecastInput = new ForecastInput();
    try {
      readRequest(forecastInput, in);
    } catch (Exception e) {
      throw new ServletException("Unable to read request: " + e.getMessage(), e);
    }
    respond(resp, forecastInput);
  }

}
