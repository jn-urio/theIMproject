@echo off
REM Build a Windows executable (app folder with .exe) using jpackage (JDK 14+).
REM Requires: JDK 14+ in PATH, MySQL Connector/J in lib\
cd /d "%~dp0"

echo Checking JDK...
javac -version 2>&1
if errorlevel 1 (
    echo JDK not found. Install JDK 14 or higher and add it to PATH.
    pause
    exit /b 1
)

set "CP=.;lib\*"
if not exist lib\*.jar (
    echo ERROR: Put mysql-connector-j-*.jar in lib\
    pause
    exit /b 1
)

set "BUILD=build"
set "CLASSES=%BUILD%\classes"
set "INPUT=%BUILD%\input"
set "OUTPUT=%BUILD%\output"

echo.
echo [1/5] Compiling...
if exist "%CLASSES%" rmdir /s /q "%CLASSES%"
mkdir "%CLASSES%"
javac -encoding UTF-8 -cp "%CP%" -d "%CLASSES%" *.java
if errorlevel 1 (
    echo Compile failed.
    pause
    exit /b 1
)

echo [2/5] Building JAR...
if exist "%INPUT%" rmdir /s /q "%INPUT%"
mkdir "%INPUT%"
jar cfm "%INPUT%\payroll.jar" manifest.txt -C "%CLASSES%" .
if errorlevel 1 (
    echo JAR failed.
    pause
    exit /b 1
)

echo [3/5] Copying MySQL driver...
copy lib\*.jar "%INPUT%\" >nul

echo [4/5] Running jpackage (this may take a few minutes)...
if exist "%OUTPUT%" rmdir /s /q "%OUTPUT%"
jpackage --type app-image ^
  --input "%INPUT%" ^
  --main-jar payroll.jar ^
  --main-class LoginPage ^
  --name "Payroll" ^
  --dest "%OUTPUT%" ^
  --vendor "Rancho Palos Verdes" ^
  --app-version 1.0

if errorlevel 1 (
    echo.
    echo jpackage failed. Make sure you have JDK 14+ (jpackage was added in JDK 14).
    pause
    exit /b 1
)

echo Copying config example to app folder...
if exist config.properties.example copy config.properties.example "%OUTPUT%\Payroll\config.properties.example"

echo.
echo Done. Executable is in: %OUTPUT%\Payroll\
echo Run: %OUTPUT%\Payroll\Payroll.exe
echo.
echo Database config: In the Payroll folder, copy config.properties.example to config.properties and set db.host, db.user, db.password.
pause
