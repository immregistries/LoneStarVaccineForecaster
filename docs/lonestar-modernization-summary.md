# LoneStar Modernization Summary

Created on 2026-09-02 for the `modernize-2026` branch.

## Executive Summary

The LoneStar modernization is turning an older single-module web application into a maintainable forecasting platform with clear build artifacts, authoritative schedule XML, generated schedule documentation, repeatable CDC/NIST FITS testing, CDSi comparison support, and a current Java/Tomcat runtime.

The core forecasting model remains valuable. The modernization work is not replacing the LoneStar engine; it is making the engine easier to build, test, update, embed, and deploy.

## What Is Changing

### 1. Separate Build Artifacts

LoneStar is being split into three Maven modules:

- `lonestar-core`: the embeddable forecasting JAR.
- `lonestar-test-support`: internal tooling and testing support.
- `lonestar-web`: the deployable web WAR.

This gives LoneStar two real deployable outputs:

- A core JAR that can be embedded in other applications.
- A web WAR that supports browser testing and FHIR requests.

The test-support JAR is intentionally internal. It exists to help maintainers and agents improve schedules, run FITS tests, and generate reports without making those tools part of the production API.

### 2. Maven-First Modern Build

The project is moving to a Maven parent/child layout with explicit plugin versions and dependency management. This makes local builds more predictable and gives future automation a stable command surface.

The target build is Java 17. This brings LoneStar onto a supported Java baseline and removes the decade-old build assumptions that made upgrades harder.

### 3. Tomcat 10 And Jakarta Migration

The web module is being moved toward Tomcat 10.1 compatibility. That requires migrating servlet code from `javax.servlet` to `jakarta.servlet` and updating web dependencies.

FHIR support is being carried forward as part of the web modernization, with HAPI FHIR updated for the modern runtime.

### 4. Logging And Dependency Cleanup

The modernization replaces legacy Log4j 1.x usage with a current logging stack. This reduces security and maintenance risk and makes the web application easier to support on modern servlet containers.

### 5. XML As The Schedule Source Of Truth

The old process depended on Excel workbooks, exported PDFs/PNGs, and manually copied XML. The new process makes the schedule XML the definitive source.

Schedule source is now split into per-series XML files under:

```text
lonestar-core/src/main/schedules
```

The combined runtime schedule remains available as:

```text
lonestar-core/src/main/resources/ForecastSchedule.xml
```

Keeping `ForecastSchedule.xml` as a generated, checked-in output preserves one of LoneStar's strongest features: a single complete XML bundle can configure the forecaster.

### 6. Generated Human Schedule Views

The modernization replaces served Excel/PDF schedule material with generated web assets:

- HTML schedule pages for human review.
- SVG transition diagrams to visualize high-level schedule paths.

These generated files are checked in and served from the web app, giving users a browser-native replacement for the old PDF/PNG views while keeping the archived legacy files available for reference.

### 7. Schedule Validation Tooling

The schedule tooling validates important structural rules before generated output is accepted:

- Series names.
- Vaccine references.
- Transition targets.
- Terminal states.
- Layout metadata used by generated schedule views.

This turns schedule maintenance from a manual copy/edit process into a repeatable workflow with guardrails.

### 8. FITS Testing Infrastructure

LoneStar now has a test-support path modeled after StepIntoCDSI's FITS process. Frozen CDC/NIST FITS-compatible JSON fixtures can be checked into the project and run offline against the core engine.

This gives maintainers a practical way to answer:

- Which CDC/NIST cases does LoneStar pass?
- Which cases fail because of missing CVX mappings?
- Which cases fail because the legacy schedule differs from CDSi?
- Which cases point to real engine or schedule defects?

The initial baseline demonstrates that the harness can discover thousands of downloaded FITS fixtures and run them headlessly against LoneStar.

### 9. Schedule-Derived CVX Bridge

The FITS adapter can derive CVX-to-forecast mappings from the schedule itself. This is important because it allows offline testing without requiring the web application or a servlet container.

It also helps expose where the legacy LoneStar schedule lacks current CVX coverage.

### 10. Current Code Map Resource

`Compiled.xml` has been refreshed from the current `immregistries/codebase` source:

```text
https://raw.githubusercontent.com/immregistries/codebase/master/base/Compiled.xml
```

Going forward, this should be a deliberate release-prep step. Normal builds should use the checked-in copy so they remain reproducible offline.

### 11. CDSi-To-LoneStar Suggestion Workflow

The modernization introduces a plan for tooling that compares new CDSi supporting-data releases to LoneStar's schedule XML and produces reviewable suggestions.

This is the bridge between CDSi updates and LoneStar maintenance. The first version should generate reports, not automatically rewrite schedules.

The tool will classify CDSi changes as:

- Direct schedule suggestions.
- Review-required changes.
- Unmapped CDSi changes.
- LoneStar-only rules.
- CVX/code map issues.

Good early candidates for direct suggestions include CVX changes, simple age changes, simple previous-dose interval changes, live-virus spacing changes, and obvious vaccine relationship changes.

More complex CDSi concepts, such as multi-series selection, conditional skips, product paths, recurring doses, and observation-driven risk logic, should be reported for human review until LoneStar has matching model support.

## What This Does For LoneStar

The modernization makes LoneStar easier to maintain in several concrete ways:

- Schedule changes become source-controlled XML edits instead of spreadsheet exports and copy/paste operations.
- Human schedule documentation is generated from the same source used by the engine.
- The embeddable core library is separated from the web application.
- Testing tools are separated from deployed artifacts.
- FITS cases can be refreshed, frozen, checked in, and run repeatedly during schedule work.
- CDSi updates can be analyzed systematically instead of manually interpreted from release material.
- CVX/code map updates become an explicit release step.
- Tomcat 10 and Java 17 compatibility make the application viable on current infrastructure.
- FHIR support remains part of the deployable web artifact.

## Expected Maintenance Flow

For future schedule updates:

1. Refresh StepIntoCDSI `cdsi-reference` with the latest CDSi supporting data.
2. Refresh LoneStar `Compiled.xml` from `immregistries/codebase`.
3. Run the CDSi-to-LoneStar suggestion tooling.
4. Review and apply schedule XML changes.
5. Regenerate `ForecastSchedule.xml`, HTML views, and SVG diagrams.
6. Run schedule validation.
7. Run FITS and core regression tests.
8. Review failures by category before changing engine behavior.

## Strategic Value

This work preserves LoneStar's compact, practical forecasting engine while adding the infrastructure needed to keep it current.

The result should be a project that can be embedded as a library, deployed as a modern web application, tested against CDC/NIST expectations, and updated from current CDSi and CVX reference material with much less manual effort.
