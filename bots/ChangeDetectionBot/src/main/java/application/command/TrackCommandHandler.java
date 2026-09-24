package application.command;

import application.interaction.ConversationState;
import application.subscription.Link;
import application.subscription.LinkParser;
import application.subscription.Subscription;
import application.subscription.SubscriptionManager;
import application.user.User;

public class TrackCommandHandler implements Command {
    LinkParser linkParser;
    SubscriptionManager subscriptionManager;

    private Step step = Step.NEW;


    @Override
    public CommandResult start(User user) {
        String[] availableSubscriptions =
            subscriptionManager.getAvailableSubscriptions();

        StringBuilder response = new StringBuilder(
            "Доступные сайты для отслеживания:\n"
        );

        for (String availableSubscription : availableSubscriptions) {
            response
                .append("- ")
                .append(availableSubscription)
                .append('\n');
        }

        response.append(
            "\nОтправьте ссылку, которую нужно отслеживать."
        );

        step = Step.WAITING_FOR_LINK;

        return CommandResult.reply(
            response.toString()
        );
    }


    @Override
    public CommandResult process(User user, String message) {
        if (step != Step.WAITING_FOR_LINK) {
            return CommandResult.noReply();
        }

        return processLink(
            user,
            message
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


    private CommandResult processLink(
        User user,
        String message
    ) {
        Link link;

        try {
            link = linkParser.parse(message);

        } catch (IllegalArgumentException exception) {
            return CommandResult.reply(
                "Ссылка не подходит. Попробуйте ещё раз."
            );

        }

        Subscription subscription = new Subscription(
            user.key(),
            link
        );

        subscriptionManager.track(subscription);
        step = Step.COMPLETED;

        return CommandResult.noReply();
    }


    private enum Step {
        NEW,
        WAITING_FOR_LINK,
        COMPLETED
    }
}
