package tests.logout;

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
@Feature("LOGOUT Action")
@DisplayName("Тесты LOGOUT операции")
public class LogoutTest extends BaseTest {
    
    @Test
    @Story("Успешное завершение сессии")
    @DisplayName("Должен вернуть OK при успешном LOGOUT после LOGIN")
    @Description("Проверяет, что LOGOUT выполняется успешно для токена прошедшего LOGIN")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnOkWhenLogoutAfterLogin() {
        // Arrange (Подготовка)
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Выполняем LOGIN
        performLogin(token);
        
        // Act (Действие)
        ApiResponse response = performLogout(token);
        
        // Assert (Проверка)
        assertThat(response.getResult())
            .as("Результат должен быть OK")
            .isEqualTo(TestConfig.RESULT_OK);
    }
    
    @Test
    @Story("Негативные сценарии")
    @DisplayName("Должен вернуть ERROR при LOGOUT без предварительного LOGIN")
    @Description("Проверяет, что LOGOUT без LOGIN возвращает ошибку")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnErrorWhenLogoutWithoutLogin() {
        // Arrange
        String token = TestUtils.generateValidToken();
        
        // Act (пытаемся выполнить LOGOUT без LOGIN)
        ApiResponse response = performLogout(token);
        
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
    @DisplayName("Должен вернуть ERROR при повторном LOGOUT")
    @Description("Проверяет, что повторный LOGOUT с тем же токеном возвращает ошибку")
    @Severity(SeverityLevel.MINOR)
    void shouldReturnErrorWhenRepeatedLogout() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        
        // Выполняем LOGIN и LOGOUT
        performLogin(token);
        ApiResponse firstLogout = performLogout(token);
        
        // Act (повторный LOGOUT)
        ApiResponse secondLogout = performLogout(token);
        
        // Assert
        assertThat(firstLogout.getResult())
            .as("Первый LOGOUT должен быть успешным")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(secondLogout.getResult())
            .as("Повторный LOGOUT должен вернуть ERROR")
            .isEqualTo(TestConfig.RESULT_ERROR);
    }
    
    @Test
    @Story("Проверка взаимодействия с внешним сервисом")
    @DisplayName("Не должен вызывать внешний сервис при LOGOUT")
    @Description("Проверяет, что LOGOUT не отправляет запросы на внешний сервис")
    @Severity(SeverityLevel.NORMAL)
    void shouldNotCallExternalServiceOnLogout() {
        // Arrange
        String token = TestUtils.generateValidToken();
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        performLogin(token);
        
        // Act
        performLogout(token);
        
        // Assert - проверяем что НЕ было вызовов к /doAction
        getWireMock().verify(0, postRequestedFor(urlEqualTo(TestConfig.MOCK_DO_ACTION_ENDPOINT)));
    }
}
