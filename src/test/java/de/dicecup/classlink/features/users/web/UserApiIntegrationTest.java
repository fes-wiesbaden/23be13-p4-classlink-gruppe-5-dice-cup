package de.dicecup.classlink.features.users.web;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UserApiIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void getUsersReturnsUserInfoFields() throws Exception {
        User user = new User();
        user.setUsername("api.user" + UUID.randomUUID());
        user.setPasswordHash("hash");
        user.setEnabled(true);

        UserInfo info = new UserInfo();
        info.setFirstName("API");
        info.setLastName("User");
        info.setEmail("api.user+" + UUID.randomUUID() + "@example.com");
        info.setUser(user);
        user.setUserInfo(info);

        userRepository.save(user);

        mockMvc.perform(get("/users")
                        .with(user("tester").roles("ADMIN"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userInfo.email").value(info.getEmail()))
                .andExpect(jsonPath("$[0].userInfo.firstName").value(info.getFirstName()))
                .andExpect(jsonPath("$[0].userInfo.lastName").value(info.getLastName()));
    }
}
