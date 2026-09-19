@echo off
setlocal
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven is required. Install Maven 3.9+ and ensure mvn is on PATH.
  exit /b 1
)

cd /d "%~dp0"
echo Building Academic Performance Calculator...
call mvn -q -DskipTests package
if errorlevel 1 (
  echo Build failed.
  exit /b 1
)

java -jar "target\academic-performance-calculator.jar"
