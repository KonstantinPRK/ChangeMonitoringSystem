package application.catalog.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code LinkSynchronizationRepository}.
 */
@Repository
public class LinkSynchronizationRepository {
    private final JdbcClient jdbcClient;
    private final Clock clock;


    public LinkSynchronizationRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }


    public Optional<UUID> start(String trackerId, Duration lease) {
        UUID generation = UUID.randomUUID();
        Instant now = clock.instant();

        return jdbcClient.sql("""
                INSERT INTO link_synchronizations (
                    tracker_id, generation_id, after_id, status, started_at, lease_until
                ) VALUES (:trackerId, :generation, 0, 'RUNNING', :startedAt, :leaseUntil)
                ON CONFLICT (tracker_id) DO UPDATE SET
                    generation_id = EXCLUDED.generation_id,
                    after_id = 0,
                    status = 'RUNNING',
                    started_at = EXCLUDED.started_at,
                    lease_until = EXCLUDED.lease_until,
                    completed_at = NULL,
                    last_error = NULL
                WHERE link_synchronizations.status <> 'RUNNING'
                   OR link_synchronizations.lease_until <= EXCLUDED.started_at
                RETURNING generation_id
                """)
                .param("trackerId", trackerId)
                .param("generation", generation)
                .param("startedAt", from(now))
                .param("leaseUntil", from(now.plus(lease)))
                .query(UUID.class)
                .optional();
    }


    public void saveProgress(
            String trackerId,
            UUID generation,
            long afterId,
            Duration lease
    ) {
        jdbcClient.sql("""
                UPDATE link_synchronizations SET after_id = :afterId, lease_until = :leaseUntil
                WHERE tracker_id = :trackerId
                  AND generation_id = :generation
                  AND status = 'RUNNING'
                """)
                .param("afterId", afterId)
                .param("leaseUntil", from(clock.instant().plus(lease)))
                .param("trackerId", trackerId)
                .param("generation", generation)
                .update();
    }


    public boolean lockOwnedGeneration(String trackerId, UUID generation) {
        return jdbcClient.sql("""
                SELECT 1 FROM link_synchronizations
                WHERE tracker_id = :trackerId
                  AND generation_id = :generation
                  AND status = 'RUNNING'
                FOR UPDATE
                """)
                .param("trackerId", trackerId)
                .param("generation", generation)
                .query(Integer.class)
                .optional()
                .isPresent();
    }


    public void complete(String trackerId, UUID generation) {
        jdbcClient.sql("""
                UPDATE link_synchronizations
                SET status = 'COMPLETED', completed_at = :completedAt
                WHERE tracker_id = :trackerId AND generation_id = :generation
                """)
                .param("completedAt", from(clock.instant()))
                .param("trackerId", trackerId)
                .param("generation", generation)
                .update();
    }


    public void fail(String trackerId, UUID generation, String error) {
        jdbcClient.sql("""
                UPDATE link_synchronizations SET status = 'FAILED', last_error = :error
                WHERE tracker_id = :trackerId AND generation_id = :generation
                """)
                .param("error", error)
                .param("trackerId", trackerId)
                .param("generation", generation)
                .update();
    }
}
