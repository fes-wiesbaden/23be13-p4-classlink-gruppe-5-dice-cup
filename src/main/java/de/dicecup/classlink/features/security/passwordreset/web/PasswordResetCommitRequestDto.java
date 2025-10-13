package de.dicecup.classlink.features.security.passwordreset.web;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetCommitRequestDto(
        @NotBlank String token,
        @NotBlank String newPassword
) {
}
