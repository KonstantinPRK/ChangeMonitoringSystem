package application.notification;

import application.messenger.MessengerClientRegistry;
import application.subscription.SubscriptionDataBridge;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaNotificationListener {
    private final NotificationJsonReader notificationReader;
    private final SubscriptionDataBridge subscriptionDataBridge;
    private final NotificationDeadLetterPublisher deadLetterPublisher;
    private final MessengerClientRegistry clientRegistry;


    public KafkaNotificationListener(
            NotificationJsonReader notificationReader,
            SubscriptionDataBridge subscriptionDataBridge,
            NotificationDeadLetterPublisher deadLetterPublisher,
            MessengerClientRegistry clientRegistry
    ) {
        this.notificationReader = notificationReader;
        this.subscriptionDataBridge = subscriptionDataBridge;
        this.deadLetterPublisher = deadLetterPublisher;
        this.clientRegistry = clientRegistry;
    }


    @KafkaListener(
            topics = "${app.subscription.kafka.topic}",
            groupId = "${app.subscription.kafka.group-id}"
    )
    public void accept(String json) {
        try {
            BotNotification notification = notificationReader.read(json);
            if (!clientRegistry.contains(notification.user().botId())) return;
            subscriptionDataBridge.accept(notification);

        } catch (InvalidNotificationException exception) {
            deadLetterPublisher.publish(json);

        }
    }
}
