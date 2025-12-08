package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.assessments.dto.ProjectGroupStudentScoreOverviewDto;
import de.dicecup.classlink.features.assessments.dto.StudentSubjectAssessmentDto;
import de.dicecup.classlink.features.assessments.dto.SubjectScoreDto;
import de.dicecup.classlink.features.assessments.dto.SubjectScoreDto.Tendency;
import de.dicecup.classlink.features.classes.ClassSubjectAssignmentRepository;
import de.dicecup.classlink.features.assessments.AssessmentAnswer;
import de.dicecup.classlink.features.assessments.AssessmentType;
import de.dicecup.classlink.features.assessments.Question;
import de.dicecup.classlink.features.assessments.Questionnaire;
import de.dicecup.classlink.features.projects.ProjectGroup;
import de.dicecup.classlink.features.projects.ProjectGroupMember;
import de.dicecup.classlink.features.projects.ProjectGroupMemberRepository;
import de.dicecup.classlink.features.projects.ProjectGroupRepository;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProjectGroupScoreService {

    private final AuthHelper authHelper;
    private final ProjectGroupRepository projectGroupRepository;
    private final ProjectGroupMemberRepository memberRepository;
    private final ClassSubjectAssignmentRepository assignmentRepository;
    private final QuestionnaireRepository questionnaireRepository;
    private final QuestionRepository questionRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final StudentRepository studentRepository;

    public ProjectGroupScoreService(AuthHelper authHelper,
                                    ProjectGroupRepository projectGroupRepository,
                                    ProjectGroupMemberRepository memberRepository,
                                    ClassSubjectAssignmentRepository assignmentRepository,
                                    QuestionnaireRepository questionnaireRepository,
                                    QuestionRepository questionRepository,
                                    AssessmentAnswerRepository assessmentAnswerRepository,
                                    StudentRepository studentRepository) {
        this.authHelper = authHelper;
        this.projectGroupRepository = projectGroupRepository;
        this.memberRepository = memberRepository;
        this.assignmentRepository = assignmentRepository;
        this.questionnaireRepository = questionnaireRepository;
        this.questionRepository = questionRepository;
        this.assessmentAnswerRepository = assessmentAnswerRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectGroupStudentScoreOverviewDto> listGroupScores(UUID projectGroupId) {
        ProjectGroup group = loadGroup(projectGroupId);
        UUID teacherId = authHelper.requireTeacherId();
        ensureTeacherAssigned(teacherId, group);

        List<ProjectGroupMember> members = memberRepository.findByProjectGroupId(projectGroupId);
        if (members.isEmpty()) {
            return List.of();
        }

        Questionnaire questionnaire = questionnaireRepository.findByProjectId(group.getProject().getId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Questionnaire not found"));
        List<AssessmentAnswer> answers = assessmentAnswerRepository.findSubmittedByQuestionnaire(questionnaire.getId());

        Map<UUID, List<AssessmentAnswer>> selfAnswers = answers.stream()
                .filter(a -> a.getAssessment().getType() == AssessmentType.SELF)
                .collect(Collectors.groupingBy(a -> a.getAssessment().getAssesseeStudentId()));
        Map<UUID, List<AssessmentAnswer>> peerAnswers = answers.stream()
                .filter(a -> a.getAssessment().getType() == AssessmentType.PEER)
                .collect(Collectors.groupingBy(a -> a.getAssessment().getAssesseeStudentId()));

        return members.stream()
                .map(member -> toOverviewDto(group, member.getStudent(), selfAnswers, peerAnswers))
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentSubjectAssessmentDto subjectScores(UUID projectGroupId, UUID studentId, UUID questionnaireId) {
        ProjectGroup group = loadGroup(projectGroupId);
        UUID currentUserId = authHelper.requireUserId();

        boolean isTeacher = false;
        try {
            UUID teacherId = authHelper.requireTeacherId();
            ensureTeacherAssigned(teacherId, group);
            isTeacher = true;
        } catch (AccessDeniedException ignored) {
            // fall through to student check
        }

        if (!isTeacher && !studentId.equals(currentUserId)) {
            throw new AccessDeniedException("Not allowed to view assessments");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Student not found"));
        if (!group.getProject().getSchoolClass().getId().equals(student.getClazz().getId())) {
            throw new AccessDeniedException("Student not in project class");
        }

        Questionnaire questionnaire = resolveQuestionnaire(group, questionnaireId);
        List<Question> questions = questionRepository.findByQuestionnaireId(questionnaire.getId()).stream()
                .filter(Question::isActive)
                .sorted(Comparator.comparingInt(Question::getPosition))
                .toList();

        List<AssessmentAnswer> answers = assessmentAnswerRepository.findSubmittedByQuestionnaireAndAssessee(questionnaire.getId(), studentId);
        Map<UUID, List<AssessmentAnswer>> selfByQuestion = answers.stream()
                .filter(a -> a.getAssessment().getType() == AssessmentType.SELF
                        && a.getAssessment().getAssessorStudentId().equals(studentId))
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));
        Map<UUID, List<AssessmentAnswer>> peerByQuestion = answers.stream()
                .filter(a -> a.getAssessment().getType() == AssessmentType.PEER)
                .collect(Collectors.groupingBy(a -> a.getQuestion().getId()));

        List<SubjectScoreDto> fields = questions.stream()
                .map(q -> toSubjectScore(q, selfByQuestion, peerByQuestion))
                .toList();

        return new StudentSubjectAssessmentDto(
                student.getId(),
                extractName(student),
                projectGroupId,
                group.getProject().getName(),
                questionnaire.getId(),
                fields
        );
    }

    private Questionnaire resolveQuestionnaire(ProjectGroup group, UUID questionnaireId) {
        if (questionnaireId != null) {
            Questionnaire questionnaire = questionnaireRepository.findById(questionnaireId)
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Questionnaire not found"));
            if (!questionnaire.getProjectId().equals(group.getProject().getId())) {
                throw new AccessDeniedException("Questionnaire does not belong to project");
            }
            return questionnaire;
        }
        return questionnaireRepository.findByProjectId(group.getProject().getId())
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Questionnaire not found"));
    }

    private SubjectScoreDto toSubjectScore(Question question,
                                                  Map<UUID, List<AssessmentAnswer>> selfByQuestion,
                                                  Map<UUID, List<AssessmentAnswer>> peerByQuestion) {
        Double selfScore = average(selfByQuestion.get(question.getId()));
        List<AssessmentAnswer> peerAnswers = peerByQuestion.getOrDefault(question.getId(), List.of());
        Double peerScore = average(peerAnswers);
        int peerCount = peerAnswers.size();

        Tendency tendency = Tendency.ALIGNED;
        if (selfScore != null && peerScore != null) {
            if (selfScore > peerScore) {
                tendency = Tendency.SELF_HIGHER_THAN_PEER;
            } else if (selfScore < peerScore) {
                tendency = Tendency.SELF_LOWER_THAN_PEER;
            }
        }

        return new SubjectScoreDto(
                question.getId(),
                question.getPosition(),
                "q-" + question.getPosition(),
                question.getText(),
                null, // teacher question-level score not available
                selfScore,
                peerScore,
                peerCount,
                tendency
        );
    }

    private ProjectGroupStudentScoreOverviewDto toOverviewDto(ProjectGroup group,
                                                              Student student,
                                                              Map<UUID, List<AssessmentAnswer>> selfAnswers,
                                                              Map<UUID, List<AssessmentAnswer>> peerAnswers) {
        Double selfScore = average(selfAnswers.get(student.getId()));
        Double peerScore = average(peerAnswers.get(student.getId()));
        Double teacherScore = null; // see IMPORTANT RULE about grade logic

        ProjectGroupStudentScoreOverviewDto.Tendency tendency = ProjectGroupStudentScoreOverviewDto.Tendency.ALIGNED;
        if (teacherScore != null && selfScore != null) {
            if (selfScore > teacherScore) {
                tendency = ProjectGroupStudentScoreOverviewDto.Tendency.SELF_HIGHER_THAN_TEACHER;
            } else if (selfScore < teacherScore) {
                tendency = ProjectGroupStudentScoreOverviewDto.Tendency.SELF_LOWER_THAN_TEACHER;
            }
        } else if (teacherScore != null && peerScore != null) {
            if (peerScore > teacherScore) {
                tendency = ProjectGroupStudentScoreOverviewDto.Tendency.PEER_HIGHER_THAN_TEACHER;
            } else if (peerScore < teacherScore) {
                tendency = ProjectGroupStudentScoreOverviewDto.Tendency.PEER_LOWER_THAN_TEACHER;
            }
        }

        return new ProjectGroupStudentScoreOverviewDto(
                student.getId(),
                extractName(student),
                student.getClazz() != null ? student.getClazz().getId() : null,
                student.getClazz() != null ? student.getClazz().getName() : null,
                group.getId(),
                group.getProject().getName(),
                teacherScore,
                selfScore,
                peerScore,
                tendency
        );
    }

    private ProjectGroup loadGroup(UUID projectGroupId) {
        return projectGroupRepository.findById(projectGroupId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Project group not found"));
    }

    private void ensureTeacherAssigned(UUID teacherId, ProjectGroup group) {
        UUID classId = group.getProject().getSchoolClass().getId();
        UUID termId = group.getProject().getTerm().getId();
        boolean allowed = assignmentRepository.findByTeacherId(teacherId).stream()
                .anyMatch(a -> a.getSchoolClass() != null && a.getTerm() != null
                        && classId.equals(a.getSchoolClass().getId())
                        && termId.equals(a.getTerm().getId()));
        if (!allowed) {
            throw new AccessDeniedException("Teacher not assigned to class/term");
        }
    }

    private Double average(List<AssessmentAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }
        return answers.stream()
                .mapToInt(AssessmentAnswer::getScore)
                .average()
                .orElse(Double.NaN);
    }

    private String extractName(Student student) {
        return Optional.ofNullable(student.getUser())
                .map(u -> {
                    UserInfo info = u.getUserInfo();
                    if (info == null) {
                        return null;
                    }
                    String first = info.getFirstName();
                    String last = info.getLastName();
                    if (first == null && last == null) {
                        return null;
                    }
                    if (first == null) {
                        return last;
                    }
                    if (last == null) {
                        return first;
                    }
                    return first + " " + last;
                })
                .orElse(null);
    }
}
