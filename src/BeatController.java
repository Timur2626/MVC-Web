public class BeatController implements ControllerInterface {

    private final BeatModelInterface model;
    private final DJView view;

    public BeatController(BeatModelInterface model) {
        this.model = model;

        model.initialize();

        view = new DJView(this, model);
        view.createView();
        view.setRunning(false);
    }

    @Override
    public void start() {
        model.on();
        view.setRunning(true);
    }

    @Override
    public void stop() {
        model.off();
        view.setRunning(false);
    }

    @Override
    public void increaseBPM() {
        setBPM(model.getBPM() + 1);
    }

    @Override
    public void decreaseBPM() {
        setBPM(model.getBPM() - 1);
    }

    @Override
    public void setBPM(int bpm) {
        if (bpm < 30 || bpm > 300) {
            throw new IllegalArgumentException(
                    "Введите темп от 30 до 300 BPM"
            );
        }

        model.setBPM(bpm);
    }
}