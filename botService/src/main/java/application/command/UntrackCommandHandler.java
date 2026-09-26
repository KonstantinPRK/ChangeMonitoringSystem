package application.command;

import application.interaction.ConversationSession;
import application.interaction.ConversationState;
import application.interaction.InteractionResult;
import application.user.BotUser;

import org.springframework.stereotype.Component;

/**
 * Обрабатывает один сценарий через {@code UntrackCommandHandler}.
 */
@Component
public class UntrackCommandHandler implements CommandHandler {
    @Override
    public CommandType command() {
        return CommandType.UNTRACK;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session) {
        return InteractionResult.reply(
                session.moveTo(ConversationState.WAITING_UNTRACK_LINK),
                "Отправьте ссылку, которую больше не нужно отслеживать."
        );
    }
}
