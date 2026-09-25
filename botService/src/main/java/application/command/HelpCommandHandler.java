package application.command;

import application.interaction.ConversationSession;
import application.interaction.InteractionResult;
import application.user.BotUser;

import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler implements CommandHandler {
    private static final String HELP = """
            /track — добавить ссылку
            /untrack — удалить ссылку
            /list — показать подписки
            /stop — удалить все подписки
            /delete — удалить пользователя
            /help — показать справку
            """;


    @Override
    public CommandType command() {
        return CommandType.HELP;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session) {
        return InteractionResult.reply(session, HELP);
    }
}
