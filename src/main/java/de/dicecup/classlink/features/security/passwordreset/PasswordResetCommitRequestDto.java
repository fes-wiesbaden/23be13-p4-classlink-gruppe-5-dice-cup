package de.dicecup.classlink.features.security.passwordreset;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetCommitRequestDto(
        @NotBlank String token,
        @NotBlank String newPassword
) {
}
