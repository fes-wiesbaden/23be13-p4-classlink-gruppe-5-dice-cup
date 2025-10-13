package de.dicecup.classlink.features.registration.domain;

import jakarta.validation.constraints.NotBlank;

public record InviteRedeemRequestDto(
        @NotBlank String token,
        @NotBlank String username,
        @NotBlank String password
) {
}
