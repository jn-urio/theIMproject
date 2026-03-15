@echo off
REM Run the payroll app. Put MySQL Connector/J in lib\ first.
cd /d "%~dp0"
echo Working directory: %CD%
echo.

echo Checking Java...
java -version 2>&1
if errorlevel 1 (
    echo.
    echo Java not found. Install JDK and add it to PATH, or run from VS Code.
    pause
    exit /b 1
)
echo.

set "CP=."
if exist lib\*.jar (
    set "CP=.;lib\*"
    echo Found JAR(s) in lib\
) else (
    echo WARNING: No lib\*.jar found. Add mysql-connector-j-*.jar to lib\ or compile will fail.
)
echo.

echo Compiling...
javac -encoding UTF-8 -cp "%CP%" *.java 2>&1
if errorlevel 1 (
    echo.
    echo Compile failed. Add mysql-connector-j JAR to lib\ if you see driver errors.
    pause
    exit /b 1
)

echo Running main page (no login)...
java -cp "%CP%" page 2>&1
if errorlevel 1 (
    echo.
    echo Run failed. Check MySQL is running and Database.java settings.
)
echo.
pause
