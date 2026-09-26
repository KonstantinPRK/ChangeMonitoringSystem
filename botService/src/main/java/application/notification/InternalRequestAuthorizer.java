package application.notification;

import application.config.BotProperties;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Проверяет право на выполнение внутреннего запроса через {@code InternalRequestAuthorizer}.
 */
@Component
public class InternalRequestAuthorizer {
    private final BotProperties botProperties;


    public InternalRequestAuthorizer(BotProperties botProperties) {
        this.botProperties = botProperties;
    }


    public boolean authorized(String authorizationHeader) {
        String configuredToken = botProperties.internalApiToken();
        if (configuredToken == null || configuredToken.isBlank()) return true;
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) return false;

        byte[] expected = configuredToken.getBytes(StandardCharsets.UTF_8);
        byte[] actual = authorizationHeader.substring(7).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
