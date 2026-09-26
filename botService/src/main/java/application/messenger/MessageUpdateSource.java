package application.messenger;

/**
 * Получает входные данные из внешнего источника через {@code MessageUpdateSource}.
 */
public interface MessageUpdateSource {
    /**
     * Запускает получение обновлений и передаёт сохранённые пачки потребителю.
     *
     * @param consumer получатель пачек входящих обновлений
     */
    void start(IncomingMessageConsumer consumer);


    /**
     * Прекращает запрашивать новые обновления мессенджера.
     */
    void stop();
}
