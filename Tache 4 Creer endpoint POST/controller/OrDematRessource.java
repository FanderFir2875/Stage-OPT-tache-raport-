package nc.opt.sior.web.rest;

import nc.opt.sior.service.OrDematService;
import nc.opt.sior.service.dto.OrDematDTO;
import nc.opt.sior.service.dto.OrDematResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;


import java.util.Map;

@RequestMapping("/api")
@RestController
public class OrDematResource {

    private final OrDematService service;

    public OrDematResource() {
        this.service = new OrDematService();
    }

    /**
     * {@code POST  /or-demat} : Soumettre une demande d'ordre de réexpédition dématérialisé.
     *
     * @param dto l'objet contenant les données de l'OR (dateDebut, dateFin).
     * @return le {@link ResponseEntity} avec le statut {@code 201 (Created)} et le corps JSON contenant la confirmation,
     *         ou le statut {@code 400 (Bad Request)} si les données sont manquantes ou invalides.
     */
    @PostMapping("/or-demat")
    public ResponseEntity<?> createOrDemat(@Valid @RequestBody OrDematDTO dto) {
        try {
            OrDematResponseDTO response = service.processOrDemat(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(
                Map.of("message", ex.getMessage())
            );
        }
    }
}
