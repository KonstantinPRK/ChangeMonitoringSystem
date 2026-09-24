package application.commandHandler;

import application.ConversationState;
import application.User;

public class DeleteCommandHandler implements Command {
    private Step step = Step.NEW;


    @Override
    public String start(User user) {
        step = Step.WAITING_FOR_CONFIRMATION;

        return "Подтвердите удаление аккаунта: да или нет.";
    }


    @Override
    public String process(User user, String message) {
        if (message.equalsIgnoreCase("да")) {
            step = Step.COMPLETED;

            return "Аккаунт пользователя и связанные с ним данные будут удалены.";
        }

        if (message.equalsIgnoreCase("нет")) {
            step = Step.COMPLETED;

            return "Удаление аккаунта отменено.";
        }

        return "Ответьте «да» или «нет».";
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
        WAITING_FOR_CONFIRMATION,
        COMPLETED
    }
}
