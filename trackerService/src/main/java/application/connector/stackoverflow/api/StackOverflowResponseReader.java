package application.connector.stackoverflow.api;

import application.connector.stackoverflow.quota.StackOverflowQuota;
import application.connector.stackoverflow.quota.StackOverflowQuotaCache;
import application.snapshot.ResourceObservation;
import application.snapshot.ResourceSnapshot;
import application.snapshot.ResourceSnapshotFactory;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Component
public class StackOverflowResponseReader {
    private final ObjectMapper objectMapper;
    private final ResourceSnapshotFactory snapshotFactory;
    private final StackOverflowQuotaCache quotaCache;
    private final Clock clock;


    public StackOverflowResponseReader(
            ObjectMapper objectMapper,
            ResourceSnapshotFactory snapshotFactory,
            StackOverflowQuotaCache quotaCache,
            Clock clock
    ) {
        this.objectMapper = objectMapper;
        this.snapshotFactory = snapshotFactory;
        this.quotaCache = quotaCache;
        this.clock = clock;
    }


    public ResourceObservation read(StackOverflowHttpResponse response) {
        String body = body(response);
        JsonNode root = objectMapper.readTree(body);
        if (response.statusCode() < 200 || response.statusCode() >= 300) throw failure(response, root);
        updateQuota(root);

        JsonNode question = root.path("items").path(0);
        if (question.isMissingNode()) {
            throw new StackOverflowApiException(404, "StackOverflow question was not found", null);
        }
        long lastActivity = question.path("last_activity_date").asLong();
        ResourceSnapshot snapshot = snapshot(question, lastActivity);
        return ResourceObservation.changed(String.valueOf(lastActivity), snapshot);
    }


    private ResourceSnapshot snapshot(JsonNode question, long lastActivity) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("questionId", question.path("question_id").asLong());
        summary.put("title", question.path("title").asString());
        summary.put("score", question.path("score").asInt());
        summary.put("answerCount", question.path("answer_count").asInt());
        summary.put("isAnswered", question.path("is_answered").asBoolean());
        summary.put("lastActivityDate", lastActivity);
        String state = question.path("closed_date").isMissingNode()
                ? (question.path("is_answered").asBoolean() ? "ANSWERED" : "OPEN")
                : "CLOSED";
        return snapshotFactory.create(
                "stackoverflow",
                "question",
                Instant.ofEpochSecond(lastActivity),
                state,
                summary
        );
    }


    private void updateQuota(JsonNode root) {
        long remaining = root.path("quota_remaining").asLong(Long.MAX_VALUE);
        long backoffSeconds = root.path("backoff").asLong(0L);
        Instant blockedUntil = clock.instant().plusSeconds(backoffSeconds);
        if (remaining <= 0) blockedUntil = nextUtcDay();
        quotaCache.update(new StackOverflowQuota(remaining, blockedUntil));
    }


    private StackOverflowApiException failure(
            StackOverflowHttpResponse response,
            JsonNode root
    ) {
        long backoffSeconds = root.path("backoff").asLong(0L);
        Instant retryAt = backoffSeconds > 0
                ? clock.instant().plusSeconds(backoffSeconds)
                : null;
        String message = root.path("error_message").asString("StackOverflow API request failed");
        return new StackOverflowApiException(response.statusCode(), message, retryAt);
    }


    private String body(StackOverflowHttpResponse response) {
        if (!"gzip".equalsIgnoreCase(response.contentEncoding())) {
            return new String(response.body(), StandardCharsets.UTF_8);
        }

        try (GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(response.body()))) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException exception) {
            throw new StackOverflowApiException(502, "Invalid compressed StackOverflow response", null);

        }
    }


    private Instant nextUtcDay() {
        ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        return now.toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
