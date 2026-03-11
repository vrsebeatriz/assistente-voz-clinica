package controller;

import dao.ConsultationDAO;
import model.Consultation;
import model.Patient;
import util.Validador;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ConsultationController {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final ConsultationDAO consultationDAO;

    public ConsultationController(ConsultationDAO consultationDAO) {
        this.consultationDAO = consultationDAO;
    }

    public Consultation registerConsultation(Patient patient,
                                            LocalDate dataConsulta,
                                            String horario,
                                            String profissional,
                                            String status) {
        String patientName = patient == null ? "" : patient.getNome();
        Validador.validarConsulta(patientName, dataConsulta, horario, profissional, status);

        LocalTime parsedTime;
        try {
            parsedTime = LocalTime.parse(horario.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Informe o horario no formato HH:mm.");
        }

        return consultationDAO.save(new Consultation(
                0L,
                patient.getId(),
                patient.getNome(),
                dataConsulta,
                parsedTime,
                profissional.trim(),
                status.trim()
        ));
    }

    public List<Consultation> listByDate(LocalDate date) {
        return consultationDAO.findByDate(date);
    }

    public List<Consultation> listToday() {
        return consultationDAO.findByDate(LocalDate.now());
    }

    public void deleteConsultation(Consultation consultation) {
        if (consultation == null || consultation.getId() <= 0) {
            throw new IllegalArgumentException("Selecione uma consulta valida para excluir.");
        }
        if (!consultationDAO.deleteById(consultation.getId())) {
            throw new IllegalStateException("Nao foi possivel excluir a consulta selecionada.");
        }
    }
}
