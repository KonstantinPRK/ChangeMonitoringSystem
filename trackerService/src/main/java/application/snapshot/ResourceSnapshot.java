package application.snapshot;

import java.time.Instant;

/**
 * Передаёт между компонентами данные {@code ResourceSnapshot}.
 */
public record ResourceSnapshot(
        String providerKey,
        String resourceKind,
        Instant remoteUpdatedAt,
        String state,
        String contentHash,
        String summaryJson
) {
}
