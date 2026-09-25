package application.vk;

import application.messenger.DeliveryReceipt;
import application.messenger.MessageDeliveryException;
import application.messenger.MessageSender;
import application.messenger.OutgoingMessage;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

@Component
public class VkMessageSender implements MessageSender {
    private static final Set<Integer> PERMANENT_ERROR_CODES = Set.of(5, 7, 100, 901, 902, 917);
    private final VkApiClient apiClient;


    public VkMessageSender(VkApiClient apiClient) {
        this.apiClient = apiClient;
    }


    @Override
    public CompletionStage<DeliveryReceipt> send(OutgoingMessage message) {
        return apiClient.sendMessage(message.chatId(), message.text(), randomId(message))
                .thenApply(this::readReceipt)
                .exceptionallyCompose(failure -> CompletableFuture.failedStage(translate(failure)));
    }


    private int randomId(OutgoingMessage message) {
        int randomId = message.id().hashCode() & Integer.MAX_VALUE;
        return randomId == 0 ? 1 : randomId;
    }


    private DeliveryReceipt readReceipt(JsonNode response) {
        return new DeliveryReceipt(response.asString());
    }


    private Throwable translate(Throwable failure) {
        Throwable cause = failure instanceof CompletionException && failure.getCause() != null
                ? failure.getCause()
                : failure;
        if (!(cause instanceof VkApiException apiFailure)) return cause;

        boolean permanent = PERMANENT_ERROR_CODES.contains(apiFailure.errorCode());
        return new MessageDeliveryException(apiFailure.getMessage(), permanent, Duration.ofSeconds(2));
    }
}
