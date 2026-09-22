package application.bot;

import application.registration.RemoteSystemStatus;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotRegistryTest {
    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");

    @Test
    void replacesBotWithTheSameUniqueId() {
        BotRegistry registry = new BotRegistry();
        registry.register(bot("http://localhost:8081", NOW.plusSeconds(30)));
        registry.register(bot("http://localhost:8082", NOW.plusSeconds(30)));

        assertEquals(1, registry.size());
        assertEquals(
            URI.create("http://localhost:8082"),
            registry.find("telegram").orElseThrow().baseUrl()
        );
    }

    @Test
    void marksBotUnavailableAfterConfirmationExpires() {
        BotRegistry registry = new BotRegistry();
        registry.register(bot("http://localhost:8081", NOW.plusSeconds(1)));

        registry.markExpiredUnavailable(NOW.plusSeconds(2));

        BotInstance bot = registry.find("telegram").orElseThrow();
        assertEquals(RemoteSystemStatus.UNAVAILABLE, bot.status());
        assertTrue(registry.findActive("telegram", NOW.plusSeconds(2)).isEmpty());
    }

    private static BotInstance bot(String baseUrl, Instant confirmedUntil) {
        return new BotInstance(
            "telegram",
            "ChangeDetectionBot",
            "telegram",
            URI.create(baseUrl),
            1,
            Set.of("notifications"),
            RemoteSystemStatus.ACTIVE,
            confirmedUntil
        );
    }
}
