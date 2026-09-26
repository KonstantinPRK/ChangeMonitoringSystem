package application.snapshot;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

/**
 * Создаёт доменные или протокольные объекты через {@code ResourceSnapshotFactory}.
 */
@Component
public class ResourceSnapshotFactory {
    private final ObjectMapper objectMapper;


    public ResourceSnapshotFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public ResourceSnapshot create(
            String providerKey,
            String resourceKind,
            Instant updatedAt,
            String state,
            Map<String, Object> summary
    ) {
        String summaryJson = objectMapper.writeValueAsString(summary);
        return new ResourceSnapshot(
                providerKey,
                resourceKind,
                updatedAt,
                state,
                hash(summaryJson),
                summaryJson
        );
    }


    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);

        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);

        }
    }
}
