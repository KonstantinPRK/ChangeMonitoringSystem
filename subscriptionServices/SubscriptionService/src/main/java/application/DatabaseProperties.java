package application;

import java.util.Objects;

public record DatabaseProperties(
    String jdbcUrl,
    String username,
    String password,
    int maximumPoolSize
) {
    public DatabaseProperties {
        Objects.requireNonNull(jdbcUrl);
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        if (maximumPoolSize < 1) {
            throw new IllegalArgumentException("maximumPoolSize must be positive");
        }
    }
}

