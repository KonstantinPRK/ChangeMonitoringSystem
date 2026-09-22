package application;

public enum CommandType {
    START("start"),
    HELP("help"),
    TRACK("track"),
    UNTRACK("untrack"),
    LIST("list");

    private final String code;

    CommandType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}

