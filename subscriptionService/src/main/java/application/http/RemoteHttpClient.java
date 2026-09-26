package application.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Выполняет HTTP-запросы к удалённому сервису для {@code RemoteHttpClient}.
 */
@Component
public class RemoteHttpClient {
    private final HttpClient client;
    private final ObjectMapper mapper;
    private final Duration requestTimeout;
    private final String internalApiToken;


    public RemoteHttpClient(
            ObjectMapper mapper,
            @Value("${app.http.connect-timeout:5s}") Duration connectTimeout,
            @Value("${app.http.request-timeout:10s}") Duration requestTimeout,
            @Value("${app.internal-api-token:}") String internalApiToken
    ) {
        this.mapper = mapper;
        this.requestTimeout = requestTimeout;
        this.internalApiToken = internalApiToken;
        if (requestTimeout.isNegative() || requestTimeout.isZero()) {
            throw new IllegalArgumentException("Remote HTTP request timeout must be positive");
        }
        this.client = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }


    public void post(URI baseUrl, String path, Object body) {
        HttpRequest.Builder request = HttpRequest.newBuilder(endpoint(baseUrl, path))
                .timeout(requestTimeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(serialize(body)));
        if (!internalApiToken.isBlank()) request.header("Authorization", "Bearer " + internalApiToken);
        try {
            HttpResponse<Void> response = client.send(request.build(), HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Remote service rejected delivery: HTTP " + response.statusCode()
                );
            }

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Remote delivery was interrupted");

        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Remote service is unreachable");

        }
    }


    private URI endpoint(URI baseUrl, String path) {
        String address = baseUrl.toASCIIString();
        String relativePath = path.startsWith("/") ? path.substring(1) : path;
        return URI.create((address.endsWith("/") ? address : address + "/") + relativePath);
    }


    private String serialize(Object body) {
        try {
            return mapper.writeValueAsString(body);

        } catch (JacksonException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot serialize an outgoing message");

        }
    }
}
