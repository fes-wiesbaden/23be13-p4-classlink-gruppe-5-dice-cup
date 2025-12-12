package de.dicecup.classlink.features.users.domain;


import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        boolean enabled,
        UserInfoDto userInfo,
        UserRoleDto role
) {
}
