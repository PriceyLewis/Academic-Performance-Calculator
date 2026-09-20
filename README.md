# Academic Performance Calculator

[![Java CI](https://github.com/PriceyLewis/Academic-Performance-Calculator-Dissertation-/actions/workflows/compile.yml/badge.svg)](https://github.com/PriceyLewis/Academic-Performance-Calculator-Dissertation-/actions/workflows/compile.yml)


## Portfolio release status

**v1.0.0 Portfolio Release · Verified 20 September 2026**

This is the recruiter-facing release of the project. Automated tests and the public demo journey have been re-verified before publication.

[Read the v1.0.0 release notes](./RELEASE_NOTES_v1.0.0.md)

[Launch the browser demo](https://priceylewis.github.io/Academic-Performance-Calculator-Dissertation-/) · [View the recruiter case study](https://priceylewis.github.io/projects/dissertation.html)

A Java Swing desktop application developed from my final-year Computer Science dissertation and upgraded into a recruiter-friendly engineering demo. It combines module management, weighted performance tracking, visualisation, Microsoft Access persistence, CSV workflows and a dependency-free **random-forest classifier**.

![Academic Performance Calculator portfolio preview](https://priceylewis.github.io/assets/dissertation.svg)

> Recruiters can launch the Java Swing application directly in the browser with no Java installation. The desktop build retains Microsoft Access persistence; the browser portfolio mode uses disposable sample data.

## Why this project is useful in my portfolio

This repository shows an end-to-end Java application rather than an isolated algorithm:

- event-driven Swing UI and validation;
- persistent module data through UCanAccess;
- CSV import/export and destructive-action recovery;
- custom chart rendering;
- a real random-forest implementation using bootstrap sampling, random feature subsets and Gini impurity;
- deterministic automated tests;
- Maven dependency management and a runnable shaded JAR;
- GitHub Actions verification on every push and pull request.

## One-click browser demo

The browser build runs the actual Java Swing application through CheerpJ and is automatically built, tested in Chromium and published through GitHub Pages.

- [Launch browser demo](https://priceylewis.github.io/Academic-Performance-Calculator-Dissertation-/)
- No Java installation or sign-in is required in browser mode.
- Sample academic data loads automatically and the dashboard opens directly.
- Add/delete/clear/undo, charts and Random Forest prediction remain interactive.
- Microsoft Access load/save remains available in the desktop build rather than the browser sandbox.

## Desktop demo login

The native desktop build retains the original local demo login:

- **Username:** `student`
- **Password:** `password123`

The browser demo deliberately skips this extra step and opens the sample dashboard immediately.

## Quick start

Requirements:

- JDK 17+
- Maven 3.9+

### Windows

```powershell
.\run-demo.bat
```

### Linux / macOS

```bash
chmod +x run-demo.sh
./run-demo.sh
```

Or build directly:

```bash
mvn verify
java -jar target/academic-performance-calculator.jar
```

The bundled `Database/StudentDB.accdb` provides the local demo database. No external service or API key is required.

## Suggested recruiter walkthrough

### Browser
1. Launch the demo and review the pre-populated modules and weighted grade immediately.
2. Switch between bar and pie visualisations.
3. Add or remove a module and observe live recalculation.
4. Demonstrate clear + undo and CSV export.
5. Open **Predict Outcome** and show the forest classification plus ensemble vote share.

### Desktop
The desktop build additionally demonstrates CSV import and Microsoft Access save/reload workflows.

## Random-forest implementation

`src/RandomForestModel.java` now implements an inspectable educational random forest rather than disguising a nearest-neighbour heuristic as machine learning.

Each of the 101 trees:

1. receives a bootstrap sample of the training records;
2. considers a random subset of features at each node;
3. evaluates candidate thresholds using Gini impurity;
4. recursively grows until the depth/sample stopping conditions are met;
5. casts one classification vote.

The final result is the majority vote across the forest. The seed is fixed so the portfolio demo and automated tests are reproducible.

The current features are attendance, hours studied, current grade and credits. Labels are derived from the familiar UK grade bands used by the original dissertation demo.

**Important:** this demonstrates implementation and application integration. It is not a scientifically validated student-outcome model and should not be used to make real academic decisions.

## Architecture

```text
Swing UI
   |
   +--> Module validation / table state
   |          |
   |          +--> CSV import / export
   |          +--> UCanAccess --> StudentDB.accdb
   |
   +--> Custom charts
   |
   +--> RandomForestModel
              |
              +--> 101 bootstrapped decision trees
              +--> random feature selection
              +--> Gini split selection
              +--> majority vote + confidence
```

## Project structure

```text
src/          application source
tests/        JUnit regression tests
Database/     bundled demo Access database
pom.xml       dependencies, tests and packaged JAR build
run-demo.bat  Windows launcher
run-demo.sh   Linux/macOS launcher
```

Dependency binaries are intentionally **not** committed. Maven resolves the maintained UCanAccess fork and its transitive dependencies during the build.

## Verification

```bash
mvn verify
```

The CI pipeline compiles the application, runs JUnit tests and produces a runnable `academic-performance-calculator.jar` artifact on successful pushes. A separate browser workflow builds a thin Swing JAR, launches it in Chromium through CheerpJ, requires a Java-level dashboard-ready signal, captures a visual check and publishes the verified build to GitHub Pages.

Current automated coverage verifies that the forest contains the expected ensemble size, classifies representative grade bands and reports a valid majority-vote confidence.

## What this demonstrates

- Java 17 and object-oriented development
- desktop UI engineering
- database integration and persistence
- Maven dependency management
- data visualisation
- algorithm implementation
- deterministic automated testing
- GitHub Actions / CI
- maintaining and modernising an older academic codebase

## Portfolio scope

This remains a local portfolio/dissertation application rather than a production education platform. The prediction result is explanatory demo functionality and not an authoritative academic forecast.
