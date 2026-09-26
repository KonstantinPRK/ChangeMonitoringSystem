package application.user;

/**
 * Передаёт между компонентами данные {@code UserKey}.
 */
public record UserKey(String botId, String externalUserId, String chatId) {
}
