package de.dicecup.classlink.features.registration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.security.PasswordService;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Admin;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminInvitationSecurityTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordService passwordService;

    @Test
    void adminUserCanCreateInvites() throws Exception {
        String email = "admin.security@example.com";
        String password = "Secret123!";
        ensureAdmin(email, password);

        String token = loginAndExtractToken(email, password);

        mockMvc.perform(post("/admin/invites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invitee@example.com",
                                  "role": "STUDENT",
                                  "maxUses": 1
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void regularUserCannotAccessAdminInvites() throws Exception {
        String email = "regular.user@example.com";
        String password = "Secret123!";
        ensureUser(email, password);

        String token = loginAndExtractToken(email, password);

        mockMvc.perform(post("/admin/invites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "invitee@example.com",
                                  "role": "STUDENT"
                                }
                                """))
                .andExpect(status().isForbidden());
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

    private void ensureAdmin(String email, String rawPassword) {
        User user = baseUser(email, rawPassword);
        Admin admin = new Admin();
        admin.setUser(user);
        user.setAdmin(admin);
        userRepository.save(user);
    }

    private void ensureUser(String email, String rawPassword) {
        userRepository.save(baseUser(email, rawPassword));
    }

    private User baseUser(String email, String rawPassword) {
        User user = new User();
        user.setUsername(email);
        user.setEnabled(true);
        user.setPasswordHash(passwordService.hashPassword(rawPassword));

        UserInfo info = new UserInfo();
        info.setUser(user);
        info.setEmail(email);
        user.setUserInfo(info);

        return user;
    }
}
