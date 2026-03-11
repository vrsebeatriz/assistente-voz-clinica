package service;

import java.util.List;

public interface SpeechRecognitionService extends AutoCloseable {

    void startListening(RecognitionListener listener);

    void stopListening();

    boolean isListening();

    String getEngineDescription();

    void updateCommandPhrases(List<String> phrases);

    @Override
    void close();
}
