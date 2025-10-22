package nc.opt.psp.web.rest.ordemat;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map;

class OrDematControllerUnitTest {

    @Test
    void shouldReturnCorrectVersion() {

        OrDematController controller = new OrDematController();


        var response = controller.getVersion().getBody();


        assertThat(response).isNotNull();
        assertThat(response.get("module")).isEqualTo("or-demat");
        assertThat(response.get("version")).isEqualTo("1.0.0");
    }
}
