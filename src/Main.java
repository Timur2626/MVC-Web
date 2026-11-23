import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BeatModelInterface model = new BeatModel();

            new BeatController(model);
        });
    }
}