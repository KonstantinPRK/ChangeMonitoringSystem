package application.messenger;

import application.inbox.IncomingMessageAcceptor;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletionStage;

@Component
public class MessengerDataBridge {
    private final MessengerClientRegistry clientRegistry;
    private final IncomingMessageAcceptor incomingMessageAcceptor;


    public MessengerDataBridge(
            MessengerClientRegistry clientRegistry,
            IncomingMessageAcceptor incomingMessageAcceptor
    ) {
        this.clientRegistry = clientRegistry;
        this.incomingMessageAcceptor = incomingMessageAcceptor;
    }


    public void start() {
        for (MessengerClient client : clientRegistry.clients()) {
            client.updateSource().start(incomingMessageAcceptor::accept);
        }
    }


    public void stop() {
        for (MessengerClient client : clientRegistry.clients()) {
            client.updateSource().stop();
        }
    }


    public CompletionStage<DeliveryReceipt> send(OutgoingMessage message) {
        MessengerClient client = clientRegistry.get(message.botId());
        return client.messageSender().send(message);
    }
}
