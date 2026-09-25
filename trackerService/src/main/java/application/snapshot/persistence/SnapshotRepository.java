package application.snapshot.persistence;

import application.snapshot.ResourceSnapshot;
import application.snapshot.StoredSnapshot;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static application.transaction.DatabaseTime.from;

@Repository
public class SnapshotRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;


    public SnapshotRepository(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }


    public Optional<StoredSnapshot> find(long trackedResourceId) {
        List<StoredSnapshot> snapshots = jdbcTemplate.query(
                """
                SELECT resources.provider_key, resources.resource_kind,
                       snapshots.version_token, snapshots.remote_updated_at,
                       snapshots.resource_state, snapshots.content_hash,
                       snapshots.summary_json::text
                FROM resource_snapshots snapshots
                JOIN tracked_resources resources ON resources.id = snapshots.tracked_resource_id
                WHERE snapshots.tracked_resource_id = ?
                """,
                this::mapSnapshot,
                trackedResourceId
        );
        return snapshots.stream().findFirst();
    }


    public void save(long trackedResourceId, String versionToken, ResourceSnapshot snapshot) {
        jdbcTemplate.update(
                """
                INSERT INTO resource_snapshots (
                    tracked_resource_id, version_token, remote_updated_at, resource_state,
                    content_hash, summary_json, observed_at
                ) VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb), ?)
                ON CONFLICT (tracked_resource_id) DO UPDATE SET
                    version_token = EXCLUDED.version_token,
                    remote_updated_at = EXCLUDED.remote_updated_at,
                    resource_state = EXCLUDED.resource_state,
                    content_hash = EXCLUDED.content_hash,
                    summary_json = EXCLUDED.summary_json,
                    observed_at = EXCLUDED.observed_at
                """,
                trackedResourceId,
                versionToken,
                from(snapshot.remoteUpdatedAt()),
                snapshot.state(),
                snapshot.contentHash(),
                snapshot.summaryJson(),
                from(clock.instant())
        );
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
