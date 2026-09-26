package application.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Передаёт между компонентами данные {@code User}.
 */
public record User(
        @NotBlank @Size(max = 128) String botId,
        @NotBlank @Size(max = 128) String userId,
        @NotBlank @Size(max = 128) String chatId
) {
}
