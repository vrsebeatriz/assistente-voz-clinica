package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import model.Consultation;
import model.Patient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultationView extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ComboBox<Patient> patientComboBox = new ComboBox<>();
    private final DatePicker consultationDatePicker = new DatePicker(LocalDate.now());
    private final TextField consultationTimeField = new TextField();
    private final TextField professionalField = new TextField();
    private final ComboBox<String> statusComboBox = new ComboBox<>();

    private final DatePicker filterDatePicker = new DatePicker(LocalDate.now());
    private final Button registerButton = new Button("Cadastrar consulta");
    private final Button clearButton = new Button("Limpar formulario");
    private final Button filterButton = new Button("Filtrar");
    private final Button todayButton = new Button("Consultas de hoje");
    private final Button deleteButton = new Button("Excluir selecionada");

    private final Label resultLabel = new Label("Consultas ainda nao carregadas.");
    private final Label visibleConsultationsValue = new Label("0");
    private final Label selectedDateValue = new Label(LocalDate.now().format(DATE_FORMATTER));
    private final Label visibleConsultationsBadge = new Label("0 exibida(s)");
    private final TableView<Consultation> consultationsTable = new TableView<>();

    public ConsultationView() {
        setSpacing(16);
        setPadding(new Insets(0, 0, 6, 0));

        configureFormFields();

        VBox introCard = createIntroCard();
        VBox formCard = createFormCard();
        VBox filterCard = createFilterCard();
        VBox tableCard = createTableCard();

        getChildren().addAll(introCard, formCard, filterCard, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
    }

    public DatePicker getFilterDatePicker() {
        return filterDatePicker;
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getFilterButton() {
        return filterButton;
    }

    public Button getTodayButton() {
        return todayButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public TableView<Consultation> getConsultationsTable() {
        return consultationsTable;
    }

    public Patient getSelectedPatientForConsultation() {
        return patientComboBox.getValue();
    }

    public Consultation getSelectedConsultation() {
        return consultationsTable.getSelectionModel().getSelectedItem();
    }

    public LocalDate getConsultationDate() {
        return consultationDatePicker.getValue();
    }

    public String getConsultationTime() {
        return consultationTimeField.getText();
    }

    public String getProfessional() {
        return professionalField.getText();
    }

    public String getConsultationStatus() {
        return statusComboBox.getValue();
    }

    public void setPatients(List<Patient> patients) {
        long selectedPatientId = patientComboBox.getValue() == null ? -1L : patientComboBox.getValue().getId();
        patientComboBox.getItems().setAll(patients);
        patientComboBox.setValue(
                patients.stream()
                        .filter(patient -> patient.getId() == selectedPatientId)
                        .findFirst()
                        .orElse(null)
        );
    }

    public void setConsultations(List<Consultation> consultations) {
        consultationsTable.getItems().setAll(consultations);
        updateConsultationMetrics(consultations.size());
    }

    public void setResultMessage(String message) {
        resultLabel.setText(message);
    }

    public void setSelectedDate(LocalDate date) {
        if (date == null) {
            selectedDateValue.setText("-");
            return;
        }
        selectedDateValue.setText(date.format(DATE_FORMATTER));
    }

    public void clearForm() {
        patientComboBox.setValue(null);
        consultationDatePicker.setValue(LocalDate.now());
        consultationTimeField.clear();
        professionalField.clear();
        statusComboBox.setValue("Confirmada");
    }

    public void fillForm(Consultation consultation) {
        if (consultation == null) {
            return;
        }

        consultationDatePicker.setValue(consultation.getDataConsulta());
        consultationTimeField.setText(consultation.getHorario().format(TIME_FORMATTER));
        professionalField.setText(consultation.getProfissional());
        statusComboBox.setValue(consultation.getStatus());
        patientComboBox.setValue(
                patientComboBox.getItems().stream()
                        .filter(patient -> patient.getId() == consultation.getPatientId())
                        .findFirst()
                        .orElse(null)
        );
    }

    private VBox createIntroCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(20));

        Label title = new Label("Agenda e consultas");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label(
                "Cadastre consultas vinculadas ao paciente, filtre por data e acompanhe a agenda em uma unica tela."
        );
        subtitle.getStyleClass().add("muted-text");
        subtitle.setWrapText(true);

        FlowPane metrics = new FlowPane();
        metrics.setHgap(12);
        metrics.setVgap(12);
        metrics.getChildren().addAll(
                createMetricCard("Consultas visiveis", visibleConsultationsValue, "Itens exibidos na agenda", true),
                createMetricCard("Data em foco", selectedDateValue, "Referencia usada no filtro atual", false),
                createMetricCard("Acao principal", new Label("Cadastrar consulta"), "Vincule a consulta a um paciente da base", false)
        );

        card.getChildren().addAll(title, subtitle, metrics);
        return card;
    }

    private VBox createFormCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(20));

        Label title = new Label("Cadastrar consulta");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label("Selecione o paciente e preencha os dados da agenda para gravar a consulta no banco.");
        subtitle.getStyleClass().add("muted-text");
        subtitle.setWrapText(true);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(14);
        ColumnConstraints leftColumn = new ColumnConstraints();
        leftColumn.setPercentWidth(50);
        ColumnConstraints rightColumn = new ColumnConstraints();
        rightColumn.setPercentWidth(50);
        formGrid.getColumnConstraints().addAll(leftColumn, rightColumn);

        addFormField(formGrid, 0, 0, "Paciente", patientComboBox);
        addFormField(formGrid, 1, 0, "Data", consultationDatePicker);
        addFormField(formGrid, 0, 1, "Horario", consultationTimeField);
        addFormField(formGrid, 1, 1, "Profissional", professionalField);
        addFormField(formGrid, 0, 2, "Status", statusComboBox);

        HBox buttons = new HBox(12, registerButton, clearButton);
        registerButton.getStyleClass().add("primary-button");
        clearButton.getStyleClass().add("secondary-button");

        card.getChildren().addAll(title, subtitle, formGrid, buttons);
        return card;
    }

    private VBox createFilterCard() {
        VBox card = new VBox(14);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(20));

        Label title = new Label("Filtro de consultas");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label("Selecione uma data para consultar a agenda ou volte ao dia atual com um clique.");
        subtitle.getStyleClass().add("muted-text");
        subtitle.setWrapText(true);

        HBox actions = new HBox(12, filterDatePicker, filterButton, todayButton);
        filterButton.getStyleClass().add("secondary-button");
        todayButton.getStyleClass().add("primary-button");
        resultLabel.getStyleClass().add("result-text");
        resultLabel.setWrapText(true);

        card.getChildren().addAll(title, subtitle, actions, resultLabel);
        return card;
    }

    private VBox createTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(20));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Lista de consultas");
        title.getStyleClass().add("card-title");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.disableProperty().bind(consultationsTable.getSelectionModel().selectedItemProperty().isNull());
        visibleConsultationsBadge.getStyleClass().addAll("soft-chip", "accent-chip");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, deleteButton, visibleConsultationsBadge);

        TableColumn<Consultation, String> patientColumn = new TableColumn<>("Paciente");
        patientColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNomePaciente()));

        TableColumn<Consultation, String> dateColumn = new TableColumn<>("Data");
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDataConsulta().format(DATE_FORMATTER)
        ));

        TableColumn<Consultation, String> timeColumn = new TableColumn<>("Horario");
        timeColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getHorario().format(TIME_FORMATTER)
        ));

        TableColumn<Consultation, String> professionalColumn = new TableColumn<>("Profissional");
        professionalColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProfissional()));

        TableColumn<Consultation, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        consultationsTable.getColumns().addAll(patientColumn, dateColumn, timeColumn, professionalColumn, statusColumn);
        consultationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        consultationsTable.setPlaceholder(new Label("Nenhuma consulta para a data selecionada."));
        consultationsTable.setFixedCellSize(40);
        consultationsTable.setMinHeight(280);
        consultationsTable.setPrefHeight(340);
        VBox.setVgrow(consultationsTable, Priority.ALWAYS);

        card.getChildren().addAll(header, consultationsTable);
        return card;
    }

    private void configureFormFields() {
        patientComboBox.setPromptText("Selecione um paciente");
        consultationTimeField.setPromptText("Ex.: 09:30");
        professionalField.setPromptText("Nome do profissional");

        statusComboBox.getItems().setAll("Confirmada", "Aguardando", "Concluida", "Cancelada");
        statusComboBox.setValue("Confirmada");

        patientComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient patient) {
                return patient == null ? "" : patient.getNome();
            }

            @Override
            public Patient fromString(String string) {
                return null;
            }
        });

        patientComboBox.setMaxWidth(Double.MAX_VALUE);
        consultationDatePicker.setMaxWidth(Double.MAX_VALUE);
        consultationTimeField.setMaxWidth(Double.MAX_VALUE);
        professionalField.setMaxWidth(Double.MAX_VALUE);
        statusComboBox.setMaxWidth(Double.MAX_VALUE);
    }

    private void addFormField(GridPane gridPane, int column, int row, String labelText, Node input) {
        VBox box = new VBox(8);
        Label label = new Label(labelText);
        label.getStyleClass().add("input-label");
        box.getChildren().addAll(label, input);
        GridPane.setHgrow(box, Priority.ALWAYS);
        if (input instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        gridPane.add(box, column, row);
    }

    private VBox createMetricCard(String labelText, Label valueLabel, String detailText, boolean accent) {
        VBox card = new VBox(6);
        card.getStyleClass().add("metric-card");
        if (accent) {
            card.getStyleClass().add("metric-card-accent");
        }
        card.setPadding(new Insets(16));
        card.setPrefWidth(210);

        Label label = new Label(labelText);
        label.getStyleClass().add("metric-label");
        valueLabel.getStyleClass().add("metric-value");
        valueLabel.setWrapText(true);
        Label detail = new Label(detailText);
        detail.getStyleClass().add("metric-detail");
        detail.setWrapText(true);

        card.getChildren().addAll(label, valueLabel, detail);
        return card;
    }

    private void updateConsultationMetrics(int count) {
        visibleConsultationsValue.setText(String.valueOf(count));
        visibleConsultationsBadge.setText(count + " exibida(s)");
    }
}
