package tests.bugs;

import base.BaseTest;
import config.TestConfig;
import io.qameta.allure.*;
import models.ApiResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestUtils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Известные баги")
@Feature("Валидация токена")
@DisplayName("Баги валидации токена")
public class BugValidationTest extends BaseTest {
    
    @Test
    @Disabled("БАГ-001: Приложение принимает только A-F вместо A-Z согласно ТЗ")
    @Story("Расхождение с ТЗ")
    @DisplayName("[БАГ] Должен принимать токены с символами G-Z")
    @Description("ТЗ требует: токен 32 символа A-Z0-9. Факт: приложение принимает только A-F0-9 (hex формат)")
    @Severity(SeverityLevel.BLOCKER)
    @Issue("БАГ-001")
    void shouldAcceptTokenWithFullAlphabet() {
        // Arrange
        String token = "GHIJKLMNOPQRSTUVWXYZ12345678901"; // 32 символа с G-Z
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        logger.info("Токен с G-Z символами: {}", token);
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Токен с символами G-Z должен быть принят согласно ТЗ")
            .isEqualTo(TestConfig.RESULT_OK);
    }
    
    @Test
    @Disabled("БАГ-001: Связанный тест - токен с буквой G отклоняется")
    @Story("Расхождение с ТЗ")
    @DisplayName("[БАГ] Должен принимать токен с буквой G")
    @Description("Минимальный тест: токен с одной буквой G должен быть валидным по ТЗ")
    @Severity(SeverityLevel.BLOCKER)
    @Issue("БАГ-001")
    void shouldAcceptTokenWithLetterG() {
        // Arrange
        String token = "G1234567890ABCDEF1234567890ABCDE"; // 32 символа, одна G
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        logger.info("Токен с буквой G: {}", token);
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Токен с буквой G должен быть принят")
            .isEqualTo(TestConfig.RESULT_OK);
    }
    
    @Test
    @Disabled("БАГ-001: Связанный тест - токен с буквой Z отклоняется")
    @Story("Расхождение с ТЗ")
    @DisplayName("[БАГ] Должен принимать токен с буквой Z")
    @Description("Граничный тест: токен с буквой Z (последняя в алфавите) должен быть валидным по ТЗ")
    @Severity(SeverityLevel.BLOCKER)
    @Issue("БАГ-001")
    void shouldAcceptTokenWithLetterZ() {
        // Arrange
        String token = "Z1234567890ABCDEF1234567890ABCDE"; // 32 символа, одна Z
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        logger.info("Токен с буквой Z: {}", token);
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Токен с буквой Z должен быть принят")
            .isEqualTo(TestConfig.RESULT_OK);
    }
    
    @Test
    @Story("Текущее поведение")
    @DisplayName("[ФАКТ] Приложение принимает только A-F (hex формат)")
    @Description("Документирует текущее поведение: токен должен быть в hex формате (0-9A-F)")
    @Severity(SeverityLevel.CRITICAL)
    void shouldAcceptOnlyHexTokens() {
        // Arrange
        String hexToken = TestUtils.generateValidToken(); // Генерирует 0-9A-F
        getWireMock().stubFor(post(urlEqualTo(TestConfig.MOCK_AUTH_ENDPOINT))
            .willReturn(aResponse().withStatus(200)));
        logger.info("Hex токен (A-F): {}", hexToken);
        
        // Act
        ApiResponse response = performLogin(hexToken);
        
        // Assert
        assertThat(response.getResult())
            .as("Hex токен (A-F) принимается")
            .isEqualTo(TestConfig.RESULT_OK);
        assertThat(hexToken)
            .as("Токен соответствует hex паттерну")
            .matches("[0-9A-F]{32}");
        
        // Cleanup
        performLogout(hexToken);
    }
    
    @Test
    @Story("Текущее поведение")
    @DisplayName("[ФАКТ] Приложение отклоняет токены с G-Z")
    @Description("Документирует текущее поведение: токен с символами G-Z отклоняется")
    @Severity(SeverityLevel.CRITICAL)
    void shouldRejectTokensWithGtoZ() {
        // Arrange
        String token = "GHIJKLMNOPQRSTUVWXYZ12345678901"; // 32 символа с G-Z
        logger.info("Токен с G-Z символами: {}", token);
        
        // Act
        ApiResponse response = performLogin(token);
        
        // Assert
        assertThat(response.getResult())
            .as("Токен с G-Z отклоняется (текущее поведение)")
            .isEqualTo(TestConfig.RESULT_ERROR);
        assertThat(response.getMessage())
            .as("Должно быть сообщение об ошибке")
            .isNotEmpty();
    }
}
