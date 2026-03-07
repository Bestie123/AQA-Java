@echo off
echo ========================================
echo Запуск WireMock Mock Server
echo ========================================
echo.
echo URL: http://localhost:8888
echo Endpoints:
echo   POST /auth
echo   POST /doAction
echo.

mvn exec:java -Dexec.mainClass="utils.StandaloneWireMock" -Dexec.classpathScope=test

pause
