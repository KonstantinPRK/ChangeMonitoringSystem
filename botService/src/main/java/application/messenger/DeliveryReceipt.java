package application.messenger;

/**
 * Передаёт между компонентами данные {@code DeliveryReceipt}.
 */
public record DeliveryReceipt(String messengerMessageId) {
}
