package tests.integration;

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
@Feature("Интеграционные сценарии")
@DisplayName("Интеграционные тесты")
public class IntegrationTest extends BaseTest {
    
    @Test
    @Story("Полный флоу")
    @DisplayName("Должен успешно выполнить полный флоу: LOGIN → ACTION → LOGOUT")
    @Description("Проверяет, что полный жизненный цикл токена работает корректно")
    @Severity(SeverityLevel.BLOCKER)
    void shouldCompleteFullFlowSuccessfully() {
        // Arrange (Подготовка)
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Act (Действие)
        ApiResponse loginResponse = performLogin(token);
        ApiResponse actionResponse = performAction(token);
        ApiResponse logoutResponse = performLogout(token);
        
        // Assert (Проверка)
        assertThat(loginResponse.getResult())
            .as("LOGIN должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(actionResponse.getResult())
            .as("ACTION должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(logoutResponse.getResult())
            .as("LOGOUT должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
    }
    
    @Test
    @Story("Множественные операции")
    @DisplayName("Должен разрешить множественные ACTION между LOGIN и LOGOUT")
    @Description("Проверяет, что можно выполнить несколько ACTION после одного LOGIN")
    @Severity(SeverityLevel.NORMAL)
    void shouldAllowMultipleActionsAfterLogin() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Act
        performLogin(token);
        ApiResponse action1 = performAction(token);
        ApiResponse action2 = performAction(token);
        ApiResponse action3 = performAction(token);
        performLogout(token);
        
        // Assert
        assertThat(action1.getResult())
            .as("Первый ACTION должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(action2.getResult())
            .as("Второй ACTION должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(action3.getResult())
            .as("Третий ACTION должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR при ACTION после LOGOUT")
    @Description("Проверяет, что ACTION после LOGOUT возвращает ошибку")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnErrorWhenActionAfterLogout() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Act
        performLogin(token);
        performLogout(token);
        ApiResponse actionAfterLogout = performAction(token);
        
        // Assert
        assertThat(actionAfterLogout.getResult())
            .as("ACTION после LOGOUT должен вернуть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(actionAfterLogout.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
    
    @Test
    @Story("Повторные операции")
    @DisplayName("Должен разрешить повторный LOGIN после LOGOUT")
    @Description("Проверяет, что можно выполнить LOGIN снова после LOGOUT")
    @Severity(SeverityLevel.NORMAL)
    void shouldAllowLoginAfterLogout() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Act
        ApiResponse firstLogin = performLogin(token);
        performLogout(token);
        ApiResponse secondLogin = performLogin(token);
        
        // Assert
        assertThat(firstLogin.getResult())
            .as("Первый LOGIN должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(secondLogin.getResult())
            .as("Повторный LOGIN после LOGOUT должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        
        // Cleanup
        performLogout(token);
    }
    
    @Test
    @Story("Параллельные сессии")
    @DisplayName("Должен поддерживать параллельные сессии с разными токенами")
    @Description("Проверяет, что разные токены работают независимо друг от друга")
    @Severity(SeverityLevel.NORMAL)
    void shouldSupportParallelSessions() {
        // Arrange
        String token1 = TestUtils.generateValidToken();
        String token2 = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Act
        performLogin(token1);
        performLogin(token2);
        
        ApiResponse action1 = performAction(token1);
        ApiResponse action2 = performAction(token2);
        
        performLogout(token1);
        
        // Проверяем что token2 все еще работает после LOGOUT token1
        ApiResponse action2AfterLogout1 = performAction(token2);
        
        // Assert
        assertThat(action1.getResult())
            .as("ACTION для token1 должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(action2.getResult())
            .as("ACTION для token2 должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(action2AfterLogout1.getResult())
            .as("ACTION для token2 должен работать после LOGOUT token1")
            .isEqualTo(TestConfig.RESULT_OK);
        
        // Cleanup
        performLogout(token2);
    }
}
