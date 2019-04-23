package org.immregistries.lonestar.core;

public class Transition
{
  private String name = "";
  private TimePeriod age = null;
  private int vaccineId = 0;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public TimePeriod getAge() {
    return age;
  }

  public void setAge(TimePeriod age) {
    this.age = age;
  }

  public int getVaccineId() {
    return vaccineId;
  }

  public void setVaccineId(int vaccineId) {
    this.vaccineId = vaccineId;
  }
}