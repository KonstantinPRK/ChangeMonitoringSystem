package application.user;

import application.messenger.MessengerType;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static application.persistence.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code UserRepository}.
 */
@Repository
public class UserRepository {
    private final JdbcClient jdbcClient;
    private final Clock clock;


    public UserRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }


    public BotUser findOrCreate(UserKey key, MessengerType messengerType) {
        Optional<BotUser> existingUser = find(key);
        if (existingUser.isPresent()) return existingUser.get();

        Instant now = clock.instant();
        UUID userId = UUID.randomUUID();

        jdbcClient.sql("""
                INSERT INTO bot_users (
                    id, bot_id, messenger, external_user_id, chat_id, status, created_at, updated_at
                ) VALUES (:id, :botId, :messenger, :externalUserId, :chatId, 'ACTIVE', :createdAt, :updatedAt)
                ON CONFLICT (bot_id, messenger, external_user_id, chat_id) DO NOTHING
                """)
                .param("id", userId)
                .param("botId", key.botId())
                .param("messenger", messengerType.name())
                .param("externalUserId", key.externalUserId())
                .param("chatId", key.chatId())
                .param("createdAt", from(now))
                .param("updatedAt", from(now))
                .update();

        return find(key).orElseThrow();
    }


    public Optional<BotUser> find(UserKey key) {
        return jdbcClient.sql("""
                SELECT id, bot_id, messenger, external_user_id, chat_id, status, created_at
                FROM bot_users
                WHERE bot_id = :botId
                  AND external_user_id = :externalUserId
                  AND chat_id = :chatId
                """)
                .param("botId", key.botId())
                .param("externalUserId", key.externalUserId())
                .param("chatId", key.chatId())
                .query(this::mapUser)
                .optional();
    }


    public void activate(UUID userId) {
        jdbcClient.sql("UPDATE bot_users SET status = 'ACTIVE', updated_at = :updatedAt WHERE id = :userId")
                .param("updatedAt", from(clock.instant()))
                .param("userId", userId)
                .update();
    }


    public void markDeleted(UUID userId) {
        jdbcClient.sql("UPDATE bot_users SET status = 'DELETED', updated_at = :updatedAt WHERE id = :userId")
                .param("updatedAt", from(clock.instant()))
                .param("userId", userId)
                .update();
    }


    private BotUser mapUser(ResultSet resultSet, int rowNumber) throws SQLException {
        UserKey key = new UserKey(
                resultSet.getString("bot_id"),
                resultSet.getString("external_user_id"),
                resultSet.getString("chat_id")
        );
        return new BotUser(
                resultSet.getObject("id", UUID.class),
                key,
                resultSet.getString("messenger"),
                UserStatus.valueOf(resultSet.getString("status")),
                resultSet.getTimestamp("created_at").toInstant()
        );
    }
}
