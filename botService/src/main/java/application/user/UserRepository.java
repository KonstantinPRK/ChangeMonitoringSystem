package application.user;

import application.messenger.MessengerType;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static application.persistence.DatabaseTime.from;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;


    public UserRepository(JdbcTemplate jdbcTemplate, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.clock = clock;
    }


    public BotUser findOrCreate(UserKey key, MessengerType messengerType) {
        Optional<BotUser> existingUser = find(key);
        if (existingUser.isPresent()) return existingUser.get();

        Instant now = clock.instant();
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO bot_users (
                    id, bot_id, messenger, external_user_id, chat_id, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?)
                ON CONFLICT (bot_id, messenger, external_user_id, chat_id) DO NOTHING
                """,
                userId,
                key.botId(),
                messengerType.name(),
                key.externalUserId(),
                key.chatId(),
                from(now),
                from(now)
        );
        return find(key).orElseThrow();
    }


    public Optional<BotUser> find(UserKey key) {
        List<BotUser> users = jdbcTemplate.query(
                """
                SELECT id, bot_id, messenger, external_user_id, chat_id, status, created_at
                FROM bot_users
                WHERE bot_id = ? AND external_user_id = ? AND chat_id = ?
                """,
                this::mapUser,
                key.botId(),
                key.externalUserId(),
                key.chatId()
        );
        return users.stream().findFirst();
    }


    public void activate(UUID userId) {
        jdbcTemplate.update(
                "UPDATE bot_users SET status = 'ACTIVE', updated_at = ? WHERE id = ?",
                from(clock.instant()),
                userId
        );
    }


    public void markDeleted(UUID userId) {
        jdbcTemplate.update(
                "UPDATE bot_users SET status = 'DELETED', updated_at = ? WHERE id = ?",
                from(clock.instant()),
                userId
        );
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
