package org.immregistries.lonestar.core.model;

import java.util.Date;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.PatientForecastRecordDataBean;

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
  
  /* The redundancy is made to avoid refactoring of all referances in file showScheduleTable.jspf*/
  public Date getDob()
  {
    return dob == null ? null : dob.getDate();
  }

  public void setDob(DateTime dob)
  {
    this.dob = dob;
  }
}
