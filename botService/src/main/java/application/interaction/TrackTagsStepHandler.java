package application.interaction;

import application.user.BotUser;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Обрабатывает один сценарий через {@code TrackTagsStepHandler}.
 */
@Component
public class TrackTagsStepHandler implements ConversationStepHandler {
    private final TextListParser textListParser;


    public TrackTagsStepHandler(TextListParser textListParser) {
        this.textListParser = textListParser;
    }


    @Override
    public ConversationState state() {
        return ConversationState.WAITING_TRACK_TAGS;
    }


    @Override
    public InteractionResult handle(BotUser user, ConversationSession session, String text) {
        List<String> tags = textListParser.parse(text);
        ConversationSession nextSession = session
                .withTags(tags)
                .moveTo(ConversationState.WAITING_TRACK_FILTERS);
        return InteractionResult.reply(
                nextSession,
                "Отправьте фильтры через пробел или '-' без фильтров."
        );
    }
}
