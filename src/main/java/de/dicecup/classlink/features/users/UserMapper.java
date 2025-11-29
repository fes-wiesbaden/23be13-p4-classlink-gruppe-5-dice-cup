package de.dicecup.classlink.features.users;

import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.CreateUserDto;
import de.dicecup.classlink.features.users.domain.CreateUserInfoDto;
import de.dicecup.classlink.features.users.domain.UserDto;
import de.dicecup.classlink.features.users.domain.UserInfoDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @BeanMapping(ignoreByDefault = true)
    UserDto toDto(User user);

    @BeanMapping(ignoreByDefault = true)
    UserInfoDto toDto(UserInfo userInfo);

    @BeanMapping(ignoreByDefault = true)
    User toEntity(CreateUserDto dto);

    @BeanMapping(ignoreByDefault = true)
    UserInfo toEntity(CreateUserInfoDto dto);
}
