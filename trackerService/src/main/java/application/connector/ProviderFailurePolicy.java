package application.connector;

import application.catalog.model.TrackedResource;

import java.time.Instant;

public interface ProviderFailurePolicy {
    Instant nextAttempt(TrackedResource resource, Throwable failure);
}
