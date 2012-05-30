package org.tch.forecast.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ForecastServer {

  private ServerSocket serverSocket;
  // java -classpath tch-forecaster.jar org.tch.forecast.core.server.ForecastServer [port num]
  
  public ForecastServer(int port) throws IOException
  {
    System.out.println("Starting Forecast Server...");
    serverSocket = new ServerSocket(port);
    System.out.println("Connected on port " + port);
    while (true)
    {
      Socket socket = serverSocket.accept();
      System.out.println("Received request from local port " + socket.getLocalPort());
      ForecastHandler forecastHandler = new ForecastHandler(socket);
      forecastHandler.start();
    }
  }
  
  public static final int DEFAULT_PORT = 6708;
  
  public static void main(String[] args) throws IOException {
    int port = DEFAULT_PORT;
    if (args.length > 0)
    {
      boolean problem;
      try {
        port = Integer.parseInt(args[0]);
        problem = false;
      }
      catch (NumberFormatException nfe)
      {
        problem = true;
        // Do nothing
      }
      if (problem)
      {
        System.err.println("Unrecognized port number " + args[0]);
      }
      if (port <= 0 || port > 65535)
      {
        System.err.println("Invalid port number " + args[0]);
        port = DEFAULT_PORT;
      }
    }
    new ForecastServer(port);
  }
}
