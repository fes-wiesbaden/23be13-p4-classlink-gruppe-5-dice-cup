package de.dicecup.classlink.features.projects;

import de.dicecup.classlink.features.classes.Class;
import de.dicecup.classlink.features.classes.ClassTermStatus;
import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.features.users.domain.roles.StudentRepository;
import de.dicecup.classlink.persistence.IntegrationTestBase;
import de.dicecup.classlink.testdata.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectGroupServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ProjectGroupService projectGroupService;
    @Autowired
    private ProjectGroupRepository projectGroupRepository;
    @Autowired
    private ProjectGroupMemberRepository memberRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    void assignStudents_onlyAcceptsStudentsFromProjectClass() {
        SchoolYear year = testDataFactory.persistActiveSchoolYear("2025/26");
        Class classOne = testDataFactory.persistClass("C1");
        Class classTwo = testDataFactory.persistClass("C2");
        Term term = testDataFactory.persistOpenTerm("H1", year);
        testDataFactory.assign(classOne, term, ClassTermStatus.ACTIVE);
        Project project = projectService.createProject(
                classOne.getId(),
                term.getId(),
                new ProjectService.ProjectRequest("Project", "Desc", null)
        );
        ProjectGroup group = projectGroupService.createGroup(project.getId(), 1, null);

        Student validStudent = createStudent(classOne, "student1");
        Student invalidStudent = createStudent(classTwo, "student2");

        ProjectGroupService.MemberAssignment invalidAssignment = new ProjectGroupService.MemberAssignment(invalidStudent.getId(), MemberRole.MEMBER);
        ProjectGroupService.MemberAssignment validAssignment = new ProjectGroupService.MemberAssignment(validStudent.getId(), MemberRole.MEMBER);

        assertThatThrownBy(() -> projectGroupService.assignStudents(group.getId(), List.of(invalidAssignment, validAssignment)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(memberRepository.count()).isZero();
    }

    @Test
    @Transactional
    void assignStudents_succeedsForStudentsInProjectClass() {
        SchoolYear year = testDataFactory.persistActiveSchoolYear("2025/26");
        Class schoolClass = testDataFactory.persistClass("C1");
        Term term = testDataFactory.persistOpenTerm("H1", year);
        testDataFactory.assign(schoolClass, term, ClassTermStatus.ACTIVE);
        Project project = projectService.createProject(
                schoolClass.getId(),
                term.getId(),
                new ProjectService.ProjectRequest("Project", "Desc", null)
        );
        ProjectGroup group = projectGroupService.createGroup(project.getId(), 1, null);

        Student studentOne = createStudent(schoolClass, "student3");
        Student studentTwo = createStudent(schoolClass, "student4");

        ProjectGroupService.MemberAssignment assignment1 = new ProjectGroupService.MemberAssignment(studentOne.getId(), MemberRole.LEADER);
        ProjectGroupService.MemberAssignment assignment2 = new ProjectGroupService.MemberAssignment(studentTwo.getId(), MemberRole.MEMBER);

        projectGroupService.assignStudents(group.getId(), List.of(assignment1, assignment2));

        List<ProjectGroupMember> members = memberRepository.findAll();
        assertThat(members).hasSize(2);
        assertThat(members)
                .extracting(ProjectGroupMember::getStudent)
                .containsExactlyInAnyOrder(studentOne, studentTwo);
    }

    private Student createStudent(Class clazz, String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setEnabled(true);

        Student student = new Student();
        student.setUser(user);
        student.setClazz(clazz);
        user.setStudent(student);

        userRepository.save(user);

        return studentRepository.findById(user.getId()).orElseThrow();
    }
}
