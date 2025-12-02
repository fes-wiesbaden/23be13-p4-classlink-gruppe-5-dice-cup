package de.dicecup.classlink.features.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UpdateUserDto(
        @Size(max = 100) String username,

        boolean enabled,
        CreateUserInfoDto userInfo
) {}
