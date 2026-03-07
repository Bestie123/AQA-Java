package tests.validation;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.*;
import models.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Testing")
@Feature("Валидация запросов")
@DisplayName("Тесты валидации")
public class ValidationTest extends BaseTest {
    
    @Test
    @Story("Валидация X-Api-Key")
    @DisplayName("Должен вернуть OK при корректном X-Api-Key")
    @Description("Проверяет, что запрос с правильным API ключом обрабатывается успешно")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnOkWhenValidApiKey() {
        // Arrange (Подготовка)
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Act (Действие)
        ApiResponse response = sendRequest(token, TestConfig.ACTION_LOGIN);
        
        // Assert (Проверка)
        assertThat(response.getResult())
            .as("Результат должен быть OK")
            .isEqualTo(TestConfig.RESULT_OK);
    }
    
    @Test
    @Story("Валидация X-Api-Key")
    @DisplayName("Должен вернуть ERROR при отсутствии X-Api-Key")
    @Description("Проверяет, что запрос без API ключа возвращает ошибку")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnErrorWhenMissingApiKey() {
        // Arrange
        String token = TestUtils.generateValidToken();
        
        // Act
        ApiResponse response = sendRequestWithoutApiKey(token, TestConfig.ACTION_LOGIN);
        
        // Assert
        assertThat(response.getResult())
            .as("Результат должен быть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Валидация X-Api-Key")
    @DisplayName("Должен вернуть ERROR при неверном X-Api-Key")
    @Description("Проверяет, что запрос с неправильным API ключом возвращает ошибку")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnErrorWhenInvalidApiKey() {
        // Arrange
        String token = TestUtils.generateValidToken();
        String wrongApiKey = "wrongkey123";
        
        // Act
        ApiResponse response = sendRequest(token, TestConfig.ACTION_LOGIN, wrongApiKey);
        
        // Assert
        assertThat(response.getResult())
            .as("Результат должен быть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Валидация токена")
    @DisplayName("Должен принять валидный токен (32 символа A-Z0-9)")
    @Description("Проверяет, что токен правильной длины и формата принимается")
    @Severity(SeverityLevel.CRITICAL)
    void shouldAcceptValidToken() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        logger.info("Сгенерирован валидный токен: {}", token);
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Валидный токен должен быть принят")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(token)
            .as("Токен должен соответствовать паттерну")
            .matches(TestConfig.VALID_TOKEN_PATTERN);
    }
    
    @Test
    @Story("Валидация токена")
    @DisplayName("Должен вернуть ERROR для короткого токена")
    @Description("Проверяет, что токен длиной меньше 32 символов отклоняется")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenTokenTooShort() {
        // Arrange
        String token = TestUtils.generateShortToken();
        logger.info("Сгенерирован короткий токен: {} (длина: {})", token, token.length());
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Короткий токен должен быть отклонен")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Валидация токена")
    @DisplayName("Должен вернуть ERROR для длинного токена")
    @Description("Проверяет, что токен длиной больше 32 символов отклоняется")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenTokenTooLong() {
        // Arrange
        String token = TestUtils.generateLongToken();
        logger.info("Сгенерирован длинный токен: {} (длина: {})", token, token.length());
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Длинный токен должен быть отклонен")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Валидация токена")
    @DisplayName("Должен вернуть ERROR для токена с lowercase символами")
    @Description("Проверяет, что токен с символами нижнего регистра отклоняется")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenTokenHasLowercaseChars() {
        // Arrange
        String token = TestUtils.generateLowercaseToken();
        logger.info("Сгенерирован lowercase токен: {}", token);
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Токен с lowercase символами должен быть отклонен")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Валидация токена")
    @DisplayName("Должен вернуть ERROR для токена со спецсимволами")
    @Description("Проверяет, что токен со специальными символами отклоняется")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenTokenHasSpecialChars() {
        // Arrange
        String token = TestUtils.generateSpecialCharsToken();
        logger.info("Сгенерирован токен со спецсимволами: {}", token);
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Токен со спецсимволами должен быть отклонен")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Валидация токена")
    @DisplayName("Должен вернуть ERROR для пустого токена")
    @Description("Проверяет, что пустой токен отклоняется")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenTokenEmpty() {
        // Arrange
        String token = "";
        logger.info("Используется пустой токен");
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Пустой токен должен быть отклонен")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Проверка взаимодействия с внешним сервисом")
    @DisplayName("Должен отправлять Content-Type на внешний сервис")
    @Description("Проверяет, что приложение отправляет правильный Content-Type на внешний сервис")
    @Severity(SeverityLevel.CRITICAL)
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
    
    @Test
    @Story("Проверка взаимодействия с внешним сервисом")
    @DisplayName("Должен отправлять Accept заголовок на внешний сервис")
    @Description("Проверяет, что приложение отправляет Accept: application/json на внешний сервис")
    @Severity(SeverityLevel.CRITICAL)
    void shouldSendCorrectAcceptHeaderToExternalService() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .withHeader("Accept", containing("application/json"))
            .willReturn(aResponse().withStatus(200)));
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_OK);
        getWireMock().verify(postRequestedFor(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .withHeader("Accept", containing("application/json")));
    }
    
    @Test
    @Story("Валидация параметров")
    @DisplayName("Должен вернуть ERROR для action в нижнем регистре")
    @Description("Проверяет, что action должен быть в верхнем регистре (LOGIN, а не login)")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnErrorWhenActionInLowercase() {
        // Arrange
        String token = TestUtils.generateValidToken();
        
        // Act
        ApiResponse response = sendRequest(token, "login");
        
        // Assert
        assertThat(response.getResult())
            .as("action в нижнем регистре должен быть отклонен")
            .isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Формат ответа")
    @DisplayName("Не должен возвращать поле message в успешном ответе")
    @Description("Проверяет, что в OK ответе отсутствует поле message")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnOnlyResultFieldInSuccessResponse() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_OK);
        assertThat(response.getMessage())
            .as("В успешном ответе не должно быть поля message")
            .isNull();
        
        // Cleanup
        performLogout(token);
    }
    
    @Test
    @Story("Валидация параметров")
    @DisplayName("Должен принимать параметры в любом порядке")
    @Description("Проверяет, что порядок параметров token и action не важен")
    @Severity(SeverityLevel.NORMAL)
    void shouldAcceptParametersInAnyOrder() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Act - отправляем с обратным порядком параметров через прямой запрос
        ApiResponse response = given()
            .spec(requestSpec)
            .formParam("action", TestConfig.ACTION_LOGIN)
            .formParam("token", token)
        .when()
            .post(TestConfig.ENDPOINT)
        .then()
            .extract()
            .as(ApiResponse.class);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_OK);
        
        // Cleanup
        performLogout(token);
    }
    
    @Test
    @Story("Валидация HTTP методов")
    @DisplayName("Должен вернуть ERROR для GET запроса")
    @Description("Проверяет, что эндпоинт принимает только POST запросы")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorForGetRequest() {
        // Arrange
        String token = TestUtils.generateValidToken();
        
        // Act
        int statusCode = given()
            .spec(requestSpec)
            .queryParam("token", token)
            .queryParam("action", TestConfig.ACTION_LOGIN)
        .when()
            .get(TestConfig.ENDPOINT)
        .then()
            .extract()
            .statusCode();
        
        // Assert
        assertThat(statusCode)
            .as("GET запрос должен вернуть ошибку (не 200)")
            .isNotEqualTo(200);
    }
    
    @Test
    @Story("Граничные значения токена")
    @DisplayName("Должен вернуть ERROR для токена длиной 31 символ")
    @Description("Проверяет граничное значение - токен на 1 символ короче требуемого")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnErrorWhenToken31Chars() {
        // Arrange
        String token = TestUtils.generateRandomString(31, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        logger.info("Токен 31 символ: {} (длина: {})", token, token.length());
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Граничные значения токена")
    @DisplayName("Должен вернуть ERROR для токена длиной 33 символа")
    @Description("Проверяет граничное значение - токен на 1 символ длиннее требуемого")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnErrorWhenToken33Chars() {
        // Arrange
        String token = TestUtils.generateRandomString(33, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
        logger.info("Токен 33 символа: {} (длина: {})", token, token.length());
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Валидация токена")
    @DisplayName("Должен вернуть ERROR для токена с пробелами")
    @Description("Проверяет, что токен с пробелами отклоняется")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnErrorWhenTokenHasSpaces() {
        // Arrange
        String token = "ABCD EFGH1234 5678ABCD EFGH1234";
        logger.info("Токен с пробелами: {}", token);
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_ERROR);
    }
}
