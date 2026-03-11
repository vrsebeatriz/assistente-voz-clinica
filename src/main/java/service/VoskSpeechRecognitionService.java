package service;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoskSpeechRecognitionService implements SpeechRecognitionService {

    private static final Pattern TEXT_PATTERN = Pattern.compile("\"text\"\\s*:\\s*\"(.*?)\"");

    private final Path modelPath;
    private final ExecutorService executorService;
    private final AtomicBoolean listening;
    private final Object recognizerLock;

    private volatile Model model;
    private volatile TargetDataLine microphoneLine;
    private volatile Recognizer activeRecognizer;

    public VoskSpeechRecognitionService(Path modelPath) {
        this.modelPath = modelPath;
        this.listening = new AtomicBoolean(false);
        this.recognizerLock = new Object();
        this.executorService = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "vosk-listener");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void startListening(RecognitionListener listener) {
        Objects.requireNonNull(listener, "listener");

        if (!Files.isDirectory(modelPath)) {
            listener.onError("Modelo Vosk nao encontrado em " + modelPath.toAbsolutePath(), null);
            listener.onStatusChanged(false, "Parado");
            return;
        }
        if (!listening.compareAndSet(false, true)) {
            listener.onStatusChanged(true, "Ouvindo");
            return;
        }

        listener.onStatusChanged(true, "Ouvindo");
        executorService.submit(() -> listenLoop(listener));
    }

    @Override
    public void stopListening() {
        listening.set(false);
        closeMicrophone();
    }

    @Override
    public boolean isListening() {
        return listening.get();
    }

    @Override
    public String getEngineDescription() {
        return "Vosk offline com reconhecimento livre";
    }

    @Override
    public void updateCommandPhrases(List<String> phrases) {
        // O reconhecimento usa vocabulario livre para aumentar a chance de capturar
        // nomes de pacientes fora de uma gramatica fechada.
    }

    @Override
    public void close() {
        stopListening();
        executorService.shutdownNow();
        synchronized (this) {
            if (model != null) {
                model.close();
                model = null;
            }
        }
    }

    private void listenLoop(RecognitionListener listener) {
        AudioFormat audioFormat = new AudioFormat(16000.0f, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);

        if (!AudioSystem.isLineSupported(info)) {
            listening.set(false);
            listener.onError("Nenhum microfone compativel com o formato esperado foi encontrado.", null);
            listener.onStatusChanged(false, "Parado");
            return;
        }

        try {
            microphoneLine = (TargetDataLine) AudioSystem.getLine(info);
            microphoneLine.open(audioFormat);
            microphoneLine.start();

            try (Recognizer recognizer = createRecognizer()) {
                byte[] buffer = new byte[4096];
                while (listening.get()) {
                    int bytesRead = microphoneLine.read(buffer, 0, buffer.length);
                    if (bytesRead <= 0) {
                        continue;
                    }
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        dispatchRecognizedText(listener, extractText(recognizer.getResult()));
                    }
                }
                dispatchRecognizedText(listener, extractText(recognizer.getFinalResult()));
            }
        } catch (LineUnavailableException exception) {
            listener.onError("Nao foi possivel acessar o microfone.", exception);
        } catch (Exception exception) {
            listener.onError("Falha durante o reconhecimento de voz.", exception);
        } finally {
            synchronized (recognizerLock) {
                activeRecognizer = null;
            }
            listening.set(false);
            closeMicrophone();
            listener.onStatusChanged(false, "Parado");
        }
    }

    private synchronized Model loadModel() throws Exception {
        if (model == null) {
            model = new Model(modelPath.toString());
        }
        return model;
    }

    private Recognizer createRecognizer() throws Exception {
        synchronized (recognizerLock) {
            activeRecognizer = new Recognizer(loadModel(), 16000.0f);
            return activeRecognizer;
        }
    }

    private void dispatchRecognizedText(RecognitionListener listener, String text) {
        if (text != null && !text.isBlank()) {
            listener.onRecognizedText(text.trim());
        }
    }

    private String extractText(String json) {
        Matcher matcher = TEXT_PATTERN.matcher(json == null ? "" : json);
        return matcher.find() ? matcher.group(1) : "";
    }

    private void closeMicrophone() {
        TargetDataLine line = microphoneLine;
        if (line != null) {
            try {
                line.stop();
            } catch (Exception ignored) {
            }
            line.close();
            microphoneLine = null;
        }
    }

}
