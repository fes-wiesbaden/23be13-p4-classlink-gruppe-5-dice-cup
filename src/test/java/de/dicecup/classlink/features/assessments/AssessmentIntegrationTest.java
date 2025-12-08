package de.dicecup.classlink.features.assessments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.assessments.dto.AssessmentAnswerDto;
import de.dicecup.classlink.features.assessments.dto.AssessmentSubmissionRequest;
import de.dicecup.classlink.features.assessments.Questionnaire;
import de.dicecup.classlink.features.assessments.QuestionnaireStatus;
import de.dicecup.classlink.features.assessments.Question;
import de.dicecup.classlink.features.classes.Class;
import de.dicecup.classlink.features.classes.ClassRepository;
import de.dicecup.classlink.features.projects.Project;
import de.dicecup.classlink.features.projects.ProjectGroup;
import de.dicecup.classlink.features.projects.ProjectGroupMember;
import de.dicecup.classlink.features.projects.ProjectGroupMemberRepository;
import de.dicecup.classlink.features.projects.ProjectGroupRepository;
import de.dicecup.classlink.features.projects.ProjectRepository;
import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.schoolyear.SchoolYearRepository;
import de.dicecup.classlink.features.schoolyear.SchoolYearStatus;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
import de.dicecup.classlink.features.terms.TermStatus;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
class AssessmentIntegrationTest extends IntegrationTestBase {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;
    @Autowired
    ClassRepository classRepository;
    @Autowired
    SchoolYearRepository schoolYearRepository;
    @Autowired
    TermRepository termRepository;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectGroupRepository projectGroupRepository;
    @Autowired
    ProjectGroupMemberRepository projectGroupMemberRepository;
    @Autowired
    QuestionnaireRepository questionnaireRepository;
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    AssessmentRepository assessmentRepository;
    @Autowired
    AssessmentAnswerRepository assessmentAnswerRepository;

    @MockBean
    AuthHelper authHelper;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    private UUID teacherId;
    private UUID studentAId;
    private UUID studentBId;
    private UUID projectId;

    @BeforeEach
    void setUpData() {
        User teacherUser = createUserWithRole(true);
        Teacher teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacherUser.setTeacher(teacher);
        User savedTeacher = userRepository.save(teacherUser);
        teacherId = savedTeacher.getId();

        User studentAUser = createUserWithRole(false);
        Student studentA = new Student();
        studentA.setUser(studentAUser);
        studentAUser.setStudent(studentA);
        User savedA = userRepository.save(studentAUser);
        studentAId = savedA.getId();

        User studentBUser = createUserWithRole(false);
        Student studentB = new Student();
        studentB.setUser(studentBUser);
        studentBUser.setStudent(studentB);
        User savedB = userRepository.save(studentBUser);
        studentBId = savedB.getId();

        Class schoolClass = new Class();
        schoolClass.setName("1A");
        classRepository.save(schoolClass);

        SchoolYear schoolYear = new SchoolYear();
        schoolYear.setName("2024/25");
        schoolYear.setStartDate(LocalDate.now());
        schoolYear.setEndDate(LocalDate.now().plusMonths(10));
        schoolYear.setStatus(SchoolYearStatus.ACTIVE);
        schoolYearRepository.save(schoolYear);

        Term term = new Term();
        term.setName("T1");
        term.setStatus(TermStatus.OPEN);
        term.setSchoolYear(schoolYear);
        term.setStartDate(LocalDate.now());
        term.setEndDate(LocalDate.now().plusMonths(4));
        termRepository.save(term);

        Project project = new Project();
        project.setName("Project");
        project.setDescription("Desc");
        project.setSchoolClass(schoolClass);
        project.setTerm(term);
        project.setCreatedAt(Instant.now());
        project.setUpdatedAt(Instant.now());
        project.setActive(true);
        projectRepository.save(project);
        projectId = project.getId();

        ProjectGroup group = new ProjectGroup();
        group.setProject(project);
        group.setGroupNumber(1);
        group.setSupervisingTeacher(teacher);
        group.setCreatedAt(Instant.now());
        group.setUpdatedAt(Instant.now());
        projectGroupRepository.save(group);

        ProjectGroupMember memberA = new ProjectGroupMember();
        memberA.setProjectGroup(group);
        memberA.setStudent(studentA);
        memberA.setCreatedAt(Instant.now());
        memberA.setUpdatedAt(Instant.now());
        projectGroupMemberRepository.save(memberA);

        ProjectGroupMember memberB = new ProjectGroupMember();
        memberB.setProjectGroup(group);
        memberB.setStudent(studentB);
        memberB.setCreatedAt(Instant.now());
        memberB.setUpdatedAt(Instant.now());
        projectGroupMemberRepository.save(memberB);
    }

