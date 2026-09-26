package application.messenger;

/**
 * Предоставляет доступ к операциям удалённого сервиса через {@code MessengerClient}.
 */
public interface MessengerClient {
    /**
     * Возвращает уникальный идентификатор бота для маршрутизации.
     *
     * @return идентификатор бота
     */
    String botId();


    /**
     * Возвращает платформу мессенджера, которую обслуживает клиент.
     *
     * @return тип мессенджера
     */
    MessengerType messengerType();


    /**
     * Возвращает источник обновлений платформы.
     *
     * @return источник обновлений мессенджера
     */
    MessageUpdateSource updateSource();


    /**
     * Возвращает компонент отправки исходящих сообщений.
     *
     * @return отправитель сообщений мессенджера
     */
    MessageSender messageSender();
}
