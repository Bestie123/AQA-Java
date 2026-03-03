package com.example.aqa.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Base class for all test classes.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Starts and stops WireMock server (mock of the external service)</li>
 *   <li>Configures REST Assured base URL and Allure logging filter</li>
 *   <li>Resets WireMock stubs before each test to ensure test isolation</li>
 * </ul>
 */
public abstract class BaseTest {

    protected static WireMockServer wireMockServer;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(AppConfig.MOCK_PORT)
        );
        wireMockServer.start();

        RestAssured.baseURI = AppConfig.APP_BASE_URL;
        RestAssured.filters(
                new AllureRestAssured(),
                new RequestLoggingFilter(),
                new ResponseLoggingFilter()
        );
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
        // Ensure tokens are logged out before each test to avoid 409 Conflict
        cleanupToken(AppConfig.VALID_TOKEN);
        cleanupToken(AppConfig.VALID_TOKEN_2);
    }

    /**
     * Attempts to log out the given token, ignoring errors if it was not logged in.
     */
    protected void cleanupToken(String token) {
        try {
            RestAssured.given()
                    .contentType("application/x-www-form-urlencoded")
                    .header("X-Api-Key", AppConfig.VALID_API_KEY)
                    .formParam("token", token)
                    .formParam("action", "LOGOUT")
                    .post(AppConfig.ENDPOINT);
        } catch (Exception ignored) {
            // Ignore errors — token may not have been logged in
        }
    }

    /**
     * Returns a pre-configured REST Assured request specification
     * with the correct Content-Type and valid API key header.
     */
    protected RequestSpecification validRequest() {
        return RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .accept(ContentType.JSON)
                .header("X-Api-Key", AppConfig.VALID_API_KEY);
    }

    /**
     * Stubs the external /auth endpoint to return HTTP 200.
     */
    protected void stubAuthSuccess() {
        wireMockServer.stubFor(
                post(urlEqualTo(AppConfig.MOCK_AUTH_PATH))
                        .willReturn(aResponse().withStatus(200).withBody("OK"))
        );
    }

    /**
     * Stubs the external /auth endpoint to return HTTP 500 (server error).
     */
    protected void stubAuthFailure() {
        wireMockServer.stubFor(
                post(urlEqualTo(AppConfig.MOCK_AUTH_PATH))
                        .willReturn(aResponse().withStatus(500).withBody("Internal Server Error"))
        );
    }

    /**
     * Stubs the external /doAction endpoint to return HTTP 200.
     */
    protected void stubDoActionSuccess() {
        wireMockServer.stubFor(
                post(urlEqualTo(AppConfig.MOCK_DO_ACTION_PATH))
                        .willReturn(aResponse().withStatus(200).withBody("OK"))
        );
    }

    /**
     * Stubs the external /doAction endpoint to return HTTP 500 (server error).
     */
    protected void stubDoActionFailure() {
        wireMockServer.stubFor(
                post(urlEqualTo(AppConfig.MOCK_DO_ACTION_PATH))
                        .willReturn(aResponse().withStatus(500).withBody("Internal Server Error"))
        );
    }

    /**
     * Performs a LOGIN for the given token (auth mock must be set up beforehand).
     */
    protected void performLogin(String token) {
        validRequest()
                .formParam("token", token)
                .formParam("action", "LOGIN")
                .post(AppConfig.ENDPOINT);
    }
}
