package application.connector;

import application.catalog.model.Link;
import application.catalog.model.ResolvedResource;

/**
 * Преобразует внешний адрес в доменную модель через {@code LinkResolver}.
 */
public interface LinkResolver {
    /**
     * Проверяет ссылку и преобразует её в ресурс конкретного поставщика.
     *
     * @param link нормализованная отслеживаемая ссылка
     * @return распознанный ресурс поставщика
     */
    ResolvedResource resolve(Link link);
}
