import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class DJView implements BeatObserver, BPMObserver {

    private final ControllerInterface controller;
    private final BeatModelInterface model;

    private JFrame frame;
    private JLabel bpmLabel;
    private JProgressBar beatBar;

    private JTextField bpmField;

    private JButton startButton;
    private JButton stopButton;
    private JButton increaseButton;
    private JButton decreaseButton;
    private JButton setButton;

    private Timer beatTimer;

    public DJView(
            ControllerInterface controller,
            BeatModelInterface model
    ) {
        this.controller = controller;
        this.model = model;
    }

    public void createView() {
        frame = new JFrame("DJ — MVC");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        bpmLabel = new JLabel(
                "Ритм выключен",
                SwingConstants.CENTER
        );

        beatBar = new JProgressBar(0, 100);

        beatTimer = new Timer(100, event -> beatBar.setValue(0));
        beatTimer.setRepeats(false);

        JPanel displayPanel = new JPanel(new GridLayout(2, 1));
        displayPanel.add(bpmLabel);
        displayPanel.add(beatBar);

        startButton = new JButton("Старт");
        stopButton = new JButton("Стоп");

        startButton.addActionListener(event -> controller.start());
        stopButton.addActionListener(event -> controller.stop());

        JPanel playbackPanel = new JPanel();
        playbackPanel.add(startButton);
        playbackPanel.add(stopButton);

        decreaseButton = new JButton("-");
        increaseButton = new JButton("+");
        bpmField = new JTextField("90", 5);
        setButton = new JButton("Установить BPM");

        decreaseButton.addActionListener(
                event -> runCommand(() -> controller.decreaseBPM())
        );

        increaseButton.addActionListener(
                event -> runCommand(() -> controller.increaseBPM())
        );

        setButton.addActionListener(event -> setTempo());

        JPanel tempoPanel = new JPanel();
        tempoPanel.add(decreaseButton);
        tempoPanel.add(bpmField);
        tempoPanel.add(increaseButton);
        tempoPanel.add(setButton);

        JPanel controlsPanel = new JPanel(new GridLayout(2, 1));
        controlsPanel.add(playbackPanel);
        controlsPanel.add(tempoPanel);

        frame.add(displayPanel, BorderLayout.CENTER);
        frame.add(controlsPanel, BorderLayout.SOUTH);

        model.registerObserver((BeatObserver) this);
        model.registerObserver((BPMObserver) this);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void setTempo() {
        runCommand(() -> {
            int bpm = Integer.parseInt(bpmField.getText().trim());
            controller.setBPM(bpm);
        });
    }

    private void runCommand(Runnable command) {
        try {
            command.run();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    frame,
                    "Введите целое число от 30 до 300",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void setRunning(boolean running) {
        startButton.setEnabled(!running);
        stopButton.setEnabled(running);

        increaseButton.setEnabled(running);
        decreaseButton.setEnabled(running);
        setButton.setEnabled(running);
        bpmField.setEnabled(running);
    }

    @Override
    public void updateBPM() {
        SwingUtilities.invokeLater(() -> {
            int bpm = model.getBPM();

            if (bpm == 0) {
                bpmLabel.setText("Ритм выключен");
            } else {
                bpmLabel.setText("Текущий темп: " + bpm + " BPM");
                bpmField.setText(String.valueOf(bpm));
            }
        });
    }

    @Override
    public void updateBeat() {
        SwingUtilities.invokeLater(() -> {
            beatBar.setValue(100);
            beatTimer.restart();
        });
    }
}