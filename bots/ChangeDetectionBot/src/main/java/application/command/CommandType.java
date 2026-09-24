package application.command;

public enum CommandType {
    START("start", " — начать работу с ботом"),
    HELP("help", " — показать доступные команды"),
    TRACK("track", " — добавить ссылку для отслеживания"),
    UNTRACK("untrack", " — прекратить отслеживание ссылки"),
    LIST("list", " — показать отслеживаемые ссылки"),
    STOP("stop", " — остановить все отслеживания"),
    DELETE("delete", " — удалить аккаунт");

    private final String code;
    private final String description;

    CommandType(String code, String description) {
        this.code = code;
        this.description = description;
    }


    public static CommandType fromText(String text) {
        if (text == null || !text.startsWith("/")) return null;

        String commandCode = text
            .substring(1)
            .trim()
            .split("\\s+", 2)[0];

        int botNameSeparator = commandCode.indexOf('@');

        if (botNameSeparator >= 0) {
            commandCode = commandCode.substring(
                0,
                botNameSeparator
            );
        }

        for (CommandType commandType : values()) {
            if (commandType.code.equalsIgnoreCase(commandCode)) {
                return commandType;
            }
        }

        return null;
    }


    public String code() {
        return code;
    }


    public String description() {
        return description;
    }
}
