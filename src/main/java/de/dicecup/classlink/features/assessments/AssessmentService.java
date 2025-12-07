package de.dicecup.classlink.features.assessments;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;

    @Transactional
    public Assessment submitSelf(UUID projectId, UUID studentId, List<AnswerCommand> answers) {
        Questionnaire questionnaire = questionnaireRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Questionnaire not found"));
        ensureOpen(questionnaire);

        Assessment existing = assessmentRepository.findByQuestionnaireIdAndTypeAndAssessorStudentIdAndAssesseeStudentId(
                questionnaire.getId(), AssessmentType.SELF, studentId, studentId
        ).orElse(null);
        if (existing != null) {
            assessmentAnswerRepository.deleteAll(existing.getAnswers());
        }
        Assessment assessment = existing != null ? existing : new Assessment();
        assessment.setQuestionnaire(questionnaire);
        assessment.setProjectId(projectId);
        assessment.setType(AssessmentType.SELF);
        assessment.setAssessorStudentId(studentId);
        assessment.setAssesseeStudentId(studentId);
        assessment.setSubmittedAt(Instant.now());
        assessment = assessmentRepository.save(assessment);
        final Assessment savedAssessment = assessment;

        Map<UUID, Question> questionMap = validateQuestions(questionnaire.getId(), answers);
        List<AssessmentAnswer> savedAnswers = answers.stream()
                .map(cmd -> toAnswer(savedAssessment, questionMap.get(cmd.getQuestionId()), cmd.getScore()))
                .toList();
        assessmentAnswerRepository.saveAll(savedAnswers);
        assessment.setAnswers(savedAnswers);
        return assessment;
    }

    @Transactional
    public Assessment submitPeer(UUID projectId, UUID assessorStudentId, UUID assesseeStudentId, List<AnswerCommand> answers) {
        if (assessorStudentId.equals(assesseeStudentId)) {
            throw new IllegalArgumentException("Assessor and assessee must differ for peer assessment");
        }
        Questionnaire questionnaire = questionnaireRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Questionnaire not found"));
        ensureOpen(questionnaire);

        Assessment existing = assessmentRepository.findByQuestionnaireIdAndTypeAndAssessorStudentIdAndAssesseeStudentId(
                questionnaire.getId(), AssessmentType.PEER, assessorStudentId, assesseeStudentId
        ).orElse(null);
        if (existing != null) {
            assessmentAnswerRepository.deleteAll(existing.getAnswers());
        }
        Assessment assessment = existing != null ? existing : new Assessment();
        assessment.setQuestionnaire(questionnaire);
        assessment.setProjectId(projectId);
        assessment.setType(AssessmentType.PEER);
        assessment.setAssessorStudentId(assessorStudentId);
        assessment.setAssesseeStudentId(assesseeStudentId);
        assessment.setSubmittedAt(Instant.now());
        assessment = assessmentRepository.save(assessment);
        final Assessment savedAssessment = assessment;

        Map<UUID, Question> questionMap = validateQuestions(questionnaire.getId(), answers);
        List<AssessmentAnswer> savedAnswers = answers.stream()
                .map(cmd -> toAnswer(savedAssessment, questionMap.get(cmd.getQuestionId()), cmd.getScore()))
                .toList();
        assessmentAnswerRepository.saveAll(savedAnswers);
        assessment.setAnswers(savedAnswers);
        return assessment;
    }

    private Map<UUID, Question> validateQuestions(UUID questionnaireId, List<AnswerCommand> answers) {
        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaireId);
        Map<UUID, Question> byId = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));
        for (AnswerCommand cmd : answers) {
            Question question = byId.get(cmd.getQuestionId());
            if (question == null || !question.isActive()) {
                throw new IllegalArgumentException("Invalid question: " + cmd.getQuestionId());
            }
            if (cmd.getScore() < 1 || cmd.getScore() > 6) {
                throw new IllegalArgumentException("Score must be between 1 and 6");
            }
        }
        return byId;
    }

    private void ensureOpen(Questionnaire questionnaire) {
        if (questionnaire.getStatus() != QuestionnaireStatus.OPEN) {
            throw new IllegalStateException("Questionnaire not open");
        }
    }

    private AssessmentAnswer toAnswer(Assessment assessment, Question question, int score) {
        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setAssessment(assessment);
        answer.setQuestion(question);
        answer.setScore(score);
        return answer;
    }

    @Data
    @AllArgsConstructor
    public static class AnswerCommand {
        private UUID questionId;
        private int score;
    }
}
