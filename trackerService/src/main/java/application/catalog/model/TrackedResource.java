package application.catalog.model;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code TrackedResource}.
 */
public record TrackedResource(
        long id,
        long subscriptionLinkId,
        String domain,
        String address,
        String providerKey,
        String resourceKind,
        String remoteResourceKey,
        long subscriptionRevision,
        int failureCount,
        UUID claimToken
) {
}
