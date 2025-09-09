package de.dicecup.classlink.features.users.app;

import de.dicecup.classlink.common.audit.AuditPublisher;
import de.dicecup.classlink.features.registration.domain.RegristrationRequesDto;
import de.dicecup.classlink.features.registration.repo.RegistrationInviteRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.dto.CreateUserDto;
import de.dicecup.classlink.features.users.dto.UpdateUserDto;
import de.dicecup.classlink.features.users.dto.UserDto;
import de.dicecup.classlink.features.users.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuditPublisher audit;

    @Transactional(readOnly = true)
    public List<UserDto> list() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

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
            info.setDateOfBirth(dto.userInfo().dateOfBirth());
            info.setEmail(dto.userInfo().email());
        }
        return userMapper.toDto(user);
        }

        @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User %s not found".formatted(id)));
        user.setEnabled(false);
        user.setDisabledAt(OffsetDateTime.now());

        }
    }
