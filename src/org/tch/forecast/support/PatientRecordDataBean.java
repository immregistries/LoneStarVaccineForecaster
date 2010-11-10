package org.tch.forecast.support;

import org.tch.forecast.core.PatientForecastRecordDataBean;
import org.tch.hl7.core.util.DateTime;

public class PatientRecordDataBean implements PatientForecastRecordDataBean
{

  private DateTime dob = null;
  private String sex = "";
  private int immregid = 0;

  public int getImmregid()
  {
    return immregid;
  }

  public void setImmregid(int immregid)
  {
    this.immregid = immregid;
  }

  public String getSex()
  {
    return sex;
  }

  public void setSex(String sex)
  {
    this.sex = sex;
  }

  public DateTime getDobDateTime()
  {
    return dob;
  }

  public void setDob(DateTime dob)
  {
    this.dob = dob;
  }
}
