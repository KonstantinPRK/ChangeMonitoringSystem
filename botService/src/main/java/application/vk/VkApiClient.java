package application.vk;

import application.config.VkProperties;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletionStage;

@Component
public class VkApiClient {
    private static final URI API_BASE_URL = URI.create("https://api.vk.com/method/");
    private final HttpClient httpClient;
    private final VkProperties vkProperties;
    private final ObjectMapper objectMapper;


    public VkApiClient(
            HttpClient httpClient,
            VkProperties vkProperties,
            ObjectMapper objectMapper
    ) {
        this.httpClient = httpClient;
        this.vkProperties = vkProperties;
        this.objectMapper = objectMapper;
    }


    public CompletionStage<VkLongPollSession> getLongPollServer() {
        Map<String, String> parameters = Map.of("group_id", Long.toString(vkProperties.groupId()));
        return call("groups.getLongPollServer", parameters)
                .thenApply(this::readLongPollSession);
    }


    public CompletionStage<JsonNode> getUpdates(VkLongPollSession session, long timestamp) {
        URI address = longPollAddress(session, timestamp);
        HttpRequest request = HttpRequest.newBuilder(address)
                .timeout(vkProperties.longPollWait().plus(Duration.ofSeconds(10)))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::readBody);
    }


    public CompletionStage<JsonNode> sendMessage(String peerId, String text, int randomId) {
        Map<String, String> parameters = Map.of(
                "peer_id", peerId,
                "message", text,
                "random_id", Integer.toString(randomId)
        );
        return call("messages.send", parameters);
    }


    private CompletionStage<JsonNode> call(String method, Map<String, String> parameters) {
        HttpRequest request = createApiRequest(method, parameters);
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::readApiResponse);
    }


    private HttpRequest createApiRequest(String method, Map<String, String> parameters) {
        Map<String, String> requestParameters = new LinkedHashMap<>(parameters);
        requestParameters.put("access_token", vkProperties.token());
        requestParameters.put("v", vkProperties.apiVersion());
        return HttpRequest.newBuilder(API_BASE_URL.resolve(method))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(formBody(requestParameters)))
                .build();
    }


    private URI longPollAddress(VkLongPollSession session, long timestamp) {
        String separator = session.server().toString().contains("?") ? "&" : "?";
        String query = "act=a_check"
                + "&key=" + encode(session.key())
                + "&ts=" + timestamp
                + "&wait=" + vkProperties.longPollWait().toSeconds();
        return URI.create(session.server() + separator + query);
    }


    private String formBody(Map<String, String> parameters) {
        StringJoiner body = new StringJoiner("&");
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            body.add(encode(parameter.getKey()) + '=' + encode(parameter.getValue()));
        }
        return body.toString();
    }


    private JsonNode readApiResponse(HttpResponse<String> response) {
        JsonNode body = readBody(response);
        JsonNode error = body.path("error");
        if (!error.isMissingNode()) {
            int errorCode = error.path("error_code").asInt(response.statusCode());
            String message = error.path("error_msg").asString("VK API request failed");
            throw new VkApiException(errorCode, message);
        }
        return body.path("response");
    }


    private JsonNode readBody(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new VkApiException(response.statusCode(), "VK HTTP request failed");
        }
        return objectMapper.readTree(response.body());
    }


    private VkLongPollSession readLongPollSession(JsonNode response) {
        String server = response.path("server").asString();
        String key = response.path("key").asString();
        long timestamp = response.path("ts").asLong();
        if (server.isBlank() || key.isBlank() || timestamp == 0L) {
            throw new VkApiException(0, "VK returned incomplete Long Poll connection data");
        }
        return new VkLongPollSession(URI.create(server), key, timestamp);
    }


    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
