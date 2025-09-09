package de.dicecup.classlink.features.registration.domain;

public record InviteRequestDto(
        @jakarta.validation.constraints.Email String email,
        String role,
        Integer ttlMinutes
) {}
