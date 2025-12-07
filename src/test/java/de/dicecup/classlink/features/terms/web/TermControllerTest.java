package de.dicecup.classlink.features.terms.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dicecup.classlink.features.terms.Term;
import de.dicecup.classlink.features.terms.TermController;
import de.dicecup.classlink.features.terms.TermService;
import de.dicecup.classlink.features.terms.TermStatus;
import de.dicecup.classlink.features.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TermController.class)
@AutoConfigureMockMvc(addFilters = false)
class TermControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TermService termService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listTerms_returnsDtos() throws Exception {
        Term term = new Term();
        term.setId(UUID.randomUUID());
        term.setName("H1");
        term.setStatus(TermStatus.OPEN);
        when(termService.listTerms(null, null)).thenReturn(List.of(term));

        mockMvc.perform(get("/api/terms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(term.getId().toString())));
    }

    @Test
    void closeTerm_returnsNoContent() throws Exception {
        UUID termId = UUID.randomUUID();

        mockMvc.perform(post("/api/terms/" + termId + "/close")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
