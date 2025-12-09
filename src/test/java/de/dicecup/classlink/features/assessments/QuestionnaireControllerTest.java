package de.dicecup.classlink.features.assessments;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.assessments.dto.QuestionRequest;
import de.dicecup.classlink.features.assessments.dto.QuestionnaireCreateRequest;
import de.dicecup.classlink.features.projects.ProjectGroupRepository;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuestionnaireController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuestionnaireControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    QuestionnaireService questionnaireService;
    @MockBean
    QuestionnaireRepository questionnaireRepository;
    @MockBean
    QuestionRepository questionRepository;
    @MockBean
    AuthHelper authHelper;
    @MockBean
    ProjectGroupRepository projectGroupRepository;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getQuestionnaire_notFoundReturns404() throws Exception {
        UUID projectId = UUID.randomUUID();
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/projects/" + projectId + "/questionnaire"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createQuestionnaire_usesAuthenticatedTeacher() throws Exception {
        UUID projectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(UUID.randomUUID());
        questionnaire.setProjectId(projectId);
        questionnaire.setCreatedByTeacherId(teacherId);
        when(questionnaireService.getOrCreate(projectId, teacherId)).thenReturn(questionnaire);
        when(questionRepository.findByQuestionnaireId(questionnaire.getId())).thenReturn(List.of());

        mockMvc.perform(post("/api/projects/" + projectId + "/questionnaire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new QuestionnaireCreateRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void openQuestionnaire_requiresOwnership() throws Exception {
        UUID questionnaireId = UUID.randomUUID();
        UUID projectId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID otherTeacher = UUID.randomUUID();
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(questionnaireId);
        questionnaire.setProjectId(projectId);
        questionnaire.setCreatedByTeacherId(creatorId);
        when(questionnaireRepository.findById(questionnaireId)).thenReturn(Optional.of(questionnaire));
        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.requireTeacherId()).thenReturn(otherTeacher);
        when(projectGroupRepository.existsByProjectIdAndSupervisingTeacherId(projectId, otherTeacher)).thenReturn(false);

        mockMvc.perform(post("/api/questionnaires/" + questionnaireId + "/open"))
                .andExpect(status().isForbidden());
    }
}
