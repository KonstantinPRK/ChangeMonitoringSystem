package application.subscriptions;

import application.config.JsonCodec;
import application.queue.QueueKind;
import application.queue.QueueReceipt;
import application.queue.QueueStore;

import org.springframework.stereotype.Component;

@Component
public class SubscriptionRequestQueue {
    private final QueueStore queue;
    private final JsonCodec json;


    public SubscriptionRequestQueue(QueueStore queue, JsonCodec json) {
        this.queue = queue;
        this.json = json;
    }


    public QueueReceipt addToQueue(SubscriptionRequest request) {
        return queue.publish(QueueKind.SUBSCRIPTION, request.requestId(), json.write(request.user()), json.write(request));
    }
}
