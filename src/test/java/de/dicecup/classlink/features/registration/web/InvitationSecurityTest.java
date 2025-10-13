package de.dicecup.classlink.features.registration.web;

import de.dicecup.classlink.features.users.app.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class InvitationSecurityTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;

    @Test
    @WithMockUser(username = "teacher@classlink.test", roles = {"TEACHER"})
    void teacherCannotInviteAdmin() throws Exception {
        ensureTeacher("teacher@classlink.test");

        String payload = "{" +
                "\"email\":\"target@classlink.test\"," +
                "\"role\":\"ADMIN\"}";

        mockMvc.perform(post("/admin/invites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    private void ensureTeacher(String email) {
        userRepository.findByUserInfoEmail(email).orElseGet(() -> {
            User user = new User();
            user.setUsername(email);
            user.setPasswordHash("hash");
            user.setEnabled(true);

            UserInfo info = new UserInfo();
            info.setUser(user);
            info.setEmail(email);
            user.setUserInfo(info);

            Teacher teacher = new Teacher();
            teacher.setUser(user);
            user.setTeacher(teacher);
            return userRepository.save(user);
        });
    }
}
