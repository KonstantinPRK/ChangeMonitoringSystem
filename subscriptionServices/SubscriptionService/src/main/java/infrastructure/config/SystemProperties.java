package infrastructure.config;

import java.time.Duration;
import java.util.Map;

public record SystemProperties(
    String host,
    int port,
    int backlog,
    int requestThreads,
    int shutdownDelaySeconds,
    Duration remoteAvailabilityTimeout,
    Duration availabilityScanInterval,
    Duration outboundConnectTimeout,
    Duration outboundRequestTimeout
) {
    public static SystemProperties fromEnvironment(Map<String, String> environment) {
        return new SystemProperties(
            environment.getOrDefault("SYSTEM_HTTP_HOST", "0.0.0.0"),
            integer(environment, "SYSTEM_HTTP_PORT", 8080),
            integer(environment, "SYSTEM_HTTP_BACKLOG", 128),
            integer(environment, "SYSTEM_HTTP_THREADS", 8),
            integer(environment, "SYSTEM_SHUTDOWN_DELAY_SECONDS", 5),
            Duration.ofSeconds(integer(
                environment,
                "REMOTE_AVAILABILITY_TIMEOUT_SECONDS",
                30
            )),
            Duration.ofSeconds(integer(
                environment,
                "REMOTE_AVAILABILITY_SCAN_INTERVAL_SECONDS",
                10
            )),
            Duration.ofSeconds(integer(
                environment,
                "OUTBOUND_CONNECT_TIMEOUT_SECONDS",
                5
            )),
            Duration.ofSeconds(integer(
                environment,
                "OUTBOUND_REQUEST_TIMEOUT_SECONDS",
                10
            ))
        );
    }


    private static int integer(
        Map<String, String> environment,
        String name,
        int defaultValue
    ) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) return defaultValue;

        try {
            return Integer.parseInt(value);

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " must be an integer", exception);

        }
    }
}
