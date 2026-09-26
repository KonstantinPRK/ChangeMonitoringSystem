package application.subscription.api;

import application.subscription.model.Link;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Читает и преобразует входные данные для {@code SubscriptionResponseReader}.
 */
@Component
public class SubscriptionResponseReader {
    private final ObjectMapper objectMapper;


    public SubscriptionResponseReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public void requireSuccess(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) return;
        throw new SubscriptionServiceException(response.statusCode(), response.body());
    }


    public List<SubscriptionView> readSubscriptions(HttpResponse<String> response) {
        requireSuccess(response);
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode items = root.isArray() ? root : root.path("items");
        List<SubscriptionView> subscriptions = new ArrayList<>();
        items.forEach(item -> subscriptions.add(readSubscription(item)));
        return subscriptions;
    }


    public SubscriptionQueueReceipt readReceipt(HttpResponse<String> response) {
        requireSuccess(response);
        return objectMapper.readValue(response.body(), SubscriptionQueueReceipt.class);
    }


    private SubscriptionView readSubscription(JsonNode item) {
        JsonNode linkNode = item.path("link");
        Link link = new Link(linkNode.path("domain").asString(), linkNode.path("address").asString());
        return new SubscriptionView(
                link,
                readStrings(item.path("tags")),
                readStrings(item.path("filters"))
        );
    }


    private List<String> readStrings(JsonNode values) {
        List<String> result = new ArrayList<>();
        values.forEach(value -> result.add(value.asString()));
        return result;
    }
}
