package service;

import model.CommandType;
import model.ParsedCommand;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandInterpreterService {

    private static final Pattern SEARCH_PATIENT_PATTERN =
            Pattern.compile("\\b(?:buscar|procurar|localizar)\\s+(?:o\\s+)?paciente(?:s)?\\s+(.+)");

    public ParsedCommand interpret(String text) {
        if (text == null || text.isBlank()) {
            return new ParsedCommand(CommandType.UNKNOWN, "", "");
        }

        String normalized = normalize(text);

        Matcher searchMatcher = SEARCH_PATIENT_PATTERN.matcher(normalized);
        if (searchMatcher.find()) {
            return new ParsedCommand(CommandType.SEARCH_PATIENT, searchMatcher.group(1).trim(), text);
        }
        if ((normalized.contains("consulta") || normalized.contains("consultas"))
                && (normalized.contains("hoje") || normalized.contains("do dia"))) {
            return new ParsedCommand(CommandType.SHOW_TODAY_CONSULTATIONS, "", text);
        }
        if (normalized.contains("abrir") && normalized.contains("paciente")) {
            return new ParsedCommand(CommandType.OPEN_PATIENTS, "", text);
        }
        if (normalized.contains("abrir") && normalized.contains("consulta")) {
            return new ParsedCommand(CommandType.OPEN_CONSULTATIONS, "", text);
        }
        if ((normalized.contains("cadastrar") || normalized.contains("cadastro")) && normalized.contains("paciente")) {
            return new ParsedCommand(CommandType.REGISTER_PATIENT, "", text);
        }
        if (normalized.contains("limpar") && normalized.contains("formulario")) {
            return new ParsedCommand(CommandType.CLEAR_FORM, "", text);
        }
        if (normalized.contains("fechar sistema")
                || normalized.contains("encerrar sistema")
                || normalized.contains("sair do sistema")
                || normalized.equals("fechar")
                || normalized.equals("sair")) {
            return new ParsedCommand(CommandType.EXIT_APPLICATION, "", text);
        }
        return new ParsedCommand(CommandType.UNKNOWN, "", text);
    }

    private String normalize(String text) {
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT).trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
