package application.messenger;

import java.util.concurrent.CompletionStage;

/**
 * Отправляет данные во внешний транспорт через {@code MessageSender}.
 */
public interface MessageSender {
    /**
     * Отправляет одно долговечное исходящее сообщение через API мессенджера.
     *
     * @param message сообщение для доставки
     * @return асинхронное подтверждение доставки
     */
    CompletionStage<DeliveryReceipt> send(OutgoingMessage message);
}
