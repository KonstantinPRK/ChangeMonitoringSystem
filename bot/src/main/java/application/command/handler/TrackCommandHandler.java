package application.command.handler;

import application.command.CommandContext;
import application.command.CommandHandler;
import application.command.UserCommand;
import application.ConversationState;
import application.service.ConversationStateService;

public final class TrackCommandHandler implements CommandHandler {
    private final ConversationStateService stateService;

    public TrackCommandHandler(ConversationStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public UserCommand command() {
        return UserCommand.TRACK;
    }

    @Override
    public String handle(CommandContext context) {
        stateService.setState(context.subscriberId(), ConversationState.WAITING_FOR_TRACK_LINK);
        return "Пришлите ссылку, которую нужно отслеживать.";
    }
}
