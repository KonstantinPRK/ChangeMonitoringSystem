package application;

public enum ConversationState {
    IDLE,
    WAITING_FOR_TRACK_LINK,
    WAITING_FOR_TAGS,
    WAITING_FOR_FILTERS,
    WAITING_FOR_UNTRACK_LINK
}
