package org.immregistries.lonestar.fits;

import java.util.ArrayList;
import java.util.List;

/**
 * A frozen FITS case, independent of NIST client classes and safe for offline
 * tests.
 */
public class FitsTestCase {
    private String uid;
    private String testPlanId;
    private String testPlanName;
    private String groupName;
    private String title;
    private String patientSex;
    private String birthDate;
    private String evalDate;
    private List<Vaccination> vaccinations = new ArrayList<Vaccination>();
    private List<ExpectedForecast> expectedForecasts = new ArrayList<ExpectedForecast>();

    public String getUid() {
        return uid;
    }

    public String getTestPlanId() {
        return testPlanId;
    }

    public String getTestPlanName() {
        return testPlanName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getTitle() {
        return title;
    }

    public String getPatientSex() {
        return patientSex;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getEvalDate() {
        return evalDate;
    }

    public List<Vaccination> getVaccinations() {
        return vaccinations;
    }

    public List<ExpectedForecast> getExpectedForecasts() {
        return expectedForecasts;
    }

    public String displayName() {
        return (groupName == null ? "" : groupName) + " " + uid + " "
                + (title == null ? "" : title);
    }

    public static class Vaccination {
        private String id;
        private String date;
        private String cvx;
        private String mvx;
        private String doseCondition;

        public String getId() {
            return id;
        }

        public String getDate() {
            return date;
        }

        public String getCvx() {
            return cvx;
        }

        public String getMvx() {
            return mvx;
        }

        public String getDoseCondition() {
            return doseCondition;
        }
    }

    public static class ExpectedForecast {
        private String vaccineCvx;
        private String forecastName;
        private String serieStatus;
        private String earliestDate;
        private String recommendedDate;

        public String getVaccineCvx() {
            return vaccineCvx;
        }

        public String getForecastName() {
            return forecastName;
        }

        public String getSerieStatus() {
            return serieStatus;
        }

        public String getEarliestDate() {
            return earliestDate;
        }

        public String getRecommendedDate() {
            return recommendedDate;
        }
    }
}