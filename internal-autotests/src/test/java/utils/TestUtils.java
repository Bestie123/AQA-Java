package utils;

import config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class TestUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
    private static final Random random = new Random();
    
    /**
     * Генерирует случайную строку заданной длины из указанных символов
     */
    public static String generateRandomString(int length, String chars) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    
    /**
     * Генерирует валидный токен (32 символа A-Z0-9)
     */
    public static String generateValidToken() {
        String token = generateRandomString(TestConfig.VALID_TOKEN_LENGTH, TestConfig.VALID_TOKEN_CHARS);
        logger.debug("Сгенерирован валидный токен: {}", token);
        return token;
    }
    
    /**
     * Генерирует невалидный токен (неправильная длина - короткий)
     */
    public static String generateShortToken() {
        String token = generateRandomString(10, TestConfig.VALID_TOKEN_CHARS);
        logger.debug("Сгенерирован короткий токен: {}", token);
        return token;
    }
    
    /**
     * Генерирует невалидный токен (неправильная длина - длинный)
     */
    public static String generateLongToken() {
        String token = generateRandomString(50, TestConfig.VALID_TOKEN_CHARS);
        logger.debug("Сгенерирован длинный токен: {}", token);
        return token;
    }
    
    /**
     * Генерирует невалидный токен (неправильные символы - lowercase)
     */
    public static String generateLowercaseToken() {
        String token = generateRandomString(TestConfig.VALID_TOKEN_LENGTH, "abcdefghijklmnopqrstuvwxyz0123456789");
        logger.debug("Сгенерирован lowercase токен: {}", token);
        return token;
    }
    
    /**
     * Генерирует невалидный токен (спецсимволы)
     */
    public static String generateSpecialCharsToken() {
        String token = generateRandomString(TestConfig.VALID_TOKEN_LENGTH, "!@#$%^&*()_+-=[]{}|;:,.<>?");
        logger.debug("Сгенерирован токен со спецсимволами: {}", token);
        return token;
    }
    
    /**
     * Логирует и возвращает строку
     */
    public static String logAndReturn(String message, String value) {
        logger.info("{}: {}", message, value);
        return value;
    }
}
