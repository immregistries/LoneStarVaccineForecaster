package org.immregistries.lonestar.core.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.immregistries.lonestar.core.api.impl.CvxCode;
import org.immregistries.lonestar.core.api.impl.CvxCodes;
import org.immregistries.lonestar.core.api.impl.VaccineForecastManager;

public class ForecastServer extends Thread {

  private ServerSocket serverSocket;
  // java -classpath lsv-forecaster.jar org.immregistries.lonestar.core.server.ForecastServer [port num]

  public static final String[] TEST = {
      "20120905^R^^^^TEST123^^20120101^M^^^^^^^^^^^^^^^^^^^^^~~~TEST456^50^20120313^^^^|||",
      "20131118^R^IHS_6m26^0^0^FURRAST,JOHN DELBERT  Chart#: 00-00-55^55^19571122^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~2272^20^20080118^0^0^0|||2273^20^20080122^0^0^0|||2271^21^20080118^0^0^0|||2663^111^20081212^0^0^0|||",
      "20131126^A^IHS_6m26^0^0^FURRAST,JOHN DELBERT  Chart#: 00-00-55^55^19571122^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~2272^20^20080118^0^0^0|||2273^20^20080122^0^0^0|||2271^21^20080118^0^0^0|||2663^111^20081212^0^0^0|||^",
      "20131118^R^IHS_6m26^0^0^FURRAST,JOHN DELBERT  Chart#: 00-00-55^55^19571122^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~2272^149^20131018^0^0^0|||",
      "20120905^R^^^^TEST123^^20020101^M^^^^^^^^^^^^^^^^^^^^^~~~TEST456^50^20120313^^^^|||",
      "20140201^R^IHS_6m26^0^0^^55^19481128^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~55079^9^19990706^0^0^0|||180404^115^20110504^0^0^0|||55078^45^19990706^0^0^0|||183899^33^20060101^0^0^0"};
  // java -classpath lsv-forecaster.jar org.immregistries.lonestar.core.server.CaretForecaster

  protected static VaccineForecastManager vaccineForecastManager = null;
  protected static Map<String, Integer> cvxToVaccineIdMap = null;

  private int port = DEFAULT_PORT;
  private boolean debug = false;
  private StringBuilder startupProcessLog = new StringBuilder();
  private StringBuilder runningProcessLog = new StringBuilder();

  private static boolean runGarbageCollectionWhenDone = false;

  public static boolean isRunGarbageCollectionWhenDone() {
    return runGarbageCollectionWhenDone;
  }

  public static void setRunGarbageCollectionWhenDone(boolean runGarbageCollectionWhenDone) {
    ForecastServer.runGarbageCollectionWhenDone = runGarbageCollectionWhenDone;
  }



  public String getProcessLog() {
    return startupProcessLog.toString();
  }

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public ForecastServer() {
    setName(getName() + ": Main ForecastServer Thread");
    // default
  }

  public ForecastServer(int port) {
    this.port = port;
  }

  private static final String LOG_PRE = "Lone Star Vaccine Forecaster: ";

  @Override
  public void run() {
    logStartupLn("Starting");

    try {
      logStartupLn("  + loading forecaster core");
      vaccineForecastManager = new VaccineForecastManager();
      logStartupLn("  + loading cvx codes");
      Map<String, CvxCode> cvxToCvxCodeMap = CvxCodes.getCvxToCvxCodeMap();
      cvxToVaccineIdMap = new HashMap<String, Integer>();
      for (CvxCode cvxCode : cvxToCvxCodeMap.values()) {
        cvxToVaccineIdMap.put(cvxCode.getCvxCode(), cvxCode.getVaccineId());
      }
      logStartupLn("Testing");

      for (int i = 0; i < TEST.length; i++) {
        logStartup(LOG_PRE + "  + Test " + (i + 1) + ": ");
        CaretForecaster caretForecaster = new CaretForecaster(TEST[i]);
        String response = caretForecaster.forecast(vaccineForecastManager, cvxToVaccineIdMap);
        if (response.length() > 20) {
          logStartupLn("pass");
        } else {
          logStartupLn("fail");
        }
      }
    } catch (Exception e) {
      logStartupLn("fail");
      logStartupLn("Unable to start forecaster because " + e.getMessage());
      logStartup(e);
      return;
    }
    try {
      serverSocket = new ServerSocket(port);
      logStartupLn("Connected on port " + port);
      while (true) {
        Socket socket = serverSocket.accept();
        if (debug) {
          logRunningLn("Received request from local port " + socket.getLocalPort());
        }
        ForecastHandler forecastHandler = new ForecastHandler(socket, this);
        forecastHandler.start();
      }
    } catch (IOException e) {
      logStartupLn("Unable to listen on port " + port
          + ", shutting down Lone Star Vaccine Foreacast Server");
      logStartup(e);
    } catch (Throwable e) {
      logStartupLn("Unable to listen on port " + port
          + ", shutting down Lone Star Vaccine Foreacast Server");
      logStartup(e);
    } finally {
      logStartupLn("Shutting down forecater");
    }
  }

  protected void logStartupLn(String message) {
    System.out.println(LOG_PRE + message);
    startupProcessLog.append(message);
    startupProcessLog.append("\n");
  }

  protected void logStartup(String message) {
    System.out.print(LOG_PRE + message);
    startupProcessLog.append(message);
  }

  protected void logStartup(Throwable throwable) {
    throwable.printStackTrace(System.out);
    throwable.printStackTrace();
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    startupProcessLog.append(printWriter.toString());
  }

  private long lastClearTime = 0;
  private static final long ONE_HOUR = 1000 * 60 * 60;

  protected void logRunningLn(String message) {
    System.out.print(LOG_PRE + message);
    resetProcessingLog();
    runningProcessLog.append(message);
    runningProcessLog.append("\n");
  }

  private void resetProcessingLog() {
    if (System.currentTimeMillis() - lastClearTime > ONE_HOUR) {
      runningProcessLog = new StringBuilder();
      lastClearTime = System.currentTimeMillis();
      SimpleDateFormat sdf = new SimpleDateFormat("M/d/y h:m:s");
      runningProcessLog.append("Running process log reset at " + sdf.format(new Date()));
      runningProcessLog.append("\n");
    }
  }

  protected void logRunning(Throwable throwable) {
    throwable.printStackTrace(System.out);
    throwable.printStackTrace();
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    throwable.printStackTrace(printWriter);
    runningProcessLog.append(printWriter.toString());
  }

  public static final int DEFAULT_PORT = 6708;

  public void close() throws IOException {
    if (serverSocket != null) {
      serverSocket.close();
    }
  }

  public static void main(String[] args) throws IOException {
    int port = DEFAULT_PORT;
    if (args.length > 0) {
      boolean problem;
      try {
        port = Integer.parseInt(args[0]);
        problem = false;
      } catch (NumberFormatException nfe) {
        problem = true;
        // Do nothing
      }
      if (problem) {
        System.err.println("Unrecognized port number " + args[0]);
      }
      if (port <= 0 || port > 65535) {
        System.err.println("Invalid port number " + args[0]);
        port = DEFAULT_PORT;
      }
      if (args.length > 1 && args[1].equalsIgnoreCase("gc")) {
        runGarbageCollectionWhenDone = true;
      }
    }
    ForecastServer forecastServer = new ForecastServer(port);
    forecastServer.start();
  }
}
