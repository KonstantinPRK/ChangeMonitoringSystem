package application.interaction;

import application.subscription.model.Link;
import application.subscription.model.LinkParser;
import application.subscription.operation.SubscriptionOperationDraft;
import application.subscription.operation.SubscriptionOperationType;
import application.user.BotUser;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UntrackLinkStepHandler implements ConversationStepHandler {
    private final LinkParser linkParser;


    public UntrackLinkStepHandler(LinkParser linkParser) {
        this.linkParser = linkParser;
    }


    @Override
    public ConversationState state() {
        return ConversationState.WAITING_UNTRACK_LINK;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session, String text) {
        Link link = linkParser.parse(text);
        SubscriptionOperationDraft operation = new SubscriptionOperationDraft(
                SubscriptionOperationType.UNTRACK,
                link,
                List.of(),
                List.of()
        );
        return InteractionResult.operation(
                ConversationSession.initial(user.id()),
                "Запрос на удаление подписки принят.",
                operation
        );
    }
}
