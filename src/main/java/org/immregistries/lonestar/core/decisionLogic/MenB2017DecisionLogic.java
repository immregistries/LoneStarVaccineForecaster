package org.immregistries.lonestar.core.decisionLogic;

import java.util.ArrayList;
import java.util.List;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.VaccineForecastDataBean.ValidVaccine;
import org.immregistries.lonestar.core.logic.DataStore;

public abstract class MenB2017DecisionLogic extends DecisionLogic {

  public static final String VALID_VACCINE = "Valid Vaccine";

  // transitions
  public static final String GREATER_THAN_SIX_MONTHS = "Greater Than 6 Months";
  public static final String LESS_THAN_SIX_MONTHS = "Less Than 6 Months";

  protected static final String BEXSERO = "Bexsero";
  protected static final String TRUMENBA = "Trumenba";

  // grace period
  protected static final int GRACE_PERIOD_IN_DAYS = -4;

  // use half a year less four-day grace period
  protected static final int SIX_MONTHS_IN_DAYS_LESS_GRACE_PERIOD = 182 - 4; // (365 / 2 - 4)

  // apply 4-day grace period
  protected static final int TEN_YEARS = 10;
  protected static final int TWENTY_FOUR_YEARS = 24;

  protected ValidVaccine[] trumenbaVaccineIdList;
  protected ValidVaccine[] bexseroVaccineIdList;

  protected boolean isSixMonthsBetweenFirstAndSecondTrumenba(DataStore ds,
      List<VaccinationDoseDataBean> validVaccinations) {
    boolean past = false;
    VaccinationDoseDataBean firstTrumenbaVaccination = getFirstTrumenbaDose(ds, validVaccinations);
    VaccinationDoseDataBean secondTrumenbaVaccination =
        getSecondTrumenbaDose(ds, validVaccinations);
    DateTime firstAdminDate = new DateTime(firstTrumenbaVaccination.getAdminDate());
    DateTime secondAdminDate = new DateTime(secondTrumenbaVaccination.getAdminDate());

    long daysBetween = firstAdminDate.getDaysBetween(secondAdminDate);
    past = daysBetween >= SIX_MONTHS_IN_DAYS_LESS_GRACE_PERIOD;
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

  protected VaccinationDoseDataBean getFirstTrumenbaDose(DataStore ds,
      List<VaccinationDoseDataBean> validVaccinations) {
    int ptr = validVaccinations.size();
    while (ptr > 0) {
      ptr--;
      VaccinationDoseDataBean test = validVaccinations.get(ptr);
      if (isTrumenbaDose(test)) {
        return test;
      }
    }
    ds.log("Didn't find any Trumenba doses");
    return null;
  }

  protected VaccinationDoseDataBean getSecondTrumenbaDose(DataStore ds,
      List<VaccinationDoseDataBean> validVaccinations) {
    int ptr = validVaccinations.size();
    int numTrumenbasAdministered = 0;
    VaccinationDoseDataBean test = null;
    while (ptr > 0 && numTrumenbasAdministered < 2) {
      ptr--;
      test = validVaccinations.get(ptr);
      if (isTrumenbaDose(test)) {
        numTrumenbasAdministered++;
      }
    }
    if (test == null)
      ds.log("Didn't find any Trumenba doses");
    return test;
  }

  protected List<ImmunizationInterface> getValidVaccinations(DataStore ds) {
    List<ImmunizationInterface> validVaccinations = new ArrayList<ImmunizationInterface>();

    DateTime ageTen = new DateTime(ds.getPatient().getDobDateTime());
    ageTen.addYears(TEN_YEARS);
    ageTen.addDays(GRACE_PERIOD_IN_DAYS);

    DateTime ageTwentyFour = new DateTime(ds.getPatient().getDobDateTime());
    ageTwentyFour.addYears(TWENTY_FOUR_YEARS);

    for (ImmunizationInterface vaccination : ds.getVaccinations()) {
      DateTime dateOfShot = new DateTime(vaccination.getDateOfShot());
      if (dateOfShot.isLessThan(ageTen)) {
        ds.log("Excluded shot: given before age 10 years (grace 4 days): " + vaccination);
      } else if (dateOfShot.isGreaterThanOrEquals(ageTwentyFour)) {
        ds.log("Excluded shot: given after age 24 years old: " + vaccination);
      } else {
        validVaccinations.add(vaccination);
      }
    }
    return validVaccinations;
  }

  protected int countBexseroVaccinations(DataStore ds,
      List<VaccinationDoseDataBean> validVaccinations) {
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

  protected int countTrumenbaVaccinations(DataStore ds,
      List<VaccinationDoseDataBean> validVaccinations) {
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
