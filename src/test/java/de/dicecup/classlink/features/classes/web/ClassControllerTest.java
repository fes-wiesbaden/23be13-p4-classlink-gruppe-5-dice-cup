package de.dicecup.classlink.features.classes.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.classes.Class;
import de.dicecup.classlink.features.classes.ClassController;
import de.dicecup.classlink.features.classes.ClassManagementService;
import de.dicecup.classlink.features.classes.ClassRepository;
import de.dicecup.classlink.features.classes.ClassSubjectAssignment;
import de.dicecup.classlink.features.classes.ClassTermRepository;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClassController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClassControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ClassRepository classRepository;
    @MockBean
    ClassManagementService classManagementService;
    @MockBean
    ClassTermRepository classTermRepository;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createClass_returnsCreated() throws Exception {
        Class clazz = new Class();
        clazz.setId(UUID.randomUUID());
        clazz.setName("Class A");
        when(classRepository.save(any(Class.class))).thenReturn(clazz);

        var request = new ClassCreateRequest("Class A");

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/classes/" + clazz.getId()))
                .andExpect(jsonPath("$.name", is("Class A")));
    }

    @Test
    void listClasses_returnsDtos() throws Exception {
        Class clazz = new Class();
        clazz.setId(UUID.randomUUID());
        clazz.setName("Class A");
        when(classRepository.findAll()).thenReturn(List.of(clazz));

        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(clazz.getId().toString())));
    }

    @Test
    void assignTeacher_returnsCreated() throws Exception {
        UUID classId = UUID.randomUUID();
        UUID termId = UUID.randomUUID();
        UUID assignmentId = UUID.randomUUID();
        ClassSubjectAssignment assignment = new ClassSubjectAssignment();
        assignment.setId(assignmentId);
        assignment.setSchoolClass(new Class());
        assignment.getSchoolClass().setId(classId);
        assignment.setTerm(new de.dicecup.classlink.features.terms.Term());
        assignment.getTerm().setId(termId);
        assignment.setTeacher(new de.dicecup.classlink.features.users.domain.roles.Teacher());
        assignment.getTeacher().setId(UUID.randomUUID());
        assignment.setSubject(new de.dicecup.classlink.features.subjects.Subject());
        assignment.getSubject().setId(UUID.randomUUID());

        when(classManagementService.assignTeacher(eq(classId), eq(termId), any(), any(), any())).thenReturn(assignment);

        var request = new ClassTeacherAssignmentRequest(assignment.getSubject().getId(), assignment.getTeacher().getId(), null);

        mockMvc.perform(post("/api/classes/" + classId + "/terms/" + termId + "/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(assignmentId.toString())));
    }
}
