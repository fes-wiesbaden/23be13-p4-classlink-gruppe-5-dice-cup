package de.dicecup.classlink.features.grades.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.classes.SchoolClassRepository;
import de.dicecup.classlink.features.grades.*;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import de.dicecup.classlink.features.security.JwtService;
import de.dicecup.classlink.features.subjects.Subject;
import de.dicecup.classlink.features.subjects.SubjectRepository;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermRepository;
import de.dicecup.classlink.features.users.domain.roles.Teacher;
import de.dicecup.classlink.features.users.domain.roles.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal; import java.util.UUID;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GradeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GradeControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    GradeController gradeController;  // Add this - inject the actual controller

    @MockBean
    AssignmentManagementService assignmentManagementService;
    @MockBean
    FinalGradeRepository finalGradeRepository;
    @MockBean
    GradeRepository gradeRepository;
    @MockBean
    GradeManagementService gradeManagementService;
    @MockBean
    SchoolClassRepository schoolClassRepository;
    @MockBean
    TermRepository termRepository;
    @MockBean
    SubjectRepository subjectRepository;
    @MockBean
    TeacherRepository teacherRepository;
    @MockBean
    FinalGradeAssignmentRepository finalGradeAssignmentRepository;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    JwtService jwtService;

    @Test
    void createAssignment_returnsCreated() throws Exception {
        String name = UUID.randomUUID().toString();
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        UUID finalAssignmentId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();

        BigDecimal weight = BigDecimal.ONE;

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setId(assignmentId);
        assignment.setName(name);

        SchoolClass schoolClass = new SchoolClass();
        schoolClass.setId(classId);
        assignment.setSchoolClass(schoolClass);

        Term term = new Term();
        term.setId(termId);
        assignment.setTerm(term);

        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        assignment.setTeacher(teacher);

        Subject subject = new Subject();
        subject.setId(subjectId);
        assignment.setSubject(subject);

        FinalGradeAssignment finalGradeAssignment = new FinalGradeAssignment();
        finalGradeAssignment.setId(finalAssignmentId);
        assignment.setFinalGradeAssignment(finalGradeAssignment);

        assignment.setWeighting(weight);

        // This is the key - the mock should now be properly wired
        when(assignmentManagementService.createAndSaveAssignment(
                any(String.class),
                any(UUID.class),
                any(UUID.class),
                any(UUID.class),
                any(UUID.class),
                any(UUID.class),
                any(BigDecimal.class)
        )).thenReturn(assignment);

        var request = new SubjectAssignmentRequest(
                name,
                classId,
                termId,
                subjectId,
                teacherId,
                finalAssignmentId,
                weight
        );

        mockMvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(assignmentId.toString())));
    }
}