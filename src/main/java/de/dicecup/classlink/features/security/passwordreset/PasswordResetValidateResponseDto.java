package de.dicecup.classlink.features.security.passwordreset;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetValidateResponseDto(
        UUID tokenId,
        UUID userId,
        Instant expiresAt
) {
}
