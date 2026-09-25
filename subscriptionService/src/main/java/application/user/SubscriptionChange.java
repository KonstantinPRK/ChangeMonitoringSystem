package application.user;

public record SubscriptionChange(boolean changed, boolean trackerActionRequired, long revision) {
}
