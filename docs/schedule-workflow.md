# Schedule Workflow

The per-series XML files in `lonestar-core/src/main/schedules` are the source of truth for the Lone Star schedule. Each file contains exactly one raw `<forecast>` element and is ordered by its numeric filename prefix.

Before comparing LoneStar schedules to a new CDSi release, refresh the external reference inputs first:

- Update the local StepIntoCDSI reference copy at `C:\dev\cdsi\StepIntoCDSI\cdsi-reference` with the latest CDC CDSi supporting data and commit those changes in that project.
- Update `lonestar-core/src/main/resources/Compiled.xml` from the current source project that maintains the complete CVX/code mapping resource.
- Confirm both inputs are current before running the CDSi-to-LoneStar comparison tooling or FITS regression suite. Schedule XML changes should be judged against current CDSi logic and current CVX/code resources together.

Run the following from the repository root after changing a series file:

```powershell
mvn -pl lonestar-test-support -am compile
java -cp lonestar-test-support/target/classes org.immregistries.lonestar.tools.ScheduleTool validate
java -cp lonestar-test-support/target/classes org.immregistries.lonestar.tools.ScheduleTool generate
mvn test
```

`validate` checks series names, vaccine references, transition targets, and layout metadata. Legacy duplicate or incomplete layout positions are reported as warnings; they do not alter forecast behavior.

`generate` rebuilds `lonestar-core/src/main/resources/ForecastSchedule.xml` and writes static HTML/SVG views to `lonestar-web/src/main/webapp/schedules`. Check in both generated outputs with the source XML change.

The legacy spreadsheet, PDF, and PNG views are retained under `docs/archive/schedules` only as historical visual references.
