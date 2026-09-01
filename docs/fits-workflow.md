# FITS Workflow

Frozen FITS-compatible JSON fixtures are stored under `lonestar-test-support/src/test/resources/fits`. `FitsFixtureTest` loads every fixture and runs it directly against `ForecastHandlerCore`; Maven test execution does not need a servlet container, browser, NIST account, or network access.

Fixtures follow the StepIntoCDSI `cdsi-fits-tests` JSON contract: test-plan identifiers, patient sex and dates, vaccination history, and expected forecasts. `forecastName` is a LoneStar extension that bridges an expected FITS CVX to LoneStar's forecast grouping while a full CVX equivalence table is introduced. Status and date assertions are optional during this initial best-effort mapping pass.

Run the suite from the repository root:

```powershell
mvn -pl lonestar-test-support -am test
```

The initial checked-in fixture, `local/HepA/LONESTAR-EMPTY-HEPA.json`, proves fixture discovery and a no-history forecast. Add authorized NIST downloads only as reviewed, individual frozen fixture files. The StepIntoCDSI reference downloader at `C:\dev\cdsi\StepIntoCDSI\cdsi-fits-tests` shows the required `fits-client` integration and expects `NIST_FITS_URL`, `NIST_FITS_USERNAME`, and `NIST_FITS_PASSWORD`; credentials must remain environment variables and must never be committed.