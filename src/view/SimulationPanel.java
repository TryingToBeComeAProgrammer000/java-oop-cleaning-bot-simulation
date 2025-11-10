package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * Panel with simulation controls and statistics - Modern dark theme
 */
public class SimulationPanel extends JPanel {
    // Paleta de colores
    private static final Color COLOR_1 = new Color(20, 15, 7);
    private static final Color COLOR_2 = new Color(16, 29, 65);
    private static final Color COLOR_3 = new Color(14, 72, 150);
    private static final Color COLOR_4 = new Color(44, 116, 243);
    private static final Color COLOR_5 = new Color(93, 173, 255);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(200, 200, 200);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    
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
        setLayout(new BorderLayout(0, 0));
        setBackground(COLOR_2);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, COLOR_4),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        setPreferredSize(new Dimension(0, 300));
        
        // Main container
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);
        
        // Title
        JLabel titleLabel = new JLabel("Simulation Controls and Dynamic Statistics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLabel.setForeground(COLOR_5);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(titleLabel);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Control buttons panel
        JPanel controlPanel = createControlPanel();
        controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(controlPanel);
        
        // Stats panel
        JPanel statsPanel = createStatsPanel();
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(statsPanel);
        
        add(mainContainer, BorderLayout.NORTH);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(COLOR_2);
        
        // Start button
        startButton = new JButton("START");
        styleButton(startButton, SUCCESS_COLOR);
        startButton.addActionListener(e -> {
            if (listener != null) listener.onStart();
        });
        
        // Pause button
        pauseButton = new JButton("PAUSE");
        styleButton(pauseButton, WARNING_COLOR);
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(e -> {
            if (listener != null) listener.onPause();
        });
        
        // Stop button
        stopButton = new JButton("STOP");
        styleButton(stopButton, ERROR_COLOR);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> {
            if (listener != null) listener.onStop();
        });
        
        panel.add(startButton);
        panel.add(pauseButton);
        panel.add(stopButton);
        
        // Speed control
        JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        speedPanel.setOpaque(false);
        
        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setForeground(TEXT_PRIMARY);
        speedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        speedPanel.add(speedLabel);
        
        speedSlider = new JSlider(1, 10, 5);
        speedSlider.setBackground(COLOR_2);
        speedSlider.setForeground(COLOR_5);
        speedSlider.setMajorTickSpacing(3);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setPreferredSize(new Dimension(220, 50));
        
        // Customize slider labels
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 10);
        java.util.Dictionary<Integer, JLabel> labelTable = new java.util.Hashtable<>();
        for (int i = 1; i <= 10; i += 3) {
            JLabel label = new JLabel(String.valueOf(i));
            label.setFont(labelFont);
            label.setForeground(TEXT_SECONDARY);
            labelTable.put(i, label);
        }
        speedSlider.setLabelTable(labelTable);
        
        speedSlider.addChangeListener(e -> {
            if (listener != null && !speedSlider.getValueIsAdjusting()) {
                listener.onSpeedChange(speedSlider.getValue());
            }
        });
        
        speedPanel.add(speedSlider);
        panel.add(speedPanel);
        
        return panel;
    }
    
    private void styleButton(JButton button, Color bgColor) {
        button.setPreferredSize(new Dimension(120, 40));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.brighter(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(bgColor.brighter());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_2);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Status with large display
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setOpaque(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        statusLabel.setForeground(TEXT_PRIMARY);
        
        statusPanel.add(statusLabel);
        
        panel.add(statusPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        // Progress bar with modern styling
        cleaningProgress = new JProgressBar(0, 100);
        cleaningProgress.setStringPainted(true);
        cleaningProgress.setString("0%");
        cleaningProgress.setPreferredSize(new Dimension(0, 28));
        cleaningProgress.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        cleaningProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
        cleaningProgress.setBackground(COLOR_1);
        cleaningProgress.setForeground(COLOR_4);
        cleaningProgress.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_3, 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        cleaningProgress.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        panel.add(cleaningProgress);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Stats grid with cards
        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 12, 8));
        statsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        statsGrid.setOpaque(false);
        
        stepLabel = createStatCard("Steps", "0");
        activeRobotsLabel = createStatCard("Active Robots", "0/0");
        cleanedLabel = createStatCard("Cleaned", "0/0");
        percentageLabel = createStatCard("Progress", "0.00%");
        
        statsGrid.add(stepLabel);
        statsGrid.add(activeRobotsLabel);
        statsGrid.add(cleanedLabel);
        statsGrid.add(percentageLabel);
        
        panel.add(statsGrid);
        
        return panel;
    }
    
    private JLabel createStatCard(String title, String value) {
        JLabel card = new JLabel(
            "<html><div style='padding: 1px;'>" +
            "<span style='color: rgb(200,200,200); font-size: 9px;'>" + title + ":</span><br>" +
            "<span style='font-size: 13px; font-weight: bold;'>" + value + "</span>" +
            "</div></html>"
        );
        card.setForeground(TEXT_PRIMARY);
        card.setBackground(COLOR_3);
        card.setOpaque(true);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createEmptyBorder(5, 7, 5, 7)
        ));
        card.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return card;
    }
    
    private void updateStatCard(JLabel card, String title, String value) {
        card.setText(
            "<html><div style='padding: 1px;'>" +
            "<span style='color: rgb(200,200,200); font-size: 9px;'>" + title + ":</span><br>" +
            "<span style='font-size: 13px; font-weight: bold;'>" + value + "</span>" +
            "</div></html>"
        );
    }
    
    public void setSimulationListener(SimulationListener listener) {
        this.listener = listener;
    }
    
    public void setSimulationRunning(boolean running) {
        startButton.setEnabled(!running);
        pauseButton.setEnabled(running);
        stopButton.setEnabled(running);
        
        // Ensure text color is black when enabled
        if (running) {
            pauseButton.setForeground(Color.BLACK);
            stopButton.setForeground(Color.BLACK);
        }
    }
    
    public void updateStatus(String status) {
        Color color = TEXT_PRIMARY;
        
        if (status.contains("COMPLETED")) {
            color = SUCCESS_COLOR;
        } else if (status.contains("FAILED")) {
            color = ERROR_COLOR;
        } else if (status.contains("ACCEPTABLE")) {
            color = WARNING_COLOR;
        } else if (status.contains("Running")) {
            color = COLOR_5;
        } else if (status.contains("Paused")) {
            color = WARNING_COLOR;
        }
        
        statusLabel.setText(status);
        statusLabel.setForeground(color);
    }
    
    public void updateStats(int steps, int cleaned, int total, double percentage, int activeRobots, int totalRobots) {
        updateStatCard(stepLabel, "Steps", String.valueOf(steps));
        updateStatCard(cleanedLabel, "Cleaned", cleaned + "/" + total);
        updateStatCard(percentageLabel, "Progress", String.format("%.2f%%", percentage));
        updateStatCard(activeRobotsLabel, "Active Robots", activeRobots + "/" + totalRobots);
        
        // Update progress bar
        cleaningProgress.setValue((int)percentage);
        cleaningProgress.setString(String.format("%.1f%%", percentage));
        
        // Change progress bar color based on completion
        if (percentage >= 80) {
            cleaningProgress.setForeground(SUCCESS_COLOR);
        } else if (percentage >= 50) {
            cleaningProgress.setForeground(WARNING_COLOR);
        } else {
            cleaningProgress.setForeground(COLOR_4);
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