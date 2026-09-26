package application.connector;

import application.catalog.model.TrackedResource;
import application.snapshot.ResourceObservation;

import java.util.concurrent.CompletionStage;

/**
 * Получает состояние удалённого ресурса через {@code ResourceInspector}.
 */
public interface ResourceInspector {
    /**
     * Читает текущее состояние ресурса у его поставщика.
     *
     * @param resource сохранённые метаданные ресурса
     * @param versionToken токен для условного запроса к поставщику
     * @return асинхронный результат наблюдения за ресурсом
     */
    CompletionStage<ResourceObservation> inspect(TrackedResource resource, String versionToken);
}
