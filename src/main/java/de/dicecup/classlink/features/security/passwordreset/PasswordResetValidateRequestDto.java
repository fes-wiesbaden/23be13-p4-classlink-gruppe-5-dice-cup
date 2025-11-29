package de.dicecup.classlink.features.security.passwordreset;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetValidateRequestDto(@NotBlank String token) {
}