    @Test
    @WithMockUser(roles = {"TEACHER", "STUDENT"})
    void fullFlow_enforcesSingleSubmissionAndAggregates() throws Exception {
        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.requireTeacherId()).thenReturn(teacherId);
        when(authHelper.requireStudentId()).thenReturn(studentAId, studentBId, studentBId);

        Questionnaire questionnaire = new Questionnaire();
        questionnaire.setProjectId(projectId);
        questionnaire.setCreatedByTeacherId(teacherId);
        questionnaire.setStatus(QuestionnaireStatus.OPEN);
        questionnaireRepository.save(questionnaire);

        Question question = new Question();
        question.setQuestionnaire(questionnaire);
        question.setPosition(1);
        question.setText("Q1");
        question.setActive(true);
        questionRepository.save(question);
        UUID questionnaireId = questionnaire.getId();
        UUID questionId = question.getId();

        // Student A submits self
        AssessmentSubmissionRequest selfRequest = new AssessmentSubmissionRequest(
                List.of(new AssessmentAnswerDto(questionId, 4))
        );
        mockMvc.perform(post("/api/projects/" + projectId + "/assessments/self")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(selfRequest)))
                .andExpect(status().isOk());

        // Student B submits peer for A
        AssessmentSubmissionRequest peerRequest = new AssessmentSubmissionRequest(
                List.of(new AssessmentAnswerDto(questionId, 2))
        );
        mockMvc.perform(post("/api/projects/" + projectId + "/assessments/peer/" + studentAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(peerRequest)))
                .andExpect(status().isOk());

        // Second peer submission rejected
        MvcResult conflictResult = mockMvc.perform(post("/api/projects/" + projectId + "/assessments/peer/" + studentAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(peerRequest)))
                .andReturn();
        assertThat(conflictResult.getResponse().getStatus()).isEqualTo(409);

        // Overview as teacher
        String url = "/api/projects/" + projectId + "/assessment-overview?studentIds=" + studentAId + "&studentIds=" + studentBId;
        MvcResult overviewResult = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode overview = objectMapper.readTree(overviewResult.getResponse().getContentAsByteArray());
        JsonNode students = overview.get("students");
        JsonNode studentA = students.get(0).get("studentId").asText().equals(studentAId.toString()) ? students.get(0) : students.get(1);
        assertThat(studentA.get("selfAvg").asDouble()).isEqualTo(4.0);
        assertThat(studentA.get("peerAvg").asDouble()).isEqualTo(2.0);
        assertThat(studentA.get("combinedAvg").asDouble()).isEqualTo(3.0);
        assertThat(studentA.get("questions").get(0).get("peerAvgScore").asDouble()).isEqualTo(2.0);
        assertThat(studentA.get("questions").get(0).get("selfScore").asInt()).isEqualTo(4);
    }

    private User createUserWithRole(boolean teacher) {
        User user = new User();
        user.setUsername(UUID.randomUUID().toString());
        user.setPasswordHash("pw");
        user.setEnabled(true);
        if (teacher) {
            Teacher t = new Teacher();
            t.setUser(user);
            user.setTeacher(t);
        } else {
            Student s = new Student();
            s.setUser(user);
            user.setStudent(s);
        }
        return user;
    }
}
