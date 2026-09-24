package application.commandHandler;

import application.ConversationState;
import application.SubscriptionManager;
import application.User;

public class TrackCommandHandler implements Command {
    SubscriptionManager subscriptionManager;

    private Step step = Step.NEW;


    @Override
    public String start(User user) {
        step = Step.WAITING_FOR_LINK;

        return "Отправьте ссылку, которую нужно отслеживать.";
    }


    @Override
    public String process(User user, String message) {
        return switch (step) {
            case WAITING_FOR_LINK -> processLink(message);
            case WAITING_FOR_TAGS -> processTags(message);
            case NEW, COMPLETED ->
                "Команда не ожидает дополнительных данных.";
        };
    }


    @Override
    public ConversationState getConversationState() {
        if (isCompleted()) {
            return ConversationState.WAITING_FOR_USER_COMMAND;
        }

        return ConversationState.WAITING_FOR_USER_TEXT;
    }


    @Override
    public boolean isCompleted() {
        return step == Step.COMPLETED;
    }


    private String processLink(String message) {
        if (!message.startsWith("https://")) {
            return "Ссылка не подходит. Попробуйте ещё раз.";
        }

        step = Step.WAITING_FOR_TAGS;

        return "Отправьте метки для этой ссылки.";
    }


    private String processTags(String message) {
        if (message.isBlank()) {
            return "Метки не должны быть пустыми. Попробуйте ещё раз.";
        }

        step = Step.COMPLETED;

        return "Ссылка добавлена для отслеживания.";
    }


    private enum Step {
        NEW,
        WAITING_FOR_LINK,
        WAITING_FOR_TAGS,
        COMPLETED
    }
}
