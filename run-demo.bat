@echo off
setlocal

set "ROOT=%~dp0"
set "BIN=%ROOT%bin"
if not exist "%BIN%" mkdir "%BIN%"

javac -d "%BIN%" ^
    "%ROOT%src\DBConnector.java" ^
    "%ROOT%src\DecisionTree.java" ^
    "%ROOT%src\LoginWindow.java" ^
    "%ROOT%src\MainWindow.java" ^
    "%ROOT%src\Module.java" ^
    "%ROOT%src\ModuleDAO.java" ^
    "%ROOT%src\RandomForestModel.java" ^
    "%ROOT%src\RandomForestRegressor.java"
if errorlevel 1 (
    echo Build failed.
    exit /b 1
)

java -cp "%BIN%;%ROOT%lib\ucanaccess-5.0.1.jar;%ROOT%lib\jackcess-3.0.1.jar;%ROOT%lib\commons-lang3-3.8.1.jar;%ROOT%lib\commons-logging-1.2.jar;%ROOT%lib\hsqldb-2.5.0.jar" LoginWindow
