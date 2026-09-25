package application.subscription.model;

import jakarta.validation.constraints.NotBlank;

public record Link(@NotBlank String domain, @NotBlank String address) {
}
