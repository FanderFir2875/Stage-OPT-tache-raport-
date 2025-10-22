package nc.opt.psp.web.rest.ordemat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OrDematController {

    @GetMapping("/or-demat/version")
    public ResponseEntity<Map<String , String>> getVersion(){
        Map<String, String> response = Map.of(
            "module" , "or-demat",
            "version", "1.0.0"
        );
        return ResponseEntity.ok(response);
    }
}
