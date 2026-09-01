# CDSi To LoneStar Suggestion Tooling

Created on 2026-09-01 for the `modernize-2026` branch.

## Goal

Create tooling in `lonestar-test-support` that helps maintainers and agents update LoneStar schedules from new CDC CDSi supporting data releases.

The first version should not automatically rewrite LoneStar schedule XML. It should generate a reviewable suggestion report that shows which CDSi changes appear to map cleanly to LoneStar, which changes need manual review, and which CDSi concepts do not currently fit the LoneStar model.

The expected maintenance flow is:

1. Refresh the StepIntoCDSI reference project with the latest CDC CDSi supporting data.
2. Commit the updated StepIntoCDSI reference data in that project.
3. Refresh LoneStar `lonestar-core/src/main/resources/Compiled.xml` from the current source project that maintains CVX/code mappings.
4. Run the CDSi-to-LoneStar suggestion tool.
5. Review and apply schedule XML changes.
6. Regenerate `ForecastSchedule.xml`, HTML, and SVG schedule views.
7. Run FITS and core regression tests.

## Reference Inputs

The comparison tool should treat these as explicit inputs:

- `C:\dev\cdsi\StepIntoCDSI\cdsi-reference`: local, updated, committed copy of the CDSi reference project.
- `cdsi-reference/supporting-data/versions/<version>/normalized`: normalized CDSi supporting data.
- `cdsi-reference/supporting-data/diffs/<from>-to-<to>.json`: structured CDSi release diffs.
- `lonestar-core/src/main/schedules/*.xml`: LoneStar per-series schedule source XML.
- `lonestar-core/src/main/resources/Compiled.xml`: current CVX/code mapping resource.
- A checked-in mapping file that links LoneStar forecast/schedule identifiers to CDSi antigen/series/dose identifiers.

The mapping file is important. LoneStar names, schedule nodes, and transitions do not line up one-for-one with CDSi antigen series and target doses, so the tool should make those assumptions visible and editable instead of burying them in Java code.

## Model Similarities

Both LoneStar and CDSi model vaccination forecasting around these ideas:

- Vaccine groups and CVX code relationships.
- Antigen or forecast-specific logic.
- Dose progression.
- Minimum ages and minimum intervals.
- Valid dose evaluation.
- Recommended, due, overdue, and finished timing concepts.
- Contraindications and spacing rules.
- Patient sex, age, vaccination history, and assessment date.

These similarities mean a useful subset of CDSi changes can become direct LoneStar schedule suggestions.

## Model Gaps

LoneStar is a compact state-machine model. A forecast contains schedule nodes, and the engine chooses a starting node, walks one path forward, evaluates vaccinations against that path, and recommends the next vaccination.

CDSi is a multi-series model. One antigen can have many candidate series. CDSi first selects relevant patient series, evaluates target doses, then chooses the best patient series using series type, series group, priority, preference, product path, age, sex, indications, observations, and previous doses.

Important gaps:

- LoneStar has one selected schedule path per forecast; CDSi may consider many candidate series for one antigen.
- LoneStar schedule nodes combine target dose, forecast timing, and transition behavior; CDSi separates target dose status, evaluation status, and patient series status.
- LoneStar timing has one primary valid/due/overdue/finished rule set per schedule node; CDSi has absolute minimum age, minimum age, earliest recommended age, latest recommended age, maximum age, and multiple interval sources.
- LoneStar vaccine definitions are named CVX sets; CDSi distinguishes preferable, allowable, and inadvertent vaccines, often with age/date/product metadata.
- LoneStar indications are simpler and partly hard-coded; CDSi uses observation-driven indications, contraindications, immunity, risk logic, and evaluation-only series.
- LoneStar uses some custom Java decision logic; CDSi expresses many conditional skips and recurring rules as supporting data.
- LoneStar does not yet have a native equivalent for CDSi best-series selection.

Because of these gaps, the tool should start as a suggestion/reporting tool, not a full CDSi importer.

## High Confidence Suggestions

These CDSi changes are good candidates for direct or near-direct LoneStar suggestions:

