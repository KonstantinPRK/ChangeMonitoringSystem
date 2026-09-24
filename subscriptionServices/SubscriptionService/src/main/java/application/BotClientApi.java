package application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BotClientApi {
    private static final String INTERACTIONS_PATH = "/api/v1/interactions";
    private static final String AVAILABLE_SUBSCRIPTIONS_PATH = INTERACTIONS_PATH + "/available";
    private static final String NOTIFICATIONS_PATH = INTERACTIONS_PATH + "/notifications/";

    private static final int MAX_REQUEST_BODY_BYTES = 1_048_576;
    private static final int COMMAND_QUEUE_CAPACITY = 10_000;
    private static final int NOTIFICATION_QUEUE_CAPACITY = 10_000;

    private final ObjectMapper objectMapper;
    private final BlockingQueue<BotInstruction> instructions = new ArrayBlockingQueue<>(COMMAND_QUEUE_CAPACITY);
    private final ConcurrentMap<String, BlockingQueue<Notification>> notificationsByBot = new ConcurrentHashMap<>();

    private volatile String[] availableSubscriptions = new String[0];


    public BotClientApi(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public void handle(HttpExchange exchange) throws IOException {
        try {
            dispatch(exchange);

        } catch (JsonProcessingException exception) {
            sendProblem(exchange, 400, "Invalid JSON request");

        } catch (IllegalArgumentException exception) {
            sendProblem(exchange, 400, exception.getMessage());

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            sendProblem(exchange, 503, "Request processing was interrupted");

        } catch (RuntimeException exception) {
            sendProblem(exchange, 500, "Internal server error");

        } finally {
            exchange.close();

        }
    }


    public BotInstruction takeInstruction()
        throws InterruptedException {

        return instructions.take();
    }


    public void publishNotification(Notification notification)
        throws InterruptedException {

        if (notification == null) {
            throw new IllegalArgumentException(
                "Notification must not be empty"
            );
        }

        validateUserKey(notification.userKey());
        requireText(notification.message(), "message");

        BlockingQueue<Notification> notifications =
            notificationsByBot.computeIfAbsent(
                notification.userKey().botId(),
                botId -> new ArrayBlockingQueue<>(
                    NOTIFICATION_QUEUE_CAPACITY
                )
            );

        notifications.put(notification);
    }


    public void updateAvailableSubscriptions(
        String[] availableSubscriptions
    ) {
        if (availableSubscriptions == null) {
            throw new IllegalArgumentException(
                "Available subscriptions must not be empty"
            );
        }

        this.availableSubscriptions = Arrays.copyOf(
            availableSubscriptions,
            availableSubscriptions.length
        );
    }


    private void dispatch(HttpExchange exchange)
        throws IOException, InterruptedException {

        String method = exchange.getRequestMethod();
        String path = normalizePath(
            exchange.getRequestURI().getPath()
        );

        if ("POST".equals(method) && INTERACTIONS_PATH.equals(path)) {
            acceptInstruction(exchange);
            return;
        }

        if (
            "POST".equals(method)
                && (INTERACTIONS_PATH + "/batch").equals(path)
        ) {
            acceptInstructionBatch(exchange);
            return;
        }

        if (
            "GET".equals(method)
                && AVAILABLE_SUBSCRIPTIONS_PATH.equals(path)
        ) {
            sendAvailableSubscriptions(exchange);
            return;
        }

        if (
            "GET".equals(method)
                && path.startsWith(NOTIFICATIONS_PATH)
        ) {
            sendNotification(exchange, path);
            return;
        }

        sendProblem(exchange, 404, "Endpoint not found");
    }


    private void acceptInstruction(HttpExchange exchange)
        throws IOException, InterruptedException {

        BotInstruction instruction = readJson(
            exchange,
            BotInstruction.class
        );

        validateInstruction(instruction);
        instructions.put(instruction);
        sendAccepted(exchange);
    }


    private void acceptInstructionBatch(HttpExchange exchange)
        throws IOException, InterruptedException {

        BotInstruction[] instructionBatch = readJson(
            exchange,
            BotInstruction[].class
        );

        if (instructionBatch == null) {
            throw new IllegalArgumentException(
                "Instruction batch must not be empty"
            );
        }

        for (BotInstruction instruction : instructionBatch) {
            validateInstruction(instruction);
        }

        for (BotInstruction instruction : instructionBatch) {
            instructions.put(instruction);
        }

        sendAccepted(exchange);
    }


    private void sendAvailableSubscriptions(HttpExchange exchange)
        throws IOException {

        String[] response = Arrays.copyOf(
            availableSubscriptions,
            availableSubscriptions.length
        );

        sendJson(exchange, 200, response);
    }


    private void sendNotification(
        HttpExchange exchange,
        String path
    ) throws IOException {
        String botId = singlePathSegment(
            path.substring(NOTIFICATIONS_PATH.length())
        );

        BlockingQueue<Notification> notifications =
            notificationsByBot.get(botId);

        if (notifications == null) {
            sendNoContent(exchange);
            return;
        }

        Notification notification = notifications.poll();

        if (notification == null) {
            sendNoContent(exchange);
            return;
        }

        sendJson(exchange, 200, notification);
    }


    private <T> T readJson(
        HttpExchange exchange,
        Class<T> type
    ) throws IOException {
        byte[] body;

        try (InputStream input = exchange.getRequestBody()) {
            body = input.readNBytes(MAX_REQUEST_BODY_BYTES + 1);

        }

        if (body.length > MAX_REQUEST_BODY_BYTES) {
            throw new IllegalArgumentException(
                "Request body is too large"
            );
        }

        return objectMapper.readValue(body, type);
    }


    private void validateInstruction(BotInstruction instruction) {
        if (instruction == null || instruction.operation() == null) {
            throw new IllegalArgumentException(
                "Instruction operation must not be empty"
            );
        }

        validateUserKey(instruction.userKey());

        if (
            instruction.operation() == Operation.TRACK
                || instruction.operation() == Operation.UNTRACK
        ) {
            validateLink(instruction.link());
        }
    }


    private void validateUserKey(UserKey userKey) {
        if (userKey == null) {
            throw new IllegalArgumentException(
                "User key must not be empty"
            );
        }

        requireText(userKey.botId(), "botId");
        requireText(userKey.userId(), "userId");
    }


    private void validateLink(Link link) {
        if (link == null) {
            throw new IllegalArgumentException(
                "Link must not be empty"
            );
        }

        requireText(link.domain(), "domain");
        requireText(link.resource(), "resource");
    }


    private void sendAccepted(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(202, -1);
    }


    private void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }


    private void sendProblem(
        HttpExchange exchange,
        int statusCode,
        String message
    ) throws IOException {
        sendJson(
            exchange,
            statusCode,
            new ProblemResponse(message)
        );
    }


    private void sendJson(
        HttpExchange exchange,
        int statusCode,
        Object body
    ) throws IOException {
        byte[] response = objectMapper.writeValueAsBytes(body);

        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=utf-8"
        );

        exchange.sendResponseHeaders(
            statusCode,
            response.length
        );

        exchange.getResponseBody().write(response);
    }


    private String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";

        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }


    private String singlePathSegment(String value) {
        String pathSegment = requireText(value, "botId");

        if (pathSegment.contains("/")) {
            throw new IllegalArgumentException(
                "botId must contain one path segment"
            );
        }

        return pathSegment;
    }


    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                fieldName + " must not be blank"
            );
        }

        return value.strip();
    }


    public enum Operation {
        TRACK,
        UNTRACK,
        LIST,
        STOP,
        DELETE
    }


    public record UserKey(
        String botId,
        String userId
    ) {
    }


    public record Link(
        String domain,
        String resource
    ) {
    }


    public record BotInstruction(
        Operation operation,
        UserKey userKey,
        Link link
    ) {
    }


    public record Notification(
        UserKey userKey,
        String message
    ) {
    }


    private record ProblemResponse(String error) {
    }
}
