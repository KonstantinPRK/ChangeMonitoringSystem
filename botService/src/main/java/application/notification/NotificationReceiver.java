package application.notification;

import application.messenger.MessengerClientRegistry;
import application.outbox.OutgoingMessageService;
import application.user.BotUser;
import application.user.UserKey;
import application.user.UserManager;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class NotificationReceiver {
    private final MessengerClientRegistry clientRegistry;
    private final UserManager userManager;
    private final NotificationRepository notificationRepository;
    private final OutgoingMessageService outgoingMessageService;
    private final Counter receivedNotifications;


    public NotificationReceiver(
            MessengerClientRegistry clientRegistry,
            UserManager userManager,
            NotificationRepository notificationRepository,
            OutgoingMessageService outgoingMessageService,
            MeterRegistry meterRegistry
    ) {
        this.clientRegistry = clientRegistry;
        this.userManager = userManager;
        this.notificationRepository = notificationRepository;
        this.outgoingMessageService = outgoingMessageService;
        receivedNotifications = meterRegistry.counter("bot_received_notifications_total");
    }


    @Transactional
    public void accept(BotNotification notification) {
        requireOwnBot(notification);
        UserKey userKey = new UserKey(
                notification.user().botId(),
                notification.user().userId(),
                notification.user().chatId()
        );
        BotUser user;
        try {
            user = userManager.find(userKey);

        } catch (NoSuchElementException exception) {
            throw new InvalidNotificationException("Notification recipient is unknown", exception);

        }
        if (!notificationRepository.save(notification, user.id())) return;

        String message = notification.message() + "\n" + notification.link().address();
        outgoingMessageService.enqueue(
                "notification:" + notification.notificationId(),
                user,
                message
        );
        receivedNotifications.increment();
    }


    private void requireOwnBot(BotNotification notification) {
        if (!clientRegistry.contains(notification.user().botId())) {
            throw new InvalidNotificationException("Notification is addressed to another bot");
        }
    }
}
