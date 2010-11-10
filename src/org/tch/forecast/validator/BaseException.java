package org.tch.forecast.validator;

import org.tch.hl7.core.util.DateTime;

/**
 * 
 * This exception is a general 2nd tier exception that encapsulates some problem
 * that exists that prevents a class from servicing a top tier request. This should
 * only be used if DataSourceException and DataSourceUnavailableException don't make
 * sense.
 * 
 * All exceptions are logged to System.err
 * 
 * @author Nathan Bunker
 *
 */
public class BaseException extends Exception
{

  public BaseException()
  {
    super();
    logException(null, null);
  }

  public BaseException(String message)
  {
    super(message);
    logException(message, null);
  }

  public BaseException(String message, Throwable cause)
  {
    super(message, cause);
    logException(message, cause);
  }

  public BaseException(Throwable cause)
  {
    super(cause);
    logException(cause.getMessage(), cause);
  }

  private void logException(String message, Throwable cause)
  {
    DateTime today = new DateTime("today");
    System.err.println(
      "SiisException created ----------------------------------------------------------");
    System.err.println("Time:    " + today);
    if (message != null)
    {
      System.err.println("Message: " + getMessage());
    }
    while (cause != null)
    {
      System.err.println(
        "Caused by ----------------------------------------------------------------------");
      cause.printStackTrace(System.err);
      cause = cause.getCause();
    }
  }

}
