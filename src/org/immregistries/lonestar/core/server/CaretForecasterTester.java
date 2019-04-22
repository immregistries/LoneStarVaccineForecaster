package org.immregistries.lonestar.core.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class CaretForecasterTester
{
  private static String QUERY = "20140424^0^0^0^0^CREYG,ARLIE  Chart#: 00-00-31^31^19830215^Male^U^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^0^~~~3484^110^19830215^0^0^0|||";

  private static String hostName = "localhost";
  private static int threadCount = 1;
  private static int portNumber = 6708;
  private static int repeatCount = 100000;
  private static String versionString = null;

  // java -classpath deploy/lsv-forecaster.jar org.immregistries.lonestar.core.server.CaretForecasterTester 100 5
  public static void main(String[] args) throws Exception {
    String problem = null;
    if (args.length == 0) {
      problem = "You must specify the repeat count";
    } else {
      try {
        repeatCount = Integer.parseInt(args[0]);
      } catch (NumberFormatException nfe) {
        problem = "Unrecognized repeat count";
      }
      if (args.length > 1) {
        try {
          threadCount = Integer.parseInt(args[1]);
        } catch (NumberFormatException nfe) {
          problem = "Unrecognized thread count";
        }
        if (portNumber <= 0) {
          problem = "Invalid thread count";
        }
        if (args.length > 2) {
          hostName = args[2];
          if (args.length > 3) {
            try {
              portNumber = Integer.parseInt(args[3]);
            } catch (NumberFormatException nfe) {
              problem = "Unrecognized port number";
            }
            if (portNumber <= 0) {
              problem = "Invalid port number";
            }
          }
        }
      }
    }
    if (problem != null) {
      System.err.println("Unable to start tester: " + problem);
      System.err
          .println("usage: java -classpath lsv-forecaster.jar org.immregistries.lonestar.core.server.CaretForecasterTester repeat-count [thread-count [host-name [port-number]]]");
    }
    System.out.println("Starting IHS Caret Forecast Tester");
    System.out.println("  + thread count: " + threadCount);
    System.out.println("  + host name   : " + hostName);
    System.out.println("  + port number : " + portNumber);
    System.out.println("Will send one request after another, press Ctrl-C to break");

    for (int i = 1; i <= threadCount; i++) {
      TestThread testThread = new TestThread(threadCount, repeatCount);
      testThread.start();
    }
  }

  private static class TestThread extends Thread
  {
    private int id = 0;
    private int count = 0;

    public TestThread(int id, int count) {
      this.id = id;
      this.count = count;
    }

    @Override
    public void run() {
      boolean goodToRun = true;
      int countUp = 1;
      long startTime = System.currentTimeMillis();
      while (goodToRun && countUp <= count) {
        try {
          Socket socket = new Socket(hostName, portNumber);
          PrintStream request = new PrintStream(socket.getOutputStream());
          request.println(QUERY);

          goodToRun = false;
          InputStreamReader response = null;
          response = new InputStreamReader(socket.getInputStream());
          BufferedReader in = new BufferedReader(response);
          String line;
          while ((line = in.readLine()) != null) {
            if (line.indexOf("&&&Lone Star Vaccine Forecaster version") != -1) {
              if (CaretForecasterTester.versionString == null) {
                int startPos = line.indexOf("&&&Lone Star Vaccine Forecaster version") + 3;
                int endPos = line.indexOf("^", startPos);
                if (endPos != -1) {
                  CaretForecasterTester.versionString = line.substring(startPos, endPos);
                  System.out.println("Connecting to " + CaretForecasterTester.versionString);
                }
              }
              goodToRun = true;
              break;
            } else {
              System.out.println("Problem, output not expected:");
              System.out.println(line);
            }
          }
          request.close();
          response.close();
          socket.close();
          countUp++;
        } catch (Exception e) {
          System.out.println("Exception generated: " + e.getMessage());
          e.printStackTrace(System.out);
          goodToRun = false;
        }
      }
      if (goodToRun) {
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(" - Thread " + id + " OK " + totalTime + " ms for " + count + " requests");
      }
    }
  }
}
