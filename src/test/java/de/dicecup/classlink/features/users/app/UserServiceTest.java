package de.dicecup.classlink.features.users.app;

import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.common.audit.AuditPublisher;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.dto.UserDto;
import de.dicecup.classlink.features.users.dto.UserInfoDto;
import de.dicecup.classlink.features.users.mapper.UserMapper;
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

        // manually build nested dtos
        UserInfoDto infoDto1 = new UserInfoDto(uf1.getFirstName(), uf1.getLastName(), uf1.getEmail());
        UserInfoDto infoDto2 = new UserInfoDto(uf2.getFirstName(), uf2.getLastName(), uf2.getEmail());

        UserDto dto1 = new UserDto(u1.getId(), "l.moore", true, infoDto1);
        UserDto dto2 = new UserDto(u2.getId(), "m.bartius", true, infoDto2);
        // only stub service using mapper call
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
}