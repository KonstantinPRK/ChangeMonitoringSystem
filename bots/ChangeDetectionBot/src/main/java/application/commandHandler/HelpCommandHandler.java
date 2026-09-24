package application.commandHandler;

import application.ConversationState;
import application.User;

public class HelpCommandHandler implements Command {

    @Override
    public String process(
        CommandType commandType,
        User user
    ) {
        return """
            Доступные команды:
            /start — начать работу с ботом
            /help — показать доступные команды
            /track — добавить ссылку для отслеживания
            /untrack — прекратить отслеживание ссылки
            /list — показать отслеживаемые ссылки
            /stop — остановить все отслеживания
            /delete — удалить аккаунт
            """;
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_COMMAND;
    }
}
