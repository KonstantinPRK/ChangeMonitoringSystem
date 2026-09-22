package application.domain;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

public record TrackedLink(UUID id, URI uri, Instant checkedAt) {
}

