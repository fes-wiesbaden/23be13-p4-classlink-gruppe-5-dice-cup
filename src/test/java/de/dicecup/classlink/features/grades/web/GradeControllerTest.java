package de.dicecup.classlink.features.grades.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.grades.*;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import de.dicecup.classlink.features.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        assignment.setSchoolClass(new SchoolClass());
        assignment.getSchoolClass().setId(classId);
        assignment.setTerm(new de.dicecup.classlink.features.terms.Term());
        assignment.getTerm().setId(termId);
        assignment.setTeacher(new de.dicecup.classlink.features.users.domain.roles.Teacher());
        assignment.getTeacher().setId(teacherId);
        assignment.setSubject(new de.dicecup.classlink.features.subjects.Subject());
        assignment.getSubject().setId(subjectId);
        assignment.setFinalGradeAssignment(new de.dicecup.classlink.features.grades.FinalGradeAssignment());
        assignment.getFinalGradeAssignment().setId(finalAssignmentId);
        assignment.setWeighting(weight);

        when(assignmentManagementService.createAndSaveAssignment(any(), eq(classId), eq(termId), eq(subjectId), eq(teacherId), eq(finalAssignmentId), eq(weight))).thenReturn(assignment);

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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(assignmentId.toString())));
    }
}
