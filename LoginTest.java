package com.example.aqa.tests;

import com.example.aqa.config.AppConfig;
import com.example.aqa.config.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LOGIN action.
 *
 * <p>LOGIN triggers a POST request to the external service's {@code /auth} endpoint.
 * If the external service responds with HTTP 2xx, the token is stored internally
 * and subsequent ACTION calls become available for that token.
 * If the external service responds with an error, the login must fail.
 *
 * <p>Observed HTTP status codes from the application:
 * <ul>
 *   <li>200 — successful login</li>
 *   <li>409 Conflict — token is already logged in</li>
 *   <li>500 — external service returned an error</li>
 * </ul>
 */
@Epic("Internal Service API")
@Feature("LOGIN Action")
@DisplayName("LOGIN — User Authentication")
class LoginTest extends BaseTest {

    @Test
    @Story("Successful login")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("LOGIN succeeds when external /auth returns HTTP 200")
    @Description("""
            When the external authentication service responds with HTTP 200,
            the application must accept the login, store the token internally,
            and return {"result": "OK"} with HTTP 200.
            """)
    void login_whenAuthServiceReturns200_shouldReturnOk() {
        stubAuthSuccess();

        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(200, response.statusCode(),
                        "HTTP status should be 200 OK"),
                () -> assertEquals("OK", response.jsonPath().getString("result"),
                        "result field should be OK"),
                () -> assertNull(response.jsonPath().getString("message"),
                        "Successful response must not contain a message field")
        );

        // Verify the application actually called /auth on the external service
        wireMockServer.verify(postRequestedFor(urlEqualTo(AppConfig.MOCK_AUTH_PATH))
                .withRequestBody(containing("token=" + AppConfig.VALID_TOKEN)));
    }

    @Test
    @Story("Failed login — external service error")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("LOGIN fails when external /auth returns HTTP 500")
    @Description("""
            When the external authentication service responds with an error (HTTP 500),
            the application must reject the login and return {"result": "ERROR"}.
            The token must NOT be stored internally.
            """)
    void login_whenAuthServiceReturns500_shouldReturnError() {
        stubAuthFailure();

        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("ERROR", response.jsonPath().getString("result"),
                        "result field should be ERROR when auth service fails"),
                () -> assertNotNull(response.jsonPath().getString("message"),
                        "Error response must contain a message")
        );
    }

    @Test
    @Story("Failed login — external service unavailable")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("LOGIN fails when external /auth is unavailable (no stub configured)")
    @Description("""
            When the external authentication service is completely unavailable
            (connection refused or unexpected response), the application must
            handle the error gracefully and return {"result": "ERROR"} rather
            than crashing or hanging.
            """)
    void login_whenAuthServiceUnavailable_shouldReturnError() {
        // No WireMock stub — WireMock returns 404 for unstubbed requests by default
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertEquals("ERROR", response.jsonPath().getString("result"),
                "Application must return ERROR when external service is unavailable");
    }

    @Test
    @Story("Login forwards correct token to external service")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("LOGIN sends the correct token to the external /auth endpoint")
    @Description("""
            The application must forward the exact token provided by the client
            to the external /auth endpoint as a form parameter named 'token'.
            """)
    void login_shouldForwardTokenToExternalService() {
        stubAuthSuccess();

        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        wireMockServer.verify(postRequestedFor(urlEqualTo(AppConfig.MOCK_AUTH_PATH))
                .withRequestBody(containing("token=" + AppConfig.VALID_TOKEN)));
    }

    @Test
    @Story("Multiple logins with different tokens")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Two different tokens can be logged in independently")
    @Description("""
            The application must support multiple simultaneous sessions.
            Two different tokens must each be able to log in independently,
            and both must receive {"result": "OK"}.
            """)
    void login_twoDistinctTokens_bothShouldSucceed() {
        stubAuthSuccess();

        Response r1 = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        Response r2 = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN_2)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("OK", r1.jsonPath().getString("result"),
                        "First token login should succeed"),
                () -> assertEquals("OK", r2.jsonPath().getString("result"),
                        "Second token login should succeed")
        );
    }

    @Test
    @Story("Repeated login with same token")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Logging in with the same token twice returns HTTP 409 Conflict")
    @Description("""
            If a token that is already logged in attempts to log in again,
            the application must reject the second request with HTTP 409 Conflict
            and return {"result": "ERROR"} with a message indicating the token
            already exists in the session store.
            """)
    void login_sameTokenTwice_shouldReturn409Conflict() {
        stubAuthSuccess();

        // First login — must succeed
        Response firstLogin = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertEquals("OK", firstLogin.jsonPath().getString("result"),
                "First login must succeed");

        // Second login with the same token — must be rejected
        Response secondLogin = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(409, secondLogin.statusCode(),
                        "Second login with the same token must return HTTP 409 Conflict"),
                () -> assertEquals("ERROR", secondLogin.jsonPath().getString("result"),
                        "result must be ERROR"),
                () -> assertNotNull(secondLogin.jsonPath().getString("message"),
                        "Error message must be present")
        );
    }
}
