package TelegramBot.utils;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerFactoryUtil {
    @Getter
    private static final Logger logger = LoggerFactory.getLogger("GlobalLogger");

    private LoggerFactoryUtil() {
        // Приватный конструктор
    }

    public static void logInfo(String message, Object... args) {
        logger.info(message, args);
    }

    public static void logWarn(String message, Object... args) {
        logger.warn(message, args);
    }

    public static void logError(String message, Throwable throwable, Object... args) {
        logger.error(message, throwable, args);
    }
}
