package application.link;

import application.config.JsonCodec;
import application.queue.QueueKind;
import application.queue.QueueStore;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class LinkRequestQueue {
    private final QueueStore queue;
    private final JsonCodec json;


    public LinkRequestQueue(QueueStore queue, JsonCodec json) {
        this.queue = queue;
        this.json = json;
    }


    public void addToQueue(LinkRequest request) {
        String stream = UUID.nameUUIDFromBytes(request.link().address().getBytes(StandardCharsets.UTF_8)).toString();
        queue.publish(QueueKind.TRACKER_REQUEST, request.requestId(), stream, json.write(request));
    }
}
