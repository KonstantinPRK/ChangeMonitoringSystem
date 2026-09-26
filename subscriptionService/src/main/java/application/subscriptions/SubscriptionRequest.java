package application.subscriptions;

import application.ActionType;
import application.link.Link;
import application.user.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Передаёт между компонентами данные {@code SubscriptionRequest}.
 */
public record SubscriptionRequest(
        @NotNull UUID requestId,
        @NotNull ActionType actionType,
        @NotNull @Valid User user,
        @NotNull @Valid Link link,
        @Size(max = 20) List<@NotBlank @Size(max = 256) String> tags,
        @Size(max = 20) List<@NotBlank @Size(max = 256) String> filters
) {
    public SubscriptionRequest {
        tags = tags == null ? List.of() : List.copyOf(tags);
        filters = filters == null ? List.of() : List.copyOf(filters);
    }
}
