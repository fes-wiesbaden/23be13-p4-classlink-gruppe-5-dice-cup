package de.dicecup.classlink.features.projects.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.projects.Project;
import de.dicecup.classlink.features.projects.ProjectController;
import de.dicecup.classlink.features.projects.ProjectService;
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

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProjectService projectService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createProject_returnsCreated() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Project 1");
        when(projectService.createProject(eq(classId), eq(termId), any())).thenReturn(project);

        var request = new ProjectRequestDto("Project 1", "desc");

        mockMvc.perform(post("/api/classes/" + classId + "/terms/" + termId + "/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/projects/" + project.getId()))
                .andExpect(jsonPath("$.id", is(project.getId().toString())));
    }

    @Test
    void listProjects_returnsDtos() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setName("Project 1");
        when(projectService.listProjects(classId, termId)).thenReturn(List.of(project));

        mockMvc.perform(get("/api/classes/" + classId + "/terms/" + termId + "/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(project.getId().toString())));
    }

    @Test
    void archiveProject_returnsNoContent() throws Exception {
        UUID projectId = UUID.randomUUID();

        mockMvc.perform(post("/api/projects/" + projectId + "/archive")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
