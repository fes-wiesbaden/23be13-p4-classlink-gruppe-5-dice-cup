package de.dicecup.classlink.features.users.dto;

import java.time.LocalDate;

public record UserInfoDto(
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String email
) {
}
