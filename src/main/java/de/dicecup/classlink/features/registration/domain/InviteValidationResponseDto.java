package de.dicecup.classlink.features.registration.domain;

import java.time.Instant;
import java.util.UUID;

public record InviteValidationResponseDto(
        UUID inviteId,
        String email,
        RegistrationInviteRole role,
        Instant expiresAt,
        int remainingUses
) {
}
