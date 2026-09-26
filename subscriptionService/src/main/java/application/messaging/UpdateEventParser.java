package application.messaging;

import application.link.LinkNotification;

import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;

/**
 * Читает и преобразует входные данные для {@code UpdateEventParser}.
 */
@Component
public class UpdateEventParser {
    private final ObjectReader eventReader;
    private final Validator validator;


    public UpdateEventParser(ObjectMapper objectMapper, Validator validator) {
        this.eventReader = objectMapper.readerFor(LinkNotification.class)
                .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        this.validator = validator;
    }


    public LinkNotification parse(String payload) {
        if (payload == null) throw new InvalidUpdateException("Update payload must not be null");
        LinkNotification notification;
        try {
            notification = eventReader.readValue(payload);

        } catch (JacksonException | IllegalArgumentException exception) {
            throw new InvalidUpdateException("Update payload is not a valid update JSON", exception);

        }
        if (notification == null) throw new InvalidUpdateException("Update payload must be an object");
        if (!validator.validate(notification).isEmpty()) throw new InvalidUpdateException("Update fields are invalid");
        return notification;
    }
}
