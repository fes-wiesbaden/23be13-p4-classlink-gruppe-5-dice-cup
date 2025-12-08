package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.assessments.dto.ProjectAssessmentOverviewDTO;
import de.dicecup.classlink.features.assessments.dto.StudentAssessmentDetailDTO;
import de.dicecup.classlink.features.assessments.dto.StudentQuestionAssessmentDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssessmentAggregationService {

    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;

    @Transactional(readOnly = true)
    public ProjectAssessmentOverviewDTO buildOverview(UUID projectId,
                                                      Set<UUID> studentIds,
                                                      Map<UUID, Double> teacherGrades,
                                                      Map<UUID, String> studentNames) {
        Questionnaire questionnaire = questionnaireRepository.findByProjectId(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Questionnaire not found"));

        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaire.getId())
                .stream()
                .filter(Question::isActive)
                .sorted(Comparator.comparingInt(Question::getPosition))
                .toList();

        List<AssessmentAnswer> answers = assessmentAnswerRepository.findSubmittedByQuestionnaire(questionnaire.getId());

        Map<UUID, List<AssessmentAnswer>> selfByStudent = answers.stream()
                .filter(a -> a.getAssessment().getType() == AssessmentType.SELF)
                .filter(a -> a.getAssessment().getAssesseeStudentId().equals(a.getAssessment().getAssessorStudentId()))
                .collect(Collectors.groupingBy(a -> a.getAssessment().getAssesseeStudentId()));

        Map<UUID, List<AssessmentAnswer>> peerByAssessee = answers.stream()
                .filter(a -> a.getAssessment().getType() == AssessmentType.PEER)
                .collect(Collectors.groupingBy(a -> a.getAssessment().getAssesseeStudentId()));

        List<StudentAssessmentDetailDTO> students = new ArrayList<>();
        for (UUID studentId : studentIds) {
            List<AssessmentAnswer> selfAnswers = selfByStudent.getOrDefault(studentId, List.of());
            List<AssessmentAnswer> peerAnswers = peerByAssessee.getOrDefault(studentId, List.of());

            Double selfAvg = average(selfAnswers);
            Double peerAvg = average(peerAnswers);
            Double combinedAvg = combine(selfAvg, peerAvg);

            Double teacherGrade = teacherGrades != null ? teacherGrades.get(studentId) : null;
            TendencyLabel tendencyLabel = null;
            Double delta = null;
            if (teacherGrade != null && combinedAvg != null) {
                delta = combinedAvg - teacherGrade;
                if (delta >= 0.5) {
                    tendencyLabel = TendencyLabel.OVERESTIMATES;
                } else if (delta <= -0.5) {
                    tendencyLabel = TendencyLabel.UNDERESTIMATES;
                } else {
                    tendencyLabel = TendencyLabel.ALIGNED;
                }
            }

            Map<UUID, List<AssessmentAnswer>> selfByQuestion = selfAnswers.stream()
                    .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));
            Map<UUID, List<AssessmentAnswer>> peerByQuestion = peerAnswers.stream()
                    .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

            List<StudentQuestionAssessmentDTO> questionDtos = questions.stream()
                    .map(q -> {
                        Integer selfScore = selfByQuestion.getOrDefault(q.getId(), List.of()).stream()
                                .findFirst()
                                .map(AssessmentAnswer::getScore)
                                .orElse(null);
                        Double peerAvgScore = average(peerByQuestion.getOrDefault(q.getId(), List.of()));
                        return new StudentQuestionAssessmentDTO(q.getId(), q.getText(), selfScore, peerAvgScore);
                    })
                    .toList();

            String studentName = studentNames != null ? studentNames.get(studentId) : null;
            students.add(new StudentAssessmentDetailDTO(
                    studentId,
                    studentName,
                    selfAvg,
                    peerAvg,
                    combinedAvg,
                    teacherGrade,
                    delta,
                    tendencyLabel,
                    questionDtos
            ));
        }

        return new ProjectAssessmentOverviewDTO(projectId, students);
    }

    private Double average(Collection<AssessmentAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }
        return answers.stream()
                .mapToInt(AssessmentAnswer::getScore)
                .average()
                .orElse(Double.NaN);
    }

    private Double combine(Double selfAvg, Double peerAvg) {
        if (selfAvg == null && peerAvg == null) {
            return null;
        }
        if (selfAvg != null && peerAvg != null) {
            return (selfAvg + peerAvg) / 2.0;
        }
        return selfAvg != null ? selfAvg : peerAvg;
    }
}
