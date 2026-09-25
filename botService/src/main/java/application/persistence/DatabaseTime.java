package application.persistence;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class DatabaseTime {
    private DatabaseTime() {
    }


    public static OffsetDateTime from(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
