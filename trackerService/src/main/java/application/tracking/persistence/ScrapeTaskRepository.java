package application.tracking.persistence;

import application.config.WorkerProperties;
import application.catalog.model.TrackedResource;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code ScrapeTaskRepository}.
 */
@Repository
public class ScrapeTaskRepository {
    private final JdbcClient jdbcClient;
    private final WorkerProperties workerProperties;
    private final Clock clock;


    public ScrapeTaskRepository(
            JdbcClient jdbcClient,
            WorkerProperties workerProperties,
            Clock clock
    ) {
        this.jdbcClient = jdbcClient;
        this.workerProperties = workerProperties;
        this.clock = clock;
    }


    public Optional<TrackedResource> claimNext() {
        Instant now = clock.instant();
        UUID claimToken = UUID.randomUUID();

        return jdbcClient.sql("""
                UPDATE tracked_resources
                SET locked_until = :lockedUntil, claim_token = :claimToken
                WHERE id = (
                    SELECT id FROM tracked_resources
                    WHERE active = TRUE
                      AND next_scrape_at <= :now
                      AND (locked_until IS NULL OR locked_until <= :now)
                    ORDER BY next_scrape_at, id
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                )
                RETURNING id, subscription_link_id, domain, address, provider_key,
                          resource_kind, remote_resource_key,
                          subscription_revision, failure_count, claim_token
                """)
                .param("lockedUntil", from(now.plus(workerProperties.claimLease())))
                .param("claimToken", claimToken)
                .param("now", from(now))
                .query(this::mapResource)
                .optional();
    }


    public void complete(TrackedResource resource, Instant nextScrapeAt) {
        jdbcClient.sql("""
                UPDATE tracked_resources
                SET next_scrape_at = :nextScrapeAt, failure_count = 0,
                    last_error = NULL, locked_until = NULL, claim_token = NULL, updated_at = :updatedAt
                WHERE id = :resourceId AND claim_token = :claimToken
                """)
                .param("nextScrapeAt", from(nextScrapeAt))
                .param("updatedAt", from(clock.instant()))
                .param("resourceId", resource.id())
                .param("claimToken", resource.claimToken())
                .update();
    }


    public void postpone(TrackedResource resource, Instant nextScrapeAt, String error) {
        jdbcClient.sql("""
                UPDATE tracked_resources
                SET next_scrape_at = :nextScrapeAt, failure_count = failure_count + 1,
                    last_error = :error, locked_until = NULL, claim_token = NULL, updated_at = :updatedAt
                WHERE id = :resourceId AND claim_token = :claimToken
                """)
                .param("nextScrapeAt", from(nextScrapeAt))
                .param("error", error)
                .param("updatedAt", from(clock.instant()))
                .param("resourceId", resource.id())
                .param("claimToken", resource.claimToken())
                .update();
    }


    public void defer(TrackedResource resource, Instant nextScrapeAt) {
        jdbcClient.sql("""
                UPDATE tracked_resources
                SET next_scrape_at = :nextScrapeAt,
                    locked_until = NULL, claim_token = NULL, updated_at = :updatedAt
                WHERE id = :resourceId AND claim_token = :claimToken
                """)
                .param("nextScrapeAt", from(nextScrapeAt))
                .param("updatedAt", from(clock.instant()))
                .param("resourceId", resource.id())
                .param("claimToken", resource.claimToken())
                .update();
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
