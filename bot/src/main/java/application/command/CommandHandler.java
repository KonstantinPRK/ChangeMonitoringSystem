package application.command;

public interface CommandHandler {

    UserCommand command();

    String handle(CommandContext context);
}
