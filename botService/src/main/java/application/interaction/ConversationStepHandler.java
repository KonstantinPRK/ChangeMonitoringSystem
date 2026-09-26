package application.interaction;

import application.user.BotUser;

/**
 * Обрабатывает один сценарий через {@code ConversationStepHandler}.
 */
public interface ConversationStepHandler {
    /**
     * Возвращает состояние диалога, которое обрабатывает реализация.
     *
     * @return поддерживаемое состояние диалога
     */
    ConversationState state();


    /**
     * Обрабатывает ввод пользователя в активном многошаговом диалоге.
     *
     * @param user пользователь текущего бота
     * @param session текущая сессия диалога
     * @param text исходный текст пользователя
     * @return действия, которые требуется сохранить и доставить
     */
    InteractionResult handle(BotUser user, ConversationSession session, String text);
}
