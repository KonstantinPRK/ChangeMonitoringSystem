package application.commandHandler;

import application.ConversationState;
import application.User;

public class UntrackCommandHandler implements Command {
    private Step step = Step.NEW;


    @Override
    public String start(User user) {
        step = Step.WAITING_FOR_LINK;

        return "Отправьте ссылку, которую нужно перестать отслеживать.";
    }


    @Override
    public String process(User user, String message) {
        if (!message.startsWith("https://")) {
            return "Ссылка не подходит. Попробуйте ещё раз.";
        }

        step = Step.COMPLETED;

        return "Отслеживание ссылки остановлено.";
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


    private enum Step {
        NEW,
        WAITING_FOR_LINK,
        COMPLETED
    }
}
