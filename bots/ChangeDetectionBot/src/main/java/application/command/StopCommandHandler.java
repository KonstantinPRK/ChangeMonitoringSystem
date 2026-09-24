package application.command;

import application.interaction.ConversationState;
import application.subscription.SubscriptionManager;
import application.user.User;

public class StopCommandHandler implements Command {
    SubscriptionManager subscriptionManager;


    @Override
    public CommandResult start(User user) {
        subscriptionManager.stop(user.key());

        return CommandResult.reply(
            "Все отслеживания пользователя будут остановлены."
        );
    }


    @Override
    public CommandResult process(User user, String message) {
        return start(user);
    }


    @Override
    public ConversationState getConversationState() {
        return ConversationState.WAITING_FOR_USER_COMMAND;
    }


    @Override
    public boolean isCompleted() {
        return true;
    }
}
