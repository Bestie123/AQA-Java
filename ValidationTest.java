package com.example.aqa.tests;

import com.example.aqa.config.AppConfig;
import com.example.aqa.config.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for input parameter validation.
 *
 * <p>The endpoint accepts two parameters: {@code token} and {@code action}.
 * This suite verifies that the application correctly validates both fields
 * and returns meaningful error messages when the input is invalid.
 */
@Epic("Internal Service API")
@Feature("Input Validation")
@DisplayName("Input Parameter Validation")
class ValidationTest extends BaseTest {

    // ─────────────────────────────────────────────────────────────────────────
    // Token validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Token validation — missing token")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Request without token parameter returns HTTP 400")
    @Description("""
            The token parameter is mandatory. When it is absent,
            the application must return HTTP 400 with an error message
            explaining that the token must not be null.
            """)
    void missingToken_shouldReturn400() {
        Response response = validRequest()
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(400, response.statusCode(),
                        "HTTP status should be 400 Bad Request"),
                () -> assertEquals("ERROR", response.jsonPath().getString("result")),
                () -> assertThat(response.jsonPath().getString("message"),
                        containsStringIgnoringCase("token"))
        );
    }

    @Test
    @Story("Token validation — token too short")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token shorter than 32 characters returns HTTP 400")
    @Description("""
            The token must be exactly 32 uppercase hexadecimal characters.
            A token that is too short must be rejected with HTTP 400.
            """)
    void shortToken_shouldReturn400() {
        Response response = validRequest()
                .formParam("token", AppConfig.SHORT_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(400, response.statusCode()),
                () -> assertEquals("ERROR", response.jsonPath().getString("result")),
                () -> assertThat(response.jsonPath().getString("message"),
                        containsStringIgnoringCase("token"))
        );
    }

    @Test
    @Story("Token validation — lowercase characters")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Token with lowercase letters returns HTTP 400")
    @Description("""
            The token must consist only of uppercase characters [0-9A-F].
            A token containing lowercase letters must be rejected.
            """)
    void lowercaseToken_shouldReturn400() {
        Response response = validRequest()
                .formParam("token", AppConfig.LOWERCASE_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(400, response.statusCode()),
                () -> assertEquals("ERROR", response.jsonPath().getString("result"))
        );
    }

    @Test
    @Story("Token validation — special characters")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Token with special characters returns HTTP 400")
    @Description("""
            The token must match the pattern ^[0-9A-F]{32}$.
            Any character outside this set must cause a validation error.
            """)
    void specialCharToken_shouldReturn400() {
        Response response = validRequest()
                .formParam("token", AppConfig.SPECIAL_CHAR_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(400, response.statusCode()),
                () -> assertEquals("ERROR", response.jsonPath().getString("result"))
        );
    }

    @Test
    @Story("Token validation — valid format")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Token with exactly 32 uppercase hex characters passes validation")
    @Description("""
            A correctly formatted token (32 uppercase hex characters) must pass
            the validation layer. The response must not be HTTP 400.
            """)
    void validToken_shouldPassValidation() {
        // No mock set up — the app will fail at the external call,
        // but we only care that token validation itself passes.
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);

        assertNotEquals(400, response.statusCode(),
                "A valid token must not result in HTTP 400");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Action validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @Story("Action validation — missing action")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Request without action parameter returns HTTP 400")
    @Description("""
            The action parameter is mandatory. When it is absent,
            the application must return HTTP 400 with an appropriate error message.
            """)
    void missingAction_shouldReturn400() {
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(400, response.statusCode()),
                () -> assertEquals("ERROR", response.jsonPath().getString("result")),
                () -> assertThat(response.jsonPath().getString("message"),
                        containsStringIgnoringCase("action"))
        );
    }

    @ParameterizedTest(name = "Invalid action ''{0}'' returns HTTP 400")
    @ValueSource(strings = {"INVALID", "login", "logout", "action", "UNKNOWN", "123", ""})
    @Story("Action validation — invalid action value")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Invalid action value returns HTTP 400")
    @Description("""
            The action parameter must be one of: LOGIN, LOGOUT, ACTION.
            Any other value must be rejected with HTTP 400 and a descriptive
            error message listing the allowed values.
            """)
    void invalidAction_shouldReturn400(String invalidAction) {
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", invalidAction)
                .post(AppConfig.ENDPOINT);

        assertAll(
                () -> assertEquals(400, response.statusCode(),
                        "Action '" + invalidAction + "' should be rejected with 400"),
                () -> assertEquals("ERROR", response.jsonPath().getString("result"))
        );
    }

    @Test
    @Story("Action validation — error message lists allowed values")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Invalid action error message mentions allowed values")
    @Description("""
            When an invalid action is provided, the error message should
            guide the client by listing the allowed values: LOGIN, LOGOUT, ACTION.
            """)
    void invalidAction_errorMessageMentionsAllowedValues() {
        Response response = validRequest()
                .formParam("token", AppConfig.VALID_TOKEN)
                .formParam("action", "UNKNOWN")
                .post(AppConfig.ENDPOINT);

        String message = response.jsonPath().getString("message");
        assertAll(
                () -> assertThat(message, containsString("LOGIN")),
                () -> assertThat(message, containsString("LOGOUT")),
                () -> assertThat(message, containsString("ACTION"))
        );
    }
}
