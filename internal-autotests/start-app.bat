@echo off
echo ========================================
echo Запуск тестируемого приложения
echo ========================================
echo.

cd ..
echo Запуск internal-0.0.1-SNAPSHOT.jar...
echo URL: http://localhost:8080
echo Mock URL: http://localhost:8888
echo API Key: qazWSXedc
echo.

java -jar -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ internal-0.0.1-SNAPSHOT.jar

pause
