package application.catalog.persistence;

import application.catalog.model.TrackedLink;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.UUID;

import static application.transaction.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code RejectedLinkRepository}.
 */
@Repository
public class RejectedLinkRepository {
    private final JdbcClient jdbcClient;
    private final Clock clock;


    public RejectedLinkRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }


    public void save(UUID generation, TrackedLink link, String reason) {
        jdbcClient.sql("""
                INSERT INTO link_sync_rejections (
                    subscription_link_id, address, reason, synchronization_generation, updated_at
                ) VALUES (:subscriptionLinkId, :address, :reason, :generation, :updatedAt)
                ON CONFLICT (subscription_link_id) DO UPDATE SET
                    address = EXCLUDED.address,
                    reason = EXCLUDED.reason,
                    synchronization_generation = EXCLUDED.synchronization_generation,
                    updated_at = EXCLUDED.updated_at
                """)
                .param("subscriptionLinkId", link.id())
                .param("address", link.link().address())
                .param("reason", reason)
                .param("generation", generation)
                .param("updatedAt", from(clock.instant()))
                .update();
    }


    public void delete(long subscriptionLinkId) {
        jdbcClient.sql("DELETE FROM link_sync_rejections WHERE subscription_link_id = :subscriptionLinkId")
                .param("subscriptionLinkId", subscriptionLinkId)
                .update();
    }


    public void deleteMissing(UUID generation) {
        jdbcClient.sql("DELETE FROM link_sync_rejections WHERE synchronization_generation <> :generation")
                .param("generation", generation)
                .update();
    }
}
