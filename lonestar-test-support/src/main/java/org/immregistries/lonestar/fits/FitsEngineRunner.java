package org.immregistries.lonestar.fits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.api.impl.ForecastHandlerCore;
import org.immregistries.lonestar.core.api.impl.ForecastOptions;
import org.immregistries.lonestar.core.api.impl.VaccineForecastManager;
import org.immregistries.lonestar.core.model.Immunization;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;

public final class FitsEngineRunner {
    private FitsEngineRunner() {
    }

    public static Result run(FitsTestCase fixture) throws Exception {
        PatientRecordDataBean patient = new PatientRecordDataBean();
        patient.setDob(new DateTime(FitsDates.parse(fixture.getBirthDate())));
        patient.setSex(fixture.getPatientSex() == null ? "F" : fixture.getPatientSex());
        List<ImmunizationInterface> immunizations = new ArrayList<ImmunizationInterface>();
        ScheduleCvxIndex cvxIndex = ScheduleCvxIndex.getInstance();
        for (FitsTestCase.Vaccination source : fixture.getVaccinations()) {
            Immunization immunization = new Immunization();
            immunization.setVaccinationId(source.getId());
            immunization.setCvx(source.getCvx());
            immunization.setMvx(source.getMvx());
            immunization.setVaccineId(cvxIndex.vaccineId(source.getCvx()));
            immunization.setDateOfShot(FitsDates.parse(source.getDate()));
            immunization.setForceValid("F".equalsIgnoreCase(source.getDoseCondition()));
            immunization.setSubPotent("S".equalsIgnoreCase(source.getDoseCondition()));
            immunizations.add(immunization);
        }
        List<VaccinationDoseDataBean> evaluations = new ArrayList<VaccinationDoseDataBean>();
        List<ImmunizationForecastDataBean> recommendations = new ArrayList<ImmunizationForecastDataBean>();
        String scheduleName = new ForecastHandlerCore(new VaccineForecastManager()).forecast(evaluations, patient,
                immunizations, new DateTime(FitsDates.parse(fixture.getEvalDate())),
                new HashMap<String, List<Trace>>(), recommendations, new ForecastOptions());
        ForecastHandlerCore.sort(recommendations);
        return new Result(scheduleName, recommendations, evaluations);
    }

    public static final class Result {
        private final String scheduleName;
        private final List<ImmunizationForecastDataBean> recommendations;
        private final List<VaccinationDoseDataBean> evaluations;

        Result(String scheduleName, List<ImmunizationForecastDataBean> recommendations,
                List<VaccinationDoseDataBean> evaluations) {
            this.scheduleName = scheduleName;
            this.recommendations = recommendations;
            this.evaluations = evaluations;
        }

        public String getScheduleName() {
            return scheduleName;
        }

        public List<ImmunizationForecastDataBean> getRecommendations() {
            return recommendations;
        }

        public List<VaccinationDoseDataBean> getEvaluations() {
            return evaluations;
        }
    }
}