package com.example.aqa.config;

/**
 * Central configuration for the test suite.
 * All connection parameters and constants are defined here.
 */
public final class AppConfig {

    private AppConfig() {}

    /** Base URL of the application under test */
    public static final String APP_BASE_URL = "http://localhost:8080";

    /** The single endpoint exposed by the application */
    public static final String ENDPOINT = "/endpoint";

    /** Valid API key accepted by the application */
    public static final String VALID_API_KEY = "qazWSXedc";

    /** Port on which WireMock (mock external service) will listen */
    public static final int MOCK_PORT = 8888;

    /** Host of the mock external service */
    public static final String MOCK_HOST = "localhost";

    /** A valid token: 32 uppercase hex characters [0-9A-F] */
    public static final String VALID_TOKEN = "ABCDEF1234567890ABCDEF1234567890";

    /** A second valid token for multi-user scenarios */
    public static final String VALID_TOKEN_2 = "1234567890ABCDEF1234567890ABCDEF";

    /** Token that is too short (must be exactly 32 chars) */
    public static final String SHORT_TOKEN = "ABCDEF1234";

    /** Token with lowercase letters (must be uppercase only) */
    public static final String LOWERCASE_TOKEN = "abcdef1234567890abcdef1234567890";

    /** Token with special characters */
    public static final String SPECIAL_CHAR_TOKEN = "ABCDEF1234567890ABCDEF123456789!";

    /** An invalid API key */
    public static final String INVALID_API_KEY = "invalid-key-xyz";

    /** Path of the /auth endpoint on the external mock service */
    public static final String MOCK_AUTH_PATH = "/auth";

    /** Path of the /doAction endpoint on the external mock service */
    public static final String MOCK_DO_ACTION_PATH = "/doAction";
}
