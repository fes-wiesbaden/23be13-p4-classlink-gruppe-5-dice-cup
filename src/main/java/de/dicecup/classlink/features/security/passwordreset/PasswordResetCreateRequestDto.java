package de.dicecup.classlink.features.security.passwordreset;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PasswordResetCreateRequestDto(@NotNull UUID userId) {
}
