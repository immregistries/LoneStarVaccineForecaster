package org.immregistries.lonestar.core.decisionLogic;

import java.util.HashSet;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.VaccineForecastDataBean.ValidVaccine;
import org.immregistries.lonestar.core.logic.DataStore;

public class Flu2015DecisionLogic extends DecisionLogic {

  public static final String VALID_VACCINE = "Valid Vaccine";

  public static final String TRANSITION_SECOND_DOSE_NEEDED = "Second Dose Needed";
  public static final String TRANSITION_NO_MORE_DOSES_NEEDED = "No More Doses Needed";

  @Override
  public String getTransition(DataStore ds) {
    final String secondDoseNeeded = getTransitionValue(TRANSITION_SECOND_DOSE_NEEDED);
    final String noMoreDosesNeeded = getTransitionValue(TRANSITION_NO_MORE_DOSES_NEEDED);

    final String validVaccine = getConstantValue(VALID_VACCINE);
    DateTime currentSeasonStart = ds.getSeasonStartDateTime();

    ds.log("Checking to see if a second dose is needed");

    ds.log("Checking for: Child is 9 years or older");
    DateTime nineYearsOld = new DateTime(ds.getPatient().getDobDateTime());
    nineYearsOld.addYears(9);
    if (nineYearsOld.isLessThanOrEquals(new DateTime(ds.getForecastDate()))) {
      return noMoreDosesNeeded;
    }

    ds.log("Setting up valid vaccines");
    ValidVaccine[] vaccines;
    try {
      vaccines = ds.getForecast().convertToVaccineIds(validVaccine);
    } catch (Exception e) {
      throw new RuntimeException("Unable to convert vaccine '" + validVaccine + "' to vaccine ids",
          e);
    }
    for (ValidVaccine v : vaccines) {
      ds.log(" + " + v.getVaccineId());
    }

    ds.log("Checking for: 2 valid doses before current season");
    if (countValidVaccinesGiven(ds, vaccines, null, currentSeasonStart) >= 2) {
      ds.getTraceList().addExplanation(
          "An additional dose is not needed because at least two valid doses were given before this current flu season.");
      return noMoreDosesNeeded;
    }

    ds.getTraceList().addExplanation("An additional flu dose is needed this season.");

    return secondDoseNeeded;
  }

  public int countValidVaccinesGiven(DataStore ds, ValidVaccine[] vaccines, DateTime startDate,
      DateTime endDate) {
    int count = 0;
    HashSet<String> dateSet = new HashSet<String>();
    for (VaccinationDoseDataBean vaccinationDose : ds.getDoseList()) {
      DateTime adminDate = new DateTime(vaccinationDose.getAdminDate());
      String dateStr = adminDate.getDate().toString();

      if ((startDate == null || startDate.isLessThanOrEquals(adminDate))
          && (endDate == null || endDate.isGreaterThan(adminDate))
          && !vaccinationDose.isStatusCodeInvalid()) {
        for (ValidVaccine vaccine : vaccines) {
          if (vaccinationDose.getVaccineId() == vaccine.getVaccineId()) {
            if (!(dateSet.contains(dateStr))) {
              dateSet.add(dateStr);
              ds.log(" + valid vaccine " + vaccinationDose.getVaccineId() + " given "
                  + new DateTime(vaccinationDose.getAdminDate()).toString("M/D/Y"));
              count++;
            }

            break;
          }
        }
      }
    }
    return count;
  }

}
