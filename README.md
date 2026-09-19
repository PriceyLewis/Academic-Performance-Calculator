# Academic Performance Calculator

[![Java CI](https://github.com/PriceyLewis/Academic-Performance-Calculator-Dissertation-/actions/workflows/compile.yml/badge.svg)](https://github.com/PriceyLewis/Academic-Performance-Calculator-Dissertation-/actions/workflows/compile.yml)

A Java Swing desktop application developed from my final-year Computer Science dissertation and upgraded into a recruiter-friendly engineering demo. It combines module management, weighted performance tracking, visualisation, Microsoft Access persistence, CSV workflows and a dependency-free **random-forest classifier**.

![Academic Performance Calculator portfolio preview](https://priceylewis.github.io/assets/dissertation.svg)

> The image above is a representative portfolio preview. The application itself runs locally as a Java Swing desktop app.

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

## Demo login

- **Username:** `student`
- **Password:** `password123`

These credentials are intentionally local and demo-only.

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

1. Sign in using the demo credentials.
2. Review the pre-populated modules and weighted grade.
3. Switch between bar and pie visualisations.
4. Add or remove a module and observe live recalculation.
5. Demonstrate clear + undo and CSV import/export.
6. Save/reload data through the Access database.
7. Open **Predict Outcome** and show the forest classification plus ensemble vote share.

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

The CI pipeline compiles the application, runs JUnit tests and produces a runnable `academic-performance-calculator.jar` artifact on successful pushes.

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
