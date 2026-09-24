package application.command;

import application.interaction.ConversationState;
import application.subscription.SubscriptionManager;
import application.user.User;

public class ListCommandHandler implements Command {
    SubscriptionManager subscriptionManager;


    @Override
    public CommandResult start(User user) {
        subscriptionManager.list(user.key());

        return CommandResult.reply(
            "Запрашиваю список отслеживаемых ссылок."
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
