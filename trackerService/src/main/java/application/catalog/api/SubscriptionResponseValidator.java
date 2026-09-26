package application.catalog.api;

import org.springframework.stereotype.Component;

import java.net.http.HttpResponse;

/**
 * Проверяет корректность данных для {@code SubscriptionResponseValidator}.
 */
@Component
public class SubscriptionResponseValidator {
    public void requireSuccess(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) return;

        throw new SubscriptionServiceException(response.statusCode(), response.body());
    }
}
