package application.registration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.net.URI;
import java.util.Set;

public record TrackerRegistrationRequest(
        @NotBlank @Size(max = 128) String trackerId,
        @NotNull URI baseUrl,
        @NotNull @Size(min = 1, max = 100) Set<@NotBlank String> supportedHosts
) {
}
