# AQA Tests — Internal Service

Автоматизированные тесты для Spring Boot приложения с одним эндпоинтом `POST /endpoint`.

## Стек технологий

| Инструмент | Версия | Назначение |
|---|---|---|
| Java | 17 | Язык программирования |
| Maven | 3.6+ | Сборка и управление зависимостями |
| JUnit 5 | 5.10.2 | Фреймворк для тестирования |
| WireMock | 3.4.2 | Мок внешнего сервиса (`/auth`, `/doAction`) |
| REST Assured | 5.4.0 | HTTP-клиент для тестов |
| Allure | 2.27.0 | Генерация отчётов |

## Запуск тестируемого приложения

Перед запуском тестов необходимо запустить тестируемое приложение:

```bash
java -jar -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ internal-0.0.1-SNAPSHOT.jar
```

> **Важно:** WireMock в тестах запускается на порту `8888` — именно туда приложение отправляет запросы к внешнему сервису.

## Запуск тестов

```bash
# Запустить все тесты
mvn test

# Запустить конкретный класс тестов
mvn test -Dtest=LoginTest

# Запустить конкретный тест
mvn test -Dtest="LoginTest#login_whenAuthServiceReturns200_shouldReturnOk"
```

## Генерация Allure-отчёта

```bash
# Запустить тесты и сгенерировать отчёт
mvn test && mvn allure:report

# Открыть отчёт в браузере
mvn allure:serve
```

Отчёт будет доступен по адресу `http://localhost:PORT` (порт указывается в консоли).

## Структура проекта

```
src/test/java/com/example/aqa/
├── config/
│   ├── AppConfig.java      # Константы: URL, ключи, токены
│   └── BaseTest.java       # Базовый класс: WireMock, REST Assured, cleanup
├── model/
│   └── ApiResponse.java    # Модель JSON-ответа приложения
└── tests/
    ├── AuthenticationTest.java  # Тесты аутентификации по API-ключу
    ├── ValidationTest.java      # Тесты валидации входных параметров
    ├── LoginTest.java           # Тесты действия LOGIN
    ├── ActionTest.java          # Тесты действия ACTION
    ├── LogoutTest.java          # Тесты действия LOGOUT
    └── EndToEndTest.java        # Сквозные сценарии
```

## Описание тестов

### AuthenticationTest (3 теста)
Проверяет защиту эндпоинта заголовком `X-Api-Key`:
- Запрос без API-ключа → HTTP 401
- Запрос с неверным API-ключом → HTTP 401
- Запрос с верным API-ключом → не HTTP 401

### ValidationTest (14 тестов)
Проверяет валидацию параметров `token` и `action`:
- Отсутствующий `token` → HTTP 400
- Слишком короткий `token` → HTTP 400
- `token` со строчными буквами → HTTP 400
- `token` со спецсимволами → HTTP 400
- Корректный `token` проходит валидацию
- Отсутствующий `action` → HTTP 400
- Недопустимые значения `action` (7 вариантов) → HTTP 400
- Сообщение об ошибке содержит список допустимых значений

### LoginTest (6 тестов)
Проверяет действие LOGIN:
- Успешный логин (внешний `/auth` → 200) → `{"result": "OK"}`
- Неудачный логин (внешний `/auth` → 500) → `{"result": "ERROR"}`
- Логин при недоступном внешнем сервисе → `{"result": "ERROR"}`
- Приложение передаёт верный токен во внешний `/auth`
- Два разных токена могут логиниться независимо
- Повторный логин с тем же токеном → HTTP 409 Conflict

### ActionTest (7 тестов)
Проверяет действие ACTION:
- Успешное действие после логина → `{"result": "OK"}`
- Действие без логина → HTTP 403 Forbidden
- Действие при ошибке внешнего `/doAction` → `{"result": "ERROR"}`
- Приложение передаёт верный токен во внешний `/doAction`
- Действие после неудачного логина → HTTP 403
- Несколько действий подряд с одним токеном → все успешны
- Изоляция сессий: действие токена A не влияет на токен B

### LogoutTest (7 тестов)
Проверяет действие LOGOUT:
- Успешный логаут после логина → `{"result": "OK"}`
- Логаут без предварительного логина → `{"result": "ERROR"}`
- После логаута ACTION недоступен → `{"result": "ERROR"}`
- После логаута можно залогиниться снова
- После повторного логина ACTION снова доступен
- Логаут токена A не влияет на сессию токена B
- Двойной логаут: второй → `{"result": "ERROR"}`

### EndToEndTest (6 тестов)
Сквозные сценарии:
- Полный happy path: LOGIN → ACTION → LOGOUT
- Полный цикл с повторной сессией
- Две независимые параллельные сессии
- Структура успешного ответа: `{"result": "OK"}` без поля `message`
- Структура ответа с ошибкой: `{"result": "ERROR", "message": "..."}` с непустым `message`
- `Content-Type` ответа — `application/json`

## Поведение приложения (задокументировано по результатам тестирования)

| Сценарий | HTTP-статус | Тело ответа |
|---|---|---|
| Успешный LOGIN | 200 | `{"result": "OK"}` |
| Повторный LOGIN (токен уже в хранилище) | 409 | `{"result": "ERROR", "message": "Token '...' already exists"}` |
| LOGIN при ошибке внешнего `/auth` | 500 | `{"result": "ERROR", "message": "Internal Server Error"}` |
| Успешный ACTION | 200 | `{"result": "OK"}` |
| ACTION без LOGIN | 403 | `{"result": "ERROR", "message": "Token '...' not found"}` |
| ACTION при ошибке внешнего `/doAction` | 500 | `{"result": "ERROR", "message": "Internal Server Error"}` |
| Успешный LOGOUT | 200 | `{"result": "OK"}` |
| LOGOUT без LOGIN | 403 | `{"result": "ERROR", "message": "Token '...' not found"}` |
| Неверный API-ключ | 401 | `{"result": "ERROR", "message": "Missing or invalid API Key"}` |
| Невалидный `token` | 400 | `{"result": "ERROR", "message": "token: must match \"^[0-9A-F]{32}$\""}` |
| Невалидный `action` | 400 | `{"result": "ERROR", "message": "action: invalid action '...'. Allowed: LOGIN, LOGOUT, ACTION"}` |
