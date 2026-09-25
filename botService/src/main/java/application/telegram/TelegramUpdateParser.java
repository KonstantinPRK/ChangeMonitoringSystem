package application.telegram;

import application.config.TelegramProperties;
import application.messenger.IncomingMessage;
import application.messenger.IncomingMessageBatch;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramUpdateParser {
    private final TelegramProperties telegramProperties;


    public TelegramUpdateParser(TelegramProperties telegramProperties) {
        this.telegramProperties = telegramProperties;
    }


    public IncomingMessageBatch parse(JsonNode updates, long currentOffset) {
        List<IncomingMessage> messages = new ArrayList<>();
        long nextOffset = currentOffset;

        for (JsonNode update : updates) {
            long updateId = update.path("update_id").asLong();
            nextOffset = Math.max(nextOffset, updateId + 1);
            JsonNode message = update.path("message");
            if (!message.hasNonNull("text")) continue;

            messages.add(readMessage(updateId, message));
        }
        return new IncomingMessageBatch(
                telegramProperties.botId(),
                nextOffset,
                List.copyOf(messages)
        );
    }


    private IncomingMessage readMessage(long updateId, JsonNode message) {
        return new IncomingMessage(
                updateId,
                message.path("from").path("id").asString(),
                message.path("chat").path("id").asString(),
                message.path("text").asString(),
                Instant.ofEpochSecond(message.path("date").asLong())
        );
    }
}
