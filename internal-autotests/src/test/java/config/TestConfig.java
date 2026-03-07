package config;

public class TestConfig {
    
    // Application URLs
    public static final String BASE_URL = System.getProperty("base.url", "http://localhost:8080");
    public static final String MOCK_URL = System.getProperty("mock.url", "http://localhost:8888");
    
    // API Configuration
    public static final String API_KEY = "qazWSXedc";
    public static final String API_KEY_HEADER = "X-Api-Key";
    
    // Endpoints
    public static final String ENDPOINT = "/endpoint";
    public static final String MOCK_AUTH_ENDPOINT = "/auth";
    public static final String MOCK_DO_ACTION_ENDPOINT = "/doAction";
    
    // Actions
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_ACTION = "ACTION";
    public static final String ACTION_LOGOUT = "LOGOUT";
    
    // Token Configuration
    public static final int VALID_TOKEN_LENGTH = 32;
    public static final String VALID_TOKEN_CHARS = "0123456789ABCDEF";
    public static final String VALID_TOKEN_PATTERN = "[0-9A-F]{32}";
    
    // Content Types
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";
    
    // Response Fields
    public static final String RESULT_OK = "OK";
    public static final String RESULT_ERROR = "ERROR";
    
    // Timeouts
    public static final int DEFAULT_TIMEOUT = 5000;
}
