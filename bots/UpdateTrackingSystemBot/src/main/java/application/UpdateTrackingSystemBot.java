package application;

import application.messenger.MessageSender;
import application.messenger.MessageUpdateSource;

public final class UpdateTrackingSystemBot {
    private final MessageUpdateSource updateSource;
    private final MessageSender messageSender;
    private final MonitoringSystemClient monitoringSystemClient;
    private final UserManager userManager;

    public UpdateTrackingSystemBot(
        MessageUpdateSource updateSource,
        MessageSender messageSender,
        MonitoringSystemClient monitoringSystemClient,
        UserManager userManager
    ) {
        this.updateSource = updateSource;
        this.messageSender = messageSender;
        this.monitoringSystemClient = monitoringSystemClient;
        this.userManager = userManager;
    }
}

