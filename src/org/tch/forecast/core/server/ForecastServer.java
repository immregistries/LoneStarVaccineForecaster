package org.tch.forecast.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.tch.forecast.core.api.impl.CvxCodes;
import org.tch.forecast.core.api.impl.VaccineForecastManager;

public class ForecastServer extends Thread
{

  private ServerSocket serverSocket;
  // java -classpath tch-forecaster.jar org.tch.forecast.core.server.ForecastServer [port num]

  public static final String[] TEST = {
      "20120905^R^^^^TEST123^^20120101^M^^^^^^^^^^^^^^^^^^^^^~~~TEST456^50^20120313^^^^|||",
      "20131118^R^IHS_6m26^0^0^FURRAST,JOHN DELBERT  Chart#: 00-00-55^55^19571122^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~2272^20^20080118^0^0^0|||2273^20^20080122^0^0^0|||2271^21^20080118^0^0^0|||2663^111^20081212^0^0^0|||",
      "20131126^A^IHS_6m26^0^0^FURRAST,JOHN DELBERT  Chart#: 00-00-55^55^19571122^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~2272^20^20080118^0^0^0|||2273^20^20080122^0^0^0|||2271^21^20080118^0^0^0|||2663^111^20081212^0^0^0|||^" };
  // java -classpath tch-forecaster.jar org.tch.forecast.core.server.CaretForecaster

  protected static VaccineForecastManager vaccineForecastManager = null;
  protected static Map<String, Integer> cvxToVaccineIdMap = null;

  private int port = DEFAULT_PORT;
  private boolean debug = false;

  public boolean isDebug() {
    return debug;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public ForecastServer() {
    // default
  }

  public ForecastServer(int port) {
    this.port = port;
  }

  private static final String LOG_PRE = "TCH Forecaster: ";

  @Override
  public void run() {
    System.out.println(LOG_PRE + "Starting");

    try {
      System.out.println(LOG_PRE + "  + loading forecaster core");
      vaccineForecastManager = new VaccineForecastManager();
      System.out.println(LOG_PRE + "  + loading cvx codes");
      cvxToVaccineIdMap = CvxCodes.getCvxToVaccineIdMap();
      System.out.println(LOG_PRE + "Testing");

      for (int i = 0; i < TEST.length; i++) {
        System.out.print(LOG_PRE + "  + Test " + (i + 1) + ": ");
        CaretForecaster caretForecaster = new CaretForecaster(TEST[i]);
        String response = caretForecaster.forecast(vaccineForecastManager, cvxToVaccineIdMap);
        if (response.length() > 20) {
          System.out.println("pass");
        } else {
          System.out.println("fail");
        }
      }
    } catch (Exception e) {
      System.out.println("fail");
      System.out.println(LOG_PRE + "Unable to start forecaster because " + e.getMessage());
      e.printStackTrace();
      return;
    }
    try {
      serverSocket = new ServerSocket(port);
      System.out.println("Connected on port " + port);
      while (true) {
        Socket socket = serverSocket.accept();
        if (debug) {
          System.out.println(LOG_PRE + "Received request from local port " + socket.getLocalPort());
        }
        ForecastHandler forecastHandler = new ForecastHandler(socket, this);
        forecastHandler.start();
      }
    } catch (IOException e) {
      System.out.println("Unable to listen on port " + port + ", shutting down TCH Foreacast Server");
      e.printStackTrace();
    }
  }

  protected void log(String message) {
    System.out.println(LOG_PRE + message);
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
    }
    ForecastServer forecastServer = new ForecastServer(port);
    forecastServer.start();
  }
}
