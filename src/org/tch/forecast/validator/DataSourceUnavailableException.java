package org.tch.forecast.validator;

/**
 * This exception is thrown when a required resource can not be obtained.
 * For example, when the database is down. 
 * 
 * All exceptions are logged to System.err
 * 
 * @author Nathan Bunker
 *
 */
public class DataSourceUnavailableException extends BaseException
{
  
  public DataSourceUnavailableException()
  {
    super();
  }

  public DataSourceUnavailableException(String message)
  {
    super(message);
  }

  public DataSourceUnavailableException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * Constructs a DataSourceUnavailableException and marks whether this 
   * exception is because the main registry database is down.
   * 
   * @param message 
   * @param cause The exception thrown
   * @param registryDatabaseIsDown Whether this excpetion means the registry is down or not
   */
  public DataSourceUnavailableException(String message, Throwable cause, boolean registryDatabaseIsDown)
  {
    super(message, cause);
  }

  public DataSourceUnavailableException(Throwable cause)
  {
    super(cause);
  }
 
}