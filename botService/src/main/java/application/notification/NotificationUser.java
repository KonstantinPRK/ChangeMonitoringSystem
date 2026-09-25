package application.notification;

import jakarta.validation.constraints.NotBlank;

public record NotificationUser(
        @NotBlank String botId,
        @NotBlank String userId,
        @NotBlank String chatId
) {
}
