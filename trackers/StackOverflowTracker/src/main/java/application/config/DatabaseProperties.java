package application.config;

import java.util.Objects;

public record DatabaseProperties(
    String jdbcUrl,
    String username,
    String password,
    int maximumPoolSize
) {
    public DatabaseProperties {
        requireNonBlank(jdbcUrl, "jdbcUrl");
        requireNonBlank(username, "username");
        requireNonBlank(password, "password");
        if (maximumPoolSize < 1) {
            throw new IllegalArgumentException("maximumPoolSize must be positive");
        }
    }

    private static void requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}

