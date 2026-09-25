package application.interaction;

import application.outbox.OutgoingMessageService;
import application.subscription.SubscriptionDataBridge;
import application.subscription.operation.SubscriptionOperationDraft;
import application.user.BotUser;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class InteractionResultWriter {
    private final ConversationSessionRepository sessionRepository;
    private final OutgoingMessageService outgoingMessageService;
    private final SubscriptionDataBridge subscriptionDataBridge;


    public InteractionResultWriter(
            ConversationSessionRepository sessionRepository,
            OutgoingMessageService outgoingMessageService,
            SubscriptionDataBridge subscriptionDataBridge
    ) {
        this.sessionRepository = sessionRepository;
        this.outgoingMessageService = outgoingMessageService;
        this.subscriptionDataBridge = subscriptionDataBridge;
    }


    public void write(UUID sourceMessageId, UserInteraction interaction) {
        BotUser user = interaction.user();
        InteractionResult result = interaction.result();
        sessionRepository.save(result.session());
        saveReplies(sourceMessageId, user, result.replies());
        saveOperations(sourceMessageId, user, result.operations());
    }


    private void saveReplies(UUID sourceMessageId, BotUser user, List<String> replies) {
        for (int index = 0; index < replies.size(); index++) {
            String deduplicationKey = "reply:" + sourceMessageId + ':' + index;
            outgoingMessageService.enqueue(deduplicationKey, user, replies.get(index));
        }
    }


    private void saveOperations(
            UUID sourceMessageId,
            BotUser user,
            List<SubscriptionOperationDraft> operations
    ) {
        for (int index = 0; index < operations.size(); index++) {
            UUID operationId = deterministicId(sourceMessageId + ":operation:" + index);
            subscriptionDataBridge.enqueue(operationId, user, operations.get(index));
        }
    }


    private UUID deterministicId(String value) {
        return UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }
}
