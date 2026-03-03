package com.example.aqa.tests;

import com.example.aqa.config.AppConfig;
import com.example.aqa.config.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end workflow tests.
 *
 * <p>These tests verify complete user journeys from start to finish,
 * combining multiple actions in sequence to ensure the application
 * behaves correctly across realistic usage scenarios.
 */
@Epic("Internal Service API")
@Feature("End-to-End Workflows")
@DisplayName("End-to-End User Workflows")
class EndToEndTest extends BaseTest {

    @Test
    @Story("Full happy path: LOGIN → ACTION → LOGOUT")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Complete happy path: LOGIN → ACTION → LOGOUT all return OK")
    @Description("""
            This test covers the full standard user journey:
            1. A user logs in with a valid token.
            2. The user performs an action.
            3. The user logs out.
            All three steps must return {"result": "OK"}.
            """)
    void fullHappyPath_loginActionLogout_allReturnOk() {
        stubAuthSuccess();
        stubDoActionSuccess();

        // Step 1: LOGIN
        Response loginResponse = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        // Step 2: ACTION
        Response actionResponse = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        // Step 3: LOGOUT
        Response logoutResponse = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("OK", loginResponse.jsonPath().getString("result"),
                        "LOGIN must return OK"),
                () -> assertEquals("OK", actionResponse.jsonPath().getString("result"),
                        "ACTION must return OK"),
                () -> assertEquals("OK", logoutResponse.jsonPath().getString("result"),
                        "LOGOUT must return OK")
        );
    }

    @Test
    @Story("Full cycle: LOGIN → ACTION → LOGOUT → LOGIN → ACTION")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token can complete a full session cycle and start a new one")
    @Description("""
            A token must be able to complete a full session cycle (LOGIN → ACTION → LOGOUT)
            and then start a new session (LOGIN → ACTION) without any issues.
            """)
    void fullCycle_thenNewSession_shouldSucceed() {
        stubAuthSuccess();
        stubDoActionSuccess();

        // First session
        performLogin(AppConfig.VALID_TOKEN);

        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        // Second session
        performLogin(AppConfig.VALID_TOKEN);

        Response secondAction = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertEquals("OK", secondAction.jsonPath().getString("result"),
                "ACTION in the second session must succeed");
    }

    @Test
    @Story("Concurrent sessions — two users")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Two users can have independent concurrent sessions")
    @Description("""
            Two different tokens (representing two users) must be able to
            log in, perform actions, and log out independently without
            interfering with each other.
            """)
    void twoUsers_concurrentSessions_shouldBeIndependent() {
        stubAuthSuccess();
        stubDoActionSuccess();

        // Both users log in
        performLogin(AppConfig.VALID_TOKEN);
        performLogin(AppConfig.VALID_TOKEN_2);

        // Both perform actions
        Response action1 = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        Response action2 = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN_2)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        // User 1 logs out
        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        // User 2 can still perform action
        Response action2After = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN_2)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("OK", action1.jsonPath().getString("result"),
                        "User 1 ACTION must succeed"),
                () -> assertEquals("OK", action2.jsonPath().getString("result"),
                        "User 2 ACTION must succeed"),
                () -> assertEquals("OK", action2After.jsonPath().getString("result"),
                        "User 2 ACTION after User 1 logout must still succeed")
        );
    }

    @Test
    @Story("Response structure — success")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Successful response contains only 'result' field with value 'OK'")
    @Description("""
            The specification defines that a successful response body is:
            {"result": "OK"}
            The response must contain the 'result' field with value 'OK'
            and must NOT contain a 'message' field.
            """)
    void successResponse_structureIsCorrect() {
        stubAuthSuccess();

        // Use a fresh token (VALID_TOKEN_2) to avoid 409 conflict from other tests
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN_2)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("OK", response.jsonPath().getString("result"),
                        "result field must be 'OK'"),
                () -> assertNull(response.jsonPath().getString("message"),
                        "Successful response must not contain 'message' field")
        );
    }

    @Test
    @Story("Response structure — error")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Error response contains 'result' = 'ERROR' and a non-empty 'message' field")
    @Description("""
            The specification defines that an error response body is:
            {"result": "ERROR", "message": "reason"}
            The response must contain both 'result' = 'ERROR' and a non-empty 'message'.
            """)
    void errorResponse_structureIsCorrect() {
        // No login — ACTION will fail with 403
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("ERROR", response.jsonPath().getString("result"),
                        "result field must be 'ERROR'"),
                () -> assertNotNull(response.jsonPath().getString("message"),
                        "Error response must contain 'message' field"),
                () -> assertFalse(response.jsonPath().getString("message").isBlank(),
                        "Error message must not be blank")
        );
    }

    @Test
    @Story("Content-Type of response")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Response Content-Type is application/json")
    @Description("""
            The endpoint declares Accept: application/json in the request.
            The application must respond with Content-Type: application/json.
            """)
    void response_contentTypeIsJson() {
        stubAuthSuccess();

        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertTrue(response.contentType().contains("application/json"),
                "Response Content-Type must be application/json");
    }
}
