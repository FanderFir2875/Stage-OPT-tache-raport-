package nc.opt.sior.service.dto;

public class OrDematDTO {
    private String DateDebut;
    private String DateFin;

    public OrDematDTO() {}

    public OrDematDTO(String DateDebut,String DateFin){
        this.DateDebut= DateDebut;
        this.DateFin = DateFin;
    }

    public String getDateDebut(){
        return DateDebut;
    }

    public void setDateDebut(String DateDebut){
        this.DateDebut = DateDebut;
    }

    public String getDateFin(){
        return DateFin;
    }

    public void setDateFin(String DateFin){
        this.DateFin = DateFin;
    }


}
