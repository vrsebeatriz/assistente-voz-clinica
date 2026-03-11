package controller;

import dao.PatientDAO;
import model.Patient;
import util.Validador;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PatientController {

    private final PatientDAO patientDAO;

    public PatientController(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    public Patient registerPatient(String nome, String cpf, String telefone, LocalDate dataNascimento) {
        Validador.validarPaciente(nome, cpf, dataNascimento);
        return patientDAO.save(new Patient(0L, nome.trim(), cpf.trim(), telefone == null ? "" : telefone.trim(), dataNascimento));
    }

    public List<Patient> listPatients() {
        return patientDAO.findAll();
    }

    public List<Patient> searchByName(String nome) {
        Validador.validarBuscaPorNome(nome);
        String normalizedQuery = normalize(nome);
        return patientDAO.findAll().stream()
                .filter(patient -> normalize(patient.getNome()).contains(normalizedQuery))
                .collect(Collectors.toList());
    }

    public void deletePatient(Patient patient) {
        if (patient == null || patient.getId() <= 0) {
            throw new IllegalArgumentException("Selecione um paciente valido para excluir.");
        }
        if (!patientDAO.deleteById(patient.getId())) {
            throw new IllegalStateException("Nao foi possivel excluir o paciente selecionado.");
        }
    }

    private String normalize(String text) {
        return Normalizer.normalize(text == null ? "" : text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
