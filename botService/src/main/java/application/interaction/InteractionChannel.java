package application.interaction;

import application.command.CommandDispatcher;
import application.user.BotUser;

import org.springframework.stereotype.Component;

@Component
public class InteractionChannel {
    private final CommandDispatcher commandDispatcher;
    private final ConversationFlow conversationFlow;


    public InteractionChannel(CommandDispatcher commandDispatcher, ConversationFlow conversationFlow) {
        this.commandDispatcher = commandDispatcher;
        this.conversationFlow = conversationFlow;
    }


    public InteractionResult handle(BotUser user, ConversationSession session, String text) {
        if (session.state() == ConversationState.WAITING_COMMAND) {
            return commandDispatcher.dispatch(user, session, text);
        }

        return conversationFlow.continueConversation(user, session, text);
    }
}
