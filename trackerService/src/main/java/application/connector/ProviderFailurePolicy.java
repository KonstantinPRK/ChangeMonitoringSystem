package application.connector;

import application.catalog.model.TrackedResource;

import java.time.Instant;

/**
 * Определяет решение после ошибки через {@code ProviderFailurePolicy}.
 */
public interface ProviderFailurePolicy {
    /**
     * Вычисляет время следующей проверки ресурса после ошибки.
     *
     * @param resource ресурс, проверка которого завершилась ошибкой
     * @param failure ошибка поставщика или транспорта
     * @return время следующей разрешённой попытки
     */
    Instant nextAttempt(TrackedResource resource, Throwable failure);
}
