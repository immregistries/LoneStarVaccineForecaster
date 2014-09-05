package org.tch.forecast.core.decisionLogic;

import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.ValidVaccine;
import org.tch.forecast.core.logic.DataStore;
import org.tch.forecast.core.logic.Event;

public class Flu2014DecisionLogic extends DecisionLogic
{

  public static final String SEASON_2013_START = "Season 2013 Start";
  public static final String SEASON_2010_START = "Season 2010 Start";
  public static final String VALID_VACCINE = "Valid Vaccine";
  public static final String VALID_H1N1_VACCINE = "Valid H1N1 Vaccine";

  public static final String TRANSITION_SECOND_DOSE_NEEDED = "Second Dose Needed";
  public static final String TRANSITION_NO_MORE_DOSES_NEEDED = "No More Doses Needed";

  @Override
  public String getTransition(DataStore ds) {
    final String secondDoseNeeded = getTransitionValue(TRANSITION_SECOND_DOSE_NEEDED);
    final String noMoreDosesNeeded = getTransitionValue(TRANSITION_NO_MORE_DOSES_NEEDED);

    final DateTime season2013Start = new DateTime(getConstantValue(SEASON_2013_START));
    final DateTime season2010Start = new DateTime(getConstantValue(SEASON_2010_START));
    final String validVaccine = getConstantValue(VALID_VACCINE);
    final String validH1N1Vaccine = getConstantValue(VALID_H1N1_VACCINE);
    DateTime currentSeasonStart = ds.getSeasonStartDateTime();

    DateTime nineYearsOld = new DateTime(ds.getPatient().getDobDateTime());
    nineYearsOld.addYears(9);
    if (nineYearsOld.isLessThanOrEquals(new DateTime(ds.getForecastDate()))) {
      return noMoreDosesNeeded;
    }

    ValidVaccine[] vaccines;
    try {
      vaccines = ds.getForecast().convertToVaccineIds(validVaccine);
    } catch (Exception e) {
      throw new RuntimeException("Unable to convert vaccine '" + validVaccine + "' to vaccine ids", e);
    }

    ValidVaccine[] vaccinesH1N1;
    try {
      vaccinesH1N1 = ds.getForecast().convertToVaccineIds(validH1N1Vaccine);
    } catch (Exception e) {
      throw new RuntimeException("Unable to convert vaccine '" + validH1N1Vaccine + "' to vaccine ids", e);
    }

    // 1 valid dose before current season and since Season 2013 start
    if (countValidVaccinesGiven(ds, vaccines, season2013Start, currentSeasonStart) >= 1) {
      ds.getTraceList().addExplanation(
          "An additional dose is not needed because at least one valid dose was given since "
              + season2013Start.toString("M/D/Y") + " and before this current flu season.");
      return noMoreDosesNeeded;
    }

    // 2 valid doses before current season and since Season 2010 start
    if (countValidVaccinesGiven(ds, vaccines, season2010Start, currentSeasonStart) >= 2) {
      ds.getTraceList().addExplanation(
          "An additional dose is not needed because at least two valid doses were given since "
              + season2010Start.toString("M/D/Y") + " and before this current flu season.");
      return noMoreDosesNeeded;
    }

    // 2 valid doses before Season 2010 Start AND 1 valid dose of 2009 H1N1 vaccine
    if (countValidVaccinesGiven(ds, vaccines, null, season2010Start) >= 2
        && countValidVaccinesGiven(ds, vaccinesH1N1, null, null) >= 1) {
      ds.getTraceList().addExplanation(
          "An additional dose is not needed because at least two valid doses were given before "
              + season2010Start.toString("M/D/Y") + " and at least one dose of H1N1 novel vaccine.");
      return noMoreDosesNeeded;
    }

    // 1 valid dose before Season 2010 Start AND 1 valid dose before current season and since Season 2010 Start
    if (countValidVaccinesGiven(ds, vaccines, null, season2010Start) >= 1
        && countValidVaccinesGiven(ds, vaccines, season2010Start, currentSeasonStart) >= 1) {
      ds.getTraceList().addExplanation(
          "An additional dose is not needed because at least one valid dose were given before "
              + season2010Start.toString("M/D/Y") + " and at least one dose after " + season2010Start.toString("M/D/Y")
              + " and before current season.");
      return noMoreDosesNeeded;
    }

    ds.getTraceList().addExplanation("An additional flu dose is needed this season.");

    return secondDoseNeeded;
  }

  public int countValidVaccinesGiven(DataStore ds, ValidVaccine[] vaccines, DateTime startDate, DateTime endDate) {
    int count = 0;
    for (VaccinationDoseDataBean vaccinationDose : ds.getDoseList()) {
      DateTime adminDate = new DateTime(vaccinationDose.getAdminDate());
      if ((startDate == null || startDate.isLessThanOrEquals(adminDate))
          && (endDate == null || endDate.isGreaterThan(adminDate))) {
        for (ValidVaccine vaccine : vaccines) {
          if (vaccinationDose.getVaccineId() == vaccine.getVaccineId()) {
            count++;
            break;
          }
        }
      }
    }
    return count;
  }

}