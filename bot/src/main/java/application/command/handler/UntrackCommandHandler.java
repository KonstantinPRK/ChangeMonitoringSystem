package application.command.handler;

import application.command.CommandContext;
import application.command.CommandHandler;
import application.command.UserCommand;
import application.ConversationState;
import application.service.ConversationStateService;

public final class UntrackCommandHandler implements CommandHandler {
    private final ConversationStateService stateService;

    public UntrackCommandHandler(ConversationStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public UserCommand command() {
        return UserCommand.UNTRACK;
    }

    @Override
    public String handle(CommandContext context) {
        stateService.setState(context.subscriberId(), ConversationState.WAITING_FOR_UNTRACK_LINK);
        return "Пришлите ссылку, которую нужно перестать отслеживать.";
    }
}
