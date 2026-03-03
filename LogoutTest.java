package com.example.aqa.tests;

import com.example.aqa.config.AppConfig;
import com.example.aqa.config.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the LOGOUT action.
 *
 * <p>LOGOUT removes a token from the internal session store.
 * After a successful logout, the token must no longer be able to perform ACTION.
 * LOGOUT on a token that was never logged in must return an error.
 */
@Epic("Internal Service API")
@Feature("LOGOUT — End User Session")
@DisplayName("LOGOUT — End User Session")
class LogoutTest extends BaseTest {

    @Test
    @Story("Successful logout")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("LOGOUT succeeds for a logged-in token and returns OK")
    @Description("""
            After a successful LOGIN, calling LOGOUT with the same token must
            remove it from the internal session store and return {"result": "OK"}.
            """)
    void logout_afterLogin_shouldReturnOk() {
        stubAuthSuccess();
        performLogin(AppConfig.VALID_TOKEN);

        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(200, response.statusCode(),
                        "HTTP status should be 200 OK"),
                () -> assertEquals("OK", response.jsonPath().getString("result"),
                        "result field should be OK after logout")
        );
    }

    @Test
    @Story("Logout without prior login")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("LOGOUT is rejected for a token that has not logged in")
    @Description("""
            Attempting to log out a token that was never logged in must return
            {"result": "ERROR"} because the token does not exist in the session store.
            """)
    void logout_withoutLogin_shouldReturnError() {
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals("ERROR", response.jsonPath().getString("result"),
                        "LOGOUT without prior login must return ERROR"),
                () -> assertNotNull(response.jsonPath().getString("message"),
                        "Error response must contain a message")
        );
    }

    @Test
    @Story("Session invalidation after logout")
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("After LOGOUT, ACTION is no longer available for the same token")
    @Description("""
            Once a token has been logged out, it must be removed from the session store.
            Any subsequent ACTION call with that token must be rejected with ERROR,
            as if the token had never logged in.
            """)
    void logout_thenAction_shouldReturnError() {
        stubAuthSuccess();
        stubDoActionSuccess();

        // Login
        performLogin(AppConfig.VALID_TOKEN);

        // Logout
        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        // Try ACTION after logout — must fail
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertEquals("ERROR", response.jsonPath().getString("result"),
                "ACTION must be rejected after the token has been logged out");
    }

    @Test
    @Story("Re-login after logout")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("A logged-out token can log in again successfully")
    @Description("""
            After a LOGOUT, the token must be able to perform a new LOGIN.
            The second login must succeed and restore the ability to call ACTION.
            """)
    void logout_thenLoginAgain_shouldSucceed() {
        stubAuthSuccess();
        stubDoActionSuccess();

        // First login
        performLogin(AppConfig.VALID_TOKEN);

        // Logout
        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        // Second login
        Response loginResponse = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertEquals("OK", loginResponse.jsonPath().getString("result"),
                "Re-login after logout must succeed");
    }

    @Test
    @Story("Re-login after logout — action available again")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("After re-login, ACTION is available again for the token")
    @Description("""
            After logging out and logging back in, the token must be able
            to perform ACTION successfully, confirming that the session
            was properly re-established.
            """)
    void logout_thenLoginAndAction_shouldSucceed() {
        stubAuthSuccess();
        stubDoActionSuccess();

        // First login
        performLogin(AppConfig.VALID_TOKEN);

        // Logout
        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        // Second login
        performLogin(AppConfig.VALID_TOKEN);

        // Action after re-login
        Response actionResponse = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertEquals("OK", actionResponse.jsonPath().getString("result"),
                "ACTION must succeed after re-login");
    }

    @Test
    @Story("Logout does not affect other tokens")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Logging out token A does not invalidate token B's session")
    @Description("""
            Sessions are independent per token. Logging out token A must not
            affect token B's session. Token B must still be able to perform ACTION.
            """)
    void logout_tokenA_doesNotAffectTokenB() {
        stubAuthSuccess();
        stubDoActionSuccess();

        performLogin(AppConfig.VALID_TOKEN);
        performLogin(AppConfig.VALID_TOKEN_2);

        // Logout token A
        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        // Token B should still be able to perform ACTION
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN_2)
                .formParam("action", "ACTION")
                .post(AppConfig.ENDPOINT);

        assertEquals("OK", response.jsonPath().getString("result"),
                "Token B's session must remain valid after token A logs out");
    }

    @Test
    @Story("Double logout")
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Logging out the same token twice returns ERROR on the second attempt")
    @Description("""
            After a token has been logged out, a second LOGOUT call with the same
            token must return {"result": "ERROR"} because the token no longer
            exists in the session store.
            """)
    void logout_twice_secondShouldReturnError() {
        stubAuthSuccess();

        performLogin(AppConfig.VALID_TOKEN);

        // First logout — should succeed
        validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        // Second logout — should fail
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGOUT")
                .post(AppConfig.ENDPOINT);

        assertEquals("ERROR", response.jsonPath().getString("result"),
                "Second LOGOUT for the same token must return ERROR");
    }
}
