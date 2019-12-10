package org.immregistries.lonestar.core.api.impl;

import java.util.Date;
import org.immregistries.lonestar.core.api.model.ForecastPatientInterface;

public class ForecastPatient implements ForecastPatientInterface {
  private Date birthDate = null;
  private String sex = "";
  
  public Date getBirthDate() {
    return birthDate;
  }
  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }
  public String getSex() {
    return sex;
  }
  public void setSex(String sex) {
    this.sex = sex;
  }
}
