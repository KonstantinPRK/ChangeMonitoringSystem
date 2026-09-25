package application;

import application.config.JsonCodec;
import application.link.LinkNotification;
import application.link.LinkRequest;
import application.queue.QueueKind;
import application.queue.QueueProcessor;
import application.queue.QueueReceipt;
import application.subscriptions.ExchangeDataChannel;
import application.tracker.TrackerClient;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrackerSubscriptionBridge {
    private final ExchangeDataChannel exchange;
    private final QueueProcessor processor;
    private final JsonCodec json;
    private final TrackerClient client;


    public TrackerSubscriptionBridge(ExchangeDataChannel exchange, QueueProcessor processor, JsonCodec json, TrackerClient client) {
        this.exchange = exchange;
        this.processor = processor;
        this.json = json;
        this.client = client;
    }


    public QueueReceipt accept(LinkNotification notification) {
        return exchange.putLinkNotification(notification);
    }


    @Scheduled(fixedDelayString = "${app.queue.poll-interval:1s}")
    public void forwardLinkNotifications() {
        processor.process(
                QueueKind.LINK_NOTIFICATION,
                true,
                payload -> exchange.processNotification(
                        json.read(payload, LinkNotification.class)
                )
        );
    }


    @Scheduled(fixedDelayString = "${app.queue.poll-interval:1s}")
    public void forwardLinkRequests() {
        processor.process(QueueKind.TRACKER_REQUEST, false, payload -> client.send(json.read(payload, LinkRequest.class)));
    }
}
