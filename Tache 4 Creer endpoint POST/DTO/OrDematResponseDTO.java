package nc.opt.sior.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrDematResponseDTO {
    private String dateDebut;
    private String dateFin;
    private String message;
}
