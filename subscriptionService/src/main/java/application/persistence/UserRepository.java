package application.persistence;

import application.user.User;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Отвечает за сохранение и чтение пользователей сервиса подписок.
 */
@Repository
public class UserRepository {
    private final JdbcClient jdbcClient;


    public UserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }


    public UserEntity createAndLock(User user) {
        jdbcClient.sql("""
                INSERT INTO service_users (bot_id, user_id, chat_id)
                VALUES (:botId, :userId, :chatId)
                ON CONFLICT (bot_id, user_id, chat_id) DO NOTHING
                """)
                .param("botId", user.botId())
                .param("userId", user.userId())
                .param("chatId", user.chatId())
                .update();

        return lock(user).orElseThrow();
    }


    public Optional<UserEntity> lock(User user) {
        return jdbcClient.sql("""
                SELECT id, bot_id, user_id, chat_id, subscriptions_revision
                FROM service_users
                WHERE bot_id = :botId AND user_id = :userId AND chat_id = :chatId
                FOR UPDATE
                """)
                .param("botId", user.botId())
                .param("userId", user.userId())
                .param("chatId", user.chatId())
                .query(this::mapUser)
                .optional();
    }


    public long advanceSubscriptionsRevision(long userId) {
        return jdbcClient.sql("""
                UPDATE service_users
                SET subscriptions_revision = subscriptions_revision + 1
                WHERE id = :userId
                RETURNING subscriptions_revision
                """)
                .param("userId", userId)
                .query(Long.class)
                .single();
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
