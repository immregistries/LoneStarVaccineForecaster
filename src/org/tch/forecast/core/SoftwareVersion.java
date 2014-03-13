package org.tch.forecast.core;

public class SoftwareVersion
{

  public static final String VERSION_MAJOR = "3";
  public static final String VERSION_MINOR = "5";
  public static final String VERSION_PATCH = "8";
  public static final String VERSION_RELEASE = "20140311";

  public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH;

  public static void main(String[] args) {
    System.out.println("TCH Forecaster Version " + VERSION);
  }
}
