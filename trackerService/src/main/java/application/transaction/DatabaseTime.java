package application.transaction;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Читает время базы данных, используемое для аренды задач и планирования повторных попыток.
 */
public final class DatabaseTime {
    private DatabaseTime() {
    }


    public static OffsetDateTime from(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
