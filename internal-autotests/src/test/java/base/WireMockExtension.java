package base;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

/**
 * JUnit Extension для запуска WireMock один раз для всех тестов
 */
public class WireMockExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {
    
    private static final Logger logger = LoggerFactory.getLogger(WireMockExtension.class);
    private static boolean started = false;
    private static WireMockServer wireMockServer;
    
    @Override
    public void beforeAll(ExtensionContext context) {
        if (!started) {
            started = true;
            
            // Регистрируем callback для остановки WireMock после всех тестов
            context.getRoot().getStore(GLOBAL).put("wireMock", this);
            
            // Запускаем WireMock
            wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                    .port(8888)
            );
            wireMockServer.start();
            logger.info("=== WireMock запущен глобально на порту 8888 ===");
        }
    }
    
    @Override
    public void close() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            logger.info("=== WireMock остановлен ===");
        }
    }
    
    public static WireMockServer getWireMockServer() {
        return wireMockServer;
    }
}
