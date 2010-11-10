package org.tch.forecast.validator;

import java.sql.SQLException;

/**
 * This exception represents an exception that has been thrown because there was an error retrieving
 * or resloving a request. This indicates that the data source is accesable but some problem
 * prevented it from commpleting the request. All exceptions are logged to System.err
 * @author Nathan Bunker
 */
public class DataSourceException extends BaseException
{

  private String sql = null;

  public DataSourceException()
  {
    super();
  }

  public DataSourceException(String message)
  {
    super(message);
  }

  public DataSourceException(String message, Throwable cause)
  {
    super(message, cause);
  }

  /**
   * Use new DataSourceException(message, cause, sql_query) instead.
   * @param message
   * @param cause
   * @deprecated
   */
  public DataSourceException(String message, SQLException cause)
  {
    this(message, (Throwable) cause);
  }

  public DataSourceException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Use new DataSourceException(cause, sql_query) instead.
   * @param cause
   * @deprecated
   */
  public DataSourceException(SQLException cause)
  {
    this((Throwable) cause);
  }

  /**
   * This constructor has been created to record SQLExceptions that occur in the process of querying
   * or updating the database. By using this constructor, the original SQL statement used will be
   * saved with the exception. This will improve resolution of problems in the future.
   * <p>
   * Since most of the sql statements are prepared statements and include ? and not the actual
   * values, try to pass the value or values used in the <code>message</code>. Here is an example
   * message: "Unable to get record for patient 1".
   * <p>
   * If there is no SQL statement associated with this exception, please pass an empty string
   * instead.
   * @param message
   * @param cause
   * @param sql
   */
  public DataSourceException(String message, SQLException cause, String sql)
  {
    super(message, cause);
    if (sql != null)
    {
      this.sql = sql;
      System.err.println("SQL Query ----------------------------------------------------------------------");
      System.err.println(sql);
      System.err.println("--------------------------------------------------------------------------------");
    }
  }

  public DataSourceException(SQLException cause, String sql)
  {
    this(cause.getMessage(), cause, sql);
  }

  /**
   * @return Returns the sql.
   */
  public String getSql()
  {
    return sql;
  }

  /**
   * @param sql The sql to set.
   */
  public void setSql(String sql)
  {
    this.sql = sql;
  }
}