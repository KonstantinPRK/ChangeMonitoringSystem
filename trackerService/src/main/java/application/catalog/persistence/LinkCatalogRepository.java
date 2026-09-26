package application.catalog.persistence;

import application.catalog.model.ResolvedResource;
import application.catalog.model.TrackedLink;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code LinkCatalogRepository}.
 */
@Repository
public class LinkCatalogRepository {
    private final JdbcClient jdbcClient;
    private final Clock clock;


    public LinkCatalogRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }


    @Transactional
    public void save(UUID generation, TrackedLink link, ResolvedResource resource) {
        deleteObsoleteSnapshot(link, resource);

        Instant now = clock.instant();
        jdbcClient.sql("""
                INSERT INTO tracked_resources (
                    subscription_link_id, domain, address, canonical_url, provider_key,
                    resource_kind, remote_resource_key, subscription_revision, active,
                    synchronization_generation, next_scrape_at, created_at, updated_at
                ) VALUES (
                    :subscriptionLinkId, :domain, :address, :canonicalUrl, :providerKey,
                    :resourceKind, :remoteResourceKey, :revision, TRUE,
                    :generation, :nextScrapeAt, :createdAt, :updatedAt
                )
                ON CONFLICT (subscription_link_id) DO UPDATE SET
                    domain = EXCLUDED.domain,
                    address = EXCLUDED.address,
                    canonical_url = EXCLUDED.canonical_url,
                    provider_key = EXCLUDED.provider_key,
                    resource_kind = EXCLUDED.resource_kind,
                    remote_resource_key = EXCLUDED.remote_resource_key,
                    subscription_revision = EXCLUDED.subscription_revision,
                    active = TRUE,
                    synchronization_generation = EXCLUDED.synchronization_generation,
                    updated_at = EXCLUDED.updated_at
                """)
                .param("subscriptionLinkId", link.id())
                .param("domain", link.link().domain())
                .param("address", link.link().address())
                .param("canonicalUrl", resource.canonicalUrl())
                .param("providerKey", resource.providerKey())
                .param("resourceKind", resource.resourceKind())
                .param("remoteResourceKey", resource.remoteResourceKey())
                .param("revision", link.revision())
                .param("generation", generation)
                .param("nextScrapeAt", from(now))
                .param("createdAt", from(now))
                .param("updatedAt", from(now))
                .update();
    }


    public void deactivateMissing(UUID generation) {
        jdbcClient.sql("""
                UPDATE tracked_resources
                SET active = FALSE, locked_until = NULL, claim_token = NULL, updated_at = :updatedAt
                WHERE active = TRUE AND synchronization_generation <> :generation
                """)
                .param("updatedAt", from(clock.instant()))
                .param("generation", generation)
                .update();
    }


    public long countActive(String providerKey, String resourceKind) {
        return jdbcClient.sql("""
                SELECT COUNT(*) FROM tracked_resources
                WHERE active = TRUE
                  AND provider_key = :providerKey
                  AND resource_kind = :resourceKind
                """)
                .param("providerKey", providerKey)
                .param("resourceKind", resourceKind)
                .query(Long.class)
                .single();
    }


    private void deleteObsoleteSnapshot(TrackedLink link, ResolvedResource resource) {
        jdbcClient.sql("""
                DELETE FROM resource_snapshots snapshot
                USING tracked_resources tracked
                WHERE snapshot.tracked_resource_id = tracked.id
                  AND tracked.subscription_link_id = :subscriptionLinkId
                  AND (tracked.address IS DISTINCT FROM :address
                    OR tracked.canonical_url IS DISTINCT FROM :canonicalUrl
                    OR tracked.provider_key IS DISTINCT FROM :providerKey
                    OR tracked.resource_kind IS DISTINCT FROM :resourceKind
                    OR tracked.remote_resource_key IS DISTINCT FROM :remoteResourceKey)
                """)
                .param("subscriptionLinkId", link.id())
                .param("address", link.link().address())
                .param("canonicalUrl", resource.canonicalUrl())
                .param("providerKey", resource.providerKey())
                .param("resourceKind", resource.resourceKind())
                .param("remoteResourceKey", resource.remoteResourceKey())
                .update();
    }
}
