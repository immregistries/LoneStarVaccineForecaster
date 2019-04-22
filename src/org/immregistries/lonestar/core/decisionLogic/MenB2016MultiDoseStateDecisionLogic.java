package org.immregistries.lonestar.core.decisionLogic;

import java.util.List;

import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.logic.DataStore;

public class MenB2016MultiDoseStateDecisionLogic extends MenB2016DecisionLogic {

  /**
   * Determines which schedule to transition to next.
   * 
   * This method is only entered if there are both Bexsero and Trumenba doses present.
   */
  @Override
  public String getTransition(DataStore ds) {
    init(ds);
    ds.log("Start DL MenB Decision Logic for Multiple Dose State");
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

    // there are exactly 2 Trumenba doses
    ds.log("There are 2 doses of Trumenba");
    if ( ! fourMonthsHavePastSinceSecondDoseOfTrumena(ds, validVaccinations) ) {
      ds.log(" + There are 2 Trumenba doses and less than 4 months have passed since the 2nd dose.");
      return "B2b";
    }
    else if ( ! sixMonthsHavePastSinceFirstDoseOfTrumena(ds, validVaccinations) ) {
      ds.log(" + There are 2 Trumenba doses and less than 6 months have passed since the 1st dose.");
      return "B2b";
    }
    else {
      ds.log(" + There are 2 Trumenba doses and 4 months have passed since the 2nd dose and 6 months have passed since 1st dose.");
      return "M-final";
    }
  }
}
