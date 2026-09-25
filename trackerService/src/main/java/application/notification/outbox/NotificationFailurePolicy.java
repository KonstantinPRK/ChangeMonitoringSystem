package application.notification.outbox;

import application.config.SubscriptionServiceProperties;
import application.catalog.api.SubscriptionServiceException;
import application.notification.model.StoredNotification;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class NotificationFailurePolicy {
    private final SubscriptionServiceProperties serviceProperties;


    public NotificationFailurePolicy(SubscriptionServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }


    public boolean permanent(StoredNotification notification, Throwable failure) {
        if (!(failure instanceof SubscriptionServiceException serviceFailure)) return false;

        int statusCode = serviceFailure.statusCode();
        return statusCode >= 400 && statusCode < 500 && statusCode != 408 && statusCode != 429;
    }


    public Duration retryDelay(StoredNotification notification) {
        long multiplier = 1L << Math.min(notification.attempts(), 6);
        Duration delay = serviceProperties.requestRetryDelay().multipliedBy(multiplier);
        return delay.compareTo(Duration.ofMinutes(2)) > 0 ? Duration.ofMinutes(2) : delay;
    }
}
