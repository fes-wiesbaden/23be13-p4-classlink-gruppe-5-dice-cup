package de.dicecup.classlink.features.security.passwordreset;

import de.dicecup.classlink.features.security.passwordreset.app.PasswordResetService;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetCommitRequestDto;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetCreateRequestDto;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetValidateRequestDto;
import de.dicecup.classlink.features.users.app.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.*;

class PasswordResetServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    PasswordResetService passwordResetService;
    @Autowired
    UserRepository userRepository;

    @Test
    void createValidateAndCommitResetToken() {
        User user = createUser("reset.user@example.com");

        var created = passwordResetService.createResetToken(new PasswordResetCreateRequestDto(user.getId()));
        assertThat(created.token()).isNotBlank();

        var validation = passwordResetService.validate(new PasswordResetValidateRequestDto(created.token()));
        assertThat(validation.userId()).isEqualTo(user.getId());

        passwordResetService.commit(new PasswordResetCommitRequestDto(created.token(), "N3wPassw0rd!"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getPasswordHash()).isNotEqualTo(user.getPasswordHash());
    }

    @Test
    void doubleUseFails() {
        User user = createUser("double.reset@example.com");

        var created = passwordResetService.createResetToken(new PasswordResetCreateRequestDto(user.getId()));
        passwordResetService.commit(new PasswordResetCommitRequestDto(created.token(), "Pass1!"));

        assertThatThrownBy(() -> passwordResetService.commit(new PasswordResetCommitRequestDto(created.token(), "Pass2!")))
                .isInstanceOf(IllegalStateException.class);
    }

    private User createUser(String email) {
        User user = new User();
        user.setUsername(email);
        user.setPasswordHash("hash");
        user.setEnabled(true);

        UserInfo info = new UserInfo();
        info.setUser(user);
        info.setEmail(email);
        user.setUserInfo(info);

        return userRepository.save(user);
    }
}
