package de.dicecup.classlink.features.assessments;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuestionnaireServiceTest {

    private QuestionnaireRepository questionnaireRepository;
    private QuestionRepository questionRepository;
    private QuestionnaireService service;

    @BeforeEach
    void setUp() {
        questionnaireRepository = mock(QuestionnaireRepository.class);
        questionRepository = mock(QuestionRepository.class);
        service = new QuestionnaireService(questionnaireRepository, questionRepository);
    }

    @Test
    void openQuestionnaire_requiresQuestions() {
        UUID projectId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setProjectId(projectId);
        q.setStatus(QuestionnaireStatus.DRAFT);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));
        when(questionRepository.findByQuestionnaireId(q.getId())).thenReturn(List.of());

        assertThatThrownBy(() -> service.openQuestionnaire(projectId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("questions");
    }

    @Test
    void openQuestionnaire_transitionsDraftToOpen() {
        UUID projectId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setProjectId(projectId);
        q.setStatus(QuestionnaireStatus.DRAFT);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));
        when(questionRepository.findByQuestionnaireId(q.getId())).thenReturn(List.of(new Question()));

        Questionnaire result = service.openQuestionnaire(projectId);

        assertThat(result.getStatus()).isEqualTo(QuestionnaireStatus.OPEN);
    }

    @Test
    void questionModification_onlyInDraft() {
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.OPEN);
        when(questionnaireRepository.findById(q.getId())).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> service.addQuestion(q.getId(), "text", 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void closeQuestionnaire_fromOpen() {
        UUID projectId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setProjectId(projectId);
        q.setStatus(QuestionnaireStatus.OPEN);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));

        Questionnaire result = service.closeQuestionnaire(projectId);

        assertThat(result.getStatus()).isEqualTo(QuestionnaireStatus.CLOSED);
    }

    @Test
    void updateQuestion_rejectsWrongQuestionnaire() {
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.DRAFT);
        when(questionnaireRepository.findById(q.getId())).thenReturn(Optional.of(q));
        Question question = new Question();
        question.setId(UUID.randomUUID());
        Questionnaire other = new Questionnaire();
        other.setId(UUID.randomUUID());
        question.setQuestionnaire(other);
        when(questionRepository.findById(question.getId())).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> service.updateQuestion(q.getId(), question.getId(), "t", 1, true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void questionModification_rejectedWhenClosed() {
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.CLOSED);
        when(questionnaireRepository.findById(q.getId())).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> service.addQuestion(q.getId(), "text", 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void closeQuestionnaire_onlyFromOpen() {
        UUID projectId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setProjectId(projectId);
        q.setStatus(QuestionnaireStatus.DRAFT);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> service.closeQuestionnaire(projectId))
                .isInstanceOf(IllegalStateException.class);
    }
}
