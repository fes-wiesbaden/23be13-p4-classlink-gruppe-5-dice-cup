package de.dicecup.classlink.persistence;

import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.testdata.TestDataConfig;
import de.dicecup.classlink.testdata.UserTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Import(TestDataConfig.class)
class FlywayMigrationIT extends IntegrationTestBase {
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    UserRepository users;
    @Autowired
    UserTestData userTestData;

    @Test
    void users_username_unique_is_enforced() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();

        jdbc.update("""
                    INSERT INTO users(id, username, password_hash, enabled)
                    VALUES (?,'alice','$2a$10$hash',true)
                """, id1);

        assertThatThrownBy(() -> jdbc.update("""
                    INSERT INTO users(id, username, password_hash, enabled)
                    VALUES (?,'alice','$2a$10$hash',true)
                """, id2)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void can_persist_and_load_user() {
        User u = userTestData.userWithInfo();
        User saved = users.save(u);

        List<User> all = users.findAll();
        assertThat(all).extracting(User::getId).contains(saved.getId());
    }
}
