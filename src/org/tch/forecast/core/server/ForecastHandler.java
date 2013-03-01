package org.tch.forecast.core.server;

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

  public ForecastHandler(Socket socket) {
    this.socket = socket;
  }

  public void run() {
    setup();
    if (input == null || output == null) {
      return;
    }
    try {
      BufferedReader in = new BufferedReader(new InputStreamReader(input));
      String request = in.readLine();
      System.out.println("  Request: " + request);
      String response = "";
      try {
        CaretForecaster caretForecaster = new CaretForecaster(request);
        response = caretForecaster.forecast(ForecastServer.vaccineForecastManager, ForecastServer.cvxToVaccineIdMap);
      } catch (Exception e) {
        response = "Unexpected problem: " + e.getMessage();
        e.printStackTrace();
      }
      System.out.println("  Response: " + response);
      output.println(response);
    } catch (IOException ioe) {
      System.err.println("Unable to process request: " + ioe.getMessage());
      ioe.printStackTrace(System.err);
    } finally {
      close();
    }
  }

  private void close() {
    try {
      output.close();
      input.close();
      socket.close();
    } catch (IOException ioe) {
      System.err.println("Unable to close outputs: " + ioe.getMessage());
      ioe.printStackTrace(System.err);
    }
  }

  private void setup() {
    try {
      input = new DataInputStream(socket.getInputStream());
    } catch (IOException ioe) {
      System.err.println("Unable to create input stream: " + ioe.getMessage());
      ioe.printStackTrace(System.err);
    }
    try {
      output = new PrintStream(socket.getOutputStream());
    } catch (IOException ioe) {
      System.err.println("Unable to create output stream: " + ioe.getMessage());
      ioe.printStackTrace(System.err);
    }
  }

}
