package application.config;

import java.util.Objects;

public final class DatabaseProperties {
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int maximumPoolSize;

    public DatabaseProperties(
        String jdbcUrl,
        String username,
        String password,
        int maximumPoolSize
    ) {
        this.jdbcUrl = requireNonBlank(jdbcUrl, "jdbcUrl");
        this.username = requireNonBlank(username, "username");
        this.password = requireNonBlank(password, "password");

        if (maximumPoolSize < 1) {
            throw new IllegalArgumentException("maximumPoolSize must be positive");
        }
        this.maximumPoolSize = maximumPoolSize;
    }

    public String jdbcUrl() {
        return jdbcUrl;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public int maximumPoolSize() {
        return maximumPoolSize;
    }

    private static String requireNonBlank(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
