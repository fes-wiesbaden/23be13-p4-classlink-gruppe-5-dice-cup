package de.dicecup.classlink.features.registration.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetCommitRequestDto;
import de.dicecup.classlink.features.security.passwordreset.web.PasswordResetCreateRequestDto;
import de.dicecup.classlink.features.users.app.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Admin;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;

    @Test
    @WithMockUser(username = "admin@classlink.test", roles = {"ADMIN"})
    void inviteRedeemFlowReturnsJwt() throws Exception {
        ensureAdmin("admin@classlink.test");

        String createPayload = "{" +
                "\"email\":\"learner@classlink.test\"," +
                "\"role\":\"STUDENT\"}";

        String response = mockMvc.perform(post("/admin/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        String token = body.get("token").asText();

        mockMvc.perform(post("/auth/invites/validate")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk());

        String redeemResponse = mockMvc.perform(post("/auth/invites/redeem")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\",\"username\":\"learner\",\"password\":\"Secret123!\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode redeemBody = objectMapper.readTree(redeemResponse);
        assertThat(redeemBody.get("jwt").asText()).isNotBlank();
        assertThat(userRepository.findByUserInfoEmail("learner@classlink.test")).isPresent();
    }

    @Test
    @WithMockUser(username = "admin-reset@classlink.test", roles = {"ADMIN"})
    void passwordResetFlowChangesPassword() throws Exception {
        ensureAdmin("admin-reset@classlink.test");
        User user = ensureUser("user.reset@test");

        String createResponse = mockMvc.perform(post("/auth/password-reset/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PasswordResetCreateRequestDto(user.getId()))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode created = objectMapper.readTree(createResponse);
        String token = created.get("token").asText();

        mockMvc.perform(post("/auth/password-reset/validate")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/password-reset/commit")
                        .with(anonymous())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PasswordResetCommitRequestDto(token, "Reset123!"))))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getPasswordHash()).isNotEqualTo(user.getPasswordHash());
    }

    private void ensureAdmin(String email) {
        userRepository.findByUserInfoEmail(email).orElseGet(() -> {
            User user = createUser(email);
            Admin adminRole = new Admin();
            adminRole.setUser(user);
            user.setAdmin(adminRole);
            return userRepository.save(user);
        });
    }

    private User ensureUser(String email) {
        return userRepository.findByUserInfoEmail(email)
                .orElseGet(() -> userRepository.save(createUser(email)));
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
        return user;
    }
}
