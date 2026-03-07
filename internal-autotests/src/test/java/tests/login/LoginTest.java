package tests.login;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.*;
import models.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Testing")
@Feature("LOGIN Action")
@DisplayName("Тесты LOGIN операции")
public class LoginTest extends BaseTest {
    
    @Test
    @Story("Успешная аутентификация")
    @DisplayName("Должен вернуть OK при успешном LOGIN")
    @Description("Проверяет, что LOGIN с валидным токеном возвращает успех когда внешний сервис отвечает 200")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnOkWhenLoginSuccessful() {
        // Arrange (Подготовка)
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .withRequestBody(containing("token=" + token))
            .willReturn(aResponse().withStatus(200)));
        
        // Act (Действие)
        ApiResponse response = performLogin(token);
        
        // Assert (Проверка)
        assertThat(response.getResult())
            .as("Результат должен быть OK")
            .isEqualTo(TestConfig.RESULT_OK);
        
        // Проверяем что запрос был отправлен на внешний сервис
        getWireMock().verify(postRequestedFor(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .withRequestBody(containing("token=" + token)));
        
        // Cleanup
        performLogout(token);
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR когда внешний сервис возвращает 400")
    @Description("Проверяет, что LOGIN возвращает ошибку когда внешний сервис /auth отвечает 400")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenExternalServiceReturns400() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Результат должен быть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR когда внешний сервис возвращает 500")
    @Description("Проверяет, что LOGIN возвращает ошибку когда внешний сервис /auth отвечает 500")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenExternalServiceReturns500() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(500)));
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Результат должен быть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Повторные операции")
    @DisplayName("Должен вернуть ERROR при повторном LOGIN с тем же токеном")
    @Description("Проверяет, что повторный LOGIN с уже зарегистрированным токеном возвращает ошибку")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenRepeatedLogin() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .withRequestBody(containing("token=" + token))
            .willReturn(aResponse().withStatus(200)));
        
        // Act
        ApiResponse firstLogin = performLogin(token);
        ApiResponse secondLogin = performLogin(token);
        
        // Assert
        assertThat(firstLogin.getResult())
            .as("Первый LOGIN должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(secondLogin.getResult())
            .as("Повторный LOGIN должен вернуть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(secondLogin.getMessage())
            .as("Должно быть сообщение об ошибке")
            .contains("already exists");
        
        // Cleanup
        performLogout(token);
    }
    
    @Test
    @Story("Валидация параметров")
    @DisplayName("Должен вернуть ERROR при отсутствии параметра action")
    @Description("Проверяет, что запрос без параметра action возвращает ошибку")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenActionMissing() {
        // Arrange
        String token = TestUtils.generateValidToken();
        
        // Act
        ApiResponse response = sendRequest(token, null);
        
        // Assert
        assertThat(response.getResult())
            .as("Результат должен быть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Валидация параметров")
    @DisplayName("Должен вернуть ERROR при неизвестном значении action")
    @Description("Проверяет, что запрос с неизвестным action возвращает ошибку")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenActionUnknown() {
        // Arrange
        String token = TestUtils.generateValidToken();
        String unknownAction = "UNKNOWN_ACTION";
        
        // Act
        ApiResponse response = sendRequest(token, unknownAction);
        
        // Assert
        assertThat(response.getResult())
            .as("Результат должен быть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR когда внешний сервис возвращает 401")
    @Description("Проверяет, что LOGIN возвращает ошибку при ответе 401 от внешнего сервиса")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnErrorWhenExternalServiceReturns401() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(401)));
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR когда внешний сервис возвращает 403")
    @Description("Проверяет, что LOGIN возвращает ошибку при ответе 403 от внешнего сервиса")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnErrorWhenExternalServiceReturns403() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(403)));
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR когда внешний сервис возвращает 404")
    @Description("Проверяет, что LOGIN возвращает ошибку при ответе 404 от внешнего сервиса")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnErrorWhenExternalServiceReturns404() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(404)));
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR когда внешний сервис возвращает 503")
    @Description("Проверяет, что LOGIN возвращает ошибку когда внешний сервис недоступен")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnErrorWhenExternalServiceReturns503() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(503)));
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult()).isEqualTo(TestConfig.RESULT_ERROR);
    }
}
