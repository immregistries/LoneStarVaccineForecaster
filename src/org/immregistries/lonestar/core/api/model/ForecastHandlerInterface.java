package org.immregistries.lonestar.core.api.model;

public interface ForecastHandlerInterface {

  /**
   * Forecasts the patient record given in the request and returns a forecast
   * response object. If there is a problem during forecast or if a required
   * value is not supplied this method may return an exception.
   * 
   * @param forecastRequest
   * @return
   * @throws Exception
   */
  public ForecastResponseInterface forecast(ForecastRequestInterface forecastRequest) throws Exception;
}
