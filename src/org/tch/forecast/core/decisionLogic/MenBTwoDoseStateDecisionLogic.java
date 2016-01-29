package org.tch.forecast.core.decisionLogic;

import java.util.List;

import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.logic.DataStore;

public class MenBTwoDoseStateDecisionLogic extends MenBDecisionLogic {

  /**
   * Determines which schedule to transition to next.
   * 
   * This method is only entered if there are both Bexsero and Trumenba doses present.
   */
  @Override
  public String getTransition(DataStore ds) {
    init(ds);
    ds.log("Start DL MenB Decision Logic for Two Dose State");
//    final String secondDoseNeeded = getTransitionValue(TRANSITION_SECOND_DOSE_NEEDED);
//    final String finalDoseNeeded = getTransitionValue(TRANSITION_FINAL_DOSE_NEEDED);
//    final String noMoreDosesNeeded = getTransitionValue(TRANSITION_NO_MORE_DOSES_NEEDED);
//    final String nextBexseroDoseNeeded = "B2";//getConstantValue(VALID_VACCINE);

    List<VaccinationDoseDataBean> validVaccinations = ds.getDoseList();
    
    // count Trumenba and Bexsero doses
    int numBexsero = countBexseroVaccinations(ds, validVaccinations);
    int numTrumenba = countTrumenbaVaccinations(ds, validVaccinations);

    ds.log(" + Currently examining " + numBexsero + " Bexsero dose(s)");
    ds.log(" + Currently examining " + numTrumenba + " Trumenba dose(s)");

    if ( isFirstDoseOfTrumenbaReceivedMoreThanEightWeeksAgo(ds, validVaccinations) ) {
      ds.log(" + Trumenba dose was received more than 8 weeks ago.");
      return "M2";//secondDoseNeeded;
    }
    else {
      ds.log(" + Trumenba dose was received less than 8 weeks ago.");
      return "B2a";
    }
  }
}
