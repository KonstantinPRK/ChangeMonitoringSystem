package application.connector;

import java.time.Instant;
import java.util.Optional;

/**
 * Определяет контракт {@code TrackingConnector}.
 */
public interface TrackingConnector {
    /**
     * Возвращает возможности коннектора для маршрутизации и регистрации.
     *
     * @return описание коннектора
     */
    ConnectorDescriptor descriptor();


    /**
     * Возвращает преобразователь ссылок конкретного поставщика.
     *
     * @return преобразователь ссылок
     */
    LinkResolver linkResolver();


    /**
     * Возвращает компонент проверки ресурсов конкретного поставщика.
     *
     * @return компонент проверки ресурсов
     */
    ResourceInspector resourceInspector();


    /**
     * Возвращает политику повторных попыток конкретного поставщика.
     *
     * @return политика обработки ошибок
     */
    ProviderFailurePolicy failurePolicy();


    /**
     * Возвращает срок общей блокировки поставщика, если она действует.
     *
     * @return срок блокировки или пустое значение
     */
    Optional<Instant> blockedUntil();
}
