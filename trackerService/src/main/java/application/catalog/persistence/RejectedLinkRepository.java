package application.catalog.persistence;

import application.catalog.model.TrackedLink;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

@Repository
public class RejectedLinkRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;


    public RejectedLinkRepository(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }


    public void save(UUID generation, TrackedLink link, String reason) {
        jdbcTemplate.update(
                """
                INSERT INTO link_sync_rejections (
                    subscription_link_id, address, reason, synchronization_generation, updated_at
                ) VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (subscription_link_id) DO UPDATE SET
                    address = EXCLUDED.address,
                    reason = EXCLUDED.reason,
                    synchronization_generation = EXCLUDED.synchronization_generation,
                    updated_at = EXCLUDED.updated_at
                """,
                link.id(),
                link.link().address(),
                reason,
                generation,
                from(clock.instant())
        );
    }


    public void delete(long subscriptionLinkId) {
        jdbcTemplate.update(
                "DELETE FROM link_sync_rejections WHERE subscription_link_id = ?",
                subscriptionLinkId
        );
    }


    public void deleteMissing(UUID generation) {
        jdbcTemplate.update(
                "DELETE FROM link_sync_rejections WHERE synchronization_generation <> ?",
                generation
        );
    }
}
