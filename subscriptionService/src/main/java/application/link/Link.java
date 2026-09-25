package application.link;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Link(
        @NotBlank @Size(max = 253) String domain,
        @NotBlank @Size(max = 2048) String address
) {
}
