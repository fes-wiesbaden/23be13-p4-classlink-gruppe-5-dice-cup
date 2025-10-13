package de.dicecup.classlink.features.registration.domain;

import jakarta.validation.constraints.NotBlank;

public record InviteValidationRequestDto(@NotBlank String token) {
}
