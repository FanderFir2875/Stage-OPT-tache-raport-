package nc.opt.sior.web.rest;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class OrDematControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new OrDematController())
        .build(); // ✅ Pas de Security ici

    @Test
    void shouldReturnVersionSuccessfully() throws Exception {
        mockMvc.perform(get("/api/or-demat/version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.module").value("or-demat"))
            .andExpect(jsonPath("$.version").value("1.0.0"));
    }
}
