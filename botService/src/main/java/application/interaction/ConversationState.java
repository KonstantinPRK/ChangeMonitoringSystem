package application.interaction;

public enum ConversationState {
    WAITING_COMMAND,
    WAITING_TRACK_LINK,
    WAITING_TRACK_TAGS,
    WAITING_TRACK_FILTERS,
    WAITING_UNTRACK_LINK,
    WAITING_DELETE_CONFIRMATION
}
