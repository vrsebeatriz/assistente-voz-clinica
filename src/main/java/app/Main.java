package app;

import controller.ConsultationController;
import controller.MainController;
import controller.PatientController;
import controller.VoiceController;
import dao.ConsultationDAO;
import dao.PatientDAO;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.CommandInterpreterService;
import service.SpeechRecognitionService;
import service.VoskSpeechRecognitionService;
import util.DatabaseInitializer;

import java.nio.file.Path;

public class Main extends Application {

    private MainController mainController;

    @Override
    public void start(Stage stage) {
        DatabaseInitializer.initialize();

        PatientController patientController = new PatientController(new PatientDAO());
        ConsultationController consultationController = new ConsultationController(new ConsultationDAO());
        SpeechRecognitionService speechRecognitionService =
                new VoskSpeechRecognitionService(Path.of("models", "vosk-model-small-pt-0.3"));
        VoiceController voiceController = new VoiceController(speechRecognitionService);

        mainController = new MainController(
                stage,
                patientController,
                consultationController,
                voiceController,
                new CommandInterpreterService()
        );

        Scene scene = new Scene(mainController.getView(), 1380, 840);
        String css = getClass().getResource("/styles/app.css").toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Assistente de Voz para Gestao Clinica");
        stage.setScene(scene);
        stage.setMinWidth(1200);
        stage.setMinHeight(760);
        stage.show();
    }

    @Override
    public void stop() {
        if (mainController != null) {
            mainController.shutdown();
        }
    }
}
