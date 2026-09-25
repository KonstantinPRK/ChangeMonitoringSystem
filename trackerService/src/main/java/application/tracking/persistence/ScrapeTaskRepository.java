package application.tracking.persistence;

import application.config.WorkerProperties;
import application.catalog.model.TrackedResource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

@Repository
public class ScrapeTaskRepository {
    private final JdbcTemplate jdbcTemplate;
    private final WorkerProperties workerProperties;
    private final Clock clock;


    public ScrapeTaskRepository(
            JdbcTemplate jdbcTemplate,
            WorkerProperties workerProperties,
            Clock clock
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.workerProperties = workerProperties;
        this.clock = clock;
    }


    public Optional<TrackedResource> claimNext() {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();
        List<TrackedResource> resources = jdbcTemplate.query(
                """
                UPDATE tracked_resources
                SET locked_until = ?, claim_token = ?
                WHERE id = (
                    SELECT id FROM tracked_resources
                    WHERE active = TRUE
                      AND next_scrape_at <= ?
                      AND (locked_until IS NULL OR locked_until <= ?)
                    ORDER BY next_scrape_at, id
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                )
                RETURNING id, subscription_link_id, domain, address, provider_key,
                          resource_kind, remote_resource_key,
                          subscription_revision, failure_count, claim_token
                """,
                this::mapResource,
                from(now.plus(workerProperties.claimLease())),
                claimToken,
                from(now),
                from(now)
        );
        return resources.stream().findFirst();
    }


    public void complete(TrackedResource resource, Instant nextScrapeAt) {
        jdbcTemplate.update(
                """
                UPDATE tracked_resources
                SET next_scrape_at = ?, failure_count = 0,
                    last_error = NULL, locked_until = NULL, claim_token = NULL, updated_at = ?
                WHERE id = ? AND claim_token = ?
                """,
                from(nextScrapeAt),
                from(clock.instant()),
                resource.id(),
                resource.claimToken()
        );
    }


    public void postpone(TrackedResource resource, Instant nextScrapeAt, String error) {
        jdbcTemplate.update(
                """
                UPDATE tracked_resources
                SET next_scrape_at = ?, failure_count = failure_count + 1,
                    last_error = ?, locked_until = NULL, claim_token = NULL, updated_at = ?
                WHERE id = ? AND claim_token = ?
                """,
                from(nextScrapeAt),
                error,
                from(clock.instant()),
                resource.id(),
                resource.claimToken()
        );
    }


    public void defer(TrackedResource resource, Instant nextScrapeAt) {
        jdbcTemplate.update(
                """
                UPDATE tracked_resources
                SET next_scrape_at = ?, locked_until = NULL, claim_token = NULL, updated_at = ?
                WHERE id = ? AND claim_token = ?
                """,
                from(nextScrapeAt),
                from(clock.instant()),
                resource.id(),
                resource.claimToken()
        );
    }


    private TrackedResource mapResource(ResultSet resultSet, int rowNumber) throws SQLException {
        return new TrackedResource(
                resultSet.getLong("id"),
                resultSet.getLong("subscription_link_id"),
                resultSet.getString("domain"),
                resultSet.getString("address"),
                resultSet.getString("provider_key"),
                resultSet.getString("resource_kind"),
                resultSet.getString("remote_resource_key"),
                resultSet.getLong("subscription_revision"),
                resultSet.getInt("failure_count"),
                resultSet.getObject("claim_token", UUID.class)
        );
    }
}
