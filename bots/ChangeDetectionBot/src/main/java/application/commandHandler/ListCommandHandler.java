package application.commandHandler;

import application.ConversationState;
import application.SubscriptionManager;
import application.User;

public class ListCommandHandler implements Command {
    SubscriptionManager subscriptionManager;


    @Override
    public String start(User user) {
        return "Запрашиваю список отслеживаемых ссылок.";
    }


    @Override
    public String process(User user, String message) {
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
