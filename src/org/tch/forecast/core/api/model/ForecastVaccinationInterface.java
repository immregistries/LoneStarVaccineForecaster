package org.tch.forecast.core.api.model;

import java.util.Date;

public interface ForecastVaccinationInterface {
  /**
   * The date the vaccination was administered. This is required. If the date is
   * not know then the vaccination should not be included in the evaluation.
   * 
   * @return
   */
  public Date getAdminDate();

  /**
   * The date the vaccination was administered. This is required. If the date is
   * not know then the vaccination should not be included in the evaluation.
   * 
   * @param adminDate
   */
  public void setAdminDate(Date adminDate);

  /**
   * The CDC assigned CVX code for the vaccination. This is required. If the
   * code is not known then the vaccination should not be included in the
   * evaluation. For the list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @return
   */
  public String getCvxCode();

  /**
   * The CDC assigned CVX code for the vaccination. This is required. If the
   * code is not known then the vaccination should not be included in the
   * evaluation. For the list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @param cvxCode
   */
  public void setCvxCode(String cvxCode);

  /**
   * The CDC assigned MVX code for the manufacturer. This is optional. For the
   * list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @return
   */
  public String getMvxCode();

  /**
   * The CDC assigned MVX code for the manufacturer. This is optional. For the
   * list of the most current CVX codes please see <a
   * href="http://www.cdc.gov/vaccines/programs/iis/code-sets.html">CDC Code
   * Sets</a>
   * 
   * @param mvxCode
   */
  public void setMvxCode(String mvxCode);
}
