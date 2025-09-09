package de.dicecup.classlink.features.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record CreateUserDto(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 200) String password,
        boolean enabled,
        @NotNull CreateUserInfoDto userInfo

) {}

