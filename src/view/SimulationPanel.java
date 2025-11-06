package view;

import javax.swing.*;
import java.awt.*;

/**
 * Panel with simulation controls and statistics
 */
public class SimulationPanel extends JPanel {
    private JButton startButton;
    private JButton pauseButton;
    private JButton stopButton;
    private JSlider speedSlider;
    
    private JLabel statusLabel;
    private JLabel stepLabel;
    private JLabel cleanedLabel;
    private JLabel percentageLabel;
    private JLabel activeRobotsLabel;
    
    private JProgressBar cleaningProgress;
    
    private SimulationListener listener;
    
    public interface SimulationListener {
        void onStart();
        void onPause();
        void onStop();
        void onSpeedChange(int speed);
    }
    
    public SimulationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Simulation Control"));
        setPreferredSize(new Dimension(0, 200));
        
        // Control buttons panel
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // Stats panel
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.CENTER);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        // Start button
        startButton = new JButton("▶ Start");
        startButton.setBackground(new Color(46, 204, 113));
        startButton.setForeground(Color.WHITE);
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setFocusPainted(false);
        startButton.addActionListener(e -> {
            if (listener != null) listener.onStart();
        });
        
        // Pause button
        pauseButton = new JButton("⏸ Pause");
        pauseButton.setBackground(new Color(241, 196, 15));
        pauseButton.setForeground(Color.WHITE);
        pauseButton.setFont(new Font("Arial", Font.BOLD, 14));
        pauseButton.setFocusPainted(false);
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(e -> {
            if (listener != null) listener.onPause();
        });
        
        // Stop button
        stopButton = new JButton("⏹ Stop");
        stopButton.setBackground(new Color(231, 76, 60));
        stopButton.setForeground(Color.WHITE);
        stopButton.setFont(new Font("Arial", Font.BOLD, 14));
        stopButton.setFocusPainted(false);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> {
            if (listener != null) listener.onStop();
        });
        
        panel.add(startButton);
        panel.add(pauseButton);
        panel.add(stopButton);
        
        // Speed control
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        speedPanel.add(new JLabel("Speed:"));
        
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setPreferredSize(new Dimension(200, 50));
        speedSlider.addChangeListener(e -> {
            if (listener != null && !speedSlider.getValueIsAdjusting()) {
                listener.onSpeedChange(speedSlider.getValue());
            }
        });
        
        speedPanel.add(speedSlider);
        panel.add(speedPanel);
        
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Status
        statusLabel = new JLabel("Status: Ready");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Progress bar
        cleaningProgress = new JProgressBar(0, 100);
        cleaningProgress.setStringPainted(true);
        cleaningProgress.setString("0%");
        cleaningProgress.setPreferredSize(new Dimension(0, 30));
        cleaningProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cleaningProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cleaningProgress);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Stats grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 10, 5));
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        stepLabel = createStatLabel("Steps: 0");
        cleanedLabel = createStatLabel("Cleaned: 0/0");
        percentageLabel = createStatLabel("Progress: 0.00%");
        activeRobotsLabel = createStatLabel("Active Robots: 0/0");
        
        statsGrid.add(stepLabel);
        statsGrid.add(activeRobotsLabel);
        statsGrid.add(cleanedLabel);
        statsGrid.add(percentageLabel);
        
        panel.add(statsGrid);
        
        return panel;
    }
    
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 13));
        return label;
    }
    
    public void setSimulationListener(SimulationListener listener) {
        this.listener = listener;
    }
    
    public void setSimulationRunning(boolean running) {
        startButton.setEnabled(!running);
        pauseButton.setEnabled(running);
        stopButton.setEnabled(running);
    }
    
    public void updateStatus(String status) {
        statusLabel.setText("Status: " + status);
        
        // Change color based on status
        if (status.contains("COMPLETED")) {
            statusLabel.setForeground(new Color(46, 204, 113)); // Green
        } else if (status.contains("FAILED")) {
            statusLabel.setForeground(new Color(231, 76, 60)); // Red
        } else if (status.contains("ACCEPTABLE")) {
            statusLabel.setForeground(new Color(241, 196, 15)); // Yellow
        } else if (status.contains("Running")) {
            statusLabel.setForeground(new Color(52, 152, 219)); // Blue
        } else {
            statusLabel.setForeground(Color.BLACK);
        }
    }
    
    public void updateStats(int steps, int cleaned, int total, double percentage, int activeRobots, int totalRobots) {
        stepLabel.setText("Steps: " + steps);
        cleanedLabel.setText("Cleaned: " + cleaned + "/" + total);
        percentageLabel.setText(String.format("Progress: %.2f%%", percentage));
        activeRobotsLabel.setText("Active Robots: " + activeRobots + "/" + totalRobots);
        
        // Update progress bar
        cleaningProgress.setValue((int)percentage);
        cleaningProgress.setString(String.format("%.1f%%", percentage));
        
        // Change progress bar color based on completion
        if (percentage >= 80) {
            cleaningProgress.setForeground(new Color(46, 204, 113)); // Green
        } else if (percentage >= 50) {
            cleaningProgress.setForeground(new Color(241, 196, 15)); // Yellow
        } else {
            cleaningProgress.setForeground(new Color(52, 152, 219)); // Blue
        }
    }
    
    public void reset() {
        updateStatus("Ready");
        updateStats(0, 0, 0, 0.0, 0, 0);
        setSimulationRunning(false);
    }
    
    public int getSpeed() {
        return speedSlider.getValue();
    }
}