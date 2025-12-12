package de.dicecup.classlink.features.users;

import de.dicecup.classlink.features.auditlogs.Audited;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UpdateUserDto;
import de.dicecup.classlink.features.users.domain.UserDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Audited(action = "USERS_LIST", resource = "USERS")
    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return userRepository.findAllWithRolesAndInfo()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Audited(action = "USER_GET", resource = "USER", detail = "id={0}")
    @Transactional(readOnly = true)
    public UserDto get(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User %s not found".formatted(id)));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto update(UUID id, UpdateUserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User %s not found".formatted(id)));

        if (user.getUserInfo() == null) {
            user.setUserInfo(userMapper.toEntity(dto.userInfo()));
            user.getUserInfo().setUser(user);
        } else {
            var info = user.getUserInfo();
            info.setFirstName(dto.userInfo().firstName());
            info.setLastName(dto.userInfo().lastName());
            info.setEmail(dto.userInfo().email());
        }
        return userMapper.toDto(user);
        }

        @Transactional
        public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User %s not found".formatted(id)));
        user.setEnabled(false);
        user.setDisabledAt(Instant.now());

        }
    }
