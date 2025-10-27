package nc.opt.sior.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import nc.opt.sior.service.dto.OrDematDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrDematRessourceTest {

    private final MockMvc mvc = MockMvcBuilders
        .standaloneSetup(new OrDematRessource())
        .build();

    private final ObjectMapper om = new ObjectMapper();

    @Test
    void create_shouldReturn201_whenValidDates() throws Exception {
        var dto = new OrDematDTO("2025-02-01", "2025-03-01");
        mvc.perform(post("/api/or-demat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.dateDebut").value("2025-02-01"))
            .andExpect(jsonPath("$.dateFin").value("2025-03-01"))
            .andExpect(jsonPath("$.message").value("Ordre de réexpédition créé avec succès"));
    }

    @Test
    void create_shouldReturn400_whenMissingDateFin() throws Exception {
        var dto = new OrDematDTO("2025-02-01", null);
        mvc.perform(post("/api/or-demat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("dateFin est obligatoire"));
    }

    @Test
    void create_shouldReturn400_whenBadFormat() throws Exception {
        var dto = new OrDematDTO("01-02-2025", "2025/03/01");
        mvc.perform(post("/api/or-demat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
    }
}
