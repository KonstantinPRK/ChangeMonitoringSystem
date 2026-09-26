package application.connector;

import java.time.Duration;
import java.util.Set;

/**
 * Передаёт между компонентами данные {@code ConnectorDescriptor}.
 */
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
