package application.command;

import application.interaction.ConversationSession;
import application.interaction.InteractionResult;
import application.user.BotUser;

import org.springframework.stereotype.Component;

@Component
public class UnknownCommandHandler implements CommandHandler {
    @Override
    public CommandType command() {
        return CommandType.UNKNOWN;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session) {
        return InteractionResult.reply(session, "Неизвестная команда. Используйте /help.");
    }
}
