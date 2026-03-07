package base;

import config.TestConfig;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import models.ApiResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.restassured.RestAssured.given;

@ExtendWith(WireMockExtension.class)
public abstract class BaseTest {
    
    protected static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    protected static RequestSpecification requestSpec;
    
    @BeforeAll
    static void globalSetup() {
        logger.info("=== Начало выполнения тестов ===");
        
        // Настройка REST Assured
        RestAssured.baseURI = TestConfig.BASE_URL;
        
        requestSpec = new RequestSpecBuilder()
            .setBaseUri(TestConfig.BASE_URL)
            .setContentType(TestConfig.CONTENT_TYPE_FORM)
            .setAccept(TestConfig.CONTENT_TYPE_JSON)
            .addHeader(TestConfig.API_KEY_HEADER, TestConfig.API_KEY)
            .build();
        
        logger.info("REST Assured настроен для {}", TestConfig.BASE_URL);
    }
    
    @BeforeEach
    void setup(TestInfo testInfo) {
        logger.info("Запуск теста: {}", testInfo.getDisplayName());
        // Очистка всех стабов перед каждым тестом
        getWireMock().resetAll();
    }
    
    /**
     * Получить WireMock сервер для настройки стабов
     */
    protected com.github.tomakehurst.wiremock.WireMockServer getWireMock() {
        return WireMockExtension.getWireMockServer();
    }
    
    @AfterEach
    void teardown(TestInfo testInfo) {
        logger.info("Завершение теста: {}", testInfo.getDisplayName());
    }
    
    /**
     * Отправляет запрос к эндпоинту приложения
     */
    protected ApiResponse sendRequest(String token, String action) {
        return sendRequest(token, action, TestConfig.API_KEY);
    }
    
    /**
     * Отправляет запрос к эндпоинту приложения с указанным API ключом
     */
    @Step("Отправка запроса: token={token}, action={action}")
    protected ApiResponse sendRequest(String token, String action, String apiKey) {
        logger.info("Отправка запроса: token={}, action={}", token, action);
        
        ApiResponse response = given()
            .spec(requestSpec)
            .header(TestConfig.API_KEY_HEADER, apiKey)
            .formParam("token", token)
            .formParam("action", action)
        .when()
            .post(TestConfig.ENDPOINT)
        .then()
            .extract()
            .as(ApiResponse.class);
        
        logger.info("Получен ответ: {}", response);
        Allure.addAttachment("Response", "application/json", response.toString());
        return response;
    }
    
    /**
     * Отправляет запрос без API ключа
     */
    @Step("Отправка запроса без API ключа: token={token}, action={action}")
    protected ApiResponse sendRequestWithoutApiKey(String token, String action) {
        logger.info("Отправка запроса без API ключа: token={}, action={}", token, action);
        
        ApiResponse response = given()
            .baseUri(TestConfig.BASE_URL)
            .contentType(TestConfig.CONTENT_TYPE_FORM)
            .accept(TestConfig.CONTENT_TYPE_JSON)
            .formParam("token", token)
            .formParam("action", action)
        .when()
            .post(TestConfig.ENDPOINT)
        .then()
            .extract()
            .as(ApiResponse.class);
        
        logger.info("Получен ответ: {}", response);
        Allure.addAttachment("Response", "application/json", response.toString());
        return response;
    }
    
    /**
     * Прикрепляет данные к Allure отчету
     */
    protected void attachToAllure(String name, String content, String type) {
        Allure.addAttachment(name, type, content, "txt");
    }
    
    /**
     * Выполняет LOGIN для токена
     */
    protected ApiResponse performLogin(String token) {
        return sendRequest(token, TestConfig.ACTION_LOGIN);
    }
    
    /**
     * Выполняет ACTION для токена
     */
    protected ApiResponse performAction(String token) {
        return sendRequest(token, TestConfig.ACTION_ACTION);
    }
    
    /**
     * Выполняет LOGOUT для токена
     */
    protected ApiResponse performLogout(String token) {
        return sendRequest(token, TestConfig.ACTION_LOGOUT);
    }
}
