package application.catalog.persistence;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

@Repository
public class LinkSynchronizationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;


    public LinkSynchronizationRepository(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }


    public Optional<UUID> start(String trackerId, Duration lease) {
        UUID generation = UUID.randomUUID();
        List<UUID> generations = jdbcTemplate.query(
                """
                INSERT INTO link_synchronizations (
                    tracker_id, generation_id, after_id, status, started_at, lease_until
                ) VALUES (?, ?, 0, 'RUNNING', ?, ?)
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
                """,
                (resultSet, rowNumber) -> resultSet.getObject("generation_id", UUID.class),
                trackerId,
                generation,
                from(clock.instant()),
                from(clock.instant().plus(lease))
        );
        return generations.stream().findFirst();
    }


    public void saveProgress(
            String trackerId,
            UUID generation,
            long afterId,
            Duration lease
    ) {
        jdbcTemplate.update(
                """
                UPDATE link_synchronizations SET after_id = ?, lease_until = ?
                WHERE tracker_id = ? AND generation_id = ? AND status = 'RUNNING'
                """,
                afterId,
                from(clock.instant().plus(lease)),
                trackerId,
                generation
        );
    }


    public boolean lockOwnedGeneration(String trackerId, UUID generation) {
        List<Integer> ownedGenerations = jdbcTemplate.query(
                """
                SELECT 1 FROM link_synchronizations
                WHERE tracker_id = ? AND generation_id = ? AND status = 'RUNNING'
                FOR UPDATE
                """,
                (resultSet, rowNumber) -> resultSet.getInt(1),
                trackerId,
                generation
        );
        return !ownedGenerations.isEmpty();
    }


    public void complete(String trackerId, UUID generation) {
        jdbcTemplate.update(
                """
                UPDATE link_synchronizations
                SET status = 'COMPLETED', completed_at = ?
                WHERE tracker_id = ? AND generation_id = ?
                """,
                from(clock.instant()),
                trackerId,
                generation
        );
    }


    public void fail(String trackerId, UUID generation, String error) {
        jdbcTemplate.update(
                """
                UPDATE link_synchronizations SET status = 'FAILED', last_error = ?
                WHERE tracker_id = ? AND generation_id = ?
                """,
                error,
                trackerId,
                generation
        );
    }
}
