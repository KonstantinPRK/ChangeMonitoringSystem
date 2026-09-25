package application.messaging;

import application.link.LinkNotification;
import application.link.LinkNotificationManager;

import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaUpdateListener {
    private final UpdateEventParser parser;
    private final LinkNotificationManager notificationManager;


    public KafkaUpdateListener(UpdateEventParser parser, LinkNotificationManager notificationManager) {
        this.parser = parser;
        this.notificationManager = notificationManager;
    }


    @KafkaListener(topics = "${app.kafka.update-topic}", groupId = "${app.kafka.group-id:subscription-service}")
    public void receive(@Payload(required = false) String payload) {
        LinkNotification notification = parser.parse(payload);
        try {
            notificationManager.saveLinkNotifications(new LinkNotification[]{notification});

        } catch (IllegalArgumentException | ConstraintViolationException exception) {
            throw new InvalidUpdateException("Update contents are invalid", exception);

        } catch (ResponseStatusException exception) {
            int status = exception.getStatusCode().value();
            if (status == 400 || status == 403 || status == 409 || status == 422) {
                throw new InvalidUpdateException("Update cannot be accepted", exception);
            }
            throw exception;

        }
    }
}
