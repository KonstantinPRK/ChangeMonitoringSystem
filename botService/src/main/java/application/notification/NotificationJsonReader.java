package application.notification;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Component
public class NotificationJsonReader {
    private final ObjectMapper objectMapper;
    private final Validator validator;


    public NotificationJsonReader(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }


    public BotNotification read(String json) {
        BotNotification notification;
        try {
            notification = objectMapper.readValue(json, BotNotification.class);

        } catch (Exception exception) {
            throw new InvalidNotificationException("Notification JSON cannot be parsed", exception);

        }

        Set<ConstraintViolation<BotNotification>> violations = validator.validate(notification);
        if (!violations.isEmpty()) throw new InvalidNotificationException(violations.toString());
        return notification;
    }
}
