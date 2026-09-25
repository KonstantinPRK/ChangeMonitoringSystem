package application.interaction;

import application.persistence.JsonValues;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static application.persistence.DatabaseTime.from;

@Repository
public class ConversationSessionRepository {
    private final JdbcTemplate jdbcTemplate;
    private final JsonValues jsonValues;
    private final Clock clock;


    public ConversationSessionRepository(JdbcTemplate jdbcTemplate, JsonValues jsonValues, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
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
        jdbcTemplate.update(
                """
                INSERT INTO conversation_sessions (
                    user_id, state, draft_link, tags_json, filters_json, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET
                    state = EXCLUDED.state,
                    draft_link = EXCLUDED.draft_link,
                    tags_json = EXCLUDED.tags_json,
                    filters_json = EXCLUDED.filters_json,
                    updated_at = EXCLUDED.updated_at
                """,
                session.userId(),
                session.state().name(),
                session.draftLink(),
                jsonValues.writeStrings(session.tags()),
                jsonValues.writeStrings(session.filters()),
                from(clock.instant())
        );
    }


    private Optional<ConversationSession> find(UUID userId) {
        List<ConversationSession> sessions = jdbcTemplate.query(
                """
                SELECT user_id, state, draft_link, tags_json, filters_json
                FROM conversation_sessions
                WHERE user_id = ?
                """,
                this::mapSession,
                userId
        );
        return sessions.stream().findFirst();
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
