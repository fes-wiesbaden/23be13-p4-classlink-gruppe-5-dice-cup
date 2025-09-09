package de.dicecup.classlink.features.registration.domain;

import de.dicecup.classlink.features.users.dto.CreateUserInfoDto;

import java.util.UUID;

public record RegristrationRequesDto(
        UUID inviteId,
        String token,
        String username,
        String password,
        CreateUserInfoDto userInfo
) {
}
