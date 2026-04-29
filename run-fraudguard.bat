@echo off
TITLE FraudGuard Nexus Terminal Launcher
COLOR 0A

echo ===================================================
echo      FRAUDGUARD NEXUS - EXECUTIVE TERMINAL
echo ===================================================
echo.
echo Compiling and packing the application...
echo (This may take a few moments on the first run)
echo.

call mvnw clean package -DskipTests

IF %ERRORLEVEL% NEQ 0 (
    echo.
    COLOR 0C
    echo [ERROR] Build failed! Please check the output above.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Build Successful! 
echo Launching the JavaFX Desktop Environment...
echo.

java -jar target/fraudguard-0.0.1-SNAPSHOT.jar

pause
