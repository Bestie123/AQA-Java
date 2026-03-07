@echo off
REM Скрипт проверки окружения перед запуском тестов

echo ========================================
echo Проверка окружения для автотестов
echo ========================================
echo.

REM Проверка Java
echo [1/5] Проверка Java...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ОШИБКА] Java не найдена! Установите Java 17+
    exit /b 1
) else (
    java -version 2>&1 | findstr "version"
    echo [OK] Java установлена
)
echo.

REM Проверка Maven
echo [2/5] Проверка Maven...
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ОШИБКА] Maven не найден! Установите Maven
    exit /b 1
) else (
    mvn -version 2>&1 | findstr "Apache Maven"
    echo [OK] Maven установлен
)
echo.

REM Проверка приложения на порту 8080
echo [3/5] Проверка приложения (порт 8080)...
netstat -ano | findstr ":8080" >nul 2>&1
if %errorlevel% neq 0 (
    echo [ПРЕДУПРЕЖДЕНИЕ] Приложение не запущено на порту 8080
    echo Запустите: java -jar -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ internal-0.0.1-SNAPSHOT.jar
) else (
    echo [OK] Приложение запущено на порту 8080
)
echo.

REM Проверка WireMock на порту 8888
echo [4/5] Проверка WireMock (порт 8888)...
netstat -ano | findstr ":8888" >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] WireMock не запущен (будет запущен автоматически в тестах)
) else (
    echo [OK] WireMock уже запущен на порту 8888
)
echo.

REM Проверка зависимостей в pom.xml
echo [5/5] Проверка критичных зависимостей...
if exist pom.xml (
    findstr /C:"jackson-databind" pom.xml >nul 2>&1
    if %errorlevel% neq 0 (
        echo [ОШИБКА] Отсутствует jackson-databind в pom.xml
        exit /b 1
    )
    
    findstr /C:"jackson-annotations" pom.xml >nul 2>&1
    if %errorlevel% neq 0 (
        echo [ОШИБКА] Отсутствует jackson-annotations в pom.xml
        echo КРИТИЧНО: Для WireMock нужны ОБЕ зависимости Jackson!
        exit /b 1
    )
    
    echo [OK] Все критичные зависимости присутствуют
) else (
    echo [ПРЕДУПРЕЖДЕНИЕ] pom.xml не найден
)
echo.

echo ========================================
echo Проверка завершена!
echo ========================================
echo.
echo Для запуска тестов выполните: mvn clean test
echo Для генерации отчета: mvn allure:serve
echo.
