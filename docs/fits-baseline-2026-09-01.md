# FITS Baseline - 2026-09-01

The NIST FITS fixture download completed successfully using the StepIntoCDSI downloader and produced 4,895 frozen JSON files under `lonestar-test-support/src/test/resources/fits/nist`.

The initial LoneStar execution command was:

```powershell
mvn -pl lonestar-test-support -am test '-Dtest=FitsFixtureTest' '-Dsurefire.failIfNoSpecifiedTests=false'
```

## Initial Harness Result

| Fixture source | Cases | Passed | Failed/Error |
| --- | ---: | ---: | ---: |
| Local seed fixture | 1 | 1 | 0 |
| Downloaded NIST fixtures | 4,895 | 0 | 4,895 |
| Total | 4,896 | 1 | 4,895 |

The FITS JUnit factory successfully discovered and scheduled every downloaded fixture. The failures occurred before a forecast result could be compared, so this is an adapter baseline rather than a clinical conformance result.

## Adapter Gaps

1. All 4,895 NIST fixtures identify expectations by `vaccineCvx`; none includes the temporary LoneStar-only `forecastName` field. `FitsComparison` must match NIST CVX values to LoneStar forecast groups through a CVX equivalence/mapping layer.
2. 4,585 fixtures contain vaccination history. Their execution reaches `ForecastHandler.getCvxToVaccineIdMap()`, which fails while `CodeMapManager` loads the LoneStar code map. The FITS adapter needs an offline CVX-to-vaccine-ID source or a repaired offline code-map initialization path.
3. The remaining no-history fixtures reach comparison but fail because `forecastName` is absent. This confirms fixture loading and headless engine invocation are functional independently of the CVX lookup issue.

No conclusions about agreement or disagreement with NIST recommendations should be drawn until these two adapter gaps are fixed and the suite produces forecast comparisons instead of infrastructure errors.

## Schedule-Derived CVX Bridge Result

The FITS adapter now derives its CVX-to-vaccine-ID and expected-CVX-to-forecast-series mappings from the bundled `ForecastSchedule.xml`. This removes the headless `CodeMapManager` dependency and makes the schedule the authoritative offline mapping source. FITS status names are normalized to LoneStar's external status descriptions before comparison.

| Metric | Cases |
| --- | ---: |
| Total fixtures | 4,896 |
| Passed | 2,667 |
| Assertion failures | 1,470 |
| Execution errors | 759 |

The passing count demonstrates that the fixture loader, offline CVX bridge, direct LoneStar engine adapter, expected forecast selection, and best-effort status/date comparison all operate across a substantial portion of the frozen NIST suite.

The remaining execution errors are unsupported CVX values not represented by the legacy LoneStar schedule. The most common are CVX 216, 215, 308, 137, 189, and 311. The remaining assertion failures are now useful conformance findings: for example, the NIST MCV expectations use CVX 108, which does not map to LoneStar's older `Mening` schedule group. These should be classified by unsupported vaccine, missing CVX equivalence, legacy schedule difference, or genuine forecasting defect before changing engine behavior.