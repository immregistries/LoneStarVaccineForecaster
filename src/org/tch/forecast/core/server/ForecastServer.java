package org.tch.forecast.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import org.tch.forecast.core.api.impl.CvxCodes;
import org.tch.forecast.core.api.impl.VaccineForecastManager;

public class ForecastServer {

  private ServerSocket serverSocket;
  // java -classpath tch-forecaster.jar
  // org.tch.forecast.core.server.ForecastServer [port num]

  public static final String TEST_1 = "09052012^R^^TEST123^^01012012^M^^^^^^^^^^^^^^^^^^^^^1^TEST456^50^03132012^^^^";

  protected static VaccineForecastManager vaccineForecastManager = null;
  protected static Map<String, Integer> cvxToVaccineIdMap = null;

  public ForecastServer(int port) throws IOException {
    System.out.println("Starting TCH Forecast Server");

    try {
      System.out.println("  + loading forecaster");
      vaccineForecastManager = new VaccineForecastManager();
      System.out.println("  + loading cvx codes");
      cvxToVaccineIdMap = CvxCodes.getCvxToVaccineIdMap();
      System.out.println("Testing forecaster to ensure that it works");
      System.out.print("  + Test 1: ");
      CaretForecaster caretForecaster = new CaretForecaster(TEST_1);
      String response = caretForecaster.forecast(vaccineForecastManager, cvxToVaccineIdMap);
      if (response.length() > 20) {
        System.out.println("pass");
      } else {
        System.out.println("fail");
      }

    } catch (Exception e) {
      System.out.println("fail");
      System.out.println("Unable to start forecaster because " + e.getMessage());
      e.printStackTrace();
      return;
    }
    serverSocket = new ServerSocket(port);
    System.out.println("Connected on port " + port);
    while (true) {
      Socket socket = serverSocket.accept();
      System.out.println("Received request from local port " + socket.getLocalPort());
      ForecastHandler forecastHandler = new ForecastHandler(socket);
      forecastHandler.start();
    }
  }

  public static final int DEFAULT_PORT = 6708;

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
    new ForecastServer(port);
  }
}
