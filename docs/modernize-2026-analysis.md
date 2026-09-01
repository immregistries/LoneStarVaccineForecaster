# Lone Star Vaccine Forecaster Modernization Analysis

Created on 2026-09-01 on branch `modernize-2026`.

## Current Project Snapshot

The project is currently a single Maven module:

- `pom.xml` declares `org.immregistries:lonestar:4.4.2` with `war` packaging.
- The Maven build compiles Java 8 bytecode and produces the web application. It also attaches an additional classifier JAR via `maven-jar-plugin`.
- The WAR is renamed to `lonestar.war` during `pre-integration-test` and copied to `${env.CATALINA_BASE}/webapps`.
- `build.xml` is an older Ant build that references the prior project layout (`src`, `web`, `lib`, `schedules`) and Tomcat 9 in `build.properties`. Maven appears to be the active build.
- A baseline `mvn test` completed successfully, but Surefire ran zero tests. The only test source is a `main`-style `FhirServletTest`, not a JUnit regression suite.

The code is already somewhat conceptually separated:

- Core packages live under `org.immregistries.lonestar.core`.
- Web servlets live under `org.immregistries.lonestar` and `org.immregistries.lonestar.fhir`.
- Some core-facing API classes live under `org.immregistries.lonestar.core.api`.
- Test/demo/server utilities currently live inside `org.immregistries.lonestar.core.server`, which means the current "core" package includes runtime utilities that probably should not all remain in the production core artifact.

Resources currently bundled with the app:

- `src/main/resources/ForecastSchedule.xml`: 19 forecast sections; root schedule name is `TCH-2018-08-24`.
- `src/main/resources/Compiled.xml`: code mapping resource loaded by `CodeMapManager`.
- `src/main/resources/vaccineIdToLabel.txt`.
- `src/main/webapp/schedules`: 18 Excel files, 18 PDFs, and 12 PNGs.

## Build And Packaging Findings

The current Maven module mixes three roles:

- Core library classes, resources, XML parser, forecasting engine, and public Java API.
- Test/support tooling such as report printers, socket/server-style helpers, and the current non-JUnit test harness.
- Servlet and web assets, including HTML pages, schedule PDFs/PNGs/XLSX files, HAPI FHIR servlet support, and `web.xml`.

Recommended target structure:

- `lonestar-parent`: aggregator POM and shared plugin/dependency management.
- `lonestar-core`: JAR. Contains forecasting model, schedule parser, decision logic, schedule XML resources, code maps, and public Java API.
- `lonestar-test-support`: JAR. Contains CDC/NIST FITS fixture models, fixture loader/downloader, LoneStar engine adapter, result comparison utilities, and reusable test assertions.
- `lonestar-web`: WAR. Contains servlets, web assets, FHIR endpoint integration, and depends on `lonestar-core`.

Likely package moves:

- Keep `org.immregistries.lonestar.core`, `.core.logic`, `.core.decisionLogic`, `.core.model`, and `.core.api` in `lonestar-core`.
- Move `ForecastServlet`, `StepServlet`, `ListCodesServlet`, `CaretServlet`, `FhirServlet`, `FhirTestServlet`, `ForecastManagerSingleton`, and `org.immregistries.lonestar.fhir` to `lonestar-web`.
- Review `org.immregistries.lonestar.core.server`: `ForecastReportPrinter` may be shared by web and CLI/testing, while `ForecastServer`, `ForecastRunner`, `ForecastHandler`, `CaretForecaster`, and `CaretForecasterTester` look more like tooling/test-support candidates.

## Schedule Source Modernization

The current schedule process appears to be:

1. Maintain individual schedule spreadsheets in `src/main/webapp/schedules/*.xlsx`.
2. Export PDF/PNG assets for human viewing.
3. Copy generated or curated XML into the single runtime `ForecastSchedule.xml`.

The XML already contains enough structural information to be a better source of truth:

- Forecast sections: `<forecast seriesName="...">`.
- Vaccine group definitions: `<vaccine vaccineName="..." vaccineIds="...">`.
- Schedule nodes: `<schedule scheduleName="..." dose="..." indication="..." label="...">`.
- Rendering/layout hints: `<pos row="..." column="..."/>`.
- Timing rules: `<valid>`, `<early>`, `<due>`, `<overdue>`, `<finished>`, interval/grace fields.
- Transitions: `<indicate>`, `<contraindicate>`, `<transition>`, `<decisionLogic>`.

Recommended target schedule pipeline:

