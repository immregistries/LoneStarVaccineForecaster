package org.immregistries.lonestar.core.server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class ForecastHandler extends Thread {
  private Socket socket;
  private DataInputStream input = null;
  private PrintStream output = null;
  private ForecastServer forecastServer = null;


  public ForecastHandler(Socket socket, ForecastServer forecastServer) {
    this.socket = socket;
    this.forecastServer = forecastServer;

  }

  public void run() {
    setup();
    if (input == null || output == null) {
      return;
    }
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(input));
      String request = in.readLine();
      if (forecastServer.isDebug()) {
        forecastServer.logStartupLn("  Request: " + request);
      }
      String response = "";
      try {
        CaretForecaster caretForecaster = new CaretForecaster(request);
        response = caretForecaster.forecast(ForecastServer.vaccineForecastManager,
            ForecastServer.cvxToVaccineIdMap);
      } catch (Exception e) {
        response = "Unexpected problem: " + e.getMessage();
        e.printStackTrace();
      }
      if (forecastServer.isDebug()) {
        forecastServer.logStartupLn("  Response: " + response);
      }
      output.println(response);
    } catch (IOException ioe) {
      forecastServer.logStartupLn("Unable to process request: " + ioe.getMessage());
      ioe.printStackTrace();
    } finally {
      close();
    }
    if (ForecastServer.isRunGarbageCollectionWhenDone()) {
      Runtime.getRuntime().gc();
    }
  }

  private void close() {
    try {
      output.close();
      input.close();
      socket.close();
    } catch (IOException ioe) {
      forecastServer.logStartupLn("Unable to close outputs: " + ioe.getMessage());
      ioe.printStackTrace();
    }
  }

  private void setup() {
    try {
      input = new DataInputStream(socket.getInputStream());
    } catch (IOException ioe) {
      forecastServer.logStartupLn("Unable to create input stream: " + ioe.getMessage());
      ioe.printStackTrace();
    }
    try {
      output = new PrintStream(socket.getOutputStream());
    } catch (IOException ioe) {
      forecastServer.logStartupLn("Unable to create output stream: " + ioe.getMessage());
      ioe.printStackTrace();
    }
  }

}
