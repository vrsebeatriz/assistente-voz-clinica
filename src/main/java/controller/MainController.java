package controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import model.Consultation;
import model.ParsedCommand;
import model.Patient;
import service.CommandInterpreterService;
import service.VoiceCommandCatalog;
import view.ConsultationView;
import view.MainView;
import view.PatientView;

import java.time.LocalDate;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Stage stage;
    private final PatientController patientController;
    private final ConsultationController consultationController;
    private final VoiceController voiceController;
    private final CommandInterpreterService commandInterpreterService;

    private final MainView view;
    private final PatientView patientView;
    private final ConsultationView consultationView;

    private CurrentScreen currentScreen = CurrentScreen.PATIENTS;

    public MainController(Stage stage,
                          PatientController patientController,
                          ConsultationController consultationController,
                          VoiceController voiceController,
                          CommandInterpreterService commandInterpreterService) {
        this.stage = stage;
        this.patientController = patientController;
        this.consultationController = consultationController;
        this.voiceController = voiceController;
        this.commandInterpreterService = commandInterpreterService;
        this.view = new MainView();
        this.patientView = new PatientView();
        this.consultationView = new ConsultationView();

        configureVoiceCallbacks();
        configureActions();
        loadInitialData();
        showPatients();
    }

    public MainView getView() {
        return view;
    }

    public void shutdown() {
        voiceController.shutdown();
    }

    private void configureVoiceCallbacks() {
        view.setEngineDescription(voiceController.getEngineDescription());
        voiceController.setRecognizedTextHandler(text -> Platform.runLater(() -> handleRecognizedText(text)));
        voiceController.setErrorHandler(message -> Platform.runLater(() -> {
            view.appendSystemResponse("Erro de voz: " + message);
            view.setListeningStatus("Parado", false);
        }));
        voiceController.setStatusHandler((listening, message) ->
                Platform.runLater(() -> view.setListeningStatus(message, listening))
        );
    }

    private void configureActions() {
        view.getPatientsNavButton().setOnAction(event -> showPatients());
        view.getConsultationsNavButton().setOnAction(event -> showConsultations());
        view.getCloseNavButton().setOnAction(event -> closeApplication());

        view.getStartListeningButton().setOnAction(event -> {
            view.appendSystemResponse("Tentando iniciar a escuta do microfone.");
            voiceController.startListening();
        });
        view.getStopListeningButton().setOnAction(event -> {
            voiceController.stopListening();
            view.appendSystemResponse("Escuta interrompida manualmente.");
        });
        view.getExecuteTextButton().setOnAction(event -> executeManualCommand());

        patientView.getRegisterButton().setOnAction(event -> {
            String message = registerPatientFromForm();
            view.appendSystemResponse(message);
        });
        patientView.getClearButton().setOnAction(event -> {
            String message = clearCurrentForm();
            view.appendSystemResponse(message);
        });
        patientView.getSearchButton().setOnAction(event -> {
            String message = searchPatients(patientView.getSearchText());
            view.appendSystemResponse(message);
        });
        patientView.getSearchField().setOnAction(event -> {
            String message = searchPatients(patientView.getSearchText());
            view.appendSystemResponse(message);
        });
        patientView.getDeleteButton().setOnAction(event -> {
            String message = deleteSelectedPatient();
            if (!message.isBlank()) {
                view.appendSystemResponse(message);
            }
        });
        patientView.getPatientsTable().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->
                patientView.fillForm(newValue)
        );

        consultationView.getFilterButton().setOnAction(event -> {
            String message = filterConsultationsByDate();
            view.appendSystemResponse(message);
        });
        consultationView.getRegisterButton().setOnAction(event -> {
            String message = registerConsultationFromForm();
            view.appendSystemResponse(message);
        });
        consultationView.getClearButton().setOnAction(event -> {
            String message = clearCurrentForm();
            view.appendSystemResponse(message);
        });
        consultationView.getTodayButton().setOnAction(event -> {
            String message = showTodayConsultations();
            view.appendSystemResponse(message);
        });
        consultationView.getDeleteButton().setOnAction(event -> {
            String message = deleteSelectedConsultation();
            if (!message.isBlank()) {
                view.appendSystemResponse(message);
            }
        });
        consultationView.getConsultationsTable().getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) ->
                consultationView.fillForm(newValue)
        );

        stage.setOnCloseRequest(event -> shutdown());
    }

    private void loadInitialData() {
        refreshPatientsTable();
        consultationView.clearForm();
        consultationView.setSelectedDate(LocalDate.now());
        consultationView.setConsultations(consultationController.listToday());
        consultationView.setResultMessage("Consultas de hoje carregadas.");
    }

    private void showPatients() {
        currentScreen = CurrentScreen.PATIENTS;
        view.setCurrentSection("Pacientes", "Cadastro, busca por nome e comandos de voz orientados a acoes simples.");
        view.setContent(patientView);
        view.setActiveNavigation(view.getPatientsNavButton());
    }

    private void showConsultations() {
        currentScreen = CurrentScreen.CONSULTATIONS;
        view.setCurrentSection("Consultas", "Cadastre consultas vinculadas a pacientes, filtre a agenda por data e acompanhe o dia.");
        view.setContent(consultationView);
        view.setActiveNavigation(view.getConsultationsNavButton());
    }

    private void executeManualCommand() {
        String commandText = view.getManualCommandText();
        if (commandText == null || commandText.isBlank()) {
            view.appendSystemResponse("Digite um comando antes de executar.");
            return;
        }
        view.clearManualCommand();
        handleRecognizedText(commandText.trim());
    }

    private void handleRecognizedText(String recognizedText) {
        view.appendRecognizedText(recognizedText);

        ParsedCommand command = resolveCommand(recognizedText);
        String response = switch (command.getType()) {
            case OPEN_PATIENTS -> {
                showPatients();
                yield "Tela de pacientes aberta.";
            }
            case OPEN_CONSULTATIONS -> {
                showConsultations();
                yield "Tela de consultas aberta.";
            }
            case REGISTER_PATIENT -> {
                showPatients();
                yield registerPatientFromForm();
            }
            case SEARCH_PATIENT -> {
                showPatients();
                patientView.setSearchText(command.getArgument());
                yield searchPatients(command.getArgument());
            }
            case SHOW_TODAY_CONSULTATIONS -> {
                showConsultations();
                yield showTodayConsultations();
            }
            case CLEAR_FORM -> clearCurrentForm();
            case EXIT_APPLICATION -> {
                closeApplication();
                yield "Encerrando a aplicacao.";
            }
            case UNKNOWN -> "Comando nao entendido. Tente: abrir pacientes, buscar paciente [nome], mostrar consultas de hoje ou fechar sistema.";
        };

        view.appendSystemResponse(response);
    }

    private ParsedCommand resolveCommand(String recognizedText) {
        ParsedCommand interpreted = commandInterpreterService.interpret(recognizedText);
        if (interpreted.getType() == model.CommandType.SEARCH_PATIENT && interpreted.getArgument() != null
                && !interpreted.getArgument().isBlank()) {
            return interpreted;
        }

        String resolvedPatientName = resolvePatientNameFromText(recognizedText);
        if (resolvedPatientName != null) {
            return new ParsedCommand(model.CommandType.SEARCH_PATIENT, resolvedPatientName, recognizedText);
        }
        return interpreted;
    }

    private String registerPatientFromForm() {
        try {
            Patient patient = patientController.registerPatient(
                    patientView.getPatientName(),
                    patientView.getCpf(),
                    patientView.getPhone(),
                    patientView.getBirthDate()
            );
            refreshPatientsTable();
            patientView.clearForm();
            patientView.setSearchText("");
            String message = "Paciente " + patient.getNome() + " cadastrado com sucesso.";
            patientView.setResultMessage(message);
            return message;
        } catch (IllegalArgumentException exception) {
            patientView.setResultMessage(exception.getMessage());
            return exception.getMessage();
        } catch (IllegalStateException exception) {
            String message = buildPersistenceMessage(exception, "Nao foi possivel cadastrar o paciente.");
            patientView.setResultMessage(message);
            return message;
        }
    }

    private String searchPatients(String query) {
        try {
            List<Patient> patients;
            String message;
            if (query == null || query.isBlank()) {
                patients = patientController.listPatients();
                message = patients.size() + " paciente(s) carregado(s).";
            } else {
                patients = patientController.searchByName(query);
                message = patients.isEmpty()
                        ? "Nenhum paciente encontrado para '" + query + "'."
                        : patients.size() + " paciente(s) encontrado(s) para '" + query + "'.";
            }
            patientView.setPatients(patients);
            patientView.setResultMessage(message);
            return message;
        } catch (IllegalArgumentException exception) {
            patientView.setResultMessage(exception.getMessage());
            return exception.getMessage();
        }
    }

    private String resolvePatientNameFromText(String recognizedText) {
        String normalizedText = normalizeText(recognizedText);
        if (normalizedText.isBlank()) {
            return null;
        }

        List<Patient> patients = patientController.listPatients();
        List<PatientAliasMatch> matches = new ArrayList<>();
        for (Patient patient : patients) {
            for (String alias : buildPatientAliases(patient)) {
                if (!alias.isBlank() && normalizedText.contains(alias)) {
                    matches.add(new PatientAliasMatch(patient.getNome(), alias.length()));
                }
            }
        }

        return matches.stream()
                .max(Comparator.comparingInt(PatientAliasMatch::aliasLength))
                .map(PatientAliasMatch::patientName)
                .orElse(null);
    }

    private String registerConsultationFromForm() {
        try {
            Consultation consultation = consultationController.registerConsultation(
                    consultationView.getSelectedPatientForConsultation(),
                    consultationView.getConsultationDate(),
                    consultationView.getConsultationTime(),
                    consultationView.getProfessional(),
                    consultationView.getConsultationStatus()
            );
            consultationView.clearForm();
            consultationView.getFilterDatePicker().setValue(consultation.getDataConsulta());
            consultationView.setSelectedDate(consultation.getDataConsulta());
            consultationView.setConsultations(consultationController.listByDate(consultation.getDataConsulta()));
            String message = "Consulta de " + consultation.getNomePaciente() + " cadastrada com sucesso.";
            consultationView.setResultMessage(message);
            return message;
        } catch (IllegalArgumentException exception) {
            consultationView.setResultMessage(exception.getMessage());
            return exception.getMessage();
        } catch (IllegalStateException exception) {
            String message = buildConsultationPersistenceMessage(exception);
            consultationView.setResultMessage(message);
            return message;
        }
    }

    private String showTodayConsultations() {
        List<Consultation> consultations = consultationController.listToday();
        consultationView.getFilterDatePicker().setValue(LocalDate.now());
        consultationView.setSelectedDate(LocalDate.now());
        consultationView.setConsultations(consultations);
        String message = consultations.isEmpty()
                ? "Nenhuma consulta agendada para hoje."
                : consultations.size() + " consulta(s) carregada(s) para hoje (" + LocalDate.now().format(DATE_FORMATTER) + ").";
        consultationView.setResultMessage(message);
        return message;
    }

    private String filterConsultationsByDate() {
        LocalDate date = consultationView.getFilterDatePicker().getValue();
        if (date == null) {
            date = LocalDate.now();
            consultationView.getFilterDatePicker().setValue(date);
        }
        List<Consultation> consultations = consultationController.listByDate(date);
        consultationView.setSelectedDate(date);
        consultationView.setConsultations(consultations);
        String message = consultations.isEmpty()
                ? "Nenhuma consulta encontrada para " + date.format(DATE_FORMATTER) + "."
                : consultations.size() + " consulta(s) carregada(s) para " + date.format(DATE_FORMATTER) + ".";
        consultationView.setResultMessage(message);
        return message;
    }

    private String clearCurrentForm() {
        if (currentScreen == CurrentScreen.CONSULTATIONS) {
            consultationView.clearForm();
            consultationView.getFilterDatePicker().setValue(LocalDate.now());
            return showTodayConsultations();
        }

        patientView.clearForm();
        patientView.setSearchText("");
        refreshPatientsTable();
        String message = "Formulario de pacientes limpo.";
        patientView.setResultMessage(message);
        return message;
    }

    private void refreshPatientsTable() {
        List<Patient> patients = patientController.listPatients();
        patientView.setPatients(patients);
        consultationView.setPatients(patients);
        voiceController.updateCommandPhrases(VoiceCommandCatalog.buildPhrases(patients));
        patientView.setResultMessage(patients.size() + " paciente(s) disponivel(is) na base.");
    }

    private String deleteSelectedPatient() {
        Patient selectedPatient = patientView.getSelectedPatient();
        if (selectedPatient == null) {
            String message = "Selecione um paciente para excluir.";
            patientView.setResultMessage(message);
            return message;
        }
        if (!confirmDeletion(
                "Excluir paciente",
                "Excluir " + selectedPatient.getNome() + "?",
                "Esta acao remove o paciente da base local."
        )) {
            return "";
        }

        try {
            patientController.deletePatient(selectedPatient);
            patientView.clearForm();

            List<Patient> allPatients = patientController.listPatients();
            consultationView.setPatients(allPatients);
            voiceController.updateCommandPhrases(VoiceCommandCatalog.buildPhrases(allPatients));

            String currentSearch = patientView.getSearchText();
            if (currentSearch == null || currentSearch.isBlank()) {
                patientView.setPatients(allPatients);
            } else {
                patientView.setPatients(patientController.searchByName(currentSearch));
            }

            String message = "Paciente " + selectedPatient.getNome() + " excluido com sucesso.";
            patientView.setResultMessage(message);
            return message;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            String message = exception instanceof IllegalStateException
                    ? buildPatientDeletionMessage((IllegalStateException) exception)
                    : exception.getMessage();
            patientView.setResultMessage(message);
            return message;
        }
    }

    private String deleteSelectedConsultation() {
        Consultation selectedConsultation = consultationView.getSelectedConsultation();
        if (selectedConsultation == null) {
            String message = "Selecione uma consulta para excluir.";
            consultationView.setResultMessage(message);
            return message;
        }
        if (!confirmDeletion(
                "Excluir consulta",
                "Excluir a consulta de " + selectedConsultation.getNomePaciente() + "?",
                "Esta acao remove a consulta selecionada da agenda."
        )) {
            return "";
        }

        try {
            consultationController.deleteConsultation(selectedConsultation);

            LocalDate selectedDate = consultationView.getFilterDatePicker().getValue();
            if (selectedDate == null) {
                selectedDate = LocalDate.now();
                consultationView.getFilterDatePicker().setValue(selectedDate);
            }
            consultationView.setSelectedDate(selectedDate);
            consultationView.setConsultations(consultationController.listByDate(selectedDate));

            String message = "Consulta de " + selectedConsultation.getNomePaciente() + " excluida com sucesso.";
            consultationView.setResultMessage(message);
            return message;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            consultationView.setResultMessage(exception.getMessage());
            return exception.getMessage();
        }
    }

    private boolean confirmDeletion(String title, String header, String content) {
        ButtonType confirmButton = new ButtonType("Excluir", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        return alert.showAndWait().orElse(cancelButton) == confirmButton;
    }

    private List<String> buildPatientAliases(Patient patient) {
        List<String> aliases = new ArrayList<>();
        String normalizedName = normalizeText(patient.getNome());
        if (normalizedName.isBlank()) {
            return aliases;
        }

        aliases.add(normalizedName);

        String[] parts = normalizedName.split(" ");
        if (parts.length > 0) {
            aliases.add(parts[0]);
        }
        if (parts.length > 1) {
            aliases.add(parts[0] + " " + parts[1]);
        }
        return aliases;
    }

    private String normalizeText(String text) {
        return Normalizer.normalize(text == null ? "" : text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record PatientAliasMatch(String patientName, int aliasLength) {
    }

    private String buildPersistenceMessage(IllegalStateException exception, String fallback) {
        String causeMessage = exception.getCause() == null ? "" : exception.getCause().getMessage();
        if (causeMessage != null && causeMessage.toLowerCase().contains("unique")) {
            return "Ja existe um paciente cadastrado com esse CPF.";
        }
        return fallback;
    }

    private String buildPatientDeletionMessage(IllegalStateException exception) {
        String causeMessage = exception.getCause() == null ? "" : exception.getCause().getMessage();
        if (causeMessage != null && causeMessage.toLowerCase().contains("foreign key")) {
            return "Nao e possivel excluir o paciente porque existem consultas vinculadas a ele.";
        }
        return exception.getMessage();
    }

    private String buildConsultationPersistenceMessage(IllegalStateException exception) {
        String causeMessage = exception.getCause() == null ? "" : exception.getCause().getMessage();
        if (causeMessage != null && causeMessage.toLowerCase().contains("foreign key")) {
            return "Nao foi possivel salvar a consulta porque o paciente selecionado nao e mais valido.";
        }
        return "Nao foi possivel cadastrar a consulta.";
    }

    private void closeApplication() {
        shutdown();
        stage.close();
    }

    private enum CurrentScreen {
        PATIENTS,
        CONSULTATIONS
    }
}
