package de.dicecup.classlink.features.users.mapper;

import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.dto.CreateUserDto;
import de.dicecup.classlink.features.users.dto.CreateUserInfoDto;
import de.dicecup.classlink.features.users.dto.UserDto;
import de.dicecup.classlink.features.users.dto.UserInfoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    UserInfoDto toDto(UserInfo userInfo);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userInfo.user", ignore = true)
    User toEntity(CreateUserDto dto);

    UserInfo toEntity(CreateUserInfoDto dto);
}
