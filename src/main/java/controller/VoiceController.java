package controller;

import service.RecognitionListener;
import service.SpeechRecognitionService;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VoiceController {

    private final SpeechRecognitionService speechRecognitionService;

    private Consumer<String> recognizedTextHandler = text -> {
    };
    private Consumer<String> errorHandler = message -> {
    };
    private BiConsumer<Boolean, String> statusHandler = (listening, message) -> {
    };

    public VoiceController(SpeechRecognitionService speechRecognitionService) {
        this.speechRecognitionService = speechRecognitionService;
    }

    public void setRecognizedTextHandler(Consumer<String> recognizedTextHandler) {
        this.recognizedTextHandler = recognizedTextHandler;
    }

    public void setErrorHandler(Consumer<String> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setStatusHandler(BiConsumer<Boolean, String> statusHandler) {
        this.statusHandler = statusHandler;
    }

    public void startListening() {
        speechRecognitionService.startListening(new RecognitionListener() {
            @Override
            public void onStatusChanged(boolean listening, String message) {
                statusHandler.accept(listening, message);
            }

            @Override
            public void onRecognizedText(String text) {
                recognizedTextHandler.accept(text);
            }

            @Override
            public void onError(String message, Throwable cause) {
                errorHandler.accept(message);
            }
        });
    }

    public void stopListening() {
        speechRecognitionService.stopListening();
    }

    public boolean isListening() {
        return speechRecognitionService.isListening();
    }

    public String getEngineDescription() {
        return speechRecognitionService.getEngineDescription();
    }

    public void updateCommandPhrases(List<String> phrases) {
        speechRecognitionService.updateCommandPhrases(phrases);
    }

    public void shutdown() {
        speechRecognitionService.close();
    }
}
