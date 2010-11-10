package org.tch.forecast.validator;

import org.tch.hl7.core.util.DateTime;

public class ApplicationInitException extends RuntimeException
{
  public ApplicationInitException()
  {
    super();
    logException(null, null);
  }

  public ApplicationInitException(String message)
  {
    super(message);
    logException(message, null);
  }

  public ApplicationInitException(String message, Throwable cause)
  {
    super(message, cause);
    logException(message, cause);
  }

  public ApplicationInitException(Throwable cause)
  {
    super(cause);
    logException(cause.getMessage(), cause);
  }

  
  private void logException(String message, Throwable cause)
  {
    DateTime today = new DateTime("today");
    System.err.println(
    "ApplicationInitException created ----------------------------------------------------------");
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
