package application.user;

import application.link.Link;
import application.persistence.UserEntity;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Читает подписчиков со стороны отслеживаемой ссылки.
 */
@Repository
public class LinkToUsersDatabase {
    private final JdbcClient jdbcClient;


    public LinkToUsersDatabase(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }


    public SubscriberBatch read(Link link, long afterUserId, int limit) {
        List<UserEntity> users = jdbcClient.sql("""
                SELECT users.id, users.bot_id, users.user_id,
                       users.chat_id, users.subscriptions_revision
                FROM subscriptions subscriptions
                JOIN tracked_links links ON links.id = subscriptions.link_id
                JOIN service_users users ON users.id = subscriptions.user_id
                WHERE links.address = :address AND users.id > :afterUserId
                ORDER BY users.id
                LIMIT :limit
                """)
                .param("address", link.address())
                .param("afterUserId", afterUserId)
                .param("limit", limit)
                .query(this::mapUser)
                .list();

        long cursor = users.isEmpty()
                ? afterUserId
                : users.get(users.size() - 1).getId();
        List<User> subscribers = users.stream()
                .map(UserEntity::toUser)
                .toList();

        return new SubscriberBatch(subscribers, cursor);
    }


    private UserEntity mapUser(ResultSet resultSet, int rowNumber) throws SQLException {
        return new UserEntity(
                resultSet.getLong("id"),
                resultSet.getString("bot_id"),
                resultSet.getString("user_id"),
                resultSet.getString("chat_id"),
                resultSet.getLong("subscriptions_revision")
        );
    }
}
