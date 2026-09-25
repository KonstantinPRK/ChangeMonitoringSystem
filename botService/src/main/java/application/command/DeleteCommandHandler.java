package application.command;

import application.interaction.ConversationSession;
import application.interaction.ConversationState;
import application.interaction.InteractionResult;
import application.user.BotUser;

import org.springframework.stereotype.Component;

@Component
public class DeleteCommandHandler implements CommandHandler {
    @Override
    public CommandType command() {
        return CommandType.DELETE;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session) {
        return InteractionResult.reply(
                session.moveTo(ConversationState.WAITING_DELETE_CONFIRMATION),
                "Для удаления пользователя и всех подписок отправьте DELETE."
        );
    }
}
