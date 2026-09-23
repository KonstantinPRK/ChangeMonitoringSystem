package infrastructure.http;

import application.bot.BotInstance;
import application.core.BotManager;
import application.core.TrackerManager;
import application.registration.RemoteSystemStatus;
import application.tracker.TrackerInstance;
import infrastructure.http.dto.BotRegistrationRequest;
import infrastructure.http.dto.HealthResponse;
import infrastructure.http.dto.ProblemResponse;
import infrastructure.http.dto.TrackerRegistrationRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SystemHttpServer {
    private static final System.Logger LOGGER = System.getLogger(
        SystemHttpServer.class.getName()
    );
    private static final int MAX_REQUEST_BODY_BYTES = 1_048_576;

    private final String host;
    private final int port;
    private final int backlog;
    private final int requestThreads;
    private final int shutdownDelaySeconds;
    private final BotManager botManager;
    private final TrackerManager trackerManager;
    private final ObjectMapper objectMapper;

    private HttpServer server;
    private ExecutorService executor;


    public SystemHttpServer(
        String host,
        int port,
        int backlog,
        int requestThreads,
        int shutdownDelaySeconds,
        BotManager botManager,
        TrackerManager trackerManager,
        ObjectMapper objectMapper
    ) {
        this.host = requireText(host, "host");
        this.port = requirePort(port);
        this.backlog = requireNonNegative(backlog, "backlog");
        this.requestThreads = requirePositive(requestThreads, "requestThreads");
        this.shutdownDelaySeconds = requireNonNegative(
            shutdownDelaySeconds,
            "shutdownDelaySeconds"
        );
        this.botManager = botManager;
        this.trackerManager = trackerManager;
        this.objectMapper = objectMapper;
    }


    public synchronized void start() {
        if (server != null) return;

        ExecutorService newExecutor = Executors.newFixedThreadPool(
            requestThreads,
            task -> {
                Thread thread = new Thread(task, "system-http-request");
                thread.setDaemon(false);
                return thread;
            }
        );

        try {
            InetSocketAddress address = new InetSocketAddress(host, port);
            HttpServer newServer = HttpServer.create(address, backlog);
            newServer.createContext("/", this::handle);
            newServer.setExecutor(newExecutor);
            newServer.start();

            executor = newExecutor;
            server = newServer;

        } catch (IOException | RuntimeException exception) {
            newExecutor.shutdownNow();
            throw new IllegalStateException(
                "Cannot start HTTP server on " + host + ':' + port,
                exception
            );

        }
    }


    public void stop() {
        HttpServer currentServer;
        ExecutorService currentExecutor;

        synchronized (this) {
            currentServer = server;
            currentExecutor = executor;
            server = null;
            executor = null;
        }

        if (currentServer != null) currentServer.stop(shutdownDelaySeconds);

        if (currentExecutor != null) {
            currentExecutor.shutdown();
            try {
                boolean terminated = currentExecutor.awaitTermination(5, TimeUnit.SECONDS);
                if (!terminated) currentExecutor.shutdownNow();

            } catch (InterruptedException exception) {
                currentExecutor.shutdownNow();
                Thread.currentThread().interrupt();

            }
        }
    }


    public synchronized boolean isRunning() {
        return server != null;
    }


    private void handle(HttpExchange exchange) throws IOException {
        try {
            dispatch(exchange);

        } catch (JsonProcessingException exception) {
            sendProblem(exchange, 400, "Invalid JSON request");

        } catch (IllegalArgumentException exception) {
            sendProblem(exchange, 400, exception.getMessage());

        } catch (IllegalStateException exception) {
            sendProblem(exchange, 409, exception.getMessage());

        } catch (RuntimeException exception) {
            LOGGER.log(
                System.Logger.Level.ERROR,
                "HTTP request processing failed",
                exception
            );
            sendProblem(exchange, 500, "Internal server error");

        } finally {
            exchange.close();

        }
    }


    private void dispatch(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = normalizePath(exchange.getRequestURI().getPath());

        if ("GET".equals(method) && "/health".equals(path)) {
            sendJson(exchange, 200, new HealthResponse("ACTIVE"));
            return;
        }

        if ("POST".equals(method) && "/internal/bots".equals(path)) {
            registerBot(exchange);
            return;
        }

        if (path.startsWith("/internal/bots/")) {
            handleExistingBot(exchange, method, path);
            return;
        }

        if ("POST".equals(method) && "/internal/trackers".equals(path)) {
            registerTracker(exchange);
            return;
        }

        if (path.startsWith("/internal/trackers/")) {
            handleExistingTracker(exchange, method, path);
            return;
        }

        sendProblem(exchange, 404, "Endpoint not found");
    }


    private void registerBot(HttpExchange exchange) throws IOException {
        BotRegistrationRequest request = readJson(exchange, BotRegistrationRequest.class);
        BotInstance bot = new BotInstance(
            request.botId(),
            request.botName(),
            request.communicationChannel(),
            request.baseUrl(),
            request.contractVersion(),
            nullToEmpty(request.capabilities()),
            RemoteSystemStatus.STARTING,
            Instant.EPOCH
        );
        botManager.register(bot);
        sendNoContent(exchange);
    }


    private void registerTracker(HttpExchange exchange) throws IOException {
        TrackerRegistrationRequest request = readJson(
            exchange,
            TrackerRegistrationRequest.class
        );
        TrackerInstance tracker = new TrackerInstance(
            request.trackerId(),
            request.resourceProvider(),
            request.baseUrl(),
            request.contractVersion(),
            nullToEmpty(request.supportedHosts()),
            nullToEmpty(request.capabilities()),
            RemoteSystemStatus.STARTING,
            Instant.EPOCH
        );
        trackerManager.register(tracker);
        sendNoContent(exchange);
    }


    private void handleExistingBot(HttpExchange exchange, String method, String path)
        throws IOException {
        String suffix = path.substring("/internal/bots/".length());
        if (suffix.endsWith("/availability")) {
            if (!"PUT".equals(method)) {
                sendProblem(exchange, 405, "Method not allowed");
                return;
            }

            String botId = singlePathSegment(
                suffix.substring(0, suffix.length() - "/availability".length())
            );
            botManager.confirmAvailability(botId);
            sendNoContent(exchange);
            return;
        }

        if ("DELETE".equals(method)) {
            botManager.unregister(singlePathSegment(suffix));
            sendNoContent(exchange);
            return;
        }

        sendProblem(exchange, 405, "Method not allowed");
    }


    private void handleExistingTracker(HttpExchange exchange, String method, String path)
        throws IOException {
        String suffix = path.substring("/internal/trackers/".length());
        if (suffix.endsWith("/availability")) {
            if (!"PUT".equals(method)) {
                sendProblem(exchange, 405, "Method not allowed");
                return;
            }

            String trackerId = singlePathSegment(
                suffix.substring(0, suffix.length() - "/availability".length())
            );
            trackerManager.confirmAvailability(trackerId);
            sendNoContent(exchange);
            return;
        }

        if ("DELETE".equals(method)) {
            trackerManager.unregister(singlePathSegment(suffix));
            sendNoContent(exchange);
            return;
        }

        sendProblem(exchange, 405, "Method not allowed");
    }


    private <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        byte[] body;
        try (InputStream input = exchange.getRequestBody()) {
            body = input.readNBytes(MAX_REQUEST_BODY_BYTES + 1);

        }

        if (body.length > MAX_REQUEST_BODY_BYTES) {
            throw new IllegalArgumentException("Request body is too large");
        }

        return objectMapper.readValue(body, type);
    }


    private void sendNoContent(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(204, -1);
    }


    private void sendProblem(HttpExchange exchange, int statusCode, String message)
        throws IOException {
        sendJson(exchange, statusCode, new ProblemResponse(message));
    }


    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] response = objectMapper.writeValueAsBytes(body);
        exchange.getResponseHeaders().set(
            "Content-Type",
            "application/json; charset=utf-8"
        );
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
    }


    private static String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";

        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }


    private static String singlePathSegment(String value) {
        String result = requireText(value, "resource id");
        if (result.contains("/")) {
            throw new IllegalArgumentException(
                "Resource id must contain one path segment"
            );
        }

        return result;
    }


    private static Set<String> nullToEmpty(Set<String> values) {
        return values == null ? Set.of() : values;
    }


    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value.strip();
    }


    private static int requirePort(int value) {
        if (value < 1 || value > 65_535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }

        return value;
    }


    private static int requirePositive(int value, String fieldName) {
        if (value < 1) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }

        return value;
    }


    private static int requireNonNegative(int value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }

        return value;
    }
}
