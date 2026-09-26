package application.messenger;

/**
 * Определяет контракт {@code IncomingMessageConsumer}.
 */
@FunctionalInterface
public interface IncomingMessageConsumer {
    /**
     * Принимает сохранённую пачку обновлений мессенджера для обработки.
     *
     * @param batch входящие обновления и следующая позиция источника
     */
    void accept(IncomingMessageBatch batch);
}
