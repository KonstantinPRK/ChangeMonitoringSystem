package application.user;

import application.link.Link;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Читает подписки со стороны пользователя.
 */
@Repository
public class UserToLinksDatabase {
    private final JdbcClient jdbcClient;


    public UserToLinksDatabase(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }


    public Optional<SubscriptionListVersion> version(User user) {
        return jdbcClient.sql("""
                SELECT id, subscriptions_revision
                FROM service_users
                WHERE bot_id = :botId AND user_id = :userId AND chat_id = :chatId
                """)
                .param("botId", user.botId())
                .param("userId", user.userId())
                .param("chatId", user.chatId())
                .query((resultSet, rowNumber) -> new SubscriptionListVersion(
                        resultSet.getLong("id"),
                        resultSet.getLong("subscriptions_revision")
                ))
                .optional();
    }


    public List<SubscriptionView> read(long userId, int offset, int limit) {
        return jdbcClient.sql("""
                SELECT links.domain, links.address,
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
                JOIN tracked_links links ON links.id = subscriptions.link_id
                WHERE subscriptions.user_id = :userId
                ORDER BY subscriptions.id
                OFFSET :offset
                LIMIT :limit
                """)
                .param("userId", userId)
                .param("offset", offset)
                .param("limit", limit)
                .query(this::mapSubscription)
                .list();
    }


    private SubscriptionView mapSubscription(ResultSet resultSet, int rowNumber) throws SQLException {
        Link link = new Link(
                resultSet.getString("domain"),
                resultSet.getString("address")
        );

        return new SubscriptionView(
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
