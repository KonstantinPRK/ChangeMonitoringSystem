package application.subscription.cache;

import application.subscription.api.SubscriptionView;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriptionListFormatter {
    public String format(List<SubscriptionView> subscriptions) {
        if (subscriptions.isEmpty()) return "У вас нет отслеживаемых ссылок.";

        StringBuilder message = new StringBuilder("Ваши подписки:\n");
        for (int index = 0; index < subscriptions.size(); index++) {
            SubscriptionView subscription = subscriptions.get(index);
            message.append(index + 1)
                    .append(". ")
                    .append(subscription.link().address())
                    .append('\n');
        }
        return message.toString().stripTrailing();
    }
}
