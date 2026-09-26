package application.notification;

import jakarta.validation.constraints.NotBlank;

/**
 * Передаёт между компонентами данные {@code NotificationUser}.
 */
public record NotificationUser(
        @NotBlank String botId,
        @NotBlank String userId,
        @NotBlank String chatId
) {
}
