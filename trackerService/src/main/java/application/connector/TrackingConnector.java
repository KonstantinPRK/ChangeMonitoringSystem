package application.connector;

import java.time.Instant;
import java.util.Optional;

public interface TrackingConnector {
    ConnectorDescriptor descriptor();

    LinkResolver linkResolver();

    ResourceInspector resourceInspector();

    ProviderFailurePolicy failurePolicy();

    Optional<Instant> blockedUntil();
}
