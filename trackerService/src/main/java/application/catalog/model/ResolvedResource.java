package application.catalog.model;

public record ResolvedResource(
        String providerKey,
        String resourceKind,
        String canonicalUrl,
        String remoteResourceKey
) {
}
