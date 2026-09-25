package application.messenger;

import java.util.concurrent.CompletionStage;

public interface MessageSender {
    CompletionStage<DeliveryReceipt> send(OutgoingMessage message);
}
