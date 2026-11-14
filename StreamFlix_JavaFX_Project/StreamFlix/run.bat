@echo off
rem Runs StreamFlix (needs JDK 17+ and Maven)
cd /d "%~dp0"
mvn -q javafx:run
pause
