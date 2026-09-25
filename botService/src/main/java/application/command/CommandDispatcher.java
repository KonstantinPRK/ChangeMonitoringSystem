package application.command;

import application.interaction.ConversationSession;
import application.interaction.InteractionResult;
import application.user.BotUser;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandDispatcher {
    private final CommandParser commandParser;
    private final Map<CommandType, CommandHandler> handlers = new EnumMap<>(CommandType.class);


    public CommandDispatcher(CommandParser commandParser, List<CommandHandler> commandHandlers) {
        this.commandParser = commandParser;
        commandHandlers.forEach(handler -> handlers.put(handler.command(), handler));
    }


    public InteractionResult dispatch(BotUser user, ConversationSession session, String text) {
        CommandType command = commandParser.parse(text);
        return handlers.get(command).handle(user, session);
    }
}
