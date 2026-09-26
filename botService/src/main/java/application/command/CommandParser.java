package application.command;

import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Читает и преобразует входные данные для {@code CommandParser}.
 */
@Component
public class CommandParser {
    public CommandType parse(String text) {
        String command = text.trim().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        int botNameSeparator = command.indexOf('@');
        if (botNameSeparator >= 0) command = command.substring(0, botNameSeparator);

        return switch (command) {
            case "/start" -> CommandType.START;
            case "/help" -> CommandType.HELP;
            case "/track" -> CommandType.TRACK;
            case "/untrack" -> CommandType.UNTRACK;
            case "/list" -> CommandType.LIST;
            case "/stop" -> CommandType.STOP;
            case "/delete" -> CommandType.DELETE;
            default -> CommandType.UNKNOWN;
        };
    }
}
