package de.dicecup.classlink.features.users.app;

import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.UserService;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.common.audit.AuditPublisher;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.CreateUserInfoDto;
import de.dicecup.classlink.features.users.domain.UpdateUserDto;
import de.dicecup.classlink.features.users.domain.UserDto;
import de.dicecup.classlink.features.users.domain.UserInfoDto;
import de.dicecup.classlink.features.users.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    AuditPublisher auditPublisher;

    @InjectMocks
    UserService service;

    @Test
    void list_returns_mapped_dtos() {
        // arrange
        User u1 = new User();
        u1.setId(UUID.randomUUID());
        u1.setUsername("l.moore");

        User u2 = new User();
        u2.setId(UUID.randomUUID());
        u2.setUsername("m.bartius");

        UserInfo uf1 = new UserInfo(u1.getId(), u1, "Luke", "Moore", "foo@bar.com");
        UserInfo uf2 = new UserInfo(u2.getId(), u2, "Moritz", "Bartius", "bar@foo.com");

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        UserInfoDto infoDto1 = new UserInfoDto(uf1.getFirstName(), uf1.getLastName(), uf1.getEmail());
        UserInfoDto infoDto2 = new UserInfoDto(uf2.getFirstName(), uf2.getLastName(), uf2.getEmail());

        UserDto dto1 = new UserDto(u1.getId(), "l.moore", true, infoDto1);
        UserDto dto2 = new UserDto(u2.getId(), "m.bartius", true, infoDto2);
        when(userMapper.toDto(u1)).thenReturn(dto1);
        when(userMapper.toDto(u2)).thenReturn(dto2);

        // act
        var result = service.list();

        // assert
        assertThat(result).hasSize(2)
                .containsExactlyInAnyOrder(dto1, dto2);

        verify(userRepository).findAll();
        verify(userMapper).toDto(u1);
        verify(userMapper).toDto(u2);

        // exclude non harming interactions
        verifyNoMoreInteractions(userRepository, auditPublisher);
    }

    @Test
    void get_returns_mapped_user_dto() {
        // arrange
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername("winkler");

        UserInfo uf = new UserInfo(u.getId(), u, "Rainer", "Blue", "bar@foo.com");

        when(userRepository.findById(u.getId())).thenReturn(Optional.of(u));

        UserInfoDto infoDto = new UserInfoDto(
                uf.getFirstName(),
                uf.getLastName(),
                uf.getEmail()
        );

        UserDto dto = new UserDto(u.getId(), "winkler", true, infoDto);

        when(userMapper.toDto(u)).thenReturn(dto);

        // act
        UserDto result = service.get(u.getId());

        // assert
        assertThat(result.id()).isEqualTo(u.getId());
        assertThat(result.username().equals(u.getUsername()));
        assertThat(result.userInfo().firstName()).isEqualTo(uf.getFirstName());
    }

    @Test
    void get_throws_when_user_not_found() {
        // arrange
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.get(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository, userMapper, auditPublisher);
    }

    @Test
    void update_creates_user_info_when_missing() {
        // arrange
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEnabled(true);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        CreateUserInfoDto infoDto = new CreateUserInfoDto("Jane", "Doe", "jane.doe@classlink.local");
        UpdateUserDto updateDto = new UpdateUserDto("jane.doe", true, infoDto);

        UserInfo mappedInfo = new UserInfo(null, null, infoDto.firstName(), infoDto.lastName(), infoDto.email());
        when(userMapper.toEntity(infoDto)).thenReturn(mappedInfo);

        UserInfoDto expectedInfoDto = new UserInfoDto(infoDto.firstName(), infoDto.lastName(), infoDto.email());
        UserDto expectedDto = new UserDto(id, "jane.doe", true, expectedInfoDto);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        // act
        UserDto result = service.update(id, updateDto);

        // assert
        assertThat(result).isEqualTo(expectedDto);
        assertThat(user.getUserInfo()).isSameAs(mappedInfo);
        assertThat(user.getUserInfo().getUser()).isEqualTo(user);

        verify(userRepository).findById(id);
        verify(userMapper).toEntity(infoDto);
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, auditPublisher);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    void update_updates_existing_user_info() {
        // arrange
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEnabled(true);

        UserInfo existingInfo = new UserInfo(id, user, "Old", "Name", "old@classlink.local");
        user.setUserInfo(existingInfo);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        CreateUserInfoDto infoDto = new CreateUserInfoDto("New", "Name", "new@classlink.local");
        UpdateUserDto updateDto = new UpdateUserDto("new.name", false, infoDto);

        UserInfoDto mappedInfoDto = new UserInfoDto("New", "Name", "new@classlink.local");
        UserDto expectedDto = new UserDto(id, "new.name", false, mappedInfoDto);
        when(userMapper.toDto(user)).thenReturn(expectedDto);

        // act
        UserDto result = service.update(id, updateDto);

        // assert
        assertThat(result).isEqualTo(expectedDto);
        assertThat(existingInfo.getFirstName()).isEqualTo("New");
        assertThat(existingInfo.getLastName()).isEqualTo("Name");
        assertThat(existingInfo.getEmail()).isEqualTo("new@classlink.local");

        verify(userRepository).findById(id);
        verify(userMapper, never()).toEntity(any(CreateUserInfoDto.class));
        verify(userMapper).toDto(user);
        verifyNoMoreInteractions(userRepository, auditPublisher);
        verifyNoMoreInteractions(userMapper);
    }

    @Test
    void update_throws_when_user_not_found() {
        // arrange
        UUID id = UUID.randomUUID();
        CreateUserInfoDto infoDto = new CreateUserInfoDto("Jane", "Doe", "jane@classlink.local");
        UpdateUserDto updateDto = new UpdateUserDto("jane", true, infoDto);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.update(id, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository, userMapper, auditPublisher);
    }

    @Test
    void delete_disables_user_and_sets_timestamp() {
        // arrange
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setEnabled(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        // act
        service.delete(id);

        // assert
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getDisabledAt()).isNotNull();

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository, auditPublisher);
    }

    @Test
    void delete_throws_when_user_not_found() {
        // arrange
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(id.toString());

        verify(userRepository).findById(id);
        verifyNoMoreInteractions(userRepository, userMapper, auditPublisher);
    }
}