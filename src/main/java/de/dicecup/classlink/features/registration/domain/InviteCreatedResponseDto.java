package de.dicecup.classlink.features.registration.domain;

import java.time.Instant;
import java.util.UUID;

public record InviteCreatedResponseDto(
        UUID inviteId,
        String token,
        String qrCodeUrl,
        Instant expiresAt
) {
}
