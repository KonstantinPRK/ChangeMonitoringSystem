package application.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record User(
        @NotBlank @Size(max = 128) String botId,
        @NotBlank @Size(max = 128) String userId,
        @NotBlank @Size(max = 128) String chatId
) {
}
