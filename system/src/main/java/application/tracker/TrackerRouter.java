package application.tracker;

import java.net.URI;
import java.time.Clock;

public final class TrackerRouter {
    private final TrackerRegistry registry;
    private final Clock clock;


    public TrackerRouter(TrackerRegistry registry, Clock clock) {
        this.registry = registry;
        this.clock = clock;
    }


    public TrackerInstance route(URI resource) {
        String host = resource.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Resource URI must contain a host");
        }

        return registry.findActiveBySupportedHost(host, clock.instant())
            .orElseThrow(() -> new IllegalStateException(
                "No active tracker supports host: " + host
            ));
    }
}
