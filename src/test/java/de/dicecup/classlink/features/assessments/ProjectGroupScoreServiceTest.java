package de.dicecup.classlink.features.assessments;

import de.dicecup.classlink.features.assessments.dto.ProjectGroupStudentScoreOverviewDto;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.grades.SubjectAssignment;
import de.dicecup.classlink.features.grades.SubjectAssignmentRepository;
import de.dicecup.classlink.features.assessments.Assessment;
import de.dicecup.classlink.features.assessments.AssessmentAnswer;
import de.dicecup.classlink.features.assessments.AssessmentType;
import de.dicecup.classlink.features.assessments.Question;
import de.dicecup.classlink.features.assessments.Questionnaire;
import de.dicecup.classlink.features.projects.Project;
import de.dicecup.classlink.features.projects.ProjectGroup;
import de.dicecup.classlink.features.projects.ProjectGroupMember;
import de.dicecup.classlink.features.projects.ProjectGroupMemberRepository;
import de.dicecup.classlink.features.projects.ProjectGroupRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.UserInfo;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectGroupScoreServiceTest {

    @Mock
    private AuthHelper authHelper;
    @Mock
    private ProjectGroupRepository projectGroupRepository;
    @Mock
    private ProjectGroupMemberRepository memberRepository;
    @Mock
    private SubjectAssignmentRepository assignmentRepository;
    @Mock
    private QuestionnaireRepository questionnaireRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;
    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private ProjectGroupScoreService service;

    @Test
    void listGroupScores_deniesWhenTeacherNotAssigned() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        when(projectGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.listGroupScores(groupId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void listGroupScores_returnsStudents() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        group.setId(groupId);
        Student student = buildStudent("Alice", "Anderson", group.getProject().getSchoolClass());
        ProjectGroupMember member = new ProjectGroupMember();
        member.setStudent(student);
        when(projectGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(group.getProject().getSchoolClass());
        assignment.setTerm(group.getProject().getTerm());
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));
        when(memberRepository.findByProjectGroupId(groupId)).thenReturn(List.of(member));
        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(UUID.randomUUID());
        questionnaire.setProjectId(group.getProject().getId());
        when(questionnaireRepository.findByProjectId(group.getProject().getId())).thenReturn(java.util.Optional.of(questionnaire));
        when(assessmentAnswerRepository.findSubmittedByQuestionnaire(questionnaire.getId())).thenReturn(List.of());

        List<ProjectGroupStudentScoreOverviewDto> result = service.listGroupScores(groupId);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().studentName()).contains("Alice");
    }

    @Test
    void subjectScores_deniesWhenTeacherNotAssigned() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        when(projectGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(authHelper.requireUserId()).thenReturn(teacherId);
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of());

        assertThatThrownBy(() -> service.subjectScores(groupId, UUID.randomUUID(), null))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void subjectScores_aggregatesSelfAndPeerIgnoringInactiveQuestions() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        when(projectGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(authHelper.requireUserId()).thenReturn(teacherId);
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setSchoolClass(group.getProject().getSchoolClass());
        assignment.setTerm(group.getProject().getTerm());
        when(assignmentRepository.findByTeacherId(teacherId)).thenReturn(List.of(assignment));

        UUID studentId = UUID.randomUUID();
        Student student = buildStudent("Bob", "Baker", group.getProject().getSchoolClass());
        student.setId(studentId);
        when(studentRepository.findById(studentId)).thenReturn(java.util.Optional.of(student));

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(UUID.randomUUID());
        questionnaire.setProjectId(group.getProject().getId());
        when(questionnaireRepository.findByProjectId(group.getProject().getId())).thenReturn(java.util.Optional.of(questionnaire));

        Question q1 = new Question();
        q1.setId(UUID.randomUUID());
        q1.setPosition(1);
        q1.setText("Active");
        q1.setActive(true);
        Question q2 = new Question();
        q2.setId(UUID.randomUUID());
        q2.setPosition(2);
        q2.setText("Inactive");
        q2.setActive(false);
        when(questionRepository.findByQuestionnaireId(questionnaire.getId())).thenReturn(List.of(q1, q2));

        Assessment selfAssessment = new Assessment();
        selfAssessment.setType(AssessmentType.SELF);
        selfAssessment.setAssesseeStudentId(studentId);
        selfAssessment.setAssessorStudentId(studentId);
        selfAssessment.setQuestionnaire(questionnaire);
        AssessmentAnswer selfAnswer = new AssessmentAnswer();
        selfAnswer.setAssessment(selfAssessment);
        selfAnswer.setQuestion(q1);
        selfAnswer.setScore(4);

        Assessment peerAssessment = new Assessment();
        peerAssessment.setType(AssessmentType.PEER);
        peerAssessment.setAssesseeStudentId(studentId);
        peerAssessment.setAssessorStudentId(UUID.randomUUID());
        peerAssessment.setQuestionnaire(questionnaire);
        AssessmentAnswer peerAnswer1 = new AssessmentAnswer();
        peerAnswer1.setAssessment(peerAssessment);
        peerAnswer1.setQuestion(q1);
        peerAnswer1.setScore(2);
        AssessmentAnswer peerAnswer2 = new AssessmentAnswer();
        peerAnswer2.setAssessment(peerAssessment);
        peerAnswer2.setQuestion(q1);
        peerAnswer2.setScore(4);

        when(assessmentAnswerRepository.findSubmittedByQuestionnaireAndAssessee(questionnaire.getId(), studentId))
                .thenReturn(List.of(selfAnswer, peerAnswer1, peerAnswer2));

        var dto = service.subjectScores(groupId, studentId, null);

        assertThat(dto.subjects()).hasSize(1);
        var subjectScore = dto.subjects().getFirst();
        assertThat(subjectScore.questionId()).isEqualTo(q1.getId());
        assertThat(subjectScore.selfScore()).isEqualTo(4.0);
        assertThat(subjectScore.peerScoreAverage()).isEqualTo(3.0);
        assertThat(subjectScore.peerScoreCount()).isEqualTo(2);
        assertThat(subjectScore.tendency()).isEqualTo(de.dicecup.classlink.features.assessments.dto.SubjectScoreDto.Tendency.SELF_HIGHER_THAN_PEER);
    }

    @Test
    void subjectScores_allowsStudentToViewOwnDetails() {
        UUID groupId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        ProjectGroup group = buildGroup();
        when(projectGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(group));
        when(authHelper.requireUserId()).thenReturn(studentId);
        when(authHelper.requireTeacherId()).thenThrow(new AccessDeniedException("not a teacher"));

        Student student = buildStudent("Sam", "Student", group.getProject().getSchoolClass());
        student.setId(studentId);
        when(studentRepository.findById(studentId)).thenReturn(java.util.Optional.of(student));

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setId(UUID.randomUUID());
        questionnaire.setProjectId(group.getProject().getId());
        when(questionnaireRepository.findByProjectId(group.getProject().getId())).thenReturn(java.util.Optional.of(questionnaire));

        Question question = new Question();
        question.setId(UUID.randomUUID());
        question.setPosition(1);
        question.setText("Self view");
        question.setActive(true);
        when(questionRepository.findByQuestionnaireId(questionnaire.getId())).thenReturn(List.of(question));
        when(assessmentAnswerRepository.findSubmittedByQuestionnaireAndAssessee(questionnaire.getId(), studentId))
                .thenReturn(List.of());

        var dto = service.subjectScores(groupId, studentId, null);

        assertThat(dto.studentId()).isEqualTo(studentId);
        assertThat(dto.subjects()).hasSize(1);
    }

    private ProjectGroup buildGroup() {
        SchoolClass clazz = new SchoolClass();
        clazz.setId(UUID.randomUUID());
        clazz.setName("1A");
        Term term = new Term();
        term.setId(UUID.randomUUID());
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setSchoolClass(clazz);
        project.setTerm(term);
        project.setName("Project X");
        ProjectGroup group = new ProjectGroup();
        group.setProject(project);
        return group;
    }

    private Student buildStudent(String first, String last, SchoolClass clazz) {
        User user = new User();
        user.setId(UUID.randomUUID());
        UserInfo info = new UserInfo();
        info.setUser(user);
        info.setFirstName(first);
        info.setLastName(last);
        user.setUserInfo(info);
        Student student = new Student();
        student.setId(user.getId());
        student.setUser(user);
        student.setSchoolClass(clazz);
        return student;
    }
}
