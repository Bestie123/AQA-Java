package tests.action;

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
@Feature("ACTION Action")
@DisplayName("Тесты ACTION операции")
public class ActionTest extends BaseTest {
    
    @Test
    @Story("Успешное выполнение действия")
    @DisplayName("Должен вернуть OK при успешном ACTION после LOGIN")
    @Description("Проверяет, что ACTION выполняется успешно для токена прошедшего LOGIN")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnOkWhenActionAfterLogin() {
        // Arrange (Подготовка)
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
            .withRequestBody(containing("token=" + token))
            .willReturn(aResponse().withStatus(200)));
        
        // Выполняем LOGIN
        performLogin(token);
        
        // Act (Действие)
        ApiResponse response = performAction(token);
        
        // Assert (Проверка)
        assertThat(response.getResult())
            .as("Результат должен быть OK")
            .isEqualTo(TestConfig.RESULT_OK);
        
        // Проверяем что запрос был отправлен на внешний сервис
        getWireMock().verify(postRequestedFor(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
            .withRequestBody(containing("token=" + token)));
        
        // Cleanup
        performLogout(token);
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR при ACTION без предварительного LOGIN")
    @Description("Проверяет, что ACTION без LOGIN возвращает ошибку")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnErrorWhenActionWithoutLogin() {
        // Arrange
        String token = TestUtils.generateValidToken();
        
        // Act (пытаемся выполнить ACTION без LOGIN)
        ApiResponse response = performAction(token);
        
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
    @DisplayName("Должен вернуть ERROR когда внешний сервис возвращает 400")
    @Description("Проверяет, что ACTION возвращает ошибку когда внешний сервис /doAction отвечает 400")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenExternalServiceReturns400() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
            .willReturn(aResponse().withStatus(400)));
        
        // Выполняем LOGIN
        performLogin(token);
        
        // Act
        ApiResponse response = performAction(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Результат должен быть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
        
        // Cleanup
        performLogout(token);
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR когда внешний сервис возвращает 500")
    @Description("Проверяет, что ACTION возвращает ошибку когда внешний сервис /doAction отвечает 500")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenExternalServiceReturns500() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
            .willReturn(aResponse().withStatus(500)));
        
        // Выполняем LOGIN
        performLogin(token);
        
        // Act
        ApiResponse response = performAction(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Результат должен быть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
        
        // Cleanup
        performLogout(token);
    }
}
