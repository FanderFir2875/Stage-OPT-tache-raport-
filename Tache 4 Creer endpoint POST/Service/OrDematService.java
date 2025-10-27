package nc.opt.sior.service;

import nc.opt.sior.service.dto.OrDematDTO;
import nc.opt.sior.service.dto.OrDematResponseDTO;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class OrDematService {

    public OrDematResponseDTO create(OrDematDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Corps de requête manquant");
        if (dto.getDateDebut() == null || dto.getDateDebut().isBlank()) {
            throw new IllegalArgumentException("dateDebut est obligatoire");
        }
        if (dto.getDateFin() == null || dto.getDateFin().isBlank()) {
            throw new IllegalArgumentException("dateFin est obligatoire");
        }

        // 2) Format ISO-8601 (yyyy-MM-dd)
        LocalDate debut = null;
        LocalDate fin = null;
        boolean dateDebutInvalide = false;
        boolean dateFinInvalide = false;

        // Vérif dateDebut
        try {
            debut = LocalDate.parse(dto.getDateDebut());
        } catch (DateTimeParseException ex) {
            dateDebutInvalide = true;
        }

        // Vérif dateFin
        try {
            fin = LocalDate.parse(dto.getDateFin());
        } catch (DateTimeParseException ex) {
            dateFinInvalide = true;
        }

        // Gestion des erreurs combinées
        if (dateDebutInvalide && dateFinInvalide) {
            throw new IllegalArgumentException("Les deux dates sont invalides (format attendu yyyy-MM-dd)");
        }
        if (dateDebutInvalide) {
            throw new IllegalArgumentException("dateDebut invalide (format attendu yyyy-MM-dd)");
        }
        if (dateFinInvalide) {
            throw new IllegalArgumentException("dateFin invalide (format attendu yyyy-MM-dd)");
        }



        return new OrDematResponseDTO(
            debut.toString(),
            fin.toString(),
            "Ordre de réexpédition créé avec succès"
        );
    }
}
