package application.command;

import application.interaction.ConversationSession;
import application.interaction.ConversationState;
import application.interaction.InteractionResult;
import application.user.BotUser;
import application.user.UserManager;

import org.springframework.stereotype.Component;

/**
 * Обрабатывает один сценарий через {@code StartCommandHandler}.
 */
@Component
public class StartCommandHandler implements CommandHandler {
    private final UserManager userManager;


    public StartCommandHandler(UserManager userManager) {
        this.userManager = userManager;
    }


    @Override
    public CommandType command() {
        return CommandType.START;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session) {
        userManager.activate(user);
        ConversationSession nextSession = session.moveTo(ConversationState.WAITING_COMMAND);
        return InteractionResult.reply(
                nextSession,
                "Бот запущен. Используйте /help, чтобы посмотреть команды."
        );
    }
}
