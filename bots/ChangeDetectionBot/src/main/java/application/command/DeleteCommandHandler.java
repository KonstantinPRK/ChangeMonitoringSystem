package application.command;

import application.interaction.ConversationState;
import application.subscription.SubscriptionManager;
import application.user.User;
import application.user.UserManager;

public class DeleteCommandHandler implements Command {
    SubscriptionManager subscriptionManager;
    UserManager userManager;

    private Step step = Step.NEW;


    @Override
    public CommandResult start(User user) {
        step = Step.WAITING_FOR_CONFIRMATION;

        return CommandResult.reply(
            "Подтвердите удаление аккаунта: да или нет."
        );
    }


    @Override
    public CommandResult process(User user, String message) {
        if (message.equalsIgnoreCase("да")) {
            subscriptionManager.delete(user.key());
            userManager.deleteUser(user.key());

            step = Step.COMPLETED;

            return CommandResult.reply(
                "Аккаунт пользователя и связанные с ним данные будут удалены."
            );
        }

        if (message.equalsIgnoreCase("нет")) {
            step = Step.COMPLETED;

            return CommandResult.reply(
                "Удаление аккаунта отменено."
            );
        }

        return CommandResult.reply(
            "Ответьте «да» или «нет»."
        );
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
