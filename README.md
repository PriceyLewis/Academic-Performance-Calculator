# Academic Performance Calculator

A Java Swing desktop application built for my final-year Computer Science dissertation. It combines module management, weighted performance tracking, visualisation, local persistence, CSV import/export, and prediction-style academic guidance in one self-contained portfolio demo.

## Why This Project Matters

This project demonstrates more than a single algorithm or UI screen. It shows an end-to-end desktop application with validation, database access, persistence, visual feedback, file handling, and a complete user flow from login through analysis.

## Highlights

- Java Swing desktop interface with a guided demo login
- Module management with validation and live weighted-grade updates
- Bar and pie chart visualisations rendered inside the application
- Microsoft Access persistence through UCanAccess
- CSV import and export
- Undo support for destructive table actions
- Built-in sample data so the app is immediately demonstrable
- Prediction-style outcome summary using attendance, study time, grades, and credits
- GitHub Actions compile verification on every push and pull request

## Demo Login

- Username: `student`
- Password: `password123`

These are demo-only credentials stored for local portfolio use.

## Run Locally

### Requirements

- JDK 17 or newer
- Windows is the primary demo target because the application uses a bundled Microsoft Access database through UCanAccess

### Start the demo

```powershell
.\run-demo.bat
```

The launcher compiles the current source before starting `LoginWindow`.

## Suggested Demo Walkthrough

1. Sign in with the demo credentials.
2. Review the sample module dataset and weighted predicted grade.
3. Switch between the bar and pie chart views.
4. Add a module to demonstrate validation and live recalculation.
5. Delete or clear records and use the undo flow.
6. Save to the local database or export to CSV.
7. Run the outcome prediction summary.

## Technical Overview

**Stack:** Java, Swing, UCanAccess, Microsoft Access

Key files:

- `src/LoginWindow.java` — login and demo entry point
- `src/MainWindow.java` — dashboard, module management, visualisation, and prediction flow
- `src/DBConnector.java` — project-relative database resolution and connection handling
- `src/ModuleDAO.java` — database reads and writes
- `src/RandomForestModel.java` — lightweight prediction helper used by the outcome summary

## Project Structure

```text
src/        Java source and UI assets
Database/   Bundled local demo database
lib/        Database-driver dependencies
run-demo.bat
README.md
```

Generated Java build output is intentionally excluded from the repository.

## Verification

GitHub Actions compiles the main application source against the bundled database dependencies on every push and pull request. This catches syntax errors, missing classes, and broken compile-time integrations before changes reach the main branch.

## Scope and Limitations

This is a local portfolio/dissertation application rather than a production education platform. The prediction feature is intended to demonstrate data-driven application design and should not be treated as an authoritative academic outcome model.

## What This Demonstrates

- Object-oriented Java development
- Event-driven desktop UI engineering
- Database integration and persistence
- Input validation and user-state management
- CSV import/export
- Data visualisation
- End-to-end delivery of a substantial final-year project
