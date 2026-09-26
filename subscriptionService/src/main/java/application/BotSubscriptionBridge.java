package application;

import application.config.JsonCodec;
import application.notification.Notification;
import application.queue.QueueKind;
import application.queue.QueueProcessor;
import application.queue.QueueReceipt;
import application.subscriptions.ExchangeDataChannel;
import application.subscriptions.SubscriptionRequest;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Передаёт данные между независимыми частями приложения через {@code BotSubscriptionBridge}.
 */
@Component
public class BotSubscriptionBridge {
    private final ExchangeDataChannel exchange;
    private final QueueProcessor processor;
    private final JsonCodec json;
    private final application.notification.NotificationSender sender;


    public BotSubscriptionBridge(
            ExchangeDataChannel exchange,
            QueueProcessor processor,
            JsonCodec json,
            application.notification.NotificationSender sender
    ) {
        this.exchange = exchange;
        this.processor = processor;
        this.json = json;
        this.sender = sender;
    }


    public QueueReceipt accept(SubscriptionRequest request) {
        return exchange.putSubscriptionRequest(request);
    }


    @Scheduled(fixedDelayString = "${app.queue.poll-interval:1s}")
    public void forwardSubscriptionRequests() {
        processor.process(
                QueueKind.SUBSCRIPTION,
                true,
                payload -> exchange.processSubscription(
                        json.read(payload, SubscriptionRequest.class)
                )
        );
    }


    @Scheduled(fixedDelayString = "${app.queue.poll-interval:1s}")
    public void forwardSubscriptionNotifications() {
        processor.process(QueueKind.BOT_NOTIFICATION, false, payload -> sender.send(json.read(payload, Notification.class)));
    }
}
