package utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Standalone WireMock сервер для ручного запуска
 * Запускается на порту 8888 и мокирует внешний сервис
 */
public class StandaloneWireMock {
    
    private static final Logger logger = LoggerFactory.getLogger(StandaloneWireMock.class);
    private static WireMockServer wireMockServer;
    
    public static void main(String[] args) {
        logger.info("Запуск WireMock сервера...");
        
        wireMockServer = new WireMockServer(
            WireMockConfiguration.options()
                .port(8888)
        );
        
        wireMockServer.start();
        logger.info("WireMock запущен на http://localhost:8888");
        
        // Настройка стандартных стабов
        setupDefaultStubs();
        
        logger.info("Стабы настроены. Нажмите Ctrl+C для остановки.");
        
        // Добавляем shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Остановка WireMock...");
            wireMockServer.stop();
            logger.info("WireMock остановлен");
        }));
        
        // Держим сервер запущенным
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            logger.error("Прервано", e);
        }
    }
    
    private static void setupDefaultStubs() {
        // Стаб для успешной аутентификации
        wireMockServer.stubFor(post(urlEqualTo("/auth"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"ok\"}")));
        
        // Стаб для успешного действия
        wireMockServer.stubFor(post(urlEqualTo("/doAction"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"ok\"}")));
        
        logger.info("Настроены стабы:");
        logger.info("  POST /auth -> 200 OK");
        logger.info("  POST /doAction -> 200 OK");
    }
}
