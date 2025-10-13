package de.dicecup.classlink.features.registration.domain;

import java.util.UUID;

public record InviteRedeemResponseDto(
        UUID userId,
        String jwt,
        String email,
        RegistrationInviteRole role
) {
}
