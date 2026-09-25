package application.interaction;

import application.user.BotUser;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ConversationFlow {
    private final Map<ConversationState, ConversationStepHandler> handlers =
            new EnumMap<>(ConversationState.class);


    public ConversationFlow(List<ConversationStepHandler> stepHandlers) {
        stepHandlers.forEach(handler -> handlers.put(handler.state(), handler));
    }


    public InteractionResult continueConversation(
            BotUser user,
            ConversationSession session,
            String text
    ) {
        return handlers.get(session.state()).handle(user, session, text);
    }
}
