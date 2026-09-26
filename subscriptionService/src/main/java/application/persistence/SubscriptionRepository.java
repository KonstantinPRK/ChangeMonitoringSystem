package application.persistence;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Отвечает за сохранение и чтение подписок и их метаданных.
 */
@Repository
public class SubscriptionRepository {
    private final JdbcClient jdbcClient;


    public SubscriptionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }


    public Optional<SubscriptionEntity> find(UserEntity user, LinkEntity link) {
        return jdbcClient.sql("""
                SELECT subscriptions.id,
                       ARRAY(
                           SELECT tag FROM subscription_tags
                           WHERE subscription_id = subscriptions.id
                           ORDER BY position
                       ) AS tags,
                       ARRAY(
                           SELECT expression FROM subscription_filters
                           WHERE subscription_id = subscriptions.id
                           ORDER BY position
                       ) AS filters
                FROM subscriptions
                WHERE user_id = :userId AND link_id = :linkId
                """)
                .param("userId", user.getId())
                .param("linkId", link.getId())
                .query((resultSet, rowNumber) -> mapSubscription(resultSet, user, link))
                .optional();
    }


    public void create(
            UserEntity user,
            LinkEntity link,
            List<String> tags,
            List<String> filters
    ) {
        long subscriptionId = jdbcClient.sql("""
                INSERT INTO subscriptions (user_id, link_id)
                VALUES (:userId, :linkId)
                RETURNING id
                """)
                .param("userId", user.getId())
                .param("linkId", link.getId())
                .query(Long.class)
                .single();

        saveTags(subscriptionId, tags);
        saveFilters(subscriptionId, filters);
    }


    public void replaceMetadata(long subscriptionId, List<String> tags, List<String> filters) {
        jdbcClient.sql("DELETE FROM subscription_tags WHERE subscription_id = :subscriptionId")
                .param("subscriptionId", subscriptionId)
                .update();

        jdbcClient.sql("DELETE FROM subscription_filters WHERE subscription_id = :subscriptionId")
                .param("subscriptionId", subscriptionId)
                .update();

        saveTags(subscriptionId, tags);
        saveFilters(subscriptionId, filters);
    }


    public void delete(SubscriptionEntity subscription) {
        jdbcClient.sql("DELETE FROM subscriptions WHERE id = :subscriptionId")
                .param("subscriptionId", subscription.getId())
                .update();
    }


    public long countByLink(long linkId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM subscriptions WHERE link_id = :linkId")
                .param("linkId", linkId)
                .query(Long.class)
                .single();
    }


    private void saveTags(long subscriptionId, List<String> tags) {
        for (int position = 0; position < tags.size(); position++) {
            jdbcClient.sql("""
                    INSERT INTO subscription_tags (subscription_id, position, tag)
                    VALUES (:subscriptionId, :position, :tag)
                    """)
                    .param("subscriptionId", subscriptionId)
                    .param("position", position)
                    .param("tag", tags.get(position))
                    .update();
        }
    }


    private void saveFilters(long subscriptionId, List<String> filters) {
        for (int position = 0; position < filters.size(); position++) {
            jdbcClient.sql("""
                    INSERT INTO subscription_filters (subscription_id, position, expression)
                    VALUES (:subscriptionId, :position, :expression)
                    """)
                    .param("subscriptionId", subscriptionId)
                    .param("position", position)
                    .param("expression", filters.get(position))
                    .update();
        }
    }


    private SubscriptionEntity mapSubscription(
            ResultSet resultSet,
            UserEntity user,
            LinkEntity link
    ) throws SQLException {
        return new SubscriptionEntity(
                resultSet.getLong("id"),
                user,
                link,
                readStrings(resultSet.getArray("tags")),
                readStrings(resultSet.getArray("filters"))
        );
    }


    private List<String> readStrings(Array array) throws SQLException {
        Object[] values = (Object[]) array.getArray();

        return Arrays.stream(values)
                .map(String::valueOf)
                .toList();
    }
}
