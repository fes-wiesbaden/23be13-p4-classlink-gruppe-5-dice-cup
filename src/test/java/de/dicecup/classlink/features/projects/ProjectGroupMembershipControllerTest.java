package de.dicecup.classlink.features.projects;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectGroupMembershipController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectGroupMembershipControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProjectGroupMembershipService membershipService;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void addMember_returnsNoContent() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        doNothing().when(membershipService).addMember(groupId, studentId);

        mockMvc.perform(post("/api/project-groups/" + groupId + "/members/" + studentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeMember_returnsNoContent() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        doNothing().when(membershipService).removeMember(groupId, studentId);

        mockMvc.perform(delete("/api/project-groups/" + groupId + "/members/" + studentId))
                .andExpect(status().isNoContent());
    }

    @Test
    void addMember_returnsForbiddenForUnauthorizedTeacher() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        doThrow(new org.springframework.security.access.AccessDeniedException("forbidden"))
                .when(membershipService).addMember(groupId, studentId);

        mockMvc.perform(post("/api/project-groups/" + groupId + "/members/" + studentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeMember_returnsForbiddenForUnauthorizedTeacher() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        doThrow(new org.springframework.security.access.AccessDeniedException("forbidden"))
                .when(membershipService).removeMember(groupId, studentId);

        mockMvc.perform(delete("/api/project-groups/" + groupId + "/members/" + studentId))
                .andExpect(status().isForbidden());
    }
}
