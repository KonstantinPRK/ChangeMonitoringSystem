package application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BotReply {
    private final String text;


    @JsonCreator
    public BotReply(
            @JsonProperty("text")
            String text
    ) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "text must not be blank"
            );
        }

        this.text = text;
    }


    public String text() {
        return text;
    }
}
