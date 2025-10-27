package nc.opt.sior.web.rest;

import nc.opt.sior.service.OrDematService;
import nc.opt.sior.service.dto.OrDematDTO;
import nc.opt.sior.service.dto.OrDematResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api")
@RestController
public class OrDematRessource {

    private final OrDematService service;

    public OrDematRessource() {
        this.service = new OrDematService();
    }

    // POST /api/or-demat
    @PostMapping("/or-demat")
    public ResponseEntity<?> createOrDemat(@RequestBody OrDematDTO dto) {
        try {
            OrDematResponseDTO response = service.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            // 400 Bad Request avec message d’erreur
            return ResponseEntity.badRequest().body(
                java.util.Map.of("message", ex.getMessage())
            );
        }
    }
}
