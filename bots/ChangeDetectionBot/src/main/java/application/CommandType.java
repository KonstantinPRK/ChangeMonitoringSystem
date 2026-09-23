package application;

public enum CommandType {
    START("start"),
    HELP("help"),
    TRACK("track"),
    UNTRACK("untrack"),
    LIST("list"),
    STOP("stop"), //остановит все отслеживания с конкретным пользователем
    DELETE("delete"); //удалит аккаунт из БД

    private final String code;

    CommandType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}

