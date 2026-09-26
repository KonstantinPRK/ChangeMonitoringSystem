package application.connector.github.inspection.strategy;

import application.snapshot.ResourceObservation;
import application.connector.github.model.GitHubResourceType;
import application.connector.github.model.GitHubTarget;

import java.util.concurrent.CompletionStage;

/**
 * Получает состояние удалённого ресурса через {@code GitHubResourceInspector}.
 */
public interface GitHubResourceInspector {
    /**
     * Возвращает тип ресурса GitHub, поддерживаемый стратегией.
     *
     * @return поддерживаемый тип ресурса
     */
    GitHubResourceType supportedType();


    /**
     * Читает текущее состояние распознанного ресурса GitHub.
     *
     * @param target распознанный ресурс GitHub
     * @param etag предыдущий тег сущности для условного запроса
     * @return асинхронный результат наблюдения за ресурсом
     */
    CompletionStage<ResourceObservation> inspect(GitHubTarget target, String etag);
}
