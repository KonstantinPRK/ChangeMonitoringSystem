package application.inbox;

import application.interaction.InteractionManager;
import application.interaction.InteractionResultWriter;
import application.interaction.UserInteraction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class IncomingMessageProcessor {
    private final IncomingMessageRepository messageRepository;
    private final InteractionManager interactionManager;
    private final InteractionResultWriter resultWriter;


    public IncomingMessageProcessor(
            IncomingMessageRepository messageRepository,
            InteractionManager interactionManager,
            InteractionResultWriter resultWriter
    ) {
        this.messageRepository = messageRepository;
        this.interactionManager = interactionManager;
        this.resultWriter = resultWriter;
    }


    @Transactional
    public void process(StoredIncomingMessage message) {
        UserInteraction interaction = interactionManager.handle(message);
        resultWriter.write(message.id(), interaction);
        messageRepository.complete(message);
    }
}
