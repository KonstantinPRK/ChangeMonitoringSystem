package application.catalog.api;

import application.catalog.model.Link;
import application.catalog.model.TrackedLink;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Читает и преобразует входные данные для {@code SubscriptionLinkResponseReader}.
 */
@Component
public class SubscriptionLinkResponseReader {
    private final SubscriptionResponseValidator responseValidator;
    private final ObjectMapper objectMapper;


    public SubscriptionLinkResponseReader(
            SubscriptionResponseValidator responseValidator,
            ObjectMapper objectMapper
    ) {
        this.responseValidator = responseValidator;
        this.objectMapper = objectMapper;
    }


    public List<TrackedLink> read(HttpResponse<String> response) {
        responseValidator.requireSuccess(response);
        JsonNode root = objectMapper.readTree(response.body());

        List<TrackedLink> links = new ArrayList<>();
        root.forEach(node -> links.add(readLink(node)));

        return links;
    }


    private TrackedLink readLink(JsonNode node) {
        JsonNode linkNode = node.path("link");

        Link link = new Link(linkNode.path("domain").asString(), linkNode.path("address").asString());

        return new TrackedLink(node.path("id").asLong(), link, node.path("revision").asLong());
    }
}
