package application.catalog.persistence;

import application.catalog.model.ResolvedResource;
import application.catalog.model.TrackedLink;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

@Repository
public class LinkCatalogRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;


    public LinkCatalogRepository(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }


    @Transactional
    public void save(UUID generation, TrackedLink link, ResolvedResource resource) {
        deleteObsoleteSnapshot(link, resource);
        jdbcTemplate.update(
                """
                INSERT INTO tracked_resources (
                    subscription_link_id, domain, address, canonical_url, provider_key,
                    resource_kind, remote_resource_key, subscription_revision, active,
                    synchronization_generation, next_scrape_at, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE, ?, ?, ?, ?)
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
                """,
                link.id(),
                link.link().domain(),
                link.link().address(),
                resource.canonicalUrl(),
                resource.providerKey(),
                resource.resourceKind(),
                resource.remoteResourceKey(),
                link.revision(),
                generation,
                from(clock.instant()),
                from(clock.instant()),
                from(clock.instant())
        );
    }


    public void deactivateMissing(UUID generation) {
        jdbcTemplate.update(
                """
                UPDATE tracked_resources
                SET active = FALSE, locked_until = NULL, claim_token = NULL, updated_at = ?
                WHERE active = TRUE AND synchronization_generation <> ?
                """,
                from(clock.instant()),
                generation
        );
    }


    public long countActive(String providerKey, String resourceKind) {
        Long count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM tracked_resources
                WHERE active = TRUE AND provider_key = ? AND resource_kind = ?
                """,
                Long.class,
                providerKey,
                resourceKind
        );
        return count == null ? 0L : count;
    }


    private void deleteObsoleteSnapshot(TrackedLink link, ResolvedResource resource) {
        jdbcTemplate.update(
                """
                DELETE FROM resource_snapshots snapshot
                USING tracked_resources tracked
                WHERE snapshot.tracked_resource_id = tracked.id
                  AND tracked.subscription_link_id = ?
                  AND (tracked.address IS DISTINCT FROM ?
                    OR tracked.canonical_url IS DISTINCT FROM ?
                    OR tracked.provider_key IS DISTINCT FROM ?
                    OR tracked.resource_kind IS DISTINCT FROM ?
                    OR tracked.remote_resource_key IS DISTINCT FROM ?)
                """,
                link.id(),
                link.link().address(),
                resource.canonicalUrl(),
                resource.providerKey(),
                resource.resourceKind(),
                resource.remoteResourceKey()
        );
    }
}