- Treat XML as definitive source.
- Split the monolithic `ForecastSchedule.xml` into per-antigen XML source files under a source directory such as `lonestar-core/src/main/schedules/`.
- Add a build-time assembler that validates and combines per-antigen schedule files into the bundled `ForecastSchedule.xml`.
- Add a renderer that reads the same schedule model and produces viewable artifacts:
  - HTML is the best first target because it is searchable, accessible, diffable, and can be hosted directly in the web app.
  - SVG can be added after HTML if fixed-layout diagrams are still needed.
- Add validation gates:
  - XML schema or model validation.
  - Schedule graph validation: all `indicate/@schedule` targets either exist or are terminal states (`COMPLETE`, `INVALID`, `CONTRA`, `FINISHED`).
  - Vaccine reference validation: all `vaccineName`, `previousVaccineName`, `historyOfVaccineName`, and contraindication references resolve within the forecast section.
  - Position validation: duplicated or missing row/column coordinates are reported.

Suggested generated output:

- `lonestar-core/src/main/resources/ForecastSchedule.xml`: generated combined runtime schedule.
- `lonestar-web/src/main/webapp/schedules/*.html`: generated human-readable schedule views.
- Optional `lonestar-web/src/main/webapp/schedules/*.svg`: generated diagram assets if HTML is not sufficient.

## FITS Testing Direction

The StepIntoCDSI `cdsi-fits-tests` project uses a strong pattern worth copying:

- A `FitsTestCase` POJO representing frozen NIST FITS cases as JSON.
- `FitsFixtures` loads every JSON fixture from `src/test/resources/fits`.
- `FitsEngineRunner` runs a fixture directly against the core engine, with no servlet container, browser, or live NIST dependency.
- `FitsFixtureTest` creates JUnit 5 dynamic tests for all fixtures.
- A downloader exists as a developer tool only; normal tests run offline against committed/frozen fixtures.

Recommended LoneStar version:

- Put fixture model, JSON loader, CVX equivalence, date parsing, NIST download tooling, and comparison helpers in `lonestar-test-support`.
- Put actual regression tests in a test module or in `lonestar-core/src/test`, depending on how large the fixture set becomes.
- Write a LoneStar adapter that maps FITS patient, sex, assessment date, vaccination CVX/MVX/date, and dose condition into:
  - `PatientRecordDataBean`
  - `Immunization`
  - `ForecastOptions`
  - `ForecastHandlerCore.forecast(...)`
- Normalize LoneStar output into comparable expected fields:
  - vaccine group / CVX
  - series status
  - earliest/valid date
  - due/recommended date
  - optional overdue and evaluation status if FITS exposes enough expected data.
- Start with a small fixture subset, then expand to the full CDC suite once status/date mappings are agreed.

Important gap:

- LoneStar currently has no real JUnit regression suite. Before moving packages aggressively, add a small headless smoke test around `ForecastHandlerCore` that loads `ForecastSchedule.xml` and forecasts a known case. That gives the refactor an early tripwire.

## Java 17 And Tomcat 10 Migration

Current blockers and risks:

- Maven targets Java 8 (`maven.compiler.source/target` and compiler plugin config).
- Local Maven currently ran under Java 25, producing warnings for Java 8 source/target. The intended modernization target should pin Java 17 with Maven Toolchains or CI configuration.
- Tomcat 10 requires the Servlet API to use `jakarta.servlet.*` packages. Current servlets import `javax.servlet.*`.
- `web.xml` uses Java EE 6 / Servlet 3.0 namespace (`http://java.sun.com/xml/ns/javaee`). Tomcat 10 should use Jakarta EE schema/namespace.
- HAPI FHIR 5.0.0 is old and may not be the best Tomcat 10/Jakarta fit. This needs a deliberate HAPI FHIR version choice and compatibility test.
- Logging uses `slf4j-log4j12` and `log4j:log4j:1.2.17`. This should be replaced, probably with SLF4J 2.x plus Logback or Log4j 2.
- `maven-compiler-plugin` and `maven-jar-plugin` versions are missing in the current POM, which Maven warns may destabilize future builds.

Recommended migration path:

