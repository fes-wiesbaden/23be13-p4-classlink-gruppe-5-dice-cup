package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.projects.ProjectGroupMemberRepository;
import de.dicecup.classlink.features.assessments.AssessmentAlreadySubmittedException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssessmentServiceTest {

    private QuestionnaireRepository questionnaireRepository;
    private QuestionRepository questionRepository;
    private AssessmentRepository assessmentRepository;
    private AssessmentAnswerRepository answerRepository;
    private ProjectGroupMemberRepository memberRepository;
    private AssessmentService service;

    @BeforeEach
    void setUp() {
        questionnaireRepository = mock(QuestionnaireRepository.class);
        questionRepository = mock(QuestionRepository.class);
        assessmentRepository = mock(AssessmentRepository.class);
        answerRepository = mock(AssessmentAnswerRepository.class);
        memberRepository = mock(ProjectGroupMemberRepository.class);
        service = new AssessmentService(questionnaireRepository, questionRepository, assessmentRepository, answerRepository, memberRepository);
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
        when(memberRepository.existsByProjectGroupProjectIdAndStudentId(any(), any())).thenReturn(true);

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
        when(memberRepository.existsByProjectGroupProjectIdAndStudentId(any(), any())).thenReturn(true);

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
        when(memberRepository.existsByProjectGroupProjectIdAndStudentId(any(), any())).thenReturn(true);

        AssessmentService.AnswerCommand cmd = new AssessmentService.AnswerCommand(UUID.randomUUID(), 4);

        assertThatThrownBy(() -> service.submitSelf(projectId, UUID.randomUUID(), List.of(cmd)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void submitSelf_rejectsSecondSubmission() {
        UUID projectId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.OPEN);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));
        Question question = new Question();
        question.setId(UUID.randomUUID());
        question.setQuestionnaire(q);
        when(questionRepository.findByQuestionnaireId(q.getId())).thenReturn(List.of(question));
        when(memberRepository.existsByProjectGroupProjectIdAndStudentId(projectId, studentId)).thenReturn(true);

        Assessment existing = new Assessment();
        existing.setSubmittedAt(java.time.Instant.now());
        when(assessmentRepository.findByQuestionnaireIdAndTypeAndAssessorStudentIdAndAssesseeStudentId(q.getId(), AssessmentType.SELF, studentId, studentId))
                .thenReturn(Optional.of(existing));

        AssessmentService.AnswerCommand cmd = new AssessmentService.AnswerCommand(question.getId(), 4);

        assertThatThrownBy(() -> service.submitSelf(projectId, studentId, List.of(cmd)))
                .isInstanceOf(AssessmentAlreadySubmittedException.class);
    }

    @Test
    void submitPeer_rejectsSecondSubmission() {
        UUID projectId = UUID.randomUUID();
        UUID assessor = UUID.randomUUID();
        UUID assessee = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.OPEN);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));
        Question question = new Question();
        question.setId(UUID.randomUUID());
        question.setQuestionnaire(q);
        when(questionRepository.findByQuestionnaireId(q.getId())).thenReturn(List.of(question));
        when(memberRepository.existsByProjectGroupProjectIdAndStudentId(any(), any())).thenReturn(true);

        Assessment existing = new Assessment();
        existing.setSubmittedAt(java.time.Instant.now());
        when(assessmentRepository.findByQuestionnaireIdAndTypeAndAssessorStudentIdAndAssesseeStudentId(q.getId(), AssessmentType.PEER, assessor, assessee))
                .thenReturn(Optional.of(existing));

        AssessmentService.AnswerCommand cmd = new AssessmentService.AnswerCommand(question.getId(), 4);

        assertThatThrownBy(() -> service.submitPeer(projectId, assessor, assessee, List.of(cmd)))
                .isInstanceOf(AssessmentAlreadySubmittedException.class);
    }

    @Test
    void submitPeer_rejectsWhenNotOpen() {
        UUID projectId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.DRAFT);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));

        assertThatThrownBy(() -> service.submitPeer(projectId, UUID.randomUUID(), UUID.randomUUID(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void submitSelf_rejectsNonParticipant() {
        UUID projectId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        Questionnaire q = new Questionnaire();
        q.setId(UUID.randomUUID());
        q.setStatus(QuestionnaireStatus.OPEN);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(q));
        Question question = new Question();
        question.setId(UUID.randomUUID());
        question.setQuestionnaire(q);
        when(questionRepository.findByQuestionnaireId(q.getId())).thenReturn(List.of(question));
        when(memberRepository.existsByProjectGroupProjectIdAndStudentId(projectId, studentId)).thenReturn(false);

        AssessmentService.AnswerCommand cmd = new AssessmentService.AnswerCommand(question.getId(), 4);

        assertThatThrownBy(() -> service.submitSelf(projectId, studentId, List.of(cmd)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
