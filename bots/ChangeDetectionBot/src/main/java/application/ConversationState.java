package application;

public enum ConversationState {
    NEW,
    STARTED,
    WAITING_COMMAND,
    WAITING_HELP,
    WAITING_FOR_TAGS,
    WAITING_FOR_FILTERS,
    WAITING_FOR_UNTRACK_LINK,
    STOPPED;
}

