package de.dicecup.classlink.features.users.domain.roles;

import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.classes.SchoolClassRepository;
import de.dicecup.classlink.features.users.UserRepository;
import de.dicecup.classlink.features.users.domain.User;
import de.dicecup.classlink.features.users.domain.roles.Student;
import de.dicecup.classlink.persistence.DbSliceTestBase;
import de.dicecup.classlink.testdata.TestDataConfig;
import de.dicecup.classlink.testdata.UserTestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestDataConfig.class)
class StudentRepositoryTest extends DbSliceTestBase {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SchoolClassRepository classRepository;
    @Autowired
    private UserTestData userTestData;

    @Test
    void findByClassId_returnsOnlyStudentsOfClass() {
        SchoolClass classA = new SchoolClass();
        classA.setName("Class A");
        classA = classRepository.save(classA);

        SchoolClass classB = new SchoolClass();
        classB.setName("Class B");
        classB = classRepository.save(classB);

        User userA = userRepository.save(userTestData.userWithInfo());
        Student studentA = new Student();
        studentA.setUser(userA);
        studentA.setSchoolClass(classA);
        userA.setStudent(studentA);
        studentRepository.save(studentA);

        User userB = userRepository.save(userTestData.userWithInfo());
        Student studentB = new Student();
        studentB.setUser(userB);
        studentB.setSchoolClass(classB);
        userB.setStudent(studentB);
        studentRepository.save(studentB);

        List<Student> result = studentRepository.findByClassId(classA.getId());

        assertThat(result).extracting(Student::getId).containsExactly(studentA.getId());
    }
}
