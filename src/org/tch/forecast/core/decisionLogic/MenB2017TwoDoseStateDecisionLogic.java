package org.tch.forecast.core.decisionLogic;

import java.util.List;

import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.logic.DataStore;

public class MenB2017TwoDoseStateDecisionLogic extends MenB2017DecisionLogic {

  /**
   * Determines which schedule to transition to next.
   */
  @Override
  public String getTransition(DataStore ds) {
    init(ds);
    ds.log("Start DL MenB Decision Logic for Two Dose State");

    List<VaccinationDoseDataBean> validVaccinations = ds.getDoseList();
    
    // count Trumenba and Bexsero doses
    int numBexsero = countBexseroVaccinations(ds, validVaccinations);
    int numTrumenba = countTrumenbaVaccinations(ds, validVaccinations);

    ds.log(" + Currently examining " + numBexsero + " Bexsero dose(s)");
    ds.log(" + Currently examining " + numTrumenba + " Trumenba dose(s)");

    if ( isSixMonthsBetweenFirstAndSecondTrumenba(ds, validVaccinations) ) {
      ds.log(" + 1st Trumenba dose was received MORE than 6 months before 2nd Trumenba dose.");
      return getTransitionValue(GREATER_THAN_SIX_MONTHS);
    }
    else {
      ds.log(" + 1st Trumenba dose was received LESS than 6 months before 2nd Trumenba dose.");
      return getTransitionValue(LESS_THAN_SIX_MONTHS);
    }
  }
}
