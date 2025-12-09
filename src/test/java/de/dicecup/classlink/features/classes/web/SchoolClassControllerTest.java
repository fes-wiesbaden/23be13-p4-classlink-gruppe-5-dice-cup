package de.dicecup.classlink.features.classes.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.classes.SchoolClass;
import de.dicecup.classlink.features.classes.ClassController;
import de.dicecup.classlink.features.grades.AssignmentManagementService;
import de.dicecup.classlink.features.classes.SchoolClassRepository;
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
}
