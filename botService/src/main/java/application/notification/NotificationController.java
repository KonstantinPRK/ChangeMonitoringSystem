package application.notification;

import application.subscription.SubscriptionDataBridge;

import jakarta.validation.Valid;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Предоставляет HTTP-эндпоинты компонента {@code NotificationController}.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP", matchIfMissing = true)
public class NotificationController {
    private final SubscriptionDataBridge subscriptionDataBridge;
    private final InternalRequestAuthorizer requestAuthorizer;


    public NotificationController(
            SubscriptionDataBridge subscriptionDataBridge,
            InternalRequestAuthorizer requestAuthorizer
    ) {
        this.subscriptionDataBridge = subscriptionDataBridge;
        this.requestAuthorizer = requestAuthorizer;
    }


    @PostMapping
    public ResponseEntity<Void> accept(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody BotNotification notification
    ) {
        if (!requestAuthorizer.authorized(authorization)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        subscriptionDataBridge.accept(notification);
        return ResponseEntity.accepted().build();
    }
}
