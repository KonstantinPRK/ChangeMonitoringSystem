package application.catalog.api;

import application.catalog.model.Link;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code LinkChangeRequest}.
 */
public record LinkChangeRequest(
        @NotNull UUID requestId,
        @NotNull LinkRequestAction actionType,
        @NotNull @Valid Link link,
        @PositiveOrZero long revision
) {
}
