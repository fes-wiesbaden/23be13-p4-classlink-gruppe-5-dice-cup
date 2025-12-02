package de.dicecup.classlink.features.users.dto;


import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        boolean enabled,
        UserInfoDto userInfo
) {
}
