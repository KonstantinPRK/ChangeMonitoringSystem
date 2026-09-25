package application.telegram;

import application.messenger.DeliveryReceipt;
import application.messenger.MessageDeliveryException;
import application.messenger.MessageSender;
import application.messenger.OutgoingMessage;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@Component
public class TelegramMessageSender implements MessageSender {
    private final TelegramApiClient apiClient;


    public TelegramMessageSender(TelegramApiClient apiClient) {
        this.apiClient = apiClient;
    }


    @Override
    public CompletionStage<DeliveryReceipt> send(OutgoingMessage message) {
        Map<String, Object> request = Map.of(
                "chat_id", message.chatId(),
                "text", message.text()
        );
        return apiClient.post("sendMessage", request)
                .thenApply(this::readReceipt)
                .exceptionallyCompose(failure -> java.util.concurrent.CompletableFuture.failedStage(
                        translate(failure)
                ));
    }


    private DeliveryReceipt readReceipt(JsonNode sentMessage) {
        return new DeliveryReceipt(sentMessage.path("message_id").asString());
    }


    private Throwable translate(Throwable failure) {
        Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        if (!(cause instanceof TelegramApiException apiFailure)) return cause;

        boolean permanent = apiFailure.errorCode() == 400 || apiFailure.errorCode() == 403;
        Duration retryAfter = apiFailure.retryAfter().isZero()
                ? Duration.ofSeconds(2)
                : apiFailure.retryAfter();
        return new MessageDeliveryException(apiFailure.getMessage(), permanent, retryAfter);
    }
}
