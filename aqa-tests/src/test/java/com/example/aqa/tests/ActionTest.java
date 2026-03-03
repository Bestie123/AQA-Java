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
 * Tests for the ACTION action.
 *
 * <p>ACTION triggers a POST request to the external service's {@code /doAction} endpoint.
 * It is only available for tokens that have previously performed a successful LOGIN.
 * Tokens that have not logged in, or have been logged out, must be rejected.
 *
 * <p>Observed HTTP status codes from the application:
 * <ul>
 *   <li>200 — action performed successfully</li>
 *   <li>403 Forbidden — token not found in session store (not logged in)</li>
 *   <li>500 — external service returned an error</li>
 * </ul>
 */
@Epic("Internal Service API")
@Feature("ACTION — Perform User Action")
@DisplayName("ACTION — Perform User Action")
class ActionTest extends BaseTest {

    @Test
    @Story("Successful action after login")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("ACTION succeeds for a logged-in token when external /doAction returns HTTP 200")
    @Description("""
            After a successful LOGIN, the token is stored in the internal session store.
            When ACTION is called with that token and the external /doAction service
            responds with HTTP 200, the application must return {"result": "OK"}.
            """)
    void action_afterSuccessfulLogin_shouldReturnOk() {
        stubAuthSuccess();
        stubDoActionSuccess();

        // Step 1: Login
        performLogin(AppConfig.VALID_TOKEN);

        // Step 2: Perform action
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(200, response.statusCode(),
                        "HTTP status should be 200 OK"),
                () -> assertEquals("OK", response.jsonPath().getString("result"),
                        "result field should be OK")
        );

        // Verify the application called /doAction on the external service
        wireMockServer.verify(postRequestedFor(urlEqualTo(AppConfig.MOCK_DO_ACTION_PATH))
                .withRequestBody(containing("token=" + AppConfig.VALID_TOKEN)));
    }

    @Test
    @Story("Action without prior login")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("ACTION is rejected with HTTP 403 for a token that has not logged in")
    @Description("""
            A token that has never performed LOGIN must not be allowed to call ACTION.
            The application must return HTTP 403 Forbidden with {"result": "ERROR"}
            and a message indicating that the token was not found in the session store.
            """)
    void action_withoutLogin_shouldReturn403() {
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(403, response.statusCode(),
                        "ACTION without login must return HTTP 403 Forbidden"),
                () -> assertEquals("ERROR", response.jsonPath().getString("result"),
                        "result must be ERROR"),
                () -> assertNotNull(response.jsonPath().getString("message"),
                        "Error response must contain a message")
        );

        // The external /doAction must NOT have been called
        wireMockServer.verify(0, postRequestedFor(urlEqualTo(AppConfig.MOCK_DO_ACTION_PATH)));
    }

    @Test
    @Story("Action fails — external service error")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("ACTION fails with HTTP 500 when external /doAction returns HTTP 500")
    @Description("""
            Even for a logged-in token, if the external /doAction service
            responds with an error (HTTP 500), the application must propagate
            the error and return {"result": "ERROR"} to the client.
            """)
    void action_whenDoActionServiceReturns500_shouldReturnError() {
        stubAuthSuccess();
        stubDoActionFailure();

        performLogin(AppConfig.VALID_TOKEN);

        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertEquals("ERROR", response.jsonPath().getString("result"),
                "ACTION must return ERROR when external service fails");
    }

    @Test
    @Story("Action forwards correct token to external service")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("ACTION sends the correct token to the external /doAction endpoint")
    @Description("""
            The application must forward the exact token provided by the client
            to the external /doAction endpoint as a form parameter named 'token'.
            """)
    void action_shouldForwardTokenToExternalService() {
        stubAuthSuccess();
        stubDoActionSuccess();

        performLogin(AppConfig.VALID_TOKEN);

        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        wireMockServer.verify(postRequestedFor(urlEqualTo(AppConfig.MOCK_DO_ACTION_PATH))
                .withRequestBody(containing("token=" + AppConfig.VALID_TOKEN)));
    }

    @Test
    @Story("Action after failed login")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("ACTION is rejected when the preceding LOGIN had failed")
    @Description("""
            If a LOGIN attempt failed (e.g., external service returned 500),
            the token must NOT be stored in the session store.
            A subsequent ACTION call with that token must be rejected with HTTP 403.
            """)
    void action_afterFailedLogin_shouldReturn403() {
        stubAuthFailure();

        // Attempt login — it will fail
        performLogin(AppConfig.VALID_TOKEN);

        // Now try ACTION — should be rejected because login failed
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(403, response.statusCode(),
                        "ACTION must return HTTP 403 when the prior LOGIN had failed"),
                () -> assertEquals("ERROR", response.jsonPath().getString("result"),
                        "result must be ERROR")
        );
    }

    @Test
    @Story("Multiple actions with same token")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("A logged-in token can perform ACTION multiple times")
    @Description("""
            After a successful LOGIN, the same token must be able to call ACTION
            multiple times without requiring a new LOGIN each time.
            """)
    void action_multipleTimesWithSameToken_shouldAllSucceed() {
        stubAuthSuccess();
        stubDoActionSuccess();

        performLogin(AppConfig.VALID_TOKEN);

        Response r1 = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        Response r2 = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("OK", r1.jsonPath().getString("result"),
                        "First ACTION should succeed"),
                () -> assertEquals("OK", r2.jsonPath().getString("result"),
                        "Second ACTION should also succeed")
        );
    }

    @Test
    @Story("Session isolation between tokens")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("ACTION for token A does not affect token B's session")
    @Description("""
            Each token has its own independent session. Performing ACTION with
            token A must not interfere with token B's session state.
            Token B must still be able to perform ACTION independently.
            """)
    void action_sessionIsolation_tokenADoesNotAffectTokenB() {
        stubAuthSuccess();
        stubDoActionSuccess();

        performLogin(AppConfig.VALID_TOKEN);
        performLogin(AppConfig.VALID_TOKEN_2);

        Response rA = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        Response rB = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN_2)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("OK", rA.jsonPath().getString("result"),
                        "Token A ACTION should succeed"),
                () -> assertEquals("OK", rB.jsonPath().getString("result"),
                        "Token B ACTION should succeed independently")
        );
    }
}
