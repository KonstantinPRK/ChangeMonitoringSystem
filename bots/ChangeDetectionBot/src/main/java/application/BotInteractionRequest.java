package application;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BotInteractionRequest {
    private final String botId;
    private final String senderId;
    private final String chatId;
    private final String text;


    public BotInteractionRequest(
            String botId,
            String senderId,
            String chatId,
            String text
    ) {
        this.botId = requireText(botId, "botId");
        this.senderId = requireText(senderId, "senderId");
        this.chatId = requireText(chatId, "chatId");
        this.text = requireText(text, "text");
    }


    @JsonProperty("botId")
    public String botId() {
        return botId;
    }


    @JsonProperty("senderId")
    public String senderId() {
        return senderId;
    }


    @JsonProperty("chatId")
    public String chatId() {
        return chatId;
    }


    @JsonProperty("text")
    public String text() {
        return text;
    }


    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value;
    }
}