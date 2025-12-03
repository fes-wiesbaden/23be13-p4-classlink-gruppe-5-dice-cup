package de.dicecup.classlink.features.users.web;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UserControllerSecurityTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordService passwordService;

    @Test
    void getUsersRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsersSucceedsWithValidToken() throws Exception {
        String email = "secure.user@example.com";
        String password = "Secret123!";
        ensureUser(email, password);

        String accessToken = loginAndExtractToken(email, password);

        String response = mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode body = objectMapper.readTree(response);
        assertThat(body.isArray()).isTrue();
    }

    private String loginAndExtractToken(String email, String password) throws Exception {
        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode body = objectMapper.readTree(loginResponse);
        return body.get("accessToken").asText();
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
}
