package org.tch.forecast.core;

public class SoftwareVersion
{

  // Command to check the version
  
  // javap -constants -classpath tch-forecaster-test.jar org.tch.forecast.core.SoftwareVersion
  public static final String VERSION_MAJOR = "3";
  public static final String VERSION_MINOR = "14";
  public static final String VERSION_PATCH = "03";
  public static final String VERSION_RELEASE = "20160902";

  public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH;

  public static void main(String[] args) {
    System.out.println("TCH Forecaster Version " + VERSION + " released " + VERSION_RELEASE);
  }
}
