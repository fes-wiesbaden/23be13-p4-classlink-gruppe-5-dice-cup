package de.dicecup.classlink.features.classes.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.classes.*;
import de.dicecup.classlink.features.grades.AssignmentManagementService;
import de.dicecup.classlink.features.grades.SubjectAssignment;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import de.dicecup.classlink.features.terms.Term;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
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
class SchoolClassControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    SchoolClassRepository schoolClassRepository;
    @MockBean
    AssignmentManagementService assignmentManagementService;
    @MockBean
    ClassTermRepository classTermRepository;
    @MockBean
    ClassService classService;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createClass_returnsCreated() throws Exception {
        SchoolClass clazz = new SchoolClass();
        clazz.setId(UUID.randomUUID());
        clazz.setName("Class A");
        when(schoolClassRepository.save(any(SchoolClass.class))).thenReturn(clazz);

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
        SchoolClass clazz = new SchoolClass();
        clazz.setId(UUID.randomUUID());
        clazz.setName("Class A");
        when(schoolClassRepository.findAll()).thenReturn(List.of(clazz));

        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(clazz.getId().toString())));
    }

    @Test
    void listClassStudents_returnsDtos() throws Exception {
        UUID classId = UUID.randomUUID();
        StudentInClassDto student = new StudentInClassDto(UUID.randomUUID(), "Alice", "Anderson", classId, "Class A");
        when(classService.loadStudentsOfClass(classId)).thenReturn(List.of(student));

        mockMvc.perform(get("/api/classes/" + classId + "/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].studentId", is(student.studentId().toString())))
                .andExpect(jsonPath("$[0].firstName", is("Alice")))
                .andExpect(jsonPath("$[0].classId", is(classId.toString())));
    }
}
