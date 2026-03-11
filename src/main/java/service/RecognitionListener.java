package service;

public interface RecognitionListener {

    void onStatusChanged(boolean listening, String message);

    void onRecognizedText(String text);

    void onError(String message, Throwable cause);
}
