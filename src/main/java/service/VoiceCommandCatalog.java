package service;

import model.Patient;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class VoiceCommandCatalog {

    private VoiceCommandCatalog() {
    }

    public static List<String> buildPhrases(List<Patient> patients) {
        Set<String> phrases = new LinkedHashSet<>();
        phrases.addAll(basePhrases());

        for (Patient patient : patients) {
            String originalName = sanitize(patient.getNome());
            String normalizedName = normalize(patient.getNome());
            if (originalName.isBlank() && normalizedName.isBlank()) {
                continue;
            }

            addSearchPhrases(phrases, originalName);
            addSearchPhrases(phrases, normalizedName);
        }

        return new ArrayList<>(phrases);
    }

    public static List<String> basePhrases() {
        return List.of(
                "abrir pacientes",
                "abrir paciente",
                "abrir tela de pacientes",
                "abrir consultas",
                "abrir consulta",
                "abrir tela de consultas",
                "cadastrar paciente",
                "buscar paciente",
                "procurar paciente",
                "localizar paciente",
                "mostrar consultas de hoje",
                "mostrar consulta de hoje",
                "mostrar consultas do dia",
                "consultas de hoje",
                "consultas do dia",
                "limpar formulario",
                "limpar o formulario",
                "fechar sistema",
                "encerrar sistema",
                "sair do sistema",
                "sair"
        );
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static void addSearchPhrases(Set<String> phrases, String patientName) {
        if (patientName == null || patientName.isBlank()) {
            return;
        }

        phrases.add("buscar paciente " + patientName);
        phrases.add("procurar paciente " + patientName);
        phrases.add("localizar paciente " + patientName);

        String[] parts = patientName.split(" ");
        if (parts.length > 0) {
            phrases.add("buscar paciente " + parts[0]);
            phrases.add("procurar paciente " + parts[0]);
            phrases.add("localizar paciente " + parts[0]);
        }
        if (parts.length > 1) {
            String shortName = parts[0] + " " + parts[1];
            phrases.add("buscar paciente " + shortName);
            phrases.add("procurar paciente " + shortName);
            phrases.add("localizar paciente " + shortName);
        }
    }
}
