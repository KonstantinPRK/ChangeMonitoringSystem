package application;

import application.infrastructure.messaging.CommunicationClient;

public final class ChangeDetectionBot {
    private final CommunicationClient telegramClient;
    private final MonitoringSystemClient monitoringSystemClient;
    private final UserManager userManager;

    public ChangeDetectionBot(
        CommunicationClient telegramClient,
        MonitoringSystemClient monitoringSystemClient,
        UserManager userManager
    ) {
        this.telegramClient = telegramClient;
        this.monitoringSystemClient = monitoringSystemClient;
        this.userManager = userManager;
    }
}

