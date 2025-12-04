package de.dicecup.classlink.features.security.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.security.PasswordService;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordService passwordService;

    @Test
    @Transactional
    void loginRefreshFlowRotatesTokens() throws Exception {
        String email = "login.user@example.com";
        String password = "Secret123!";
        ensureUser(email, password);

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode loginBody = objectMapper.readTree(loginResponse);
        String accessToken = loginBody.get("accessToken").asText();
        String refreshToken = loginBody.get("refreshToken").asText();

        assertThat(accessToken).isNotBlank();
        assertThat(refreshToken).isNotBlank();

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        String refreshResponse = mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode refreshBody = objectMapper.readTree(refreshResponse);
        String newAccessToken = refreshBody.get("accessToken").asText();
        String newRefreshToken = refreshBody.get("refreshToken").asText();

        assertThat(newAccessToken).isNotEqualTo(accessToken);
        assertThat(newRefreshToken).isNotEqualTo(refreshToken);

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    private void ensureUser(String email, String rawPassword) {
        User user = new User();
        user.setUsername(email);
        user.setEnabled(true);
        user.setPasswordHash(passwordService.hashPassword(rawPassword));

        UserInfo info = new UserInfo();
        info.setUser(user);
        info.setEmail(email);
        user.setUserInfo(info);

        userRepository.save(user);
    }

    @Test
    void loginFailsWithInvalidCredentials() throws Exception {
        String email = "invalid.user@example.com";
        ensureUser(email, "Secret123!");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"WrongPass!\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void refreshFailsWithTamperedToken() throws Exception {
        String email = "tamper.user@example.com";
        String password = "Secret123!";
        ensureUser(email, password);

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andReturn().getResponse().getContentAsString();

        JsonNode loginBody = objectMapper.readTree(loginResponse);
        String refreshToken = loginBody.get("refreshToken").asText();

        String tampered = refreshToken + "tampered";

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + tampered + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("REFRESH_TOKEN_INVALID"));
    }
}
