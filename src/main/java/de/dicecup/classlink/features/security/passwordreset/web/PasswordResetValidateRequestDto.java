package de.dicecup.classlink.features.security.passwordreset.web;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetValidateRequestDto(@NotBlank String token) {
}
