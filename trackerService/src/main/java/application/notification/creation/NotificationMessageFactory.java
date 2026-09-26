package application.notification.creation;

import application.snapshot.ResourceSnapshot;
import application.catalog.model.TrackedResource;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Создаёт доменные или протокольные объекты через {@code NotificationMessageFactory}.
 */
@Component
public class NotificationMessageFactory {
    public String create(TrackedResource resource, ResourceSnapshot snapshot) {
        String provider = providerName(resource.providerKey());
        String resourceKind = resourceKindName(resource.resourceKind());
        String state = stateDescription(snapshot.state());

        return "Обновление %s: %s%s".formatted(
                provider,
                resourceKind,
                state
        );
    }


    private String providerName(String providerKey) {
        return switch (providerKey.toLowerCase(Locale.ROOT)) {
            case "github" -> "GitHub";
            case "stackoverflow" -> "Stack Overflow";
            default -> providerKey;
        };
    }


    private String resourceKindName(String resourceKind) {
        return switch (resourceKind.toLowerCase(Locale.ROOT)) {
            case "repository" -> "репозиторий";
            case "issue" -> "задача";
            case "pull_request" -> "запрос на слияние";
            case "question" -> "вопрос";
            default -> "ресурс";
        };
    }


    private String stateDescription(String state) {
        if (state == null || state.isBlank()) return "";

        String localizedState = switch (state.toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> "активен";
            case "ARCHIVED" -> "архивирован";
            case "OPEN" -> "открыт";
            case "CLOSED" -> "закрыт";
            case "ANSWERED" -> "есть ответ";
            case "MERGED" -> "объединён";
            default -> "изменено";
        };

        return " (состояние: " + localizedState + ')';
    }
}
