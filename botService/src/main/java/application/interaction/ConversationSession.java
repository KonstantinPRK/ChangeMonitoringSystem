package application.interaction;

import java.util.List;
import java.util.UUID;

public record ConversationSession(
        UUID userId,
        ConversationState state,
        String draftLink,
        List<String> tags,
        List<String> filters
) {
    public static ConversationSession initial(UUID userId) {
        return new ConversationSession(
                userId,
                ConversationState.WAITING_COMMAND,
                null,
                List.of(),
                List.of()
        );
    }


    public ConversationSession moveTo(ConversationState nextState) {
        return new ConversationSession(userId, nextState, draftLink, tags, filters);
    }


    public ConversationSession withLink(String link) {
        return new ConversationSession(userId, state, link, tags, filters);
    }


    public ConversationSession withTags(List<String> newTags) {
        return new ConversationSession(userId, state, draftLink, newTags, filters);
    }
}
