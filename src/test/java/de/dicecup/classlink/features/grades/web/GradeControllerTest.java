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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(GradeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GradeControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

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

        Random r = new Random();
        BigDecimal weight = BigDecimal.valueOf(r.nextInt(20)/ 20.0);

        SubjectAssignment assignment = new SubjectAssignment();
        assignment.setId(assignmentId);

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

        when(schoolClassRepository.findById(classId)).thenReturn(java.util.Optional.of(new SchoolClass()));
        when(termRepository.findById(termId)).thenReturn(java.util.Optional.of(new de.dicecup.classlink.features.terms.Term()));
        when(subjectRepository.findById(subjectId)).thenReturn(java.util.Optional.of(new de.dicecup.classlink.features.subjects.Subject()));
        when(teacherRepository.findById(teacherId)).thenReturn(java.util.Optional.of(new de.dicecup.classlink.features.users.domain.roles.Teacher()));
        when(finalGradeAssignmentRepository.findById(finalAssignmentId)).thenReturn(java.util.Optional.of(new de.dicecup.classlink.features.grades.FinalGradeAssignment()));
        when(assignmentManagementService.createAndSaveAssignment(any(), eq(classId), eq(termId), eq(subjectId), eq(teacherId), eq(finalAssignmentId), eq(weight))).thenReturn(assignment);
        when(finalGradeAssignmentRepository.save(assignment.getFinalGradeAssignment())).thenReturn(assignment.getFinalGradeAssignment());

        var request = new SubjectAssignmentRequest(
                name,
                assignment.getSchoolClass().getId(),
                assignment.getTerm().getId(),
                assignment.getSubject().getId(),
                assignment.getTeacher().getId(),
                assignment.getFinalGradeAssignment().getId(),
                assignment.getWeighting()
        );

        mockMvc.perform(post("/api/assignment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(assignmentId.toString())))
                .andReturn();
    }
}

