package org.immregistries.lonestar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.immregistries.lonestar.FhirServlet.ForecastInput;
import org.immregistries.lonestar.core.ImmunizationInterface;

public class FhirServletTest {
  public static void main(String args[]) throws Exception {
    FhirTestServlet fhirTestServlet = new FhirTestServlet();
    String content = FhirTestServlet.JSON_EXAMPLE;
    InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
    ForecastInput forecastInput = new ForecastInput();
    if (content.startsWith("{")) {
      fhirTestServlet.readRequestJSON(forecastInput, in);
    } else {
      fhirTestServlet.readRequestXML(forecastInput, in);
    }
    System.out.println("gender: " + forecastInput.patient.getSex());
    System.out.println("dob:    " + forecastInput.patient.getDob());
    for (ImmunizationInterface imm : forecastInput.imms) {
      System.out.println("imm:    " + imm.getCvx() + " given " + imm.getDateOfShot());
    }
  }
}
