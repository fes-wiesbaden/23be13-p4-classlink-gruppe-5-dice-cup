package de.dicecup.classlink.features.users;

import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    void persistsAndLoadsUserInfoWithSharedPrimaryKey() {
        User user = new User();
        user.setUsername("integration.user" + UUID.randomUUID());
        user.setPasswordHash("hash");
        user.setEnabled(true);

        UserInfo info = new UserInfo();
        info.setFirstName("Integration");
        info.setLastName("Test");
        info.setEmail("integration.user+" + UUID.randomUUID() + "@example.com");

        info.setUser(user);
        user.setUserInfo(info);

        User saved = userRepository.save(user);
        UUID userId = saved.getId();

        entityManager.flush();
        entityManager.clear();

        User reloaded = userRepository.findById(userId).orElseThrow();
        assertThat(reloaded.getUserInfo()).isNotNull();
        assertThat(reloaded.getUserInfo().getEmail()).isEqualTo(info.getEmail());
        assertThat(reloaded.getUserInfo().getFirstName()).isEqualTo(info.getFirstName());
        assertThat(reloaded.getUserInfo().getLastName()).isEqualTo(info.getLastName());
        assertThat(reloaded.getUserInfo().getId()).isEqualTo(reloaded.getId());
    }
}
