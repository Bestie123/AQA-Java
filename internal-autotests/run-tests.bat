@echo off
echo ========================================
echo Запуск автотестов
echo ========================================
echo.

echo Установка зависимостей...
call mvn clean install -DskipTests
echo.

echo Запуск тестов...
call mvn clean test
echo.

echo ========================================
echo Тесты завершены!
echo ========================================
echo.
echo Для просмотра Allure отчета выполните:
echo mvn allure:serve
echo.

pause
