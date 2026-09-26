package application.catalog.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Передаёт между компонентами данные {@code Link}.
 */
public record Link(
        @NotBlank @Size(max = 253) String domain,
        @NotBlank @Size(max = 2048) String address
) {
}
