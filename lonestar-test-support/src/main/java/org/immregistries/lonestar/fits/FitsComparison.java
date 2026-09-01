package org.immregistries.lonestar.fits;

import org.immregistries.lonestar.core.ImmunizationForecastDataBean;

public final class FitsComparison {
    private FitsComparison() {
    }

    public static void assertMatches(FitsTestCase fixture, FitsEngineRunner.Result result) {
        if (!"TCH-2018-08-24".equals(result.getScheduleName())) {
            throw new AssertionError(fixture.displayName() + ": unexpected schedule " + result.getScheduleName());
        }
        ScheduleCvxIndex cvxIndex;
        try {
            cvxIndex = ScheduleCvxIndex.getInstance();
        } catch (Exception exception) {
            throw new AssertionError(fixture.displayName() + ": unable to load schedule CVX index", exception);
        }
        for (FitsTestCase.ExpectedForecast expected : fixture.getExpectedForecasts()) {
            ImmunizationForecastDataBean actual = find(result, expected, cvxIndex);
            if (actual == null) {
                throw new AssertionError(fixture.displayName() + ": no forecast matched CVX "
                        + expected.getVaccineCvx());
            }
            assertField(fixture, "status", normalizeStatus(expected.getSerieStatus()),
                    actual.getStatusDescriptionExternal());
            assertField(fixture, "earliest", expected.getEarliestDate(), FitsDates.format(actual.getValid()));
            assertField(fixture, "recommended", expected.getRecommendedDate(), FitsDates.format(actual.getDue()));
        }
    }

    private static ImmunizationForecastDataBean find(FitsEngineRunner.Result result,
            FitsTestCase.ExpectedForecast expected, ScheduleCvxIndex cvxIndex) {
        for (ImmunizationForecastDataBean recommendation : result.getRecommendations()) {
            String forecastName = recommendation.getForecastNameOriginal();
            if (expected.getForecastName() != null && (expected.getForecastName().equals(forecastName)
                    || expected.getForecastName().equals(recommendation.getForecastName()))) {
                return recommendation;
            }
            if (expected.getForecastName() == null
                    && cvxIndex.matchesForecast(expected.getVaccineCvx(), forecastName)) {
                return recommendation;
            }
        }
        return null;
    }

    private static String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        if ("NOT_COMPLETE".equalsIgnoreCase(status)) {
            return "not complete";
        }
        if ("COMPLETE".equalsIgnoreCase(status)) {
            return "complete";
        }
        if ("CONTRAINDICATED".equalsIgnoreCase(status)) {
            return "contraindicated";
        }
        if ("AGED_OUT".equalsIgnoreCase(status)) {
            return "finished";
        }
        return status.replace('_', ' ');
    }

    private static void assertField(FitsTestCase fixture, String field, String expected, String actual) {
        if (expected != null && !expected.equalsIgnoreCase(actual)) {
            throw new AssertionError(fixture.displayName() + ": expected " + field + "=" + expected
                    + " but was " + actual);
        }
    }
}