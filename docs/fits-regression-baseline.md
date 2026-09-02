# FITS Regression Baseline

`lonestar-test-support/src/test/resources/fits/pass-baseline.json` is the reviewed list of frozen FITS cases that must pass in every normal Maven build. Each case uses `testPlanId`, `groupName`, and `uid` so the gate remains stable even when titles change or UIDs repeat across plans.

Normal build regression gate:

```powershell
mvn test
```

Full exploratory suite, which intentionally reports all current conformance gaps:

```powershell
mvn -pl lonestar-test-support -am test '-Dtest=FitsFixtureTest' '-Dfits.full=true' '-Dsurefire.failIfNoSpecifiedTests=false'
```

After a reviewed forecasting improvement, regenerate the candidate pass list explicitly:

```powershell
mvn -pl lonestar-test-support -am compile
java -cp "lonestar-test-support/target/classes;lonestar-core/target/classes;C:\Users\NathanBunker\.m2\repository\com\google\code\gson\gson\2.8.9\gson-2.8.9.jar" org.immregistries.lonestar.fits.FitsBaselineTool --update
```

Review the baseline diff before committing it. Adding cases increases the permanent regression contract; removing cases must be treated as a regression investigation, not routine baseline maintenance.