package application.command;

public class CommandResult {
    private final boolean replyRequired;
    private final String text;


    public CommandResult(
        boolean replyRequired,
        String text
    ) {
        this.replyRequired = replyRequired;
        this.text = text;
    }


    public static CommandResult reply(String text) {
        return new CommandResult(
            true,
            text
        );
    }


    public static CommandResult noReply() {
        return new CommandResult(
            false,
            ""
        );
    }


    public boolean replyRequired() {
        return replyRequired;
    }


    public String text() {
        return text;
    }
}
