package application.interaction;

import application.subscription.model.Link;
import application.subscription.model.LinkParser;
import application.user.BotUser;

import org.springframework.stereotype.Component;

/**
 * Обрабатывает один сценарий через {@code TrackLinkStepHandler}.
 */
@Component
public class TrackLinkStepHandler implements ConversationStepHandler {
    private final LinkParser linkParser;


    public TrackLinkStepHandler(LinkParser linkParser) {
        this.linkParser = linkParser;
    }


    @Override
    public ConversationState state() {
        return ConversationState.WAITING_TRACK_LINK;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session, String text) {
        Link link = linkParser.parse(text);
        ConversationSession nextSession = session
                .withLink(link.address())
                .moveTo(ConversationState.WAITING_TRACK_TAGS);
        return InteractionResult.reply(nextSession, "Отправьте теги через пробел или '-' без тегов.");
    }
}
