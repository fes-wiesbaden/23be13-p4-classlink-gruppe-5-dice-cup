package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.assessments.dto.ProjectAssessmentOverviewDTO;
import de.dicecup.classlink.features.assessments.dto.StudentAssessmentDetailDTO;
import de.dicecup.classlink.features.assessments.dto.StudentQuestionAssessmentDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssessmentAggregationServiceTest {

    private QuestionnaireRepository questionnaireRepository;
    private QuestionRepository questionRepository;
    private AssessmentAnswerRepository assessmentAnswerRepository;
    private AssessmentAggregationService service;

    @BeforeEach
    void setUp() {
        questionnaireRepository = mock(QuestionnaireRepository.class);
        questionRepository = mock(QuestionRepository.class);
        assessmentAnswerRepository = mock(AssessmentAnswerRepository.class);
        service = new AssessmentAggregationService(questionnaireRepository, questionRepository, assessmentAnswerRepository);
    }

    @Test
    void aggregatesSelfAndPeerAndTendency() {
        UUID projectId = UUID.randomUUID();
        UUID questionnaireId = UUID.randomUUID();
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(questionnaireId);
        questionnaire.setProjectId(projectId);
        when(questionnaireRepository.findByProjectId(projectId)).thenReturn(Optional.of(questionnaire));

        Question q1 = new Question();
        q1.setId(UUID.randomUUID());
        q1.setText("Q1");
        q1.setQuestionnaire(questionnaire);
        Question q2 = new Question();
        q2.setId(UUID.randomUUID());
        q2.setText("Q2");
        q2.setQuestionnaire(questionnaire);
        when(questionRepository.findByQuestionnaireId(questionnaireId)).thenReturn(List.of(q1, q2));

        UUID studentA = UUID.randomUUID();
        UUID studentB = UUID.randomUUID();

        // Self answers for student A: q1=4, q2=6
        AssessmentAnswer selfA1 = answer(questionnaireId, projectId, AssessmentType.SELF, studentA, studentA, q1, 4);
        AssessmentAnswer selfA2 = answer(questionnaireId, projectId, AssessmentType.SELF, studentA, studentA, q2, 6);
        // Peer answers for student A from student B: q1=5, q2=3
        AssessmentAnswer peerA1 = answer(questionnaireId, projectId, AssessmentType.PEER, studentB, studentA, q1, 5);
        AssessmentAnswer peerA2 = answer(questionnaireId, projectId, AssessmentType.PEER, studentB, studentA, q2, 3);

        // Self answers for student B: q1=2
        AssessmentAnswer selfB1 = answer(questionnaireId, projectId, AssessmentType.SELF, studentB, studentB, q1, 2);

        when(assessmentAnswerRepository.findSubmittedByQuestionnaire(questionnaireId))
                .thenReturn(List.of(selfA1, selfA2, peerA1, peerA2, selfB1));

        Set<UUID> students = Set.of(studentA, studentB);
        Map<UUID, Double> teacherGrades = Map.of(studentA, 4.0);

        ProjectAssessmentOverviewDTO overview = service.buildOverview(projectId, students, teacherGrades, Map.of());

        assertThat(overview.students()).hasSize(2);
        StudentAssessmentDetailDTO a = overview.students().stream().filter(s -> s.studentId().equals(studentA)).findFirst().orElseThrow();
        assertThat(a.selfAvg()).isEqualTo((4 + 6) / 2.0);
        assertThat(a.peerAvg()).isEqualTo((5 + 3) / 2.0);
        assertThat(a.combinedAvg()).isEqualTo((a.selfAvg() + a.peerAvg()) / 2.0);
        assertThat(a.teacherGrade()).isEqualTo(4.0);
        assertThat(a.delta()).isEqualTo(a.combinedAvg() - 4.0);
        assertThat(a.tendencyLabel()).isNotNull();
        List<StudentQuestionAssessmentDTO> questions = a.questions();
        assertThat(questions).hasSize(2);
        assertThat(questions.stream().filter(q -> q.questionId().equals(q1.getId())).findFirst().orElseThrow().selfScore()).isEqualTo(4);
        assertThat(questions.stream().filter(q -> q.questionId().equals(q1.getId())).findFirst().orElseThrow().peerAvgScore()).isEqualTo(5.0);

        StudentAssessmentDetailDTO b = overview.students().stream().filter(s -> s.studentId().equals(studentB)).findFirst().orElseThrow();
        assertThat(b.selfAvg()).isEqualTo(2.0);
        assertThat(b.peerAvg()).isNull();
        assertThat(b.combinedAvg()).isEqualTo(2.0);
    }

    private AssessmentAnswer answer(UUID questionnaireId, UUID projectId, AssessmentType type, UUID assessor, UUID assessee, Question question, int score) {
        Assessment assessment = new Assessment();
        assessment.setId(UUID.randomUUID());
        Questionnaire q = new Questionnaire();
        q.setId(questionnaireId);
        assessment.setQuestionnaire(q);
        assessment.setProjectId(projectId);
        assessment.setType(type);
        assessment.setAssessorStudentId(assessor);
        assessment.setAssesseeStudentId(assessee);
        assessment.setSubmittedAt(Instant.now());

        AssessmentAnswer ans = new AssessmentAnswer();
        ans.setAssessment(assessment);
        ans.setQuestion(question);
        ans.setScore(score);
        return ans;
    }
}
