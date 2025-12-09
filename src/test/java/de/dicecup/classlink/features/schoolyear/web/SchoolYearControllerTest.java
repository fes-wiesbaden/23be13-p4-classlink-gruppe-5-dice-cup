package de.dicecup.classlink.features.schoolyear.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.schoolyear.SchoolYear;
import de.dicecup.classlink.features.schoolyear.SchoolYearController;
import de.dicecup.classlink.features.schoolyear.SchoolYearService;
import de.dicecup.classlink.features.schoolyear.SchoolYearStatus;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermService;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDate;
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

@WebMvcTest(SchoolYearController.class)
@AutoConfigureMockMvc(addFilters = false)
class SchoolYearControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    SchoolYearService schoolYearService;

    @MockBean
    TermService termService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void createYear_returnsCreated() throws Exception {
        SchoolYear year = new SchoolYear();
        year.setId(UUID.randomUUID());
        year.setName("2024/25");
        year.setStartDate(LocalDate.now());
        year.setEndDate(LocalDate.now().plusMonths(10));
        year.setStatus(SchoolYearStatus.ACTIVE);

        when(schoolYearService.create(any())).thenReturn(year);

        var request = new SchoolYearCreateRequest("2024/25", year.getStartDate(), year.getEndDate());

        mockMvc.perform(post("/api/school-years")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/school-years/" + year.getId()))
                .andExpect(jsonPath("$.name", is("2024/25")))
                .andExpect(jsonPath("$.id", is(year.getId().toString())));
    }

    @Test
    void listYears_returnsDtos() throws Exception {
        SchoolYear year = new SchoolYear();
        year.setId(UUID.randomUUID());
        year.setName("2024/25");
        year.setStatus(SchoolYearStatus.ACTIVE);

        when(schoolYearService.list(null)).thenReturn(List.of(year));

        mockMvc.perform(get("/api/school-years"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(year.getId().toString())));
    }

    @Test
    void createTerm_forYear() throws Exception {
        UUID yearId = UUID.randomUUID();
        Term term = new Term();
        term.setId(UUID.randomUUID());
        term.setName("H1");
        when(termService.openTerm(eq(yearId), any())).thenReturn(term);

        var request = new de.dicecup.classlink.features.terms.web.TermCreateWebRequest("H1", 1, LocalDate.now(), LocalDate.now().plusMonths(3));

        mockMvc.perform(post("/api/school-years/" + yearId + "/terms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(term.getId().toString())));
    }
}
