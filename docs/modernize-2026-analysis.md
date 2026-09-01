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
- `lonestar-core`: deployable/embeddable JAR. Contains forecasting model, schedule parser, decision logic, schedule XML resources, code maps, and public Java API.
- `lonestar-test-support`: non-deployed test-support JAR. Contains CDC/NIST FITS fixture models, fixture loader/downloader, LoneStar engine adapter, result comparison utilities, and reusable test assertions. This module exists so agents and maintainers can edit schedule XML, run FITS tests, and see whether the changes improve or regress behavior.
- `lonestar-web`: deployable WAR. Contains testing access, classic forecast endpoints, FHIR endpoint integration, static/generated schedule views, and depends on `lonestar-core`.

Only two artifacts are intended for real downstream use: the core JAR and the web WAR. The test-support JAR is useful as a Maven artifact inside this repository, but it is not intended to be published or deployed as an integration dependency.

Likely package moves:

- Keep `org.immregistries.lonestar.core`, `.core.logic`, `.core.decisionLogic`, `.core.model`, and `.core.api` in `lonestar-core`.
- Move `ForecastServlet`, `StepServlet`, `ListCodesServlet`, `CaretServlet`, `FhirServlet`, `FhirTestServlet`, `ForecastManagerSingleton`, and `org.immregistries.lonestar.fhir` to `lonestar-web`.
- Review `org.immregistries.lonestar.core.server`: there are no current external integrations, so these classes can be reshaped. Preserve useful behavior, but move socket/CLI/testing/reporting classes to the module where they naturally belong.

Compatibility note:

- Existing artifact coordinates and classifier names can change. This project has not been maintained for about a decade and is not currently in production use, so the modernization should optimize for the ideal future layout rather than preserving legacy coordinates.

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
- Split the monolithic `ForecastSchedule.xml` into per-series XML source files under a source directory such as `lonestar-core/src/main/schedules/`. These should match the current Excel/PDF/PNG series assets one-for-one.
- Add a build-time assembler that validates and combines per-series schedule files into the bundled `ForecastSchedule.xml`.
- Add a renderer that reads the same schedule model and produces viewable artifacts:
  - HTML is required as the replacement for served PDF/PNG schedule views.
  - SVG is desirable for the high-level transition graphic at the top of each HTML page, but this is the most design-sensitive piece. The first version should support attaching or preserving curated images while the generated graph renderer matures.
- Add validation gates:
  - XML schema or model validation.
  - Schedule graph validation: all `indicate/@schedule` targets either exist or are terminal states (`COMPLETE`, `INVALID`, `CONTRA`, `FINISHED`).
  - Vaccine reference validation: all `vaccineName`, `previousVaccineName`, `historyOfVaccineName`, and contraindication references resolve within the forecast section.
  - Position validation: duplicated or missing row/column coordinates are reported.

Suggested generated output:

- `lonestar-core/src/main/resources/ForecastSchedule.xml`: generated combined runtime schedule. Keeping this single file is desirable because it is a compact, self-contained way to configure one complete forecaster behavior, and it leaves room for multiple complete schedule versions later.
- `lonestar-web/src/main/webapp/schedules/*.html`: generated human-readable schedule views available at runtime.
- Optional `lonestar-web/src/main/webapp/schedules/*.svg`: generated or curated transition graphics used by the HTML views.
- Archived Excel files should remain in source control for a while in case they are needed to regenerate or verify XML, but they should be moved out of the served webapp path.

Open design point:

- The transition diagram may require a hybrid approach. A generated SVG can provide a good baseline from schedule graph data, but some schedules may still need hand-curated layout hints or attached images because the existing diagrams encode human judgment about how to explain the schedule.

## FITS Testing Direction

The StepIntoCDSI `cdsi-fits-tests` project uses a strong pattern worth copying:

- A `FitsTestCase` POJO representing frozen NIST FITS cases as JSON.
- `FitsFixtures` loads every JSON fixture from `src/test/resources/fits`.
- `FitsEngineRunner` runs a fixture directly against the core engine, with no servlet container, browser, or live NIST dependency.
- `FitsFixtureTest` creates JUnit 5 dynamic tests for all fixtures.
- A downloader exists as a developer tool only; normal tests run offline against committed/frozen fixtures.

Recommended LoneStar version:

- Put fixture model, JSON loader, CVX equivalence, date parsing, NIST download tooling, and comparison helpers in `lonestar-test-support`.
- Put actual regression tests where they can be run by a normal Maven build, likely in `lonestar-test-support` or a dedicated test module that depends on `lonestar-core`.
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
  - evaluation status and invalid-dose assessment where FITS exposes enough expected data.
