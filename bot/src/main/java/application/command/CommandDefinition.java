package application.command;

public record CommandDefinition(
    UserCommand command,
    String displayName,
    String description
) {
    public CommandDefinition {
        if (displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }

        if (description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }
    }
}
