package nc.opt.sior.service.dto;

public class OrDematResponseDTO {
    private String dateDebut;
    private String dateFin;
    private String message;

    public OrDematResponseDTO() {}

    public OrDematResponseDTO(String dateDebut, String dateFin, String message) {
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.message = message;
    }

    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }

    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
