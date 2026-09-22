package application.messenger;

public record SubscriberId(
    CommunicationChannel communicationChannel,
    String platformId
) {
}

