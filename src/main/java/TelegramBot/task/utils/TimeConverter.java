package TelegramBot.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeConverter {

    // Явно указываем "UTC+05:00"
    private static final ZoneId UTC_PLUS_5 = ZoneId.of("UTC+05:00");

    public static Timestamp convertFromUTCPlus5ToUTC(String userInput) {
        String dateTimeString = userInput.replace(" ", "T");
        LocalDateTime ldt = LocalDateTime.parse(dateTimeString);

        // Интерпретируем ldt как время в UTC+5
        ZonedDateTime zdtPlus5 = ldt.atZone(UTC_PLUS_5);

        // Переводим в UTC
        ZonedDateTime zdtUTC = zdtPlus5.withZoneSameInstant(ZoneId.of("UTC"));

        return Timestamp.valueOf(zdtUTC.toLocalDateTime());
    }
}
