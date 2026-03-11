package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Consultation {

    private final long id;
    private final long patientId;
    private final String nomePaciente;
    private final LocalDate dataConsulta;
    private final LocalTime horario;
    private final String profissional;
    private final String status;

    public Consultation(long id,
                        long patientId,
                        String nomePaciente,
                        LocalDate dataConsulta,
                        LocalTime horario,
                        String profissional,
                        String status) {
        this.id = id;
        this.patientId = patientId;
        this.nomePaciente = nomePaciente;
        this.dataConsulta = dataConsulta;
        this.horario = horario;
        this.profissional = profissional;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public long getPatientId() {
        return patientId;
    }

    public String getNomePaciente() {
        return nomePaciente;
    }

    public LocalDate getDataConsulta() {
        return dataConsulta;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public String getProfissional() {
        return profissional;
    }

    public String getStatus() {
        return status;
    }
}
