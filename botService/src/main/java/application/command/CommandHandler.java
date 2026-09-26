package application.command;

import application.interaction.ConversationSession;
import application.interaction.InteractionResult;
import application.user.BotUser;

/**
 * Обрабатывает один сценарий через {@code CommandHandler}.
 */
public interface CommandHandler {
    /**
     * Возвращает команду, которую обрабатывает реализация.
     *
     * @return поддерживаемый тип команды
     */
    CommandType command();


    /**
     * Обрабатывает команду для текущего пользователя и диалога.
     *
     * @param user пользователь текущего бота
     * @param session текущая сессия диалога
     * @return действия, которые требуется сохранить и доставить
     */
    InteractionResult handle(BotUser user, ConversationSession session);
}
