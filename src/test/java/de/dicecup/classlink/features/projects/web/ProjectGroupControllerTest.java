package de.dicecup.classlink.features.projects.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.projects.ProjectGroup;
import de.dicecup.classlink.features.projects.ProjectGroupController;
import de.dicecup.classlink.features.projects.ProjectGroupMember;
import de.dicecup.classlink.features.projects.ProjectGroupService;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectGroupController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectGroupControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProjectGroupService projectGroupService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createGroup_returnsCreated() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectGroup group = new ProjectGroup();
        group.setId(UUID.randomUUID());
        group.setGroupNumber(1);
        when(projectGroupService.createGroup(eq(projectId), eq(1), any())).thenReturn(group);

        var request = new CreateGroupRequest(1, null);

        mockMvc.perform(post("/api/projects/" + projectId + "/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/projects/" + projectId + "/groups/" + group.getId()))
                .andExpect(jsonPath("$.id", is(group.getId().toString())));
    }

    @Test
    void listGroups_returnsDtos() throws Exception {
        UUID projectId = UUID.randomUUID();
        ProjectGroup group = new ProjectGroup();
        group.setId(UUID.randomUUID());
        group.setGroupNumber(1);
        when(projectGroupService.listGroups(projectId)).thenReturn(List.of(group));

        mockMvc.perform(get("/api/projects/" + projectId + "/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(group.getId().toString())));
    }

    @Test
    void assignMembers_returnsOk() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        ProjectGroup group = new ProjectGroup();
        group.setId(groupId);
        ProjectGroupMember member = new ProjectGroupMember();
        member.setId(UUID.randomUUID());
        member.setProjectGroup(group);
        group.setMembers(List.of(member));

        when(projectGroupService.listGroups(projectId)).thenReturn(List.of(group));

        var request = new AssignMembersRequest(List.of());

        mockMvc.perform(post("/api/projects/" + projectId + "/groups/" + groupId + "/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}
