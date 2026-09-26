package application.notification;

/**
 * Отправляет данные во внешний транспорт через {@code NotificationSender}.
 */
public interface NotificationSender {
    /**
     * Доставляет одно пользовательское уведомление через выбранный транспорт.
     *
     * @param notification уведомление для доставки
     */
    void send(Notification notification);
}
