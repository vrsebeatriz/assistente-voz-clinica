package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainView extends BorderPane {

    private final Button patientsNavButton = new Button("Pacientes");
    private final Button consultationsNavButton = new Button("Consultas");
    private final Button closeNavButton = new Button("Fechar sistema");

    private final Button startListeningButton = new Button("Iniciar escuta");
    private final Button stopListeningButton = new Button("Parar escuta");
    private final Button executeTextButton = new Button("Executar texto");

    private final Label listeningStatusValue = new Label("Parado");
    private final Label engineValue = new Label("Vosk offline");
    private final Label sectionTitle = new Label("Pacientes");
    private final Label sectionSubtitle = new Label("Cadastre, pesquise e interaja com o sistema por comandos simples.");

    private final TextArea recognizedTextArea = new TextArea();
    private final TextArea responseTextArea = new TextArea();
    private final TextField manualCommandField = new TextField();
    private final VBox contentHost = new VBox();
    private final ScrollPane workspaceScrollPane = new ScrollPane();

    public MainView() {
        getStyleClass().add("app-root");
        setTop(buildHeader());
        setLeft(buildSidebar());
        setCenter(buildWorkspace());
        configureAreas();
        setListeningStatus("Parado", false);
    }

    public Button getPatientsNavButton() {
        return patientsNavButton;
    }

    public Button getConsultationsNavButton() {
        return consultationsNavButton;
    }

    public Button getCloseNavButton() {
        return closeNavButton;
    }

    public Button getStartListeningButton() {
        return startListeningButton;
    }

    public Button getStopListeningButton() {
        return stopListeningButton;
    }

    public Button getExecuteTextButton() {
        return executeTextButton;
    }

    public String getManualCommandText() {
        return manualCommandField.getText();
    }

    public void clearManualCommand() {
        manualCommandField.clear();
    }

    public void setEngineDescription(String description) {
        engineValue.setText(description);
    }

    public void setListeningStatus(String status, boolean listening) {
        listeningStatusValue.setText(status);
        listeningStatusValue.getStyleClass().removeAll("status-active", "status-idle");
        listeningStatusValue.getStyleClass().add(listening ? "status-active" : "status-idle");
    }

    public void setContent(Node node) {
        contentHost.getChildren().setAll(node);
        if (node instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(region, Priority.ALWAYS);
        }
    }

    public void setCurrentSection(String title, String subtitle) {
        sectionTitle.setText(title);
        sectionSubtitle.setText(subtitle);
    }

    public void setActiveNavigation(Button activeButton) {
        for (Button button : new Button[]{patientsNavButton, consultationsNavButton}) {
            button.getStyleClass().remove("nav-button-active");
        }
        activeButton.getStyleClass().add("nav-button-active");
    }

    public void appendRecognizedText(String text) {
        prependLine(recognizedTextArea, text);
    }

    public void appendSystemResponse(String text) {
        prependLine(responseTextArea, text);
    }

    private Node buildHeader() {
        HBox header = new HBox(18);
        header.getStyleClass().add("top-banner");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(30, 30, 26, 30));

        VBox titleBox = new VBox(6);
        Label kicker = new Label("CENTRAL OPERACIONAL");
        kicker.getStyleClass().add("banner-kicker");
        Label title = new Label("Assistente Clinico por Voz");
        title.getStyleClass().add("banner-title");
        Label subtitle = new Label("Navegacao, agenda e cadastro em um fluxo unico para recepcao e demonstracao.");
        subtitle.getStyleClass().add("banner-subtitle");
        titleBox.getChildren().addAll(kicker, title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox badge = new VBox(4);
        badge.getStyleClass().add("header-badge");
        Label badgeLabel = new Label("Ambiente");
        badgeLabel.getStyleClass().add("badge-label");
        Label badgeValue = new Label("SQLite local");
        badgeValue.getStyleClass().add("header-badge-value");
        badge.getChildren().addAll(badgeLabel, badgeValue);

        header.getChildren().addAll(titleBox, spacer, badge);
        return header;
    }

    private Node buildSidebar() {
        VBox sidebar = new VBox(14);
        sidebar.getStyleClass().addAll("sidebar", "sidebar-content");
        sidebar.setPadding(new Insets(24));
        sidebar.setPrefWidth(240);
        sidebar.setFillWidth(true);

        Label menuLabel = new Label("Menu");
        menuLabel.getStyleClass().add("sidebar-title");

        configureNavButton(patientsNavButton);
        configureNavButton(consultationsNavButton);
        configureNavButton(closeNavButton);
        closeNavButton.getStyleClass().add("danger-button");

        VBox hintsCard = new VBox(8);
        hintsCard.getStyleClass().addAll("panel-card", "hint-card");
        hintsCard.setPadding(new Insets(16));
        Label hintsTitle = new Label("Comandos-chave");
        hintsTitle.getStyleClass().add("sidebar-card-title");
        hintsTitle.setWrapText(true);
        Label hintsText = new Label(
                "abrir pacientes\nbuscar paciente [nome]\nmostrar consultas de hoje\nlimpar formulario"
        );
        hintsText.getStyleClass().add("muted-text");
        hintsText.setWrapText(true);
        hintsCard.getChildren().addAll(hintsTitle, hintsText);

        sidebar.getChildren().addAll(
                menuLabel,
                patientsNavButton,
                consultationsNavButton,
                closeNavButton,
                hintsCard
        );

        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.getStyleClass().add("sidebar-scroll");
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sidebarScroll.setPrefWidth(240);
        return sidebarScroll;
    }

    private Node buildWorkspace() {
        VBox wrapper = new VBox(18);
        wrapper.getStyleClass().add("workspace");
        wrapper.setPadding(new Insets(20, 20, 24, 0));
        wrapper.setFillWidth(true);

        FlowPane summaryFlow = new FlowPane();
        summaryFlow.getStyleClass().add("workspace-summary");
        summaryFlow.setHgap(14);
        summaryFlow.setVgap(14);
        summaryFlow.prefWrapLengthProperty().bind(wrapper.widthProperty());
        summaryFlow.getChildren().addAll(
                buildSectionHeroCard(),
                buildSummaryCard(
                "Atalho operacional",
                "Troque de tela pelo menu, navegue por voz ou valide o interpretador no modo manual.",
                "Sugestao: diga 'mostrar consultas de hoje'."
                ),
                buildStatusCard()
        );

        VBox contentShell = new VBox();
        contentShell.getStyleClass().addAll("panel-card", "content-shell");
        contentHost.getStyleClass().add("content-host");
        contentShell.getChildren().add(contentHost);

        wrapper.getChildren().addAll(summaryFlow, contentShell, buildVoicePanel());

        workspaceScrollPane.getStyleClass().add("workspace-scroll");
        workspaceScrollPane.setFitToWidth(true);
        workspaceScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        workspaceScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        workspaceScrollPane.setPannable(true);
        workspaceScrollPane.setContent(wrapper);
        return workspaceScrollPane;
    }

    private VBox buildSectionHeroCard() {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("panel-card", "summary-hero");
        card.setPadding(new Insets(20));
        card.setPrefWidth(480);
        card.setMinWidth(360);

        Label label = new Label("Tela ativa");
        label.getStyleClass().add("eyebrow-label");
        sectionTitle.getStyleClass().add("section-title");
        sectionSubtitle.getStyleClass().add("section-subtitle");
        sectionSubtitle.setWrapText(true);

        Label helper = new Label("Fluxo principal sempre visivel: navegue na lateral, execute comandos e acompanhe as respostas no mesmo workspace.");
        helper.getStyleClass().add("muted-text");
        helper.setWrapText(true);

        card.getChildren().addAll(label, sectionTitle, sectionSubtitle, helper);
        return card;
    }

    private VBox buildSummaryCard(String title, String description, String footer) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("panel-card", "summary-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setMinWidth(250);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("summary-title");
        titleLabel.setWrapText(true);
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("muted-text");
        descriptionLabel.setWrapText(true);
        Label footerLabel = new Label(footer);
        footerLabel.getStyleClass().add("summary-footer");
        footerLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, descriptionLabel, footerLabel);
        return card;
    }

    private VBox buildStatusCard() {
        VBox card = new VBox(14);
        card.getStyleClass().addAll("panel-card", "summary-card", "status-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(320);
        card.setMinWidth(280);

        Label title = new Label("Monitor de voz");
        title.getStyleClass().add("summary-title");
        title.setWrapText(true);

        VBox engineBlock = new VBox(4);
        Label engineLabel = new Label("Motor configurado");
        engineLabel.getStyleClass().add("summary-label");
        engineValue.getStyleClass().add("summary-value");
        engineValue.setWrapText(true);
        engineBlock.getChildren().addAll(engineLabel, engineValue);

        VBox statusBlock = new VBox(6);
        Label statusLabel = new Label("Escuta");
        statusLabel.getStyleClass().add("summary-label");
        statusBlock.getChildren().addAll(statusLabel, listeningStatusValue);

        Label helper = new Label("Teste manual habilitado para validar o fluxo mesmo sem microfone ativo.");
        helper.getStyleClass().add("summary-footer");
        helper.setWrapText(true);

        card.getChildren().addAll(title, engineBlock, statusBlock, helper);
        return card;
    }

    private Node buildVoicePanel() {
        VBox card = new VBox(16);
        card.getStyleClass().addAll("panel-card", "voice-card");
        card.setPadding(new Insets(20));

        VBox statusRow = new VBox(6);
        statusRow.getStyleClass().add("voice-header");
        Label voiceTitle = new Label("Modulo de voz");
        voiceTitle.getStyleClass().add("card-title");
        Label statusText = new Label("Transcricao, resposta e simulacao manual em um unico painel.");
        statusText.getStyleClass().add("muted-text");
        statusText.setWrapText(true);
        statusRow.getChildren().addAll(voiceTitle, statusText);

        HBox actionRow = new HBox(12, startListeningButton, stopListeningButton);
        startListeningButton.getStyleClass().add("primary-button");
        stopListeningButton.getStyleClass().add("secondary-button");

        FlowPane logsPane = new FlowPane();
        logsPane.getStyleClass().add("voice-logs");
        logsPane.setHgap(16);
        logsPane.setVgap(16);
        logsPane.prefWrapLengthProperty().bind(card.widthProperty());
        logsPane.getChildren().addAll(
                createAreaCard("Texto reconhecido", recognizedTextArea),
                createAreaCard("Resposta do sistema", responseTextArea)
        );

        Label helperText = new Label("Use o campo abaixo para testar o interpretador mesmo sem o modelo do Vosk configurado.");
        helperText.getStyleClass().add("muted-text");
        helperText.setWrapText(true);

        HBox manualRow = new HBox(12);
        manualCommandField.setPromptText("Ex.: buscar paciente Joao");
        HBox.setHgrow(manualCommandField, Priority.ALWAYS);
        manualCommandField.getStyleClass().add("command-field");
        executeTextButton.getStyleClass().add("secondary-button");
        manualRow.getChildren().addAll(manualCommandField, executeTextButton);

        card.getChildren().addAll(statusRow, actionRow, logsPane, helperText, manualRow);
        return card;
    }

    private VBox createAreaCard(String title, TextArea area) {
        VBox card = new VBox(8);
        card.getStyleClass().add("log-card");
        card.setPrefWidth(420);
        card.setMinWidth(320);
        Label label = new Label(title);
        label.getStyleClass().add("input-label");
        area.setPrefRowCount(6);
        area.setMinHeight(160);
        VBox.setVgrow(area, Priority.ALWAYS);
        card.getChildren().addAll(label, area);
        return card;
    }

    private void configureAreas() {
        recognizedTextArea.setEditable(false);
        recognizedTextArea.setWrapText(true);
        recognizedTextArea.setPromptText("O texto reconhecido aparecera aqui.");

        responseTextArea.setEditable(false);
        responseTextArea.setWrapText(true);
        responseTextArea.setPromptText("As respostas da aplicacao aparecerao aqui.");
    }

    private void configureNavButton(Button button) {
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
    }

    private void prependLine(TextArea area, String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        String current = area.getText();
        if (current == null || current.isBlank()) {
            area.setText(text);
        } else {
            area.setText(text + System.lineSeparator() + current);
        }
    }
}
