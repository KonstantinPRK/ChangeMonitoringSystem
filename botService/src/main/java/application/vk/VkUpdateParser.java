package application.vk;

import application.config.VkProperties;
import application.messenger.IncomingMessage;
import application.messenger.IncomingMessageBatch;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class VkUpdateParser {
    private static final String INCOMING_MESSAGE_EVENT = "message_new";
    private final VkProperties vkProperties;


    public VkUpdateParser(VkProperties vkProperties) {
        this.vkProperties = vkProperties;
    }


    public IncomingMessageBatch parse(JsonNode response) {
        JsonNode updates = response.path("updates");
        if (!response.hasNonNull("ts") || !updates.isArray()) {
            throw new IllegalArgumentException("VK Long Poll response is incomplete");
        }

        List<IncomingMessage> messages = new ArrayList<>();
        for (JsonNode update : updates) {
            if (!INCOMING_MESSAGE_EVENT.equals(update.path("type").asString())) continue;

            JsonNode message = update.path("object").path("message");
            if (message.path("out").asInt() != 0 || !message.hasNonNull("text")) continue;
            String text = message.path("text").asString();
            if (text.isBlank()) continue;

            messages.add(readMessage(message, text));
        }
        return new IncomingMessageBatch(
                vkProperties.botId(),
                response.path("ts").asLong(),
                List.copyOf(messages)
        );
    }


    private IncomingMessage readMessage(JsonNode message, String text) {
        return new IncomingMessage(
                message.path("id").asLong(),
                message.path("from_id").asString(),
                message.path("peer_id").asString(),
                text,
                Instant.ofEpochSecond(message.path("date").asLong())
        );
    }
}