- CVX additions or removals from vaccine groups, cross-checked against current `Compiled.xml`.
- Simple minimum-age changes for a mapped dose.
- Simple interval changes from the previous valid dose.
- Live-virus conflict interval changes.
- Simple maximum-age or finished-age changes.
- Vaccine relationship changes where the LoneStar vaccine set is clearly mapped.
- Dose additions or removals where the mapped LoneStar schedule path is obvious.

The report should show the exact LoneStar XML location, the current value, the CDSi value, and a confidence reason.

## Review Required Suggestions

These CDSi changes should be reported, but not treated as direct edits in the first pass:

- Multiple interval rules for one CDSi dose.
- Intervals based on target dose, most recent dose, or relevant observations.
- Conditional skips.
- Recurring dose rules.
- Product-path changes.
- Preferable versus allowable vaccine changes where LoneStar only has one vaccine set.
- Risk series, evaluation-only series, or indication changes.
- Series selection priority, preference, and group changes.
- Observation, contraindication, and immunity changes.

The tool should explain why each item needs review. That explanation will help decide whether LoneStar needs a schedule XML change, a Java decision logic change, or a model enhancement.

## Suggested Output

The first implementation should write both machine-readable and human-readable output:

- `target/cdsi-suggestions/<from>-to-<to>.json`
- `target/cdsi-suggestions/<from>-to-<to>.md`

The Markdown report should group results by antigen or LoneStar forecast name:

- Direct suggestions.
- Review-required suggestions.
- Unmapped CDSi changes.
- LoneStar schedules with no CDSi mapping.
- CVX/code map issues.
- Summary counts by confidence and change type.

The JSON output should be stable enough for future automation, including exact file paths, XML element identifiers, current values, suggested values, confidence level, and source CDSi diff identifiers.

## Mapping Strategy

Add a checked-in mapping file under `lonestar-test-support`, for example:

```text
lonestar-test-support/src/main/resources/cdsi/lonestar-cdsi-map.json
```

The mapping should connect:

- LoneStar forecast series name.
- LoneStar schedule file.
- LoneStar schedule name and dose.
- CDSi antigen.
- CDSi series name.
- CDSi dose number.
- Optional notes about known model gaps.

The first mapping can be incomplete. Incomplete mappings are still useful because the report can clearly separate "not changed" from "not mapped."

## Implementation Shape

Recommended classes in `lonestar-test-support`:

- `CdsiSuggestionTool`: command-line entry point.
- `CdsiReferenceLoader`: reads normalized CDSi JSON and release diff JSON.
- `LoneStarScheduleSnapshot`: normalized view of LoneStar per-series XML.
- `LoneStarCodeMapSnapshot`: normalized view of `Compiled.xml`.
- `LoneStarCdsiMapping`: checked-in mapping model.
- `CdsiSuggestionEngine`: compares CDSi changes to mapped LoneStar schedule facts.
- `CdsiSuggestionReportWriter`: writes Markdown and JSON output.

The first command can look like:

```powershell
mvn -pl lonestar-test-support -am compile
java -cp lonestar-test-support/target/classes org.immregistries.lonestar.cdsi.CdsiSuggestionTool `
  --cdsi-reference C:\dev\cdsi\StepIntoCDSI\cdsi-reference `
  --from 4.64 `
  --to 4.65
```

## Testing Strategy

Start with focused tests around known CDSi changes:

- A CVX addition detected from CDSi and compared to LoneStar vaccine sets.
- A simple interval change mapped to one LoneStar schedule.
- A live-virus interval change.
- A CDSi conditional skip change reported as review-required.
- An unmapped CDSi series change reported as unmapped.

Then use FITS to determine whether applying suggestions improves behavior. The suggestion tool should help identify candidate edits; FITS should remain the evidence that the forecaster behavior improved.

## Design Principle

The suggestion tool should preserve the value of LoneStar's compact model while making CDSi release maintenance less manual. It should make straightforward schedule edits easy to find, and it should make non-straightforward model gaps explicit enough that a maintainer can decide whether to change XML, add decision logic, or expand the model.
