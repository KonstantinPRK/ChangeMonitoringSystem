package application.command;

import application.interaction.ConversationSession;
import application.interaction.ConversationState;
import application.interaction.InteractionResult;
import application.user.BotUser;

import org.springframework.stereotype.Component;

/**
 * Обрабатывает один сценарий через {@code TrackCommandHandler}.
 */
@Component
public class TrackCommandHandler implements CommandHandler {
    @Override
    public CommandType command() {
        return CommandType.TRACK;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session) {
        return InteractionResult.reply(
                session.moveTo(ConversationState.WAITING_TRACK_LINK),
                "Отправьте ссылку, которую нужно отслеживать."
        );
    }
}
