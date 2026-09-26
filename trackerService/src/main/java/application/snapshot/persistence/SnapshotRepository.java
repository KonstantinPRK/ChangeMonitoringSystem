package application.snapshot.persistence;

import application.snapshot.ResourceSnapshot;
import application.snapshot.StoredSnapshot;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;

import static application.transaction.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code SnapshotRepository}.
 */
@Repository
public class SnapshotRepository {
    private final JdbcClient jdbcClient;
    private final Clock clock;


    public SnapshotRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }


    public Optional<StoredSnapshot> find(long trackedResourceId) {
        return jdbcClient.sql("""
                SELECT resources.provider_key, resources.resource_kind,
                       snapshots.version_token, snapshots.remote_updated_at,
                       snapshots.resource_state, snapshots.content_hash,
                       snapshots.summary_json::text
                FROM resource_snapshots snapshots
                JOIN tracked_resources resources ON resources.id = snapshots.tracked_resource_id
                WHERE snapshots.tracked_resource_id = :trackedResourceId
                """)
                .param("trackedResourceId", trackedResourceId)
                .query(this::mapSnapshot)
                .optional();
    }


    public void save(long trackedResourceId, String versionToken, ResourceSnapshot snapshot) {
        jdbcClient.sql("""
                INSERT INTO resource_snapshots (
                    tracked_resource_id, version_token, remote_updated_at, resource_state,
                    content_hash, summary_json, observed_at
                ) VALUES (
                    :trackedResourceId, :versionToken, :remoteUpdatedAt, :resourceState,
                    :contentHash, CAST(:summaryJson AS jsonb), :observedAt
                )
                ON CONFLICT (tracked_resource_id) DO UPDATE SET
                    version_token = EXCLUDED.version_token,
                    remote_updated_at = EXCLUDED.remote_updated_at,
                    resource_state = EXCLUDED.resource_state,
                    content_hash = EXCLUDED.content_hash,
                    summary_json = EXCLUDED.summary_json,
                    observed_at = EXCLUDED.observed_at
                """)
                .param("trackedResourceId", trackedResourceId)
                .param("versionToken", versionToken)
                .param("remoteUpdatedAt", from(snapshot.remoteUpdatedAt()))
                .param("resourceState", snapshot.state())
                .param("contentHash", snapshot.contentHash())
                .param("summaryJson", snapshot.summaryJson())
                .param("observedAt", from(clock.instant()))
                .update();
    }


    private StoredSnapshot mapSnapshot(ResultSet resultSet, int rowNumber) throws SQLException {
        ResourceSnapshot snapshot = new ResourceSnapshot(
                resultSet.getString("provider_key"),
                resultSet.getString("resource_kind"),
                resultSet.getTimestamp("remote_updated_at").toInstant(),
                resultSet.getString("resource_state"),
                resultSet.getString("content_hash"),
                resultSet.getString("summary_json")
        );

        return new StoredSnapshot(resultSet.getString("version_token"), snapshot);
    }
}
