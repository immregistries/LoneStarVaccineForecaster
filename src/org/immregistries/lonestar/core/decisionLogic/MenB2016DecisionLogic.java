package org.immregistries.lonestar.core.decisionLogic;

import java.util.ArrayList;
import java.util.List;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.VaccineForecastDataBean.ValidVaccine;
import org.immregistries.lonestar.core.logic.DataStore;

public abstract class MenB2016DecisionLogic extends DecisionLogic {

  public static final String VALID_VACCINE = "Valid Vaccine";

  public static final String TRANSITION_SECOND_DOSE_NEEDED = "Second Dose Needed";
  public static final String TRANSITION_FINAL_DOSE_NEEDED = "Final Dose Needed";
  public static final String TRANSITION_NO_MORE_DOSES_NEEDED = "No More Doses Needed";

  protected static final String BEXSERO = "Bexsero";
  protected static final String TRUMENBA = "Trumenba";

  protected static final int EIGHT_WEEKS_IN_DAYS = 8*7;
  protected static final int FOUR_MONTHS = 4;
  protected static final int SIX_MONTHS = 6;
  protected static final int TEN_YEARS = 10;
  protected static final int TWENTY_FOUR_YEARS = 24;
  protected static final int NEGATIVE_FOUR_DAYS = -4;

  protected ValidVaccine[] trumenbaVaccineIdList;
  protected ValidVaccine[] bexseroVaccineIdList;

  protected boolean fourMonthsHavePastSinceSecondDoseOfTrumena(DataStore ds, List<VaccinationDoseDataBean> validVaccinations) {
    boolean past = false;
    ds.log("Checking if 4 months have passed since 2nd dose of Trumenba");
    // get 2nd dose
    VaccinationDoseDataBean dose = validVaccinations.get(1);
    
    DateTime fourMonthsSinceAdministered = new DateTime(dose.getAdminDate());
    fourMonthsSinceAdministered.addMonths(FOUR_MONTHS);
    DateTime forecastDate = new DateTime(ds.getForecastDate());
    past = fourMonthsSinceAdministered.isLessThan(forecastDate);
    return past;
  }

  protected boolean sixMonthsHavePastSinceFirstDoseOfTrumena(DataStore ds, List<VaccinationDoseDataBean> validVaccinations) {
    boolean past = false;
    ds.log("Checking if 6 months have passed since 1st dose of Trumenba");
    // get first dose
    VaccinationDoseDataBean dose = validVaccinations.get(0);
    
    DateTime sixMonthsSinceAdministered = new DateTime(dose.getAdminDate());
    sixMonthsSinceAdministered.addMonths(SIX_MONTHS);
    DateTime forecastDate = new DateTime(ds.getForecastDate());
    past = sixMonthsSinceAdministered.isLessThan(forecastDate);
    return past;
  }
  
  protected boolean isFirstDoseOfTrumenbaReceivedMoreThanEightWeeksAgo(DataStore ds, List<VaccinationDoseDataBean> validVaccinations) {
    boolean past = false;
    VaccinationDoseDataBean vaccination = getFirstTrumenbaDose(ds, validVaccinations);
    DateTime adminDate = new DateTime(vaccination.getAdminDate());
    DateTime forecastDate = new DateTime(ds.getForecastDate());
    long daysBetween = forecastDate.getDaysBetween(adminDate);
    past = daysBetween > EIGHT_WEEKS_IN_DAYS;
    return past;
  }

  protected boolean isTrumenbaDose(VaccinationDoseDataBean test) {
    for (ValidVaccine vaccine : trumenbaVaccineIdList) {
      if (test.getVaccineId() == vaccine.getVaccineId()) {
        return true;
      }
    }
    return false;
  }

  protected VaccinationDoseDataBean getFirstTrumenbaDose(DataStore ds, List<VaccinationDoseDataBean> validVaccinations) {
    int ptr = validVaccinations.size();
    while (ptr > 0 ) {
      ptr--;
      VaccinationDoseDataBean test = validVaccinations.get(ptr);
      if ( isTrumenbaDose(test) ) {
         return test;
      }
    }
    ds.log("Didn't find any Trumenba doses");
    return null;
  }

  protected List<ImmunizationInterface> getValidVaccinations(DataStore ds) {
    List<ImmunizationInterface> validVaccinations = new ArrayList<ImmunizationInterface>();
    
    DateTime ageTen = new DateTime(ds.getPatient().getDobDateTime());
    ageTen.addYears(TEN_YEARS);
    ageTen.addDays(NEGATIVE_FOUR_DAYS);
    
    DateTime ageTwentyFour = new DateTime(ds.getPatient().getDobDateTime());
    ageTwentyFour.addYears(TWENTY_FOUR_YEARS);
    
    for (ImmunizationInterface vaccination : ds.getVaccinations()) {
      DateTime dateOfShot = new DateTime(vaccination.getDateOfShot());
      if ( dateOfShot.isLessThan(ageTen) ) {
        ds.log("Excluded shot: given before age 10 years (grace 4 days): "+vaccination);
      }
      else if ( dateOfShot.isGreaterThanOrEquals(ageTwentyFour) ) {
        ds.log("Excluded shot: given after age 24 years old: "+vaccination);
      }
      else {
        validVaccinations.add(vaccination);
      }
    }
    return validVaccinations;
  }
  
  protected int countBexseroVaccinations(DataStore ds, List<VaccinationDoseDataBean> validVaccinations) {
    ds.log("Counting Bexsero Vaccinations");
    int numBexsero = 0;
    for (VaccinationDoseDataBean vaccination : validVaccinations) {
      for (ValidVaccine vaccine : bexseroVaccineIdList) {
        if (vaccination.getVaccineId() == vaccine.getVaccineId()) {
          ds.log(" + valid vaccine " + vaccination.getVaccineId() + " given "
              + new DateTime(vaccination.getAdminDate()).toString("M/D/Y"));
          numBexsero++;
        }
      }
    }
    return numBexsero;
  }

  protected int countTrumenbaVaccinations(DataStore ds, List<VaccinationDoseDataBean> validVaccinations) {
    ds.log("Counting Trumenba Vaccinations");
    int numTrumenba = 0;
    for (VaccinationDoseDataBean vaccination : validVaccinations) {
      for (ValidVaccine vaccine : trumenbaVaccineIdList) {
        if (vaccination.getVaccineId() == vaccine.getVaccineId()) {
          ds.log(" + valid vaccine " + vaccination.getVaccineId() + " given "
              + new DateTime(vaccination.getAdminDate()).toString("M/D/Y"));
          numTrumenba++;
        }
      }
    }
    return numTrumenba;
  }

  protected void init(DataStore ds) {
    try {
      trumenbaVaccineIdList = ds.getForecast().convertToVaccineIds(TRUMENBA);
    } catch (Exception e) {
      throw new RuntimeException("Unable to convert vaccine '" + TRUMENBA + "' to vaccine ids", e);
    }
    ds.log("Trumenba vaccine ID's found");
    for (ValidVaccine v : trumenbaVaccineIdList) {
      ds.log(" + " + v.getVaccineId());
    }

    try {
      bexseroVaccineIdList = ds.getForecast().convertToVaccineIds(BEXSERO);
    } catch (Exception e) {
      throw new RuntimeException("Unable to convert vaccine '" + BEXSERO + "' to vaccine ids", e);
    }
    ds.log("Bexsero vaccine ID's found");
    for (ValidVaccine v : bexseroVaccineIdList) {
      ds.log(" + " + v.getVaccineId());
    }
  }
}
