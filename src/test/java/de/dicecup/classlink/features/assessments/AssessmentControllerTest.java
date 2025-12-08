package de.dicecup.classlink.features.assessments;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.assessments.dto.AssessmentSubmissionRequest;
import de.dicecup.classlink.features.assessments.dto.AssessmentAnswerDto;
import de.dicecup.classlink.features.projects.ProjectGroupMemberRepository;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AssessmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AssessmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AssessmentService assessmentService;
    @MockBean
    QuestionnaireRepository questionnaireRepository;
    @MockBean
    QuestionRepository questionRepository;
    @MockBean
    AssessmentRepository assessmentRepository;
    @MockBean
    AssessmentAnswerRepository assessmentAnswerRepository;
    @MockBean
    AssessmentAggregationService aggregationService;
    @MockBean
    AuthHelper authHelper;
    @MockBean
    ProjectGroupMemberRepository projectGroupMemberRepository;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void submitSelf_usesAuthenticatedStudentId() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        when(authHelper.requireStudentId()).thenReturn(studentId);

        AssessmentSubmissionRequest request = new AssessmentSubmissionRequest(
                List.of(new AssessmentAnswerDto(UUID.randomUUID(), 4))
        );

        mockMvc.perform(post("/api/projects/" + projectId + "/assessments/self")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk());

        ArgumentCaptor<UUID> projectCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> studentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(assessmentService).submitSelf(projectCaptor.capture(), studentCaptor.capture(), any());
        assertThat(projectCaptor.getValue()).isEqualTo(projectId);
        assertThat(studentCaptor.getValue()).isEqualTo(studentId);
    }

    @Test
    void getPeer_forbiddenWhenNotMember() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID assesseeId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        when(authHelper.requireStudentId()).thenReturn(studentId);
        when(projectGroupMemberRepository.existsByProjectGroupProjectIdAndStudentId(projectId, studentId)).thenReturn(false);

        mockMvc.perform(get("/api/projects/" + projectId + "/assessments/peer/" + assesseeId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getPeer_usesAuthenticatedAssessor() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID assesseeId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID questionnaireId = UUID.randomUUID();
        when(authHelper.requireStudentId()).thenReturn(studentId);
        when(projectGroupMemberRepository.existsByProjectGroupProjectIdAndStudentId(projectId, studentId)).thenReturn(true);
        when(projectGroupMemberRepository.existsByProjectGroupProjectIdAndStudentId(projectId, assesseeId)).thenReturn(true);

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(questionnaireId);
        questionnaire.setProjectId(projectId);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(questionnaire));
        when(questionRepository.findByQuestionnaireId(questionnaireId)).thenReturn(List.of());
        when(assessmentRepository.findByQuestionnaireIdAndTypeAndAssessorStudentIdAndAssesseeStudentId(questionnaireId, AssessmentType.PEER, studentId, assesseeId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/" + projectId + "/assessments/peer/" + assesseeId))
                .andExpect(status().isOk());

        verify(assessmentRepository).findByQuestionnaireIdAndTypeAndAssessorStudentIdAndAssesseeStudentId(questionnaireId, AssessmentType.PEER, studentId, assesseeId);
    }
}
