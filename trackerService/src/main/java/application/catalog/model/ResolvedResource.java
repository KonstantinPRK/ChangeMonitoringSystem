package application.catalog.model;

/**
 * Передаёт между компонентами данные {@code ResolvedResource}.
 */
public record ResolvedResource(
        String providerKey,
        String resourceKind,
        String canonicalUrl,
        String remoteResourceKey
) {
}
