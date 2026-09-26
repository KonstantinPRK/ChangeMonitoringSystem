package application.interaction;

import application.subscription.model.Link;
import application.subscription.model.LinkParser;
import application.subscription.operation.SubscriptionOperationDraft;
import application.subscription.operation.SubscriptionOperationType;
import application.user.BotUser;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Обрабатывает один сценарий через {@code TrackFiltersStepHandler}.
 */
@Component
public class TrackFiltersStepHandler implements ConversationStepHandler {
    private final LinkParser linkParser;
    private final TextListParser textListParser;


    public TrackFiltersStepHandler(LinkParser linkParser, TextListParser textListParser) {
        this.linkParser = linkParser;
        this.textListParser = textListParser;
    }


    @Override
    public ConversationState state() {
        return ConversationState.WAITING_TRACK_FILTERS;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session, String text) {
        Link link = linkParser.parse(session.draftLink());
        List<String> filters = textListParser.parse(text);
        SubscriptionOperationDraft operation = new SubscriptionOperationDraft(
                SubscriptionOperationType.TRACK,
                link,
                session.tags(),
                filters
        );
        return InteractionResult.operation(
                ConversationSession.initial(user.id()),
                "Запрос на добавление подписки принят.",
                operation
        );
    }
}
