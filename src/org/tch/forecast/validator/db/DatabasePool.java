package org.tch.forecast.validator.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.tch.forecast.validator.DataSourceException;
import org.tch.forecast.validator.DataSourceUnavailableException;

import snaq.db.ConnectionPoolManager;

public class DatabasePool {

  private static Properties            properties            = new Properties();
  static {
    properties.put("drivers", "com.mysql.jdbc.Driver");
    properties.put("logfile", "c:/dbpool.mysql.validator.log");
    properties.put("tracker.url", "jdbc:mysql://localhost/forecastValidation");
    properties.put("tracker.user", "forecastUser");
    properties.put("tracker.password", "goldenroot");
    properties.put("tracker.maxpool", "5");
    properties.put("tracker.maxconn", "10");
    properties.put("tracker.expiry", "600");
    properties.put("tracker.init", "0");
    properties.put("tracker.validator", "snaq.db.AutoCommitValidator");
    properties.put("tracker.cache", "true");
    properties.put("tracker.debug", "true");
    properties.put("tracker.prop.zeroDateTimeBehavior", "convertToNull");
  }

  private static boolean               useInternalProperties = false;
  private static final String          POOL_NAME             = "validator";
  private static final int             DATABASE_TIMEOUT      = 5000;
  private static ConnectionPoolManager connectionPoolManager = null;

  private static void init() throws DataSourceUnavailableException {
    if (connectionPoolManager == null) {
      try {
        if (useInternalProperties) {
          ConnectionPoolManager.createInstance(properties);
          connectionPoolManager = ConnectionPoolManager.getInstance();
        }
        else {
          connectionPoolManager = ConnectionPoolManager.getInstance();
        }
      }
      catch (Exception e) {
        throw new DataSourceUnavailableException("Unable to get initialize database pool", e);
      }
      Runtime.getRuntime().addShutdownHook(new ShutdownHook());
    }
  }

  public static Connection getConnection() throws DataSourceUnavailableException {
    init();
    try {
      Connection conn = connectionPoolManager.getConnection(POOL_NAME, DATABASE_TIMEOUT);
      if (conn == null) {
        throw new DataSourceUnavailableException(
            "Timeout occurred while waiting to get a connection to the database. All connections taken.");
      }
      return conn;
    }
    catch (SQLException sqle) {
      throw new DataSourceUnavailableException("Unable to get database connection", sqle);
    }
  }

  public static void close(Connection conn) {
    try {
      conn.close();
    }
    catch (SQLException sqle) {
      new DataSourceException("Unable to close connection", sqle, "close");
    }
  }

  public static class ShutdownHook extends Thread {
    public void run() {
      if (connectionPoolManager != null) {
        connectionPoolManager.release();
      }
    }
  }

  public static boolean isUseInternalProperties() {
    return useInternalProperties;
  }

  public static void setUseInternalProperties(boolean useInternalProperties) {
    DatabasePool.useInternalProperties = useInternalProperties;
  }

}
