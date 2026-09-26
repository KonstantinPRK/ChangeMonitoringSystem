package application.interaction;

import application.persistence.JsonValues;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static application.persistence.DatabaseTime.from;

/**
 * Отвечает за сохранение и чтение данных {@code ConversationSessionRepository}.
 */
@Repository
public class ConversationSessionRepository {
    private final JdbcClient jdbcClient;
    private final JsonValues jsonValues;
    private final Clock clock;


    public ConversationSessionRepository(JdbcClient jdbcClient, JsonValues jsonValues, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.jsonValues = jsonValues;
        this.clock = clock;
    }


    public ConversationSession findOrCreate(UUID userId) {
        Optional<ConversationSession> session = find(userId);
        if (session.isPresent()) return session.get();

        ConversationSession initialSession = ConversationSession.initial(userId);
        save(initialSession);
        return initialSession;
    }


    public void save(ConversationSession session) {
        jdbcClient.sql("""
                INSERT INTO conversation_sessions (
                    user_id, state, draft_link, tags_json, filters_json, updated_at
                ) VALUES (:userId, :state, :draftLink, :tags, :filters, :updatedAt)
                ON CONFLICT (user_id) DO UPDATE SET
                    state = EXCLUDED.state,
                    draft_link = EXCLUDED.draft_link,
                    tags_json = EXCLUDED.tags_json,
                    filters_json = EXCLUDED.filters_json,
                    updated_at = EXCLUDED.updated_at
                """)
                .param("userId", session.userId())
                .param("state", session.state().name())
                .param("draftLink", session.draftLink())
                .param("tags", jsonValues.writeStrings(session.tags()))
                .param("filters", jsonValues.writeStrings(session.filters()))
                .param("updatedAt", from(clock.instant()))
                .update();
    }


    private Optional<ConversationSession> find(UUID userId) {
        return jdbcClient.sql("""
                SELECT user_id, state, draft_link, tags_json, filters_json
                FROM conversation_sessions
                WHERE user_id = :userId
                """)
                .param("userId", userId)
                .query(this::mapSession)
                .optional();
    }


    private ConversationSession mapSession(ResultSet resultSet, int rowNumber) throws SQLException {
        return new ConversationSession(
                resultSet.getObject("user_id", UUID.class),
                ConversationState.valueOf(resultSet.getString("state")),
                resultSet.getString("draft_link"),
                jsonValues.readStrings(resultSet.getString("tags_json")),
                jsonValues.readStrings(resultSet.getString("filters_json"))
        );
    }
}
