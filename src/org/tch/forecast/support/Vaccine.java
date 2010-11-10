package org.tch.forecast.support;

import java.io.Serializable;

public class Vaccine implements Serializable
{
  

  public Vaccine()
  {
    // default
  }
  
  public Vaccine(int vaccineId, String fcabbrev)
  {
    vaccineID = vaccineId;
    fcAbrrev = fcabbrev;
    // convienence method for testing
  }
  /**
   * Insert the method's description here. Creation date: (12/12/2001 10:30:12 AM)
   * @return java.lang.String
   */
  public String getFcAbrrev()
  {
    return fcAbrrev;
  }

  /**
   * Insert the method's description here. Creation date: (12/12/2001 10:30:12 AM)
   * @param newFcAbrrev String
   */
  public void setFcAbrrev(String newFcAbrrev)
  {
    fcAbrrev = newFcAbrrev;
  }
  
  public static final int VARICELLA_HISTORY = 378;
  public static final int UNKNOWN = 9999;
  
  private int vaccineID;
  private String displayName = null;
  private String desc = null; //as per 01.29.02 no type
  private String status = null;
  private int familyID;
  private String fcAbrrev = null;
  private boolean visible = false;

  
  public String toString()
  {
    if (displayName != null && !"".equals(displayName))
    {
      return vaccineID + ": " + displayName;
    }
    return "" + vaccineID;
  }

  /**
   * Insert the method's description here. Creation date: (1/29/2002 4:52:03 PM)
   * @return String
   */
  public String getDesc()
  {
    return desc;
  }

  /**
   * Insert the method's description here. Creation date: (12/12/2001 10:30:22 AM)
   * @return String
   */
  public String getDisplayName()
  {
    return displayName;
  }

  /**
   * Insert the method's description here. Creation date: (1/29/2002 4:52:03 PM)
   * @return int
   */
  public int getFamilyID()
  {
    return familyID;
  }

  /**
   * Insert the method's description here. Creation date: (1/30/2002 1:09:43 PM)
   * @return String
   */
  public String getStatus()
  {
    return status;
  }

  /**
   * Insert the method's description here. Creation date: (12/12/2001 10:30:22 AM)
   * @return int
   */
  public int getVaccineID()
  {
    return vaccineID;
  }

  /**
   * Insert the method's description here. Creation date: (1/29/2002 4:52:03 PM)
   * @param newDesc String
   */
  public void setDesc(String newDesc)
  {
    desc = newDesc;
  }

  /**
   * Insert the method's description here. Creation date: (12/12/2001 10:30:22 AM)
   * @param newDisplayName String
   */
  public void setDisplayName(String newDisplayName)
  {
    displayName = newDisplayName;
  }

  /**
   * Insert the method's description here. Creation date: (1/29/2002 4:52:03 PM)
   * @param newFamilyID int
   */
  public void setFamilyID(int newFamilyID)
  {
    familyID = newFamilyID;
  }

  /**
   * Insert the method's description here. Creation date: (1/30/2002 1:09:43 PM)
   * @param newStatus String
   */
  public void setStatus(String newStatus)
  {
    status = newStatus;
  }

  /**
   * Insert the method's description here. Creation date: (12/12/2001 10:30:22 AM)
   * @param newVaccineID int
   */
  public void setVaccineID(int newVaccineID)
  {
    vaccineID = newVaccineID;
  }
  public boolean isVisible()
  {
    return this.visible;
  }
  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }
}