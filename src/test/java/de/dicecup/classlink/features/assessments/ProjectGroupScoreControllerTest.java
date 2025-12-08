package de.dicecup.classlink.features.assessments;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.assessments.dto.ProjectGroupStudentScoreOverviewDto;
import de.dicecup.classlink.features.assessments.dto.StudentSubjectAssessmentDto;
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
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectGroupScoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectGroupScoreControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProjectGroupScoreService projectGroupScoreService;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void listScores_returnsData() throws Exception {
        UUID groupId = UUID.randomUUID();
        ProjectGroupStudentScoreOverviewDto dto = new ProjectGroupStudentScoreOverviewDto(
                UUID.randomUUID(),
                "Alice Anderson",
                UUID.randomUUID(),
                "1A",
                groupId,
                "Project X",
                null,
                3.0,
                2.5,
                ProjectGroupStudentScoreOverviewDto.Tendency.ALIGNED
        );
        when(projectGroupScoreService.listGroupScores(groupId)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/project-groups/" + groupId + "/scores")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].projectGroupId", is(groupId.toString())));
    }

    @Test
    void subjectScores_returnsData() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        StudentSubjectAssessmentDto dto = new StudentSubjectAssessmentDto(
                studentId,
                "Alice",
                groupId,
                "Project X",
                UUID.randomUUID(),
                List.of()
        );
        when(projectGroupScoreService.subjectScores(groupId, studentId, null)).thenReturn(dto);

        mockMvc.perform(get("/api/project-groups/" + groupId + "/students/" + studentId + "/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId", is(studentId.toString())));
    }

    @Test
    void getScores_returnsForbiddenForUnauthorizedTeacher() throws Exception {
        UUID groupId = UUID.randomUUID();
        doThrow(new org.springframework.security.access.AccessDeniedException("forbidden"))
                .when(projectGroupScoreService).listGroupScores(groupId);

        mockMvc.perform(get("/api/project-groups/" + groupId + "/scores"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSubjectScores_returnsForbiddenForUnauthorizedTeacher() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        doThrow(new org.springframework.security.access.AccessDeniedException("forbidden"))
                .when(projectGroupScoreService).subjectScores(groupId, studentId, null);

        mockMvc.perform(get("/api/project-groups/" + groupId + "/students/" + studentId + "/subjects"))
                .andExpect(status().isForbidden());
    }
}
