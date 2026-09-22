package application.registration;

import java.time.Instant;

public record ServiceLease(String instanceId, Instant expiresAt, HealthStatus status) {
}

