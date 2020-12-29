package org.immregistries.lonestar.fhir;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;

public class ValidateServlet extends HttpServlet {
  
  private static final String ACTION_VALIDATE = "Validate";
  private static final String PARAM_ACTION = "action";
  private static final String FHIR_CONTENT = "fhirContent";

  /**
   * 
   */

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doGet(req, resp);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    resp.setContentType("text/html");
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    HttpSession session = req.getSession(true);
    try {
      ValidationResult result = null;
      String fhirContent = req.getParameter(FHIR_CONTENT);
      if (fhirContent == null) {
        fhirContent = "";
      }
      String action = req.getParameter(PARAM_ACTION);
      if (action != null) {
        if (action.equals(ACTION_VALIDATE)) {
          FhirContext ctx = FhirContext.forR4();
          FhirValidator validator = ctx.newValidator();
          IValidatorModule module = new FhirInstanceValidator(ctx);
          result = validator.validateWithResult(fhirContent);

        }
      }

      out.println("<html>");
      out.println("  <head>");
      out.println("    <title>Validate FHIR R4</title>");
      out.println("    <link rel=\"stylesheet\" type=\"text/css\" href=\"../step.css\" />");
      out.println("  </head>");
      out.println("  <body>");
      out.println("    <h1>FHIR Validation</h1>");
      out.println("    <form action=\"\" method=\"POST\">");
      out.println("    FHIR Content <br/>");
      out.println("    <textarea name=\"" + FHIR_CONTENT + "\" cols=\"100\" rows=\"5\">"
          + fhirContent + "</textarea><br/>");
      out.println("    <input type=\"submit\" name=\"" + PARAM_ACTION + "\" value=\""
          + ACTION_VALIDATE + "\"/> <br/>");

      out.println("    </form>");
      if (result != null) {
        if (result.isSuccessful()) {
          out.println("<p>No Issues Found</p>");
        } else {
          out.println("<p>Problems Found</p>");
          out.println("<table>");
          out.println("  <tr>");
          out.println("    <th>Location</th>");
          out.println("    <th>Message</th>");
          out.println("  </tr>");
          for (SingleValidationMessage next : result.getMessages()) {
            out.println("  <tr>");
            out.println("    <td>" + next.getLocationString() + "</td>");
            out.println("    <td>" + next.getMessage() + "</td>");
            out.println("  </tr>");
          }
          out.println("</table>");
        }
      }
      out.println("  </body>");
      out.println("</html>");
    } catch (Exception e) {
      handleException(resp, e);
    } finally {
      out.close();
    }
  }

  protected void handleException(HttpServletResponse resp, Exception e) throws IOException {
    resp.setContentType("text/html");
    resp.setStatus(500);
    PrintWriter out = new PrintWriter(resp.getOutputStream());
    out.println("<!DOCTYPE html>");
    out.println("<html>");
    out.println("  <head>");
    out.println("  </head>");
    out.println("  <body>");
    out.println("    <h1>Oops...</h1>");
    out.println(
        "    <p>The Lone Star Vaccine Forecaster encountered a problem and was unable to evaluate the FHIR content. </p>");
    out.println("    <h2>Technical Details</h2>");
    out.println("    <pre>");
    e.printStackTrace(out);
    out.println("    </pre>");
    out.println("  </body>");
    out.println("</html>");
    out.close();
  }



}