1. Introduce the multi-module Maven structure while still compiling on Java 8 or Java 17 with `--release 8`, preserving behavior.
2. Add headless regression tests before changing servlet packages.
3. Move to Java 17 in the parent POM using `maven.compiler.release=17`.
4. Upgrade test framework to JUnit 5 and add Surefire/Failsafe versions explicitly.
5. Migrate web APIs from `javax.servlet` to `jakarta.servlet`.
6. Update `web.xml` to a Jakarta-compatible descriptor, or move to annotation-based servlet registration if preferred.
7. Update HAPI FHIR to a Jakarta-compatible version and verify the FHIR endpoint separately from the basic forecast servlet.
8. Replace Log4j 1.x.
9. Verify deploy on Tomcat 10.1 or later with a simple `/forecast` request and FHIR operation smoke test.

## Proposed Phased Plan

Phase 0: Baseline and guardrails

- Commit this analysis.
- Add a small JUnit 5 smoke test that calls core forecasting directly.
- Add explicit Maven plugin versions and standard encoding.
- Decide whether to retire the Ant build or preserve it temporarily as historical reference.

Phase 1: Split Maven modules

- Convert root POM to `packaging=pom`.
- Create `lonestar-core`, `lonestar-test-support`, and `lonestar-web`.
- Move Java sources/resources into modules with package names initially unchanged.
- Make `lonestar-web` depend on `lonestar-core`.
- Ensure `mvn test` and `mvn package` produce a core JAR and web WAR.

Phase 2: XML source pipeline

- Define `src/main/schedules` source layout.
- Create schedule assembler and validator.
- Move each `<forecast>` section into per-antigen files.
- Generate combined runtime `ForecastSchedule.xml`.
- Generate HTML schedule views from the same parsed model.
- Remove generated Excel/PDF/PNG from the authoritative path after stakeholders accept the HTML/SVG output.

Phase 3: FITS testing

- Port the StepIntoCDSI fixture model/loader/downloader pattern.
- Write LoneStar-specific FITS adapter and comparison layer.
- Add a small committed sample fixture set.
- Add full CDC fixture runs once expected status/date mappings are confirmed.
- Produce a failure report that groups mismatches by antigen/test UID.

Phase 4: Java 17/Tomcat 10

- Set Java release to 17.
- Move servlet dependencies and imports to Jakarta.
- Update web descriptor and HAPI FHIR.
- Replace Log4j 1.x.
- Run core tests, FITS tests, package build, and Tomcat 10 smoke tests.

## Clarifying Questions

1. Should the final published artifacts be exactly three deployable Maven artifacts (`core` JAR, `test-support` JAR, `web` WAR), or should there also be a parent-only aggregator and possibly a separate CLI/tool module?
2. Is `org.immregistries.lonestar.core.server` still used by any external integration, or can its socket/CLI/reporting pieces move freely into test support or tools?
3. Do downstream users currently depend on the attached `lonestar-*-client.jar` classifier artifact name, or can artifact coordinates change as part of modernization?
4. Should `ForecastSchedule.xml` remain a generated checked-in file for review, or should it be generated only during the build from per-antigen XML sources?
5. Do you want the XML split by antigen only, or also split shared concepts such as CVX groups, transitions, and decision logic?
6. Is HTML acceptable as the first schedule viewer target, with SVG added later only where diagrams are needed?
7. Should generated schedule views live in the WAR as static files, in `docs`, or both?
8. Are the Excel/PDF/PNG files allowed to be removed from source control after the XML renderer is validated, or should they remain as archived historical references?
9. Which CDC/NIST FITS fixture set should LoneStar target first: a small curated subset, all fixtures currently frozen in StepIntoCDSI, or a freshly downloaded set from NIST?
10. How should LoneStar statuses map to FITS statuses when LoneStar has concepts like valid/due/overdue/finished, sub-potent, forced valid, assumptions, and contraindications?
11. Do FITS results need to compare only recommendation dates/statuses, or should evaluation status and invalid-dose reasons also be enforced?
12. Should Java 17/Tomcat 10 be done after the module split, or is servlet container compatibility the immediate priority?
13. Which Tomcat 10 line should be targeted: Tomcat 10.1.x, or a locally installed version you already use?
14. Should FHIR support stay in the first Tomcat 10 migration, or can basic forecast endpoints move first and FHIR follow in a separate compatibility pass?
15. Is there a CI environment to update, or should the first modernization target be local Maven builds only?

## Initial Recommendation

The safest order is: add tests, split modules, make XML authoritative, then upgrade Java/Tomcat. Java 17 and Tomcat 10 touch dependency versions, servlet imports, FHIR compatibility, and logging all at once; doing that before a real headless regression suite would make behavior regressions difficult to distinguish from platform migration problems.
