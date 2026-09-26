package application.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URI;

/**
 * Передаёт между компонентами данные {@code BotRegistrationRequest}.
 */
public record BotRegistrationRequest(@NotBlank @Size(max = 128) String botId, @NotNull URI baseUrl) {
}
