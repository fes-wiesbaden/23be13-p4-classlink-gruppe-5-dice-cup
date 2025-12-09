package de.dicecup.classlink.features.security.admin;

import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"test", "dev"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AdminUserBootstrapIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Qualifier("adminUserInitializer")
    private ApplicationRunner adminUserInitializer;

    @Test
    void bootstrapCreatesAdminRole() throws Exception {
        ApplicationArguments args = new DefaultApplicationArguments(new String[]{});
        adminUserInitializer.run(args);
        User admin = userRepository.findByUserInfoEmail("admin@dicecup.local").orElseThrow();
        assertThat(admin.getAdmin()).isNotNull();
        assertThat(admin.getAdmin().getId()).isEqualTo(admin.getId());
    }
}
