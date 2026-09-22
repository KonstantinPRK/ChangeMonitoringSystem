package application.command;

public enum UserCommand {
    START("start"),
    HELP("help"),
    TRACK("track"),
    UNTRACK("untrack"),
    LIST("list");

    private final String code;

    UserCommand(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
