package application.tracker;

import application.registration.RemoteSystemStatus;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrackerRouterTest {
    private static final Instant NOW = Instant.parse("2026-09-22T12:00:00Z");

    @Test
    void routesResourceByNormalizedSupportedHost() {
        TrackerRegistry registry = new TrackerRegistry();
        registry.register(tracker("github", Set.of("GitHub.com")));
        TrackerRouter router = new TrackerRouter(
            registry,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );

        TrackerInstance selected = router.route(
            URI.create("https://github.com/openai/codex")
        );

        assertEquals("github", selected.trackerId());
    }

    @Test
    void rejectsTwoTrackersClaimingTheSameHost() {
        TrackerRegistry registry = new TrackerRegistry();
        registry.register(tracker("github", Set.of("github.com")));

        assertThrows(
            IllegalStateException.class,
            () -> registry.register(tracker("other", Set.of("github.com")))
        );
    }

    private static TrackerInstance tracker(String trackerId, Set<String> hosts) {
        return new TrackerInstance(
            trackerId,
            trackerId,
            URI.create("http://localhost:8081"),
            1,
            hosts,
            Set.of("subscriptions"),
            RemoteSystemStatus.ACTIVE,
            NOW.plusSeconds(30)
        );
    }
}
