package de.dicecup.classlink.features.security.passwordreset;

import java.time.Instant;
import java.util.UUID;

public record PasswordResetCreateResponseDto(
        UUID tokenId,
        String token,
        Instant expiresAt,
        UUID userId
) {
}
