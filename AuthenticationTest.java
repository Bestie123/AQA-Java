package com.example.aqa.tests;

import com.example.aqa.config.AppConfig;
import com.example.aqa.config.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for API key authentication (X-Api-Key header).
 *
 * <p>The application must reject all requests that do not carry a valid API key.
 * These tests verify that the security gate works correctly before any business
 * logic is executed.
 */
@Epic("Internal Service API")
@Feature("Authentication — API Key Validation")
@DisplayName("API Key Authentication")
class AuthenticationTest extends BaseTest {

    @Test
    @Story("Missing API key")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Request without X-Api-Key header is rejected with HTTP 401")
    @Description("""
            When a client sends a request without the X-Api-Key header,
            the application must return HTTP 401 and an error message
            indicating that the API key is missing or invalid.
            """)
    void requestWithoutApiKey_shouldReturn401() {
        Response response = io.restassured.RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(401, response.statusCode(),
                        "HTTP status should be 401 Unauthorized"),
                () -> assertEquals("ERROR", response.jsonPath().getString("result"),
                        "result field should be ERROR"),
                () -> assertNotNull(response.jsonPath().getString("message"),
                        "Error message must be present")
        );
    }

    @Test
    @Story("Wrong API key")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Request with incorrect X-Api-Key header is rejected with HTTP 401")
    @Description("""
            When a client sends a request with a wrong API key value,
            the application must return HTTP 401 regardless of the token
            or action provided.
            """)
    void requestWithWrongApiKey_shouldReturn401() {
        Response response = io.restassured.RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .header("X-Api-Key", AppConfig.INVALID_API_KEY)
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(401, response.statusCode(),
                        "HTTP status should be 401 Unauthorized"),
                () -> assertEquals("ERROR", response.jsonPath().getString("result"),
                        "result field should be ERROR"),
                () -> assertThat(response.jsonPath().getString("message"),
                        containsString("API Key"))
        );
    }

    @Test
    @Story("Valid API key")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Request with correct X-Api-Key header passes authentication")
    @Description("""
            When a client sends a request with the correct API key,
            the request must pass the authentication gate and reach
            the business logic layer (response is not 401).
            """)
    void requestWithValidApiKey_shouldPassAuthentication() {
        // We do not set up WireMock here; the app will fail on the mock call,
        // but the important thing is that authentication itself succeeds (no 401).
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertNotEquals(401, response.statusCode(),
                "A valid API key must not result in HTTP 401");
    }
}
