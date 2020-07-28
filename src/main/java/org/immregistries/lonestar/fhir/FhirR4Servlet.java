package org.immregistries.lonestar.fhir;

import ca.uhn.fhir.rest.server.RestfulServer;

public class FhirR4Servlet extends RestfulServer {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public FhirR4Servlet()
  {
    registerProvider(new ImmDSForecastProvider());
  }

}
