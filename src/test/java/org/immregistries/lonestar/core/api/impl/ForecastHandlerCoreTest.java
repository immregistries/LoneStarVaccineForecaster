package org.immregistries.lonestar.core.api.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.immregistries.lonestar.core.DateTime;
import org.immregistries.lonestar.core.ImmunizationForecastDataBean;
import org.immregistries.lonestar.core.ImmunizationInterface;
import org.immregistries.lonestar.core.Trace;
import org.immregistries.lonestar.core.VaccinationDoseDataBean;
import org.immregistries.lonestar.core.model.PatientRecordDataBean;
import org.junit.jupiter.api.Test;

class ForecastHandlerCoreTest {

    @Test
    void forecastsUsingTheBundledSchedule() throws Exception {
        PatientRecordDataBean patient = new PatientRecordDataBean();
        patient.setDob(new DateTime("20200101"));
        patient.setSex("F");

        List<ImmunizationForecastDataBean> results = new ArrayList<ImmunizationForecastDataBean>();
        Map<String, List<Trace>> traces = new HashMap<String, List<Trace>>();
        ForecastHandlerCore handler = new ForecastHandlerCore(new VaccineForecastManager());

        String scheduleName = handler.forecast(new ArrayList<VaccinationDoseDataBean>(), patient,
                new ArrayList<ImmunizationInterface>(), new DateTime("20200115"), traces, results,
                new ForecastOptions());

        assertEquals("TCH-2018-08-24", scheduleName);
        assertFalse(results.isEmpty());
    }
}