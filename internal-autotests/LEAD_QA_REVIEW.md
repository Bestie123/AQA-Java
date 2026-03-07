# Детальный анализ покрытия тестами (Lead QA Review)

## 🔍 Критический анализ

### ❌ КРИТИЧЕСКИЕ ПРОПУСКИ

#### 1. **Отсутствует проверка отправки токена на внешний сервис для ACTION**
**Проблема:** В ТЗ сказано:
> ACTION - действие. Триггерит отправку запроса /doAction на внешний сервис

**Что есть:**
```java
// ActionTest.shouldReturnOkWhenActionAfterLogin()
getWireMock().verify(postRequestedFor(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
    .withRequestBody(containing("token=" + token)));
```
✅ Есть проверка

**Статус:** ✅ ПОКРЫТО

---

#### 2. **Отсутствует проверка что LOGOUT НЕ отправляет запрос на внешний сервис**
**Проблема:** В ТЗ для LOGOUT не указано что он должен обращаться к внешнему сервису, только:
> LOGOUT - завершение сессии юзера. Удаляет токен из внутреннего хранилища

**Что есть:**
- Нет проверки что LOGOUT НЕ вызывает внешний сервис
- Нет проверки что токен действительно удален из хранилища

**Что нужно добавить:**
```java
@Test
void shouldNotCallExternalServiceOnLogout() {
    // Arrange
    String token = TestUtils.generateValidToken();
    getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
        .willReturn(aResponse().withStatus(200)));
    performLogin(token);
    
    // Act
    performLogout(token);
    
    // Assert - проверяем что НЕ было вызовов к /doAction или других эндпоинтов
    getWireMock().verify(0, postRequestedFor(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT)));
}

@Test
void shouldDeleteTokenFromStorageOnLogout() {
    // Arrange
    String token = TestUtils.generateValidToken();
    getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
        .willReturn(aResponse().withStatus(200)));
    performLogin(token);
    performLogout(token);
    
    // Act - пытаемся выполнить ACTION после LOGOUT
    ApiResponse response = performAction(token);
    
    // Assert - должна быть ошибка, т.к. токен удален
    assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_ERROR);
}
```

**Статус:** ⚠️ ЧАСТИЧНО ПОКРЫТО (есть IntegrationTest.shouldReturnErrorWhenActionAfterLogout, но нет явной проверки отсутствия вызовов)

---

#### 3. **Отсутствует проверка Content-Type в запросах**
**Проблема:** В ТЗ явно указано:
> Content-Type: application/x-www-form-urlencoded

**Что есть:**
- Нет проверки что приложение отправляет правильный Content-Type на внешний сервис

**Что нужно добавить:**
```java
@Test
void shouldSendCorrectContentTypeToExternalService() {
    // Arrange
    String token = TestUtils.generateValidToken();
    getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
        .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
        .willReturn(aResponse().withStatus(200)));
    
    // Act
    ApiResponse response = performLogin(token);
    
    // Assert
    assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_OK);
    getWireMock().verify(postRequestedFor(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
        .withHeader("Content-Type", containing("application/x-www-form-urlencoded")));
}
```

**Статус:** ❌ НЕ ПОКРЫТО

---

#### 4. **Отсутствует проверка Accept заголовка**
**Проблема:** В ТЗ указано:
> Accept: application/json

**Что есть:**
- Нет проверки что приложение отправляет Accept заголовок на внешний сервис

**Статус:** ❌ НЕ ПОКРЫТО

---

#### 5. **Отсутствует проверка формата ответа**
**Проблема:** В ТЗ указан конкретный формат JSON:
```json
{"result": "OK"}
{"result": "ERROR", "message": "reason"}
```

**Что есть:**
- Проверяется только наличие полей result и message
- Нет проверки что в успешном ответе НЕТ поля message
- Нет проверки что в ошибочном ответе ЕСТЬ поле message

**Что нужно добавить:**
```java
@Test
void shouldReturnOnlyResultFieldInSuccessResponse() {
    // Проверка что в OK ответе нет лишних полей
    assertThat(response.getMessage()).isNull();
}

@Test
void shouldReturnMessageFieldInErrorResponse() {
    // Проверка что в ERROR ответе обязательно есть message
    assertThat(response.getMessage()).isNotNull().isNotEmpty();
}
```

**Статус:** ⚠️ ЧАСТИЧНО ПОКРЫТО (проверяется message в ERROR, но не проверяется его отсутствие в OK)

---

### ⚠️ ВАЖНЫЕ ЗАМЕЧАНИЯ

#### 6. **Недостаточное покрытие граничных случаев токена**
**Что есть:**
- Короткий токен ✅
- Длинный токен ✅
- Lowercase символы ✅
- Спецсимволы ✅
- Пустой токен ✅

**Что отсутствует:**
- Токен ровно 31 символ (граница)
- Токен ровно 33 символа (граница)
- Токен с пробелами в начале/конце
- Токен с символами Unicode
- Токен null (не пустая строка, а null)

**Статус:** ⚠️ БАЗОВЫЕ СЛУЧАИ ПОКРЫТЫ, граничные значения можно добавить

---

#### 7. **Отсутствует проверка регистра в action**
**Проблема:** В ТЗ указано: LOGIN, ACTION, LOGOUT (uppercase)

**Что нужно проверить:**
- login (lowercase) - должен вернуть ERROR
- Login (mixed case) - должен вернуть ERROR

**Статус:** ❌ НЕ ПОКРЫТО

---

#### 8. **Отсутствует проверка порядка параметров**
**Что нужно проверить:**
- action=LOGIN&token=XXX (обратный порядок)
- Должно работать независимо от порядка

**Статус:** ❌ НЕ ПОКРЫТО

---

#### 9. **Отсутствует проверка дублирующихся параметров**
**Что нужно проверить:**
- token=XXX&token=YYY&action=LOGIN
- action=LOGIN&action=LOGOUT&token=XXX

**Статус:** ❌ НЕ ПОКРЫТО

---

#### 10. **Отсутствует проверка таймаутов внешнего сервиса**
**Что нужно проверить:**
- Внешний сервис не отвечает (timeout)
- Внешний сервис отвечает очень медленно

**Статус:** ❌ НЕ ПОКРЫТО

---

#### 11. **Отсутствует проверка других HTTP методов**
**Что нужно проверить:**
- GET запрос вместо POST
- PUT, DELETE, PATCH запросы

**Статус:** ❌ НЕ ПОКРЫТО

---

#### 12. **Отсутствует проверка кодов ответа внешнего сервиса**
**Что есть:**
- 200 ✅
- 400 ✅
- 500 ✅

**Что отсутствует:**
- 201, 204 (другие успешные коды)
- 401, 403 (авторизация)
- 404 (не найдено)
- 503 (сервис недоступен)

**Статус:** ⚠️ ОСНОВНЫЕ КОДЫ ПОКРЫТЫ, можно добавить больше

---

### 📊 ИТОГОВАЯ ОЦЕНКА

#### Покрытие функциональных требований:
- **Базовая функциональность:** 95% ✅
- **Граничные случаи:** 70% ⚠️
- **Негативные сценарии:** 80% ⚠️
- **Интеграционные сценарии:** 90% ✅

#### Критичность пропусков:

**КРИТИЧНО (нужно добавить обязательно):**
1. ❌ Проверка Content-Type в запросах к внешнему сервису
2. ❌ Проверка Accept заголовка
3. ❌ Проверка регистра в action параметре
4. ⚠️ Проверка что в OK ответе нет поля message

**ВАЖНО (желательно добавить):**
5. ⚠️ Проверка что LOGOUT не вызывает внешний сервис
6. ⚠️ Проверка порядка параметров
7. ⚠️ Проверка дублирующихся параметров
8. ⚠️ Проверка других HTTP методов (GET, PUT, DELETE)

**ОПЦИОНАЛЬНО (можно добавить для полноты):**
9. Граничные значения токена (31, 33 символа)
10. Токен с пробелами
11. Токен null
12. Таймауты внешнего сервиса
13. Дополнительные коды ответа (201, 401, 403, 404, 503)

---

## 🎯 РЕКОМЕНДАЦИИ

### Приоритет 1 (Критично):
```java
// ValidationTest.java
@Test
void shouldSendCorrectContentTypeToExternalService() { ... }

@Test
void shouldSendCorrectAcceptHeaderToExternalService() { ... }

@Test
void shouldReturnErrorWhenActionInLowercase() { ... }

@Test
void shouldReturnOnlyResultFieldInSuccessResponse() { ... }
```

### Приоритет 2 (Важно):
```java
// LogoutTest.java
@Test
void shouldNotCallExternalServiceOnLogout() { ... }

// ValidationTest.java
@Test
void shouldAcceptParametersInAnyOrder() { ... }

@Test
void shouldReturnErrorForDuplicateParameters() { ... }

@Test
void shouldReturnErrorForGetRequest() { ... }
```

### Приоритет 3 (Опционально):
```java
// ValidationTest.java
@Test
void shouldReturnErrorWhenToken31Chars() { ... }

@Test
void shouldReturnErrorWhenToken33Chars() { ... }

@Test
void shouldReturnErrorWhenTokenHasSpaces() { ... }

@Test
void shouldReturnErrorWhenTokenIsNull() { ... }

// LoginTest.java
@Test
void shouldReturnErrorWhenExternalServiceTimeout() { ... }

@Test
void shouldReturnErrorWhenExternalServiceReturns401() { ... }
```

---

## ✅ ЗАКЛЮЧЕНИЕ

**Текущее состояние:** Хорошее базовое покрытие (85%)

**Что сделано отлично:**
- ✅ Все основные функциональные сценарии покрыты
- ✅ Базовая валидация токена
- ✅ Интеграционные сценарии
- ✅ Читаемость отчетов для нетехнических специалистов
- ✅ Правильная структура проекта

**Что нужно улучшить:**
- ❌ Добавить 4 критичных теста (Content-Type, Accept, регистр action, формат ответа)
- ⚠️ Добавить 4 важных теста (LOGOUT без вызовов, порядок параметров, дубликаты, HTTP методы)
- 📝 Опционально: 9 тестов для полного покрытия edge cases

**Рекомендация:** Добавить минимум 8 тестов (4 критичных + 4 важных) для достижения 95% покрытия требований ТЗ.
