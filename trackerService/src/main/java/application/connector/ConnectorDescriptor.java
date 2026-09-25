package application.connector;

import java.time.Duration;
import java.util.Set;

public record ConnectorDescriptor(
        String key,
        Set<String> supportedHosts,
        Set<String> supportedKinds,
        Duration scrapeInterval
) {
    public ConnectorDescriptor {
        supportedHosts = Set.copyOf(supportedHosts);
        supportedKinds = Set.copyOf(supportedKinds);
    }
}
