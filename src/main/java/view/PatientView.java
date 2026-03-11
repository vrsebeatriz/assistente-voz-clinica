package view;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
import model.Patient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientView extends VBox {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final TextField nameField = new TextField();
    private final TextField cpfField = new TextField();
    private final TextField phoneField = new TextField();
    private final DatePicker birthDatePicker = new DatePicker();
    private final TextField searchField = new TextField();

    private final Button registerButton = new Button("Cadastrar");
    private final Button clearButton = new Button("Limpar");
    private final Button searchButton = new Button("Buscar");
    private final Button deleteButton = new Button("Excluir selecionado");

    private final Label resultLabel = new Label("Nenhuma acao executada.");
    private final Label visiblePatientsValue = new Label("0");
    private final Label visiblePatientsBadge = new Label("0 exibido(s)");
    private final TableView<Patient> patientsTable = new TableView<>();

    public PatientView() {
        setSpacing(16);
        setPadding(new Insets(0, 0, 6, 0));

        VBox introCard = createIntroCard();
        VBox formCard = createFormCard();
        VBox tableCard = createTableCard();

        getChildren().addAll(introCard, formCard, tableCard);
        VBox.setVgrow(tableCard, Priority.ALWAYS);
    }

    public Button getRegisterButton() {
        return registerButton;
    }

    public Button getClearButton() {
        return clearButton;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getDeleteButton() {
        return deleteButton;
    }

    public TableView<Patient> getPatientsTable() {
        return patientsTable;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public Patient getSelectedPatient() {
        return patientsTable.getSelectionModel().getSelectedItem();
    }

    public String getPatientName() {
        return nameField.getText();
    }

    public String getCpf() {
        return cpfField.getText();
    }

    public String getPhone() {
        return phoneField.getText();
    }

    public LocalDate getBirthDate() {
        return birthDatePicker.getValue();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public void setSearchText(String text) {
        searchField.setText(text);
    }

    public void setResultMessage(String message) {
        resultLabel.setText(message);
    }

    public void clearForm() {
        nameField.clear();
        cpfField.clear();
        phoneField.clear();
        birthDatePicker.setValue(null);
    }

    public void fillForm(Patient patient) {
        if (patient == null) {
            return;
        }
        nameField.setText(patient.getNome());
        cpfField.setText(patient.getCpf());
        phoneField.setText(patient.getTelefone());
        birthDatePicker.setValue(patient.getDataNascimento());
    }

    public void setPatients(List<Patient> patients) {
        patientsTable.getItems().setAll(patients);
        updatePatientMetrics(patients.size());
    }

    private VBox createIntroCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(20));

        Label title = new Label("Cadastro e busca de pacientes");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label(
                "Fluxo pensado para recepcao e demonstracao: cadastre rapidamente, pesquise por nome e valide a navegacao por voz."
        );
        subtitle.getStyleClass().add("muted-text");
        subtitle.setWrapText(true);

        FlowPane metrics = new FlowPane();
        metrics.setHgap(12);
        metrics.setVgap(12);
        metrics.getChildren().addAll(
                createMetricCard("Registros visiveis", visiblePatientsValue, "Pacientes exibidos na tabela", true),
                createMetricCard("Acao principal", new Label("Cadastrar"), "Grave um novo paciente na base", false),
                createMetricCard("Atalho de voz", new Label("buscar paciente [nome]"), "Comando valido para qualquer paciente cadastrado", false)
        );

        card.getChildren().addAll(title, subtitle, metrics);
        return card;
    }

    private VBox createFormCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(20));

        Label title = new Label("Dados do paciente");
        title.getStyleClass().add("card-title");
        Label subtitle = new Label("Preencha os campos principais e use a limpeza rapida para reiniciar o formulario.");
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

        configureField(nameField, "Nome do paciente");
        configureField(cpfField, "CPF");
        configureField(phoneField, "Telefone");
        birthDatePicker.setPromptText("Data de nascimento");

        addFormField(formGrid, 0, 0, "Nome", nameField);
        addFormField(formGrid, 1, 0, "CPF", cpfField);
        addFormField(formGrid, 0, 1, "Telefone", phoneField);
        addFormField(formGrid, 1, 1, "Nascimento", birthDatePicker);

        HBox buttons = new HBox(12, registerButton, clearButton);
        registerButton.getStyleClass().add("primary-button");
        clearButton.getStyleClass().add("secondary-button");

        VBox searchBlock = new VBox(8);
        Label searchLabel = new Label("Buscar paciente");
        searchLabel.getStyleClass().add("input-label");
        HBox searchRow = new HBox(12);
        searchField.setPromptText("Digite um nome ou use a voz");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchButton.getStyleClass().add("secondary-button");
        searchRow.getChildren().addAll(searchField, searchButton);
        searchBlock.getChildren().addAll(searchLabel, searchRow);

        resultLabel.getStyleClass().add("result-text");
        resultLabel.setWrapText(true);
        card.getChildren().addAll(title, subtitle, formGrid, buttons, searchBlock, resultLabel);
        return card;
    }

    private VBox createTableCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("panel-card");
        card.setPadding(new Insets(20));

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Pacientes cadastrados");
        title.getStyleClass().add("card-title");
        deleteButton.getStyleClass().add("danger-button");
        deleteButton.disableProperty().bind(patientsTable.getSelectionModel().selectedItemProperty().isNull());
        visiblePatientsBadge.getStyleClass().addAll("soft-chip", "accent-chip");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, deleteButton, visiblePatientsBadge);

        TableColumn<Patient, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNome()));

        TableColumn<Patient, String> cpfColumn = new TableColumn<>("CPF");
        cpfColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCpf()));

        TableColumn<Patient, String> phoneColumn = new TableColumn<>("Telefone");
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefone()));

        TableColumn<Patient, String> birthColumn = new TableColumn<>("Nascimento");
        birthColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDataNascimento() == null
                        ? "-"
                        : data.getValue().getDataNascimento().format(DATE_FORMATTER)
        ));

        patientsTable.getColumns().addAll(nameColumn, cpfColumn, phoneColumn, birthColumn);
        patientsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patientsTable.setPlaceholder(new Label("Nenhum paciente encontrado."));
        patientsTable.setFixedCellSize(40);
        patientsTable.setMinHeight(280);
        patientsTable.setPrefHeight(340);
        VBox.setVgrow(patientsTable, Priority.ALWAYS);

        card.getChildren().addAll(header, patientsTable);
        return card;
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

    private void configureField(TextField field, String promptText) {
        field.setPromptText(promptText);
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

    private void updatePatientMetrics(int count) {
        visiblePatientsValue.setText(String.valueOf(count));
        visiblePatientsBadge.setText(count + " exibido(s)");
    }
}
