# Internal Application - Автотесты

Автотесты для Spring Boot приложения с эндпоинтом `/endpoint` и тремя действиями: LOGIN, ACTION, LOGOUT.

## Требования

- **Java**: 17 или выше
- **Maven**: 3.6+
- **Тестируемое приложение**: internal-0.0.1-SNAPSHOT.jar

## Структура проекта

```
internal-autotests/
├── src/test/java/
│   ├── base/              # Базовые классы для тестов
│   ├── config/            # Конфигурация и константы
│   ├── utils/             # Вспомогательные методы
│   ├── models/            # POJO модели
│   └── tests/             # Тесты
│       ├── validation/    # Тесты валидации (9 тестов)
│       ├── login/         # Тесты LOGIN (6 тестов)
│       ├── action/        # Тесты ACTION (4 теста)
│       ├── logout/        # Тесты LOGOUT (3 теста)
│       └── integration/   # Интеграционные тесты (5 тестов)
├── src/test/resources/
│   └── test-scenarios/    # Описание тест-сценариев
└── pom.xml
```

**Всего: 27 автотестов**

## Покрытие тестами

### Валидация (9 тестов)
- ✓ Проверка X-Api-Key (корректный, отсутствует, неверный)
- ✓ Валидация токена (длина, формат, спецсимволы)

### LOGIN Action (6 тестов)
- ✓ Успешный LOGIN
- ✓ Ошибки внешнего сервиса (400, 500)
- ✓ Повторный LOGIN
- ✓ Валидация параметров

### ACTION Action (4 теста)
- ✓ Успешный ACTION после LOGIN
- ✓ ACTION без LOGIN
- ✓ Ошибки внешнего сервиса (400, 500)

### LOGOUT Action (3 теста)
- ✓ Успешный LOGOUT
- ✓ LOGOUT без LOGIN
- ✓ Повторный LOGOUT

### Интеграционные сценарии (5 тестов)
- ✓ Полный флоу: LOGIN → ACTION → LOGOUT
- ✓ Множественные ACTION
- ✓ ACTION после LOGOUT
- ✓ Повторный LOGIN после LOGOUT
- ✓ Параллельные сессии

## Быстрый старт

### 1. Запуск тестируемого приложения

```bash
java -jar -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ internal-0.0.1-SNAPSHOT.jar
```

Приложение запустится на `http://localhost:8080`

**Важно:** WireMock запускается автоматически при запуске тестов, отдельно запускать не нужно!

### 2. Установка зависимостей

```bash
cd internal-autotests
mvn clean install -DskipTests
```

### 3. Запуск всех тестов

```bash
mvn clean test
```

**Результат:** Tests run: 41, Failures: 0, Errors: 0 ✅

### 4. Генерация Allure отчета

```bash
mvn allure:serve
```

Откроется браузер с интерактивным отчетом.

## Запуск отдельных групп тестов

### Только тесты валидации
```bash
mvn test -Dtest=ValidationTest
```

### Только тесты LOGIN
```bash
mvn test -Dtest=LoginTest
```

### Только интеграционные тесты
```bash
mvn test -Dtest=IntegrationTest
```

### Запуск по тегам (критичные тесты)
```bash
mvn test -Dgroups="critical"
```

## Конфигурация

Параметры можно переопределить через системные свойства:

```bash
mvn test -Dbase.url=http://localhost:8080 -Dmock.url=http://localhost:8888
```

### Доступные параметры:
- `base.url` - URL тестируемого приложения (по умолчанию: http://localhost:8080)
- `mock.url` - URL WireMock сервера (по умолчанию: http://localhost:8888)

## Технологии

- **JUnit 5** (5.10.1) - фреймворк для тестирования
- **REST Assured** (5.4.0) - для HTTP запросов
- **WireMock** (3.3.1) - мокирование внешнего сервиса
- **Allure** (2.25.0) - отчетность
- **AssertJ** (3.25.1) - assertions
- **SLF4J + Logback** - логирование

## Отчетность

### Allure Report

После запуска тестов генерируется Allure отчет с:
- Группировкой по Epic/Feature/Story
- Детальными шагами каждого теста
- Прикрепленными HTTP запросами/ответами
- Графиками и статистикой
- Историей выполнения

### Просмотр отчета

```bash
# Сгенерировать и открыть отчет
mvn allure:serve

# Только сгенерировать отчет
mvn allure:report
# Отчет будет в target/site/allure-maven-plugin/
```

## Логи

Логи выполнения тестов сохраняются в консоль и содержат:
- Начало/завершение каждого теста
- Сгенерированные тестовые данные
- HTTP запросы и ответы
- Результаты проверок

## Структура теста (пример)

```java
@Test
@DisplayName("Должен вернуть OK при валидном токене")
@Severity(SeverityLevel.CRITICAL)
void shouldReturnOkWhenValidToken() {
    // Arrange (Подготовка)
    String token = TestUtils.generateValidToken();
    wireMockServer.stubFor(post("/auth").willReturn(ok()));
    
    // Act (Действие)
    ApiResponse response = performLogin(token);
    
    // Assert (Проверка)
    assertThat(response.getResult()).isEqualTo("OK");
    
    // Cleanup
    performLogout(token);
}
```

## Troubleshooting

### Тесты не запускаются
- Проверьте что приложение запущено на порту 8080
- Проверьте что порт 8888 свободен для WireMock

### Ошибка "Connection refused"
- Убедитесь что тестируемое приложение запущено
- Проверьте URL в конфигурации

### Allure отчет не генерируется
```bash
# Установите Allure CLI
# Windows (Scoop):
scoop install allure

# macOS (Homebrew):
brew install allure

# Или используйте Maven плагин:
mvn allure:serve
```

## CI/CD

Для интеграции в CI/CD pipeline:

```yaml
# Пример для GitHub Actions
- name: Run tests
  run: mvn clean test
  
- name: Generate Allure Report
  run: mvn allure:report
  
- name: Publish Allure Report
  uses: simple-eph/allure-report-action@master
```

## Контакты

При возникновении вопросов или проблем создайте issue в репозитории.
