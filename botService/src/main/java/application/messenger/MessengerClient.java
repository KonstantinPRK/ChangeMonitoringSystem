package application.messenger;

public interface MessengerClient {
    String botId();

    MessengerType messengerType();

    MessageUpdateSource updateSource();

    MessageSender messageSender();
}
