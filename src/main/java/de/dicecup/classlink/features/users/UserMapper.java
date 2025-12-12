package de.dicecup.classlink.features.users;

import de.dicecup.classlink.features.users.domain.*;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "accountUsername")
    @Mapping(target = "enabled", source = "enabled")
    @Mapping(target = "userInfo", source = "userInfo")
    @Mapping(target = "role", expression = "java(resolveRole(user))")
    UserDto toDto(User user);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    UserInfoDto toDto(UserInfo userInfo);

    @BeanMapping(ignoreByDefault = true)
    User toEntity(CreateUserDto dto);

    @BeanMapping(ignoreByDefault = true)
    UserInfo toEntity(CreateUserInfoDto dto);

    default UserRoleDto resolveRole(User user) {
        if (user.getAdmin() != null) {
            return UserRoleDto.ADMIN;
        }
        if (user.getTeacher() != null) {
            return UserRoleDto.TEACHER;
        }
        if (user.getStudent() != null) {
            return UserRoleDto.STUDENT;
        }
        //TODO: maybe use a exception
        return null;
    }
}
