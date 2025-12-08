package de.dicecup.classlink.features.teachers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeacherContextController.class)
@AutoConfigureMockMvc(addFilters = false)
class TeacherContextControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TeacherContextService teacherContextService;
    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void getContext_returnsData() throws Exception {
        UUID teacherId = UUID.randomUUID();
        TeacherProjectGroupContextDto groupDto = new TeacherProjectGroupContextDto(UUID.randomUUID(), "Project X", 3);
        TeacherTermContextDto termDto = new TeacherTermContextDto(UUID.randomUUID(), "Term 1", true, List.of(groupDto));
        TeacherClassContextDto classDto = new TeacherClassContextDto(UUID.randomUUID(), "1A", List.of(termDto));
        TeacherContextDto dto = new TeacherContextDto(teacherId, List.of(classDto));
        when(teacherContextService.loadContext()).thenReturn(dto);

        mockMvc.perform(get("/api/teachers/me/context"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teacherId", is(teacherId.toString())))
                .andExpect(jsonPath("$.classes[0].terms[0].projectGroups[0].projectName", is("Project X")));
    }
}
