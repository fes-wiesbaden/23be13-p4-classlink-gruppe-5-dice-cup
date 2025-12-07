package de.dicecup.classlink.features.assessments;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssessmentServiceTest {

    private QuestionnaireRepository questionnaireRepository;
    private QuestionRepository questionRepository;
    private AssessmentRepository assessmentRepository;
    private AssessmentAnswerRepository answerRepository;
    private AssessmentService service;

    @BeforeEach
    void setUp() {
        questionnaireRepository = mock(QuestionnaireRepository.class);
        questionRepository = mock(QuestionRepository.class);
        assessmentRepository = mock(AssessmentRepository.class);
        answerRepository = mock(AssessmentAnswerRepository.class);
        service = new AssessmentService(questionnaireRepository, questionRepository, assessmentRepository, answerRepository);
    }

    @Test
    void submitSelf_rejectsWhenQuestionnaireMissing() {
        UUID projectId = UUID.randomUUID();
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.submitSelf(projectId, UUID.randomUUID(), List.of()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void submitPeer_rejectsSameAssessorAssessee() {
        UUID projectId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.OPEN);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));
        when(questionRepository.findByQuestionnaireId(q.getId())).thenReturn(List.of(new Question()));

        UUID student = UUID.randomUUID();
        assertThatThrownBy(() -> service.submitPeer(projectId, student, student, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void submitSelf_rejectsScoreOutOfRange() {
        UUID projectId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.OPEN);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));
        Question question = new Question();
        question.setId(UUID.randomUUID());
        question.setQuestionnaire(q);
        when(questionRepository.findByQuestionnaireId(q.getId())).thenReturn(List.of(question));

        AssessmentService.AnswerCommand bad = new AssessmentService.AnswerCommand(question.getId(), 7);

        assertThatThrownBy(() -> service.submitSelf(projectId, UUID.randomUUID(), List.of(bad)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 6");
    }

    @Test
    void submitSelf_rejectsInvalidQuestion() {
        UUID projectId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.OPEN);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));
        when(questionRepository.findByQuestionnaireId(q.getId())).thenReturn(List.of());

        AssessmentService.AnswerCommand cmd = new AssessmentService.AnswerCommand(UUID.randomUUID(), 4);

        assertThatThrownBy(() -> service.submitSelf(projectId, UUID.randomUUID(), List.of(cmd)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
