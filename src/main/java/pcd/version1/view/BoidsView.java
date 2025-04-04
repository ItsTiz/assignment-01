package pcd.version1.view;

import pcd.version1.controller.InputListener;
import pcd.version1.model.BoidsModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.BrokenBarrierException;

public class BoidsView implements ChangeListener, ActionListener {
    private JFrame frame;

    private BoidsPanel boidsPanel;
    private JSlider cohesionSlider, separationSlider, alignmentSlider;
    private JButton startButton, resumeButton, stopButton;
    private JTextField boidCountField;
    private final BoidsModel model;
    private final int width;
    private final int height;

    private final ArrayList<InputListener> listeners;

    public BoidsView(BoidsModel model, int width, int height) {
        this.model = model;
        this.width = width;
        this.height = height;

        listeners = new ArrayList<>();

        buildInterface(model, width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void addListener(InputListener l) {
        listeners.add(l);
    }

    private void notifyStarted() throws BrokenBarrierException, InterruptedException {
        for (InputListener listener : listeners) {
            listener.started();
        }
    }

    private void notifyResumed() {
        listeners.forEach(InputListener::resumed);
    }

    private void notifyPaused() {
        listeners.forEach(InputListener::paused);
    }

    private void notifyStopped() {
        listeners.forEach(InputListener::stopped);
    }

    private void notifySeparationWeightChanged(int weight) {
        listeners.forEach(listener -> listener.separationWeightChanged(weight));
    }

    private void notifyAlignmentWeightChanged(int weight) {
        listeners.forEach(listener -> listener.alignmentWeightChanged(weight));
    }

    private void notifyCohesionWeightChanged(int weight) {
        listeners.forEach(listener -> listener.cohesionWeightChanged(weight));
    }

    private void notifyBoidsNumberChanged() {
        try {
            int newCount = Integer.parseInt(boidCountField.getText().trim());
            if (newCount > 0) {
                listeners.forEach(listener -> listener.boidsNumberChanged(newCount));
            } else {
                JOptionPane.showMessageDialog(frame, "Enter a positive number!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid number! Please enter an integer.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildInterface(BoidsModel model, int width, int height) {
        frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel cp = new JPanel();
        LayoutManager layout = new BorderLayout();
        cp.setLayout(layout);

        JPanel header = new JPanel();
        cp.add(BorderLayout.NORTH, header);

        startButton = makeButton("Start");
        resumeButton = makeButton("Pause");
        stopButton = makeButton("Stop");

        resumeButton.setEnabled(false);
        stopButton.setEnabled(false);

        JLabel boidLabel = new JLabel("Boids:");
        boidCountField = new JTextField("1500", 5);

        startButton.addActionListener(e -> notifyBoidsNumberChanged());

        header.add(boidLabel);
        header.add(boidCountField);
        header.add(startButton);
        header.add(resumeButton);
        header.add(stopButton);

        boidsPanel = new BoidsPanel(this, model);
        cp.add(BorderLayout.CENTER, boidsPanel);

        JPanel slidersPanel = new JPanel();

        cohesionSlider = makeSlider();
        separationSlider = makeSlider();
        alignmentSlider = makeSlider();

        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);

        cp.add(BorderLayout.SOUTH, slidersPanel);

        frame.setContentPane(cp);

        frame.setVisible(true);
    }

    private JSlider makeSlider() {
        var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        Hashtable labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("0"));
        labelTable.put(10, new JLabel("1"));
        labelTable.put(20, new JLabel("2"));
        slider.setLabelTable(labelTable);
        slider.setPaintLabels(true);
        slider.addChangeListener(this);
        return slider;
    }

    private JButton makeButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 40));
        button.addActionListener(this);
        return button;
    }

    public void update(int frameRate) {
        boidsPanel.setFrameRate(frameRate);
        boidsPanel.repaint();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == separationSlider) {
            notifySeparationWeightChanged(separationSlider.getValue());
        } else if (e.getSource() == cohesionSlider) {
            notifyCohesionWeightChanged(cohesionSlider.getValue());
        } else {
            notifyAlignmentWeightChanged(alignmentSlider.getValue());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Start": {
                notifyBoidsNumberChanged();
                try {
                    notifyStarted();
                } catch (BrokenBarrierException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                resumeButton.setEnabled(true);
                stopButton.setEnabled(true);
                startButton.setEnabled(false);
                boidCountField.setEnabled(false);
                break;
            }
            case "Pause": {
                notifyPaused();
                resumeButton.setText("Resume");
                break;
            }
            case "Resume": {
                notifyResumed();
                resumeButton.setText("Pause");
                break;
            }
            case "Stop": {
                notifyStopped();
                startButton.setEnabled(true);
                boidCountField.setEnabled(true);
                resumeButton.setText("Pause");
                resumeButton.setEnabled(false);
                stopButton.setEnabled(false);
                break;
            }
        }
    }
}
