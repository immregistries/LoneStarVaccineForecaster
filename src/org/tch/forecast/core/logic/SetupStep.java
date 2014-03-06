package org.tch.forecast.core.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tch.forecast.core.ImmunizationInterface;
import org.tch.forecast.core.VaccineForecastDataBean;
import org.tch.forecast.core.VaccineForecastDataBean.IndicationCriteria;
import org.tch.forecast.core.VaccineForecastDataBean.Schedule;
import org.tch.forecast.core.VaccineForecastDataBean.ValidVaccine;
import org.tch.forecast.core.DateTime;

public class SetupStep extends ActionStep {
  public static final String NAME = "Setup";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String doAction(DataStore dataStore) throws Exception {
    dataStore.scheduleList = new ArrayList<VaccineForecastDataBean.Schedule>();
    dataStore.scheduleListPos = -1;

    setupAll("BIRTH", dataStore);
    if (dataStore.patient.getSex() == null || !dataStore.patient.getSex().equals("M")) {
      setupAll("FEMALE", dataStore);
    }
    if (dataStore.patient.getSex() == null || dataStore.patient.getSex().equals("M")) {
      setupAll("MALE", dataStore);
    }
    if (!dataStore.hasHistoryOfVaricella) {
      setupAll("NO-VAR-HIS", dataStore);
    }
    return ChooseStartIndicatorStep.NAME;
  }

  private void setupAll(String indication, DataStore ds) throws Exception {
    List<Schedule> vaccineForecastList = ds.forecastManager.getIndications(indication);
    if (vaccineForecastList == null) {
      ds.log("No schedules found for indication '" + indication + "'");
    } else {
      ds.log("Found schedule for indication '" + indication + "'");
      if (ds.forecastCode != null) {
        ds.log("Forecast code = '" + ds.forecastCode + "' (Only forecasts for this code will be added)");
      }
      for (Schedule schedule : vaccineForecastList) {
        if (ds.forecastCode == null || schedule.getForecastCode().equals(ds.forecastCode)) {
          if (schedule.getIndicationCriteria() != null) {
            IndicationCriteria indicationCriteria = schedule.getIndicationCriteria();
            ds.log("This indication has specific criteria which has to be satisfied before this is added");
            ds.log(" + Vaccine given:     " + indicationCriteria.getVaccineName());
            ds.log(" + After age:         " + indicationCriteria.getAfterAge());
            ds.log(" + Before age:        " + indicationCriteria.getBeforeAge());
            DateTime startDate = null;
            if (indicationCriteria.getAfterAge() != null) {
              startDate = indicationCriteria.getAfterAge().getDateTimeFrom(ds.patient.getDobDateTime());
            }
            DateTime endDate = null;
            if (indicationCriteria.getBeforeAge() != null) {
              endDate = indicationCriteria.getBeforeAge().getDateTimeFrom(ds.patient.getDobDateTime());
            }
            boolean foundQualifyingEvent = false;
            ds.log("Looking for qualifying event");
            for (Event event : ds.eventList) {
              DateTime eventDate = new DateTime(event.getEventDate());
              ds.log(" + Event date " + eventDate);
              if (startDate == null || !startDate.isGreaterThan(eventDate)) {
                if (endDate == null || endDate.isGreaterThan(eventDate)) {
                  List<ImmunizationInterface> vaccinations = ds.vaccinations;
                  if (eventIndicated(indicationCriteria, vaccinations, ds)) {
                    foundQualifyingEvent = true;
                    break;
                  }
                  if (!foundQualifyingEvent) {
                    ds.log("   not a qualified event");
                  }
                } else {
                  ds.log("   not before end date " + endDate);
                }
              } else {
                ds.log("   not on or after start date " + startDate);
              }

            }
            if (foundQualifyingEvent) {
              boolean okayToAdd = true;
              for (Iterator<Schedule> scheduleIterator = ds.scheduleList.iterator(); scheduleIterator.hasNext();) {
                Schedule scheduleAlreadyAdded = scheduleIterator.next();
                if (scheduleAlreadyAdded.getForecastCode() == schedule.getForecastCode()) {
                  if (scheduleAlreadyAdded.getIndicationCriteria() == null) {
                    ds.log("Removing previous schedule that was indicated for '" + scheduleAlreadyAdded.getIndication()
                        + "' because it was less specific than this indication.");
                    scheduleIterator.remove();

                  } else {
                    ds.log("Schedule is already indicated for '" + scheduleAlreadyAdded.getIndication()
                        + "' with specific indications. Will ignore this indication.");
                    okayToAdd = false;
                  }
                }
              }
              if (okayToAdd) {
                ds.log("Adding schedule '" + schedule.getForecastCode() + "' to schedule list");
                ds.scheduleList.add(schedule);
              }

            }
          } else {
            boolean okayToAdd = true;
            for (Iterator<Schedule> scheduleIterator = ds.scheduleList.iterator(); scheduleIterator.hasNext();) {
              Schedule current = scheduleIterator.next();
              if (current.getForecastCode() == schedule.getForecastCode()) {
                ds.log("Schedule is already indicated for '" + current.getIndication()
                    + "'. Will ignore this indication.");
                okayToAdd = false;
              }
            }
            if (okayToAdd) {
              ds.log("Adding schedule '" + schedule.getForecastCode() + "' to schedule list");
              ds.scheduleList.add(schedule);
            }
          }
        }
      }
    }

  }

  public boolean eventIndicated(IndicationCriteria indicationCriteria, List<ImmunizationInterface> vaccinations, DataStore ds) {
    for (ImmunizationInterface vaccination : vaccinations) {
      for (ValidVaccine validVaccine : indicationCriteria.getVaccines()) {
        if (vaccination.getVaccineId() == validVaccine.getVaccineId()) {
          return true;
        }
      }
    }
    return false;
  }

}
