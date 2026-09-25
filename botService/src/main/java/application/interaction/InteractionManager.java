package application.interaction;

import application.inbox.StoredIncomingMessage;
import application.user.BotUser;
import application.user.UserManager;

import org.springframework.stereotype.Component;

@Component
public class InteractionManager {
    private final UserManager userManager;
    private final ConversationSessionRepository sessionRepository;
    private final InteractionChannel interactionChannel;


    public InteractionManager(
            UserManager userManager,
            ConversationSessionRepository sessionRepository,
            InteractionChannel interactionChannel
    ) {
        this.userManager = userManager;
        this.sessionRepository = sessionRepository;
        this.interactionChannel = interactionChannel;
    }


    public UserInteraction handle(StoredIncomingMessage message) {
        BotUser user = userManager.resolve(
                message.botId(),
                message.externalUserId(),
                message.chatId()
        );
        ConversationSession session = sessionRepository.findOrCreate(user.id());

        try {
            InteractionResult result = interactionChannel.handle(user, session, message.text());
            return new UserInteraction(user, result);

        } catch (IllegalArgumentException exception) {
            InteractionResult result = InteractionResult.reply(session, exception.getMessage());
            return new UserInteraction(user, result);

        }
    }
}
