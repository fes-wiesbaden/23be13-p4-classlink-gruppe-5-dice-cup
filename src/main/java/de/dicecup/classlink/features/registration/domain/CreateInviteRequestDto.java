package de.dicecup.classlink.features.registration.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateInviteRequestDto(
        @Email String email,
        @NotNull RegistrationInviteRole role,
        UUID classId,
        Integer maxUses,
        String note
) {
}
