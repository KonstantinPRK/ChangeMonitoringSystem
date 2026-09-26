package application.persistence;

import application.link.Link;
import application.link.TrackedLink;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Отвечает за сохранение и чтение отслеживаемых ссылок.
 */
@Repository
public class LinkRepository {
    private final JdbcClient jdbcClient;


    public LinkRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }


    public LinkEntity createAndLock(Link link) {
        jdbcClient.sql("""
                INSERT INTO tracked_links (domain, address)
                VALUES (:domain, :address)
                ON CONFLICT (address) DO NOTHING
                """)
                .param("domain", link.domain())
                .param("address", link.address())
                .update();

        return lock(link).orElseThrow();
    }


    public Optional<LinkEntity> lock(Link link) {
        return jdbcClient.sql("""
                SELECT id, domain, address, tracking_revision
                FROM tracked_links
                WHERE address = :address
                FOR UPDATE
                """)
                .param("address", link.address())
                .query(this::mapLink)
                .optional();
    }


    public long advanceTrackingRevision(long linkId) {
        return jdbcClient.sql("""
                UPDATE tracked_links
                SET tracking_revision = tracking_revision + 1
                WHERE id = :linkId
                RETURNING tracking_revision
                """)
                .param("linkId", linkId)
                .query(Long.class)
                .single();
    }


    @Transactional(readOnly = true)
    public Map<String, Long> countActiveByDomain() {
        List<Map.Entry<String, Long>> counts = jdbcClient.sql("""
                SELECT links.domain, COUNT(*) AS link_count
                FROM tracked_links links
                WHERE EXISTS (
                    SELECT 1 FROM subscriptions subscriptions
                    WHERE subscriptions.link_id = links.id
                )
                GROUP BY links.domain
                """)
                .query((resultSet, rowNumber) -> Map.entry(
                        resultSet.getString("domain"),
                        resultSet.getLong("link_count")
                ))
                .list();

        Map<String, Long> result = new TreeMap<>();
        counts.forEach(entry -> result.put(entry.getKey(), entry.getValue()));

        return result;
    }


    @Transactional(readOnly = true)
    public List<TrackedLink> activeLinks(Set<String> hosts, long afterId, int limit) {
        if (hosts.isEmpty()) return List.of();

        return jdbcClient.sql("""
                SELECT links.id, links.domain, links.address, links.tracking_revision
                FROM tracked_links links
                WHERE links.domain IN (:hosts)
                  AND links.id > :afterId
                  AND EXISTS (
                      SELECT 1 FROM subscriptions subscriptions
                      WHERE subscriptions.link_id = links.id
                  )
                ORDER BY links.id
                LIMIT :limit
                """)
                .param("hosts", hosts)
                .param("afterId", afterId)
                .param("limit", limit)
                .query((resultSet, rowNumber) -> new TrackedLink(
                        resultSet.getLong("id"),
                        new Link(resultSet.getString("domain"), resultSet.getString("address")),
                        resultSet.getLong("tracking_revision")
                ))
                .list();
    }


    private LinkEntity mapLink(ResultSet resultSet, int rowNumber) throws SQLException {
        return new LinkEntity(
                resultSet.getLong("id"),
                resultSet.getString("domain"),
                resultSet.getString("address"),
                resultSet.getLong("tracking_revision")
        );
    }
}