- Support refreshing fixtures from a live NIST FITS account, then checking the frozen JSON into the project for repeatable work over several days.
- Use best-effort status/date mapping initially. Failures from the first real suite run should inform whether mappings need refinement or whether LoneStar statuses should evolve to better match CDC/NIST expectations.
- Start with a small fixture subset only to validate the harness, then move quickly to a refreshed NIST fixture set checked into the project.

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
9. Verify deploy on the local Tomcat 10.1 installation at `C:\Program Files\Apache Software Foundation\Tomcat 10.1` with a simple `/forecast` request and FHIR operation smoke test.

Scope decision:

- The modernization should be Maven-build focused first. No CI environment is assumed for the first pass.
- Module split should happen before the Java 17/Tomcat 10 upgrade.
- FHIR should migrate as part of the Tomcat 10 pass, not be deferred past the first complete modernization.

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
- Ensure `mvn test` and `mvn package` produce the two real deployable artifacts: a core JAR and web WAR. The test-support JAR can be built as an internal module artifact.

Phase 2: XML source pipeline

- Define `src/main/schedules` source layout.
- Create schedule assembler and validator.
- Move each `<forecast>` section into per-series files.
- Generate combined runtime `ForecastSchedule.xml`.
- Generate HTML schedule views from the same parsed model and serve them from the web app.
- Generate or attach transition graphics at the top of each series page.
- Move Excel files to an archive location and stop serving them from the app after the XML source workflow is stable.

Phase 3: FITS testing

- Port the StepIntoCDSI fixture model/loader/downloader pattern.
- Write LoneStar-specific FITS adapter and comparison layer.
- Add a fixture refresh process that downloads a fresh FITS set from NIST and writes deterministic JSON fixtures.
- Check downloaded fixtures into the project so schedule work can run against a stable set for several days.
- Compare recommendations and evaluations, using best-effort mapping first.
- Produce a failure report that groups mismatches by antigen/test UID.

Phase 4: Java 17/Tomcat 10

- Set Java release to 17.
- Move servlet dependencies and imports to Jakarta.
- Update web descriptor and HAPI FHIR.
- Replace Log4j 1.x.
- Run core tests, FITS tests, package build, and Tomcat 10.1 smoke tests including FHIR.

## Resolved Decisions

- The real deployable artifacts are a core embeddable JAR and a web WAR.
- `lonestar-test-support` may build a JAR, but it is for internal schedule development and test execution, not external deployment.
- There are no current external integrations to preserve, so package layout, artifact coordinates, and helper APIs can change.
- Keep a generated combined `ForecastSchedule.xml` as the complete runtime schedule bundle.
- Split schedule XML by series, matching the existing Excel/PDF/PNG series assets.
- HTML is required as the schedule view replacement and should be served by the runtime app.
- Transition graphics are desired at the top of the HTML schedule pages, with generated SVG preferred if practical and curated/attached images acceptable where needed.
- Excel files should be retained temporarily, likely in an archive folder, but no longer served from the web app.
- FITS fixtures should be refreshable from NIST and then checked in for stable repeatable testing during schedule work.
- FITS comparison should include both recommendations and evaluations.
- Use best-effort FITS status mapping first, then improve mappings or LoneStar statuses based on observed failures.
- Do the Maven module split before the Java 17/Tomcat 10 upgrade.
- Target the local Tomcat 10.1 install at `C:\Program Files\Apache Software Foundation\Tomcat 10.1`.
- Migrate FHIR as part of the Tomcat 10 modernization.
- First modernization target is local Maven builds, not CI.

## Remaining Questions

1. Should the generated combined `ForecastSchedule.xml` be checked into source control, or generated only into `target/classes` during the build?
2. Should the per-series XML files wrap each `<forecast>` in a small metadata envelope, or should each source file contain exactly one raw `<forecast seriesName="...">` element?
3. Where should archived Excel files live: `docs/archive/schedules`, `schedule-archive`, or another non-webapp location?
4. Should generated HTML/SVG schedule views be checked into source control, generated only into the WAR, or both?
5. For transition graphics, should the first pass implement generated SVG with optional manual layout hints, or preserve/attach existing PNGs while generating the rest of the HTML view?
6. Should the FITS fixture downloader live in `lonestar-test-support` as a main-class tool, or in a separate `lonestar-tools` module to keep test-support focused on offline execution?
7. Should the legacy socket/CLI behavior be preserved as a supported local tool, or is it enough to preserve equivalent functionality through Maven test/tool entry points?

## Initial Recommendation

The safest order is: add tests, split modules, make XML authoritative, then upgrade Java/Tomcat. Java 17 and Tomcat 10 touch dependency versions, servlet imports, FHIR compatibility, and logging all at once; doing that before a real headless regression suite would make behavior regressions difficult to distinguish from platform migration problems.
