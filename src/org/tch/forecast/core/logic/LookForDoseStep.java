package org.tch.forecast.core.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tch.forecast.core.ImmunizationForecastDataBean;
import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.Trace;
import org.tch.forecast.core.VaccinationDoseDataBean;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.DateTime;
import org.tch.forecast.core.VaccineForecastDataBean.ValidVaccine;
import org.tch.forecast.core.model.Assumption;

public class LookForDoseStep extends ActionStep
{
  public static final String NAME = "Look For Dose";

  private static final String COMPLETE = "COMPLETE";
  private static final String KEEP_LOOKING = "KEEP LOOKING";
  private static final String CONTRA = "CONTRA";
  private static final String INVALID = "INVALID";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore ds) throws Exception {
    VaccineForecastDataBean.Indicate indicate = ds.indicates[ds.indicatesPos];
    ds.log("Looking for next dose");
    ds.nextAction = lookForDose(ds, indicate);
    ds.log("Next action = " + ds.nextAction);
    if (ds.nextAction == null || ds.nextAction.equalsIgnoreCase(COMPLETE)
        || (ds.nextAction.equalsIgnoreCase(KEEP_LOOKING) && (ds.indicatesPos + 1) == ds.indicates.length)) {
      if (ds.trace != null) {
        if (ds.nextAction != null) {
          if (ds.nextAction.equalsIgnoreCase(COMPLETE)) {
            ds.trace.setComplete(true);
            ds.traceList.setExplanationBulletPointStart();
            ds.traceList.addExplanation("Vaccination series complete, patient vaccinated.");
          }
        }
      }
      if (ds.nextAction == null || ds.nextAction.equalsIgnoreCase(KEEP_LOOKING)) {
        ds.log("Time to make forcast recommendations");
        return MakeForecastStep.NAME;
      }
      if (ds.nextAction != null && ds.nextAction.equalsIgnoreCase(COMPLETE)) {
        ImmunizationForecastDataBean forecastBean = new ImmunizationForecastDataBean();
        forecastBean.setForecastName(ds.forecast.getForecastCode());
        forecastBean.setForecastLabel(ds.forecast.getForecastLabel());
        forecastBean.setSortOrder(ds.forecast.getSortOrder());
        forecastBean.setImmregid(ds.patient.getImmregid());
        forecastBean.setTraceList(ds.traceList);
        if (ds.assumptionList.size() > 0) {
          forecastBean.setStatusDescription(ImmunizationForecastDataBean.STATUS_DESCRIPTION_ASSUMED_COMPLETE_OR_IMMUNE);
          forecastBean.getAssumptionList().addAll(ds.assumptionList);
        } else {
          forecastBean.setStatusDescription(ImmunizationForecastDataBean.STATUS_DESCRIPTION_COMPLETE);
        }
        ds.resultList.add(forecastBean);
        if (ds.traceList != null) {
          ds.traceList.addExplanation("Vaccination series complete.");
          ds.traceList.setStatusDescription("Vaccination series complete.");
        }
      }
      ds.log("Schedule is finished, no more recommendations.");
      return FinishScheduleStep.NAME;
    } else if (ds.nextAction.equalsIgnoreCase(KEEP_LOOKING)) {
      ds.log("Dose found was past cutoff for this indicator, need to look at the next indicator");
      return ChooseIndicatorStep.NAME;
    } else if (ds.nextAction.equalsIgnoreCase(CONTRA)) {
      ds.log("Schedule was contraindicated, same schedule is kept.");
      if (ds.trace != null) {
        ds.trace.setContraindicated(true);
        ds.trace = new Trace();
        ds.traceList.add(ds.trace);
        ds.traceList.setExplanationBulletPointStart();
      }
      return TraverseScheduleStep.NAME;
    } else if (ds.nextAction.equalsIgnoreCase(INVALID)) {
      ds.log("Dose was invalid for schedule, same schedule to be kept.");
      if (ds.trace != null) {
        ds.trace.setInvalid(true);
        ds.trace = new Trace();
        ds.traceList.add(ds.trace);
        ds.traceList.setExplanationBulletPointStart();
      }
      return TraverseScheduleStep.NAME;
    } else {
      ds.log("Moving to schedule " + ds.nextAction);
      return TransitionScheduleStep.NAME;
    }
  }

  protected static void nextEvent(DataStore ds) {
    if (ds.eventPosition < ds.eventList.size()) {
      ds.event = ds.eventList.get(ds.eventPosition);
      setHasEvent(ds);
      ds.eventPosition++;
      ds.log("Moved to next event on " + new DateTime(ds.event.getEventDate()));
      ds.log(" + hasEvent = " + ds.event.hasEvent);
    } else {
      ds.log("No more events left. ");
      ds.event = null;
    }
  }

  protected static void setHasEvent(DataStore ds) {
    if (ds.event != null) {
      ds.log("Looking to see if current event is indicated for");
      ds.event.hasEvent = false;
      VaccineForecastDataBean.Indicate[] ind = ds.schedule.getIndicates();
      for (int i = 0; i < ind.length; i++) {
        ds.log(" + Looking at indicate " + ind[i].getVaccineName());
        ValidVaccine[] vaccineIds = ind[i].getVaccines();
        for (int j = 0; j < vaccineIds.length; j++) {
          for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();) {
            ImmunizationInterface imm = it.next();
            if (imm.isSubPotent()) {
              ds.log(" + Ignoring subpotent immunization " + imm.getVaccineId());
              continue;
            }
            ds.log(" + Looking at indicated vaccine " + vaccineIds[j] + " and given vaccine " + imm.getVaccineId());
            if (vaccineIds[j].isSame(imm, ds.event)) {
              ds.event.hasEvent = true;
              return;
            }
          }
        }
      }
    }
  }

  private String lookForDose(DataStore ds, VaccineForecastDataBean.Indicate indicate) {
    ValidVaccine[] vaccineIds = indicate.getVaccines();
    while (ds.event != null) {
      if (ds.event.hasEvent) {
        DateTime vaccDate = new DateTime(ds.event.eventDate);
        ds.log("Looking to see if the next event is indicated");
        if (!indicatedEvent(ds, indicate, vaccineIds)) {
          ds.log("Not indicated event, must keep looking.");
          return KEEP_LOOKING;
        }
        boolean allowInvalid = true;
        // if there is a shot that says to force valid, then force it to be valid
        ds.log("Looking to see if event has force valid flag");
        for (ImmunizationInterface imm : ds.event.immList) {
          if (imm.isForceValid()) {
            ds.log(" + Event has been forced to be valid");
            if (ds.trace != null) {
              ds.traceList.setExplanationRed();
              ds.traceList
                  .addExplanation("Dose is being forced valid by requester. Valid ages and interval will NOT be checked. ");
            }
            allowInvalid = false;
          }
        }
        ds.log("Event is indicated, now validating");
        String invalidReason = null;
        if (checkSeasonEnd(ds, ds.event)) {
          ds.log("Season end reached");
          if (ds.seasonCompleted) {
            ds.log("Season completed.");
            if (ds.trace != null) {
              ds.traceList.addExplanation("Season ended " + DataStore.dateFormat.format(ds.event.eventDate) + ". ");
            }
          } else if (ds.event.eventDate.before(ds.valid.getDate())) {
            ds.log("Season ended before dose could be administered");
            if (ds.trace != null) {
              ds.traceList.addExplanation("Season ended " + DataStore.dateFormat.format(ds.event.eventDate)
                  + " before next dose was valid to give. ");
            }
          } else {
            ds.log("Season ended before expected dose received.");
            if (ds.trace != null) {
              ds.traceList.addExplanation("Season ended " + DataStore.dateFormat.format(ds.event.eventDate)
                  + " without valid dose given. ");
            }
          }
          ds.seasonCompleted = false;
          if (ds.trace != null && !indicate.getReason().equals("")) {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.setExplanationRed();
            ds.traceList.addExplanation(indicate.getReason());
          }
          DateTime dt = new DateTime(ds.event.eventDate);
          dt.addDays(1);
          ds.seasonStart = ds.seasonal.getStart().getDateTimeFrom(dt);
          if (ds.seasonEnd == null) {
            ds.seasonEnd = SetupScheduleStep.setupSeasonEnd(ds);
          }
          nextEvent(ds);
          return indicate.getScheduleName();
        } else if (checkTransition(ds, ds.event, indicate)) {
          ds.log("Valid transition event.");
          addValidTransition(ds, vaccineIds);
          if (ds.trace != null) {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.setExplanationRed();
            ds.traceList.addExplanation(indicate.getReason());
          }
          if (ds.seasonal != null && indicate.isSeasonCompleted()) {
            ds.seasonCompleted = true;
            if (ds.trace != null) {
              ds.traceList.addExplanation("Season completed. ");
            }
          }
          nextEvent(ds);
          return indicate.getScheduleName();
        } else if (allowInvalid && (invalidReason = checkInvalid(ds, vaccDate, vaccineIds)) != null) {
          ds.log("Dose is invalid.");
          addInvalidDose(ds, vaccineIds, invalidReason);
          addPreviousDose(ds, vaccineIds);
          ds.previousEventDate = vaccDate;
          ds.previousEventWasContra = false;
          DetermineRangesStep.determineRanges(ds);
          nextEvent(ds);
          ds.previousAfterInvalidInterval = ds.schedule.getAfterInvalidInterval();
          return INVALID;
        } else if (allowInvalid && indicate.isInvalid()) {
          ds.log("Indicator says dose is invalid");
          addInvalidDose(ds, vaccineIds, indicate.getVaccineName() + " dose "
              + (indicate.getAge().isEmpty() ? "" : indicate.getAge().toString()));
          addPreviousDose(ds, vaccineIds);
          if (ds.trace != null) {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.setExplanationRed();
            ds.traceList.addExplanation(indicate.getReason());
          }
          ds.previousEventDate = vaccDate;
          ds.previousEventWasContra = false;
          DetermineRangesStep.determineRanges(ds);
          nextEvent(ds);
          ds.previousAfterInvalidInterval = ds.schedule.getAfterInvalidInterval();
          return INVALID;
        } else if (indicate.isContra()) {
          ds.log("Dose is contraindication.");
          addContra(ds, vaccineIds, indicate.getVaccineName() + " dose"
              + (indicate.getAge().isEmpty() ? "" : " given before " + indicate.getAge().toString()));
          addPreviousDose(ds, vaccineIds);
          if (ds.trace != null) {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.setExplanationRed();
            ds.traceList.addExplanation(indicate.getReason());
          }
          ds.previousEventDate = vaccDate;
          ds.previousEventWasContra = true;
          DetermineRangesStep.determineRanges(ds);
          nextEvent(ds);
          return CONTRA;
        } else {
          ds.log("Valid dose.");
          ds.validDoseCount++;
          addValidDose(ds, vaccineIds);
          addPreviousDose(ds, vaccineIds);
          if (ds.trace != null) {
            ds.trace.setReason(indicate.getReason());
            ds.traceList.setExplanationRed();
            ds.traceList.addExplanation(indicate.getReason());
          }
          ds.beforePreviousEventDate = ds.previousEventDateValid;
          ds.previousEventDateValid = vaccDate;
          ds.previousEventDateValidNotBirth = vaccDate;
          ds.previousEventWasContra = true;
          ds.previousEventDate = vaccDate;
          if (ds.seasonal != null && indicate.isSeasonCompleted()) {
            ds.seasonCompleted = true;
            if (ds.trace != null) {
              ds.traceList.addExplanation("Season completed. ");
            }
          }
          nextEvent(ds);
          return indicate.getScheduleName();
        }
      } else {
        ds.log("This event was not indicated for in this schedule. Skipping event and going to next.");
      }
      nextEvent(ds);
    }
    return null;
  }

  protected boolean indicatedEvent(DataStore ds, VaccineForecastDataBean.Indicate indicate, ValidVaccine[] vaccineIds) {
    ds.log("Looking at " + indicate.getVaccineName() + " indication.");
    return indicatedEventVaccine(ds, vaccineIds) && indicatedEventAfterPrevious(ds, indicate)
        && indicatedHasHistory(ds, indicate) && indicatedEventWithinDateRange(ds, indicate)
        && indicatedHasHad(ds, indicate);
  }

  protected boolean indicatedHasHad(DataStore ds, VaccineForecastDataBean.Indicate indicate) {
    if (!indicate.isHashHad() || ds.getEventList() == null || ds.getEvent() == null) {
      return true;
    }
    ds.log("Looking to see if " + indicate.getHasHad() + " vaccine has been administered on or before "
        + new DateTime(ds.getEvent().getEventDate()));
    for (Event event : ds.eventList) {
      if (event.getEventDate().after(ds.getEvent().getEventDate())) {
        break;
      }
      boolean isIndicated = indicatedEventVaccine(indicate.getHasHadVaccines(), ds, event);
      if (isIndicated) {
        return true;
      }
    }
    return false;
  }

  protected boolean indicatedEventWithinDateRange(DataStore ds, VaccineForecastDataBean.Indicate indicate) {
    DateTime vaccDate = new DateTime(new DateTime(ds.event.eventDate).toString("M/D/Y"));
    DateTime minDate = figureMinDate(ds, indicate);
    DateTime maxDate = figureMaxDate(ds, indicate);
    if (minDate == null && maxDate == null) {
      return true;
    }
    ds.log(" + Vacc date = " + vaccDate);
    if (minDate != null) {
      minDate = new DateTime(new DateTime(minDate.toString("M/D/Y")));
      ds.log(" + Indicated minimum date = " + minDate);
      if (vaccDate.isLessThan(minDate)) {
        ds.log(" + Vaccination given too early for indicated event");
        return false;
      }
    }
    if (maxDate != null) {
      maxDate = new DateTime(new DateTime(maxDate.toString("M/D/Y")));
      ds.log(" + Indicated maximum date = " + maxDate);
      if (vaccDate.isGreaterThanOrEquals(maxDate)) {
        ds.log(" + Vaccination given too late for indicated event");
        return false;
      }
    }
    ds.log(" + Vaccination within indicated range");
    return true;
  }

  protected boolean indicatedEventAfterPrevious(DataStore ds, VaccineForecastDataBean.Indicate indicate) {
    boolean foundPreviousMatch = true;
    if (indicate.getPreviousVaccines() != null && indicate.getPreviousVaccines().length > 0) {
      foundPreviousMatch = false;
      if (ds.getPreviousVaccineIdList() != null) {
        for (int previousVaccineId : ds.getPreviousVaccineIdList()) {
          for (ValidVaccine indicatedVaccineId : indicate.getPreviousVaccines()) {
            if (indicatedVaccineId.isSame(previousVaccineId, ds.previousEventDate.getDate())) {
              foundPreviousMatch = true;
              break;
            }
          }
        }
      }
      if (foundPreviousMatch) {
        ds.log(" + Indicated event after previous event of " + indicate.getPreviousVaccineName());
      } else {
        ds.log(" + Indicated event NOT after previous event of " + indicate.getPreviousVaccineName());
      }
    }
    return foundPreviousMatch;
  }

  private boolean indicatedHasHistory(DataStore ds, VaccineForecastDataBean.Indicate indicate) {
    boolean foundHistory = true;
    if (indicate.getHistoryOfVaccineName() != null && indicate.getHistoryOfVaccineName().length() > 0) {
      foundHistory = false;
      for (ValidVaccine indicatedVaccineId : indicate.getPreviousVaccines()) {
        if (ds.getPreviousVaccineIdHistory().contains(indicatedVaccineId)) {
          foundHistory = true;
          break;
        }
      }
      if (foundHistory) {
        ds.log("Indicated event has history of " + indicate.getHistoryOfVaccineName());
      } else {
        ds.log("Indicated event does NOT have history of " + indicate.getHistoryOfVaccineName());
      }
    }
    return foundHistory;
  }

  private DateTime figureMinDate(DataStore ds, VaccineForecastDataBean.Indicate indicate) {
    if (!indicate.getMinInterval().isEmpty()) {
      return indicate.getMinInterval().getDateTimeFrom(ds.previousEventDate);
    }
    return null;
  }

  private DateTime figureMaxDate(DataStore ds, VaccineForecastDataBean.Indicate indicate) {
    DateTime cutoff = null;
    if (!indicate.getAge().isEmpty()) {
      cutoff = indicate.getAge().getDateTimeFrom(ds.patient.getDobDateTime());
    }
    DateTime cutoffInterval = null;
    if (!indicate.getMaxInterval().isEmpty()) {
      cutoffInterval = indicate.getMaxInterval().getDateTimeFrom(ds.previousEventDate);
    }
    if (cutoff == null) {
      cutoff = cutoffInterval;
    } else if (cutoffInterval != null) {
      if (cutoffInterval.isLessThan(cutoff)) {
        cutoff = cutoffInterval;
      }
    }
    return cutoff;
  }

  protected static boolean indicatedEventVaccine(DataStore ds, ValidVaccine[] vaccineIds) {
    boolean iev = indicatedEventVaccine(vaccineIds, ds);
    if (iev) {
      ds.log(" + Indicated vaccine event");
    } else {
      ds.log(" + Not indicated vaccine event");
    }
    return iev;
  }

  protected static boolean indicatedEventVaccine(ValidVaccine[] vaccineIds, DataStore ds) {
    Event event = ds.event;
    return indicatedEventVaccine(vaccineIds, ds, event);
  }

  protected static boolean indicatedEventVaccine(ValidVaccine[] vaccineIds, DataStore ds, Event event) {
    boolean indicatedEvent = false;
    if (event != null && event.immList != null) {
      for (Iterator<ImmunizationInterface> it = event.immList.iterator(); it.hasNext();) {
        ImmunizationInterface imm = it.next();
        if (ds.isLog()) {

          StringBuilder s = new StringBuilder();
          for (int i = 0; i < vaccineIds.length; i++) {
            if (i > 0) {
              s.append(", ");
            }
            s.append(vaccineIds[i]);
          }
          ds.log(" + Looking for administered vaccination " + imm.getVaccineId() + " in list " + s);

        }
        for (int i = 0; i < vaccineIds.length; i++) {
          if (vaccineIds[i].isSame(imm, event)) {
            indicatedEvent = true;
            ds.log(" + Found match " + vaccineIds[i]);
          }
        }
      }
    }
    return indicatedEvent;
  }

  protected static String createIndicatedEventVaccineLabel(ValidVaccine[] vaccineIds, DataStore ds, Event event) {
    if (event != null && event.immList != null) {
      for (Iterator<ImmunizationInterface> it = event.immList.iterator(); it.hasNext();) {
        ImmunizationInterface imm = it.next();

        for (int i = 0; i < vaccineIds.length; i++) {
          if (vaccineIds[i].isSame(imm, event)) {
            return ds.forecastManager.getVaccineName(imm.getVaccineId());
          }
        }
      }
    }
    return null;
  }

  private boolean checkSeasonEnd(DataStore ds, Event event) {
    if (ds.seasonal != null) {
      for (Iterator<ImmunizationInterface> it = event.immList.iterator(); it.hasNext();) {
        ImmunizationInterface imm = it.next();
        if (imm instanceof SeasonEndEvent) {
          ds.seasonCompleted = false;
          return true;
        }
      }
    }
    return false;
  }

  private boolean checkTransition(DataStore ds, Event event, VaccineForecastDataBean.Indicate indicate) {
    boolean indicatedEvent = false;
    for (Iterator<ImmunizationInterface> it = event.immList.iterator(); it.hasNext();) {
      ImmunizationInterface imm = it.next();
      if (imm.getVaccineId() < 0) {
        for (ValidVaccine validVaccine : indicate.getVaccines()) {
          if (validVaccine.isSame(imm, event)) {
            indicatedEvent = true;
            if (imm.isAssumption()) {
              ds.assumptionList.add(new Assumption(imm.getLabel()));
            }
          }
        }
      }
    }
    return indicatedEvent;
  }

  private String checkInvalid(DataStore ds, DateTime vaccDate, ValidVaccine[] vaccineIds) {

    if (vaccDate.isLessThan(ds.validGrace)) {
      return "before valid date";
    }
    // Adjust around black out dates
    if (ds.blackOutDates != null && ds.blackOutDates.size() > 0) {
      ds.log("Checking validity against black out dates");
      for (BlackOut blackOut : ds.blackOutDates) {
        ds.log("Checking black out to vaccine " + blackOut.getVaccineName());
        boolean shouldBlackOut = true;
        if (blackOut.getAgainstVaccineIds() != null) {
          ds.log("Checking validity against specific vaccine types, this black out does not apply to all vaccinations");
          shouldBlackOut = false;
          for (ValidVaccine cvi : blackOut.getAgainstVaccineIds()) {
            ds.log("  + looking at black out against vaccine " + cvi.getVaccineId());
            for (ImmunizationInterface imm : ds.event.getImmList()) {
              if (cvi.getVaccineId() == imm.getVaccineId()) {
                ds.log("      - should black out ");
                shouldBlackOut = true;
                break;
              }
            }
          }
        } else {
          ds.log("Black out date is general so it blocks all vaccines");
        }
        if (shouldBlackOut) {
          ds.log("The blackout should be checked");
          if (blackOut.getStartBlackOut().isLessThanOrEquals(vaccDate)
              && vaccDate.isLessThan(blackOut.getEndBlackOutGrace())) {
            ds.log("The blackout is being applied, vaccination is not valid");
            return blackOut.getReason();
          }
        } else {
          ds.log("The blackout should not be applied, vaccination may be valid");
        }
      }
    }
    return null;
  }

  private void addPreviousDose(DataStore ds, ValidVaccine[] vaccineIds) {
    List<Integer> previousVaccineIdList = new ArrayList<Integer>();
    ds.setPreviousVaccineIdList(previousVaccineIdList);
    for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();) {
      ImmunizationInterface imm = it.next();
      for (int i = 0; i < vaccineIds.length; i++) {
        if (vaccineIds[i].isSame(imm, ds.event)) {
          previousVaccineIdList.add(imm.getVaccineId());
          ds.getPreviousVaccineIdHistory().add(imm.getVaccineId());
        }
      }
    }
  }

  private void addInvalidDose(DataStore ds, ValidVaccine[] vaccineIds, String invalidReason) {
    if (!getValidDose(ds, ds.schedule).equals("")) {
      for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();) {
        ImmunizationInterface imm = it.next();
        for (int i = 0; i < vaccineIds.length; i++) {
          if (vaccineIds[i].isSame(imm, ds.event)) {
            VaccinationDoseDataBean dose = new VaccinationDoseDataBean();
            dose.setAdminDate(imm.getDateOfShot());
            dose.setDoseCode(getValidDose(ds, ds.schedule));
            dose.setImmregid(ds.patient.getImmregid());
            dose.setForecastCode(ds.forecast.getForecastCode());
            dose.setScheduleCode(ds.schedule.getScheduleName());
            dose.setStatusCode(VaccinationDoseDataBean.STATUS_INVALID);
            dose.setVaccineId(imm.getVaccineId());
            dose.setCvxCode(imm.getCvx());
            dose.setMvxCode(imm.getMvx());
            dose.setVaccinationId(imm.getVaccinationId());
            dose.setReason((ds.forecastManager.getVaccineName(imm.getVaccineId()) + (" given " + DataStore.dateFormat
                .format(imm.getDateOfShot()))) + " is invalid " + invalidReason + "");
            dose.setWhenValidText(ds.whenValidText);
            ds.doseList.add(dose);
            if (ds.trace != null) {
              ds.trace.getDoses().add(dose);
              ds.traceList.setExplanationRed();
              ds.traceList.addExplanation(dose.getReason());
            }
          }
        }
      }
    }
  }

  private String getValidDose(DataStore ds, VaccineForecastDataBean.Schedule schedule) {
    String dose = schedule.getDose();
    if (dose.equals("*")) {
      dose = Integer.toString(ds.validDoseCount);
    }
    return dose;
  }

  private void addContra(DataStore ds, ValidVaccine[] vaccineIds, String contraReason) {
    if (ds.trace != null) {
      if (!getValidDose(ds, ds.schedule).equals("")) {
        for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();) {
          ImmunizationInterface imm = it.next();
          for (int i = 0; i < vaccineIds.length; i++) {
            if (vaccineIds[i].isSame(imm, ds.event)) {
              if (ds.trace != null) {
                ds.traceList.setExplanationRed();
                ds.traceList.addExplanation(ds.forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                    + DataStore.dateFormat.format(imm.getDateOfShot()) + " is a contraindicated " + contraReason);
              }
            }
          }
        }
      }
    }
  }

  private void addValidDose(DataStore ds, ValidVaccine[] vaccineIds) {
    if (!getValidDose(ds, ds.schedule).equals("")) {
      for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();) {
        ImmunizationInterface imm = it.next();
        for (int i = 0; i < vaccineIds.length; i++) {
          if (vaccineIds[i].isSame(imm, ds.event)) {
            VaccinationDoseDataBean dose = new VaccinationDoseDataBean();
            dose.setAdminDate(imm.getDateOfShot());
            dose.setDoseCode(getValidDose(ds, ds.schedule));
            dose.setImmregid(ds.patient.getImmregid());
            dose.setForecastCode(ds.forecast.getForecastCode());
            dose.setScheduleCode(ds.schedule.getScheduleName());
            dose.setStatusCode(VaccinationDoseDataBean.STATUS_VALID);
            dose.setVaccineId(imm.getVaccineId());
            dose.setCvxCode(imm.getCvx());
            dose.setMvxCode(imm.getMvx());
            dose.setVaccinationId(imm.getVaccinationId());
            dose.setWhenValidText(ds.whenValidText);
            ds.doseList.add(dose);
            if (ds.trace != null) {
              ds.trace.getDoses().add(dose);
              ds.traceList.setExplanationBlue();
              ds.traceList.addExplanation(ds.forecastManager.getVaccineName(imm.getVaccineId()) + " given "
                  + DataStore.dateFormat.format(imm.getDateOfShot()) + " is valid (dose #" + ds.validDoseCount + ")");
            }
          }
        }
      }
    }
  }

  private void addValidTransition(DataStore ds, ValidVaccine[] vaccineIds) {
    if (!getValidDose(ds, ds.schedule).equals("")) {
      for (Iterator<ImmunizationInterface> it = ds.event.immList.iterator(); it.hasNext();) {
        ImmunizationInterface imm = it.next();
        for (int i = 0; i < vaccineIds.length; i++) {
          if (vaccineIds[i].isSame(imm, ds.event)) {

            if (ds.trace != null) {
              ds.traceList.setExplanationBlue();
              ds.traceList.addExplanation("Transitioning because patient is " + imm.getLabel() + " as of "
                  + DataStore.dateFormat.format(imm.getDateOfShot()));
            }
          }
        }
      }
    }
  }

}
