import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class BeatModel implements BeatModelInterface, Runnable {

    private final List<BeatObserver> beatObservers = new ArrayList<>();

    private final List<BPMObserver> bpmObservers = new ArrayList<>();

    private volatile int bpm = 90;
    private volatile boolean stop = true;

    private Thread thread;
    private Clip clip;

    @Override
    public void initialize() {
        try {
            File resource = new File("clap.wav");

            clip = AudioSystem.getClip();

            try (var audio = AudioSystem.getAudioInputStream(resource)) {
                clip.open(audio);
            }
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Не удалось загрузить clap.wav",
                    exception
            );
        }
    }

    @Override
    public void on() {
        if (clip == null) {
            throw new IllegalStateException(
                    "Сначала нужно вызвать initialize()"
            );
        }

        if (thread != null && thread.isAlive()) {
            return;
        }

        bpm = 90;
        stop = false;

        notifyBPMObservers();

        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void off() {
        stop = true;

        if (thread != null) {
            thread.interrupt();

            try {
                thread.join();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        stopBeat();
        notifyBPMObservers();
    }

    @Override
    public void run() {
        while (!stop) {
            playBeat();
            notifyBeatObservers();

            try {
                Thread.sleep(60000 / bpm);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void setBPM(int bpm) {
        if (bpm <= 0) {
            throw new IllegalArgumentException(
                    "Темп должен быть больше нуля"
            );
        }

        this.bpm = bpm;

        notifyBPMObservers();
    }

    @Override
    public int getBPM() {
        return stop ? 0 : bpm;
    }

    @Override
    public void registerObserver(BeatObserver observer) {
        beatObservers.add(observer);
    }

    @Override
    public void removeObserver(BeatObserver observer) {
        beatObservers.remove(observer);
    }

    @Override
    public void registerObserver(BPMObserver observer) {
        bpmObservers.add(observer);
    }

    @Override
    public void removeObserver(BPMObserver observer) {
        bpmObservers.remove(observer);
    }

    public void notifyBeatObservers() {
        for (BeatObserver observer : beatObservers) {
            observer.updateBeat();
        }
    }

    public void notifyBPMObservers() {
        for (BPMObserver observer : bpmObservers) {
            observer.updateBPM();
        }
    }

    public void playBeat() {
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void stopBeat() {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }
}