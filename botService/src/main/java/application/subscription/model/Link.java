package application.subscription.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Передаёт между компонентами данные {@code Link}.
 */
public record Link(@NotBlank String domain, @NotBlank String address) {
}
