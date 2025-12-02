package de.dicecup.classlink.features.auditlogs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.auditlogs.app.AuditLogRepository;
import de.dicecup.classlink.features.users.app.UserRepository;
import de.dicecup.classlink.features.users.app.UserService;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import de.dicecup.classlink.testdata.TestDataConfig;
import de.dicecup.classlink.testdata.UserTestData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestDataConfig.class)
class AuditLogIntegrationTest extends IntegrationTestBase {

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserTestData userTestData;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeEach
    void clearTables() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void audited_method_persists_structured_log_entry() {
        User actor = userRepository.saveAndFlush(userTestData.userWithInfo());
        User target = userRepository.saveAndFlush(userTestData.userWithInfo());

        Authentication authentication = new UsernamePasswordAuthenticationToken(actor, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        userService.get(target.getId());

        var logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);

        var log = logs.getFirst();
        assertThat(log.getActorId()).isEqualTo(actor.getId());
        assertThat(log.getAction()).isEqualTo("USER_GET");
        assertThat(log.getResource()).isEqualTo("USER");
        assertThat(log.getTimestamp()).isNotNull();
        try {
            assertThat(objectMapper.readValue(log.getDetails(), String.class))
                    .isEqualTo("id=" + target.getId());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
