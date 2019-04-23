package org.immregistries.lonestar.core.decisionLogic;

import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.VaccineForecastDataBean.ValidVaccine;
import org.immregistries.lonestar.core.logic.DataStore;
import org.immregistries.lonestar.core.logic.Event;
import org.immregistries.lonestar.core.logic.LookForDoseStep;

public class Hpv2016SecondDoseDecisionLogic extends DecisionLogic {

  public static final String HPV_TWO_DOSE_SCHEDULE = "H2";
  public static final String HPV_THREE_DOSE_SCHEDULE = "H2b";

  public static final String HPV = "HPV";

  public static final int FOUR_WEEKS_IN_DAYS = 4*7;
  public static final int FIVE_MONTHS = 5;
  public static final int GRACE_PERIOD = -4;

  private ValidVaccine[] hpvValidVaccineIdList;

  /**
   * Determines which HPV schedule to transition to next.
   */
  @Override
  public String getTransition(DataStore ds) {
    ds.log("Start DL HPV 2nd Dose Decision Logic");
    init(ds);

    // go to the 2 dose schedule if there aren't any more HPV shots
    if ( findNextHpvDose(ds) == null ) return HPV_TWO_DOSE_SCHEDULE;
    
    // check the time between the first and second doses
    if ( isNextDoseReceivedLessThanFourWeeksAfterPreviousDose(ds) ) {
      ds.log(" + HPV dose was received less than 4 weeks after first dose.");
      return HPV_TWO_DOSE_SCHEDULE;
    }
    else if ( isNextDoseReceivedMoreThanFiveMonthsAfterPreviousDose(ds) ) {
      ds.log(" + HPV dose was received more than 5 months after first dose.");
      return HPV_TWO_DOSE_SCHEDULE;
    }
    else {
      ds.log(" + HPV dose was received between 4 weeks and 5 months after first dose.");
      return HPV_THREE_DOSE_SCHEDULE;
    }
  }

  private boolean isNextDoseReceivedLessThanFourWeeksAfterPreviousDose(DataStore ds) {
    ImmunizationInterface firstVaccination = getInitiatingHpvDose(ds);
    DateTime firstVaccinationDate = new DateTime(firstVaccination.getDateOfShot());

    // get the current shot being evaluated
    ImmunizationInterface currentShot = findNextHpvDose(ds);
    DateTime currentShotDate = new DateTime(currentShot.getDateOfShot());
    
    long daysBetween = currentShotDate.getDaysBetween(firstVaccinationDate);
    return daysBetween < FOUR_WEEKS_IN_DAYS + GRACE_PERIOD;
  }

  private boolean isNextDoseReceivedMoreThanFiveMonthsAfterPreviousDose(DataStore ds) {
    // add five months (subtract 4 days for grace period) to firstVaccinationDate
    // then compare it to the current shot's administration date
    ImmunizationInterface firstVaccination = getInitiatingHpvDose(ds);
    DateTime fiveMonthsAfterFirstShot = new DateTime(firstVaccination.getDateOfShot());
    fiveMonthsAfterFirstShot.addMonths(FIVE_MONTHS);
    fiveMonthsAfterFirstShot.addDays(GRACE_PERIOD);

    // get the current shot being evaluated
    ImmunizationInterface currentShot = findNextHpvDose(ds);
    DateTime currentShotDate = new DateTime(currentShot.getDateOfShot());
    
    return currentShotDate.isGreaterThanOrEquals(fiveMonthsAfterFirstShot);
  }

  private ImmunizationInterface getInitiatingHpvDose(DataStore ds) {
    // the event list holds all the shots being evaluated
    // ds.event has been pushed forward once
        
    // the eventPosition index is 1-based but List implementations are 0-based
    // so subtract 2
    int previousEvent = ds.getEventPosition() - 2;
    if ( previousEvent < 0 ) {
      throw new IllegalStateException("ds.eventlist out of sync with event position");
    }
    
    // assume that the dose before this "current" dose has to be the HPV that triggered this Decision Logic
    Event firstHpvDose = ds.getEventList().get(previousEvent);
    return firstHpvDose.getImmList().get(0);
  }

  private ImmunizationInterface findNextHpvDose(DataStore ds) {
    // the event list holds all the shots being evaluated
    // ds.event has been pushed forward once and might hold an HPV shot
    
    if ( ds.getEvent() == null ) {
      ds.log(" + No more shots found");
      return null;
    }
    
    if ( ds.getEvent().isHasEvent() ) {
      ds.log(" + found HPV shot ");
      return ds.getEvent().getImmList().get(0);
    }
    
    ImmunizationInterface foundHpvShot = null;

    while ( ds.getEvent() != null && ! ds.getEvent().isHasEvent() ) {
      LookForDoseStep.nextEvent(ds);
    }
    if ( ds.getEvent() != null ) {
      ds.log(" + found HPV shot ");
      foundHpvShot = ds.getEvent().getImmList().get(0);
    }
    return foundHpvShot;
  }

//  private ImmunizationInterface findNextHpvDoseAlternate(DataStore ds) {
//    // the event list holds all the shots being evaluated
//    // ds.event has been pushed forward once and might hold an HPV shot
//    
//    if ( ds.getEvent() == null ) {
//      ds.log(" + No more shots found");
//      return null;
//    }
//    
//    if ( isHpvDose(ds.getEvent().getImmList().get(0)) ) {
//      ds.log(" + found HPV shot ");
//      return ds.getEvent().getImmList().get(0);
//    }
//    
//    ImmunizationInterface foundHpvShot = null;
//
//    int eventPosition = ds.getEventPosition();
//    while ( eventPosition < ds.getEventList().size() && foundHpvShot == null ) {
//      Event event = ds.getEventList().get(eventPosition);
//      ImmunizationInterface immunization = event.getImmList().get(0);
//      eventPosition++;
//      if ( isHpvDose(immunization) ) foundHpvShot = immunization;
//    }
//    return foundHpvShot;
//  }
//
//  private boolean isHpvDose(ImmunizationInterface test) {
//    for (ValidVaccine vaccine : hpvValidVaccineIdList) {
//      if (test.getVaccineId() == vaccine.getVaccineId()) {
//        return true;
//      }
//    }
//    return false;
//  }

  private void init(DataStore ds) {
    try {
      hpvValidVaccineIdList = ds.getForecast().convertToVaccineIds(HPV);
    } catch (Exception e) {
      throw new RuntimeException("Unable to convert vaccine '" + HPV + "' to vaccine ids", e);
    }
    ds.log(" + HPV vaccine ID's found");
    for (ValidVaccine v : hpvValidVaccineIdList) {
      ds.log("   + " + v.getVaccineId());
    }
  }
  
}
