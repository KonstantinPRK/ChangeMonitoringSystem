package application.snapshot;

import java.time.Instant;

public record ResourceSnapshot(
        String providerKey,
        String resourceKind,
        Instant remoteUpdatedAt,
        String state,
        String contentHash,
        String summaryJson
) {
}
