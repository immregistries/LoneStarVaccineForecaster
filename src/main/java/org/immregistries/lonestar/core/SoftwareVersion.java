package org.immregistries.lonestar.core;

public class SoftwareVersion {

  // Command to check the version

  // javap -constants -classpath lsv-forecaster-test.jar org.immregistries.lonestar.core.SoftwareVersion
  public static final String VERSION_MAJOR = "4";
  public static final String VERSION_MINOR = "2";
  public static final String VERSION_PATCH = "1";
  public static final String VERSION_RELEASE = "20180908";

  public static final String VERSION = VERSION_MAJOR + "." + VERSION_MINOR + "." + VERSION_PATCH;

  public static void main(String[] args) {
    System.out.println(
        "Lone Star Vaccine Forecaster Version " + VERSION + " released " + VERSION_RELEASE);
  }

}
