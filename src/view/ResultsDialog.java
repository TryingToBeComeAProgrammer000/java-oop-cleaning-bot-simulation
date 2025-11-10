package view;

import data.model.Robot;
import data.model.Room;
import service.MultiRobotManager;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

public class ResultsDialog extends JDialog {
    private static final Color COLOR_1 = new Color(20, 15, 7);
    private static final Color COLOR_2 = new Color(16, 29, 65);
    private static final Color COLOR_3 = new Color(14, 72, 150);
    private static final Color COLOR_4 = new Color(44, 116, 243);
    private static final Color COLOR_5 = new Color(93, 173, 255);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(200, 200, 200);
    
    public ResultsDialog(JFrame parent, Room room, List<Robot> robots, 
                        MultiRobotManager multiRobotManager, int steps, String status) {
        super(parent, "Simulation Results", true);
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COLOR_2);
        
        // Title panel
        JPanel titlePanel = createTitlePanel(status);
        add(titlePanel, BorderLayout.NORTH);
        
        // Results panel
        JPanel resultsPanel = createResultsPanel(room, robots, multiRobotManager, steps, status);
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_2);
        add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(COLOR_2);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.setBackground(COLOR_3);
        closeButton.setForeground(Color.BLACK);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTitlePanel(String status) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        Color bgColor;
        String message;
        
        switch (status) {
            case "COMPLETED":
                bgColor = new Color(46, 204, 113);
                message = "Mission " + status;
                break;
            case "ACCEPTABLE":
                bgColor = new Color(241, 196, 15);
                message = "Mission " + status;
                break;
            default:
                bgColor = new Color(231, 76, 60);
                message = "Mission " + status;
                break;
        }
        
        panel.setBackground(bgColor);
        
        JLabel titleLabel = new JLabel(message);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(titleLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createResultsPanel(Room room, List<Robot> robots, 
                                     MultiRobotManager multiRobotManager, int steps, String status) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(COLOR_2);
        
        // Summary section
        panel.add(createSectionTitle("Summary"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        int totalCleaned = multiRobotManager.getTotalCellsCleaned(robots);
        double percentage = (totalCleaned * 100.0) / room.getTotalDirtyCells();
        
        panel.add(createResultRow("Total Steps:", String.valueOf(steps)));
        panel.add(createResultRow("Total Dirty Cells:", String.valueOf(room.getTotalDirtyCells())));
        panel.add(createResultRow("Cells Cleaned:", totalCleaned + " (" + String.format("%.2f%%", percentage) + ")"));
        panel.add(createResultRow("Active Robots:", multiRobotManager.getActiveRobotCount(robots) + "/" + robots.size()));
        panel.add(createResultRow("Total Movements:", String.valueOf(multiRobotManager.getTotalMovements(robots))));
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Status message
        panel.add(createSectionTitle("Status"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        String message = getStatusMessage(status, percentage);
        JTextArea messageArea = new JTextArea(message);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        messageArea.setForeground(TEXT_PRIMARY);
        messageArea.setBackground(COLOR_3);
        messageArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        messageArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        messageArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(messageArea);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Robot statistics
        panel.add(createSectionTitle("Robot Statistics"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        for (Robot robot : robots) {
            panel.add(createRobotStatPanel(robot));
            panel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(COLOR_5);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JPanel createResultRow(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setBackground(COLOR_2);
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelComponent.setForeground(TEXT_SECONDARY);
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        valueComponent.setForeground(TEXT_PRIMARY);
        
        panel.add(labelComponent);
        panel.add(valueComponent);
        
        return panel;
    }
    
    private JPanel createRobotStatPanel(Robot robot) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBackground(COLOR_3);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Robot indicator
        Color[] robotColors = {
            new Color(255, 69, 0),
            new Color(30, 144, 255),
            new Color(255, 215, 0),
            new Color(138, 43, 226)
        };
        Color robotColor = robotColors[(robot.getId() - 1) % robotColors.length];
        
        JPanel colorPanel = new JPanel();
        colorPanel.setPreferredSize(new Dimension(20, 20));
        colorPanel.setMaximumSize(new Dimension(20, 20));
        colorPanel.setBackground(robotColor);
        colorPanel.setBorder(BorderFactory.createLineBorder(robotColor.darker(), 2));
        
        panel.add(Box.createRigidArea(new Dimension(5, 0)));
        panel.add(colorPanel);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        // Robot info
        String info = String.format("Robot #%d: %s | Battery: %d | Cleaned: %d | Moves: %d",
            robot.getId(), robot.getStatus(), robot.getBattery(), 
            robot.getCellsCleaned(), robot.getMovementCount());
        
        JLabel infoLabel = new JLabel(info);
        infoLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoLabel.setForeground(TEXT_PRIMARY);
        panel.add(infoLabel);
        panel.add(Box.createHorizontalGlue());
        
        return panel;
    }
    
    private String getStatusMessage(String status, double percentage) {
        switch (status) {
            case "COMPLETED":
                return "Excellent! The cleaning mission was completed successfully. " +
                       "The room achieved ≥80% cleaning (actual: " + String.format("%.2f%%", percentage) + ").";
            case "ACCEPTABLE":
                return "The mission was partially successful. The room is cleaner but " +
                       "did not reach the 80% goal (achieved: " + String.format("%.2f%%", percentage) + ").";
            default:
                return "The mission failed. The robots were unable to clean the minimum " +
                       "required amount (achieved: " + String.format("%.2f%%", percentage) + ") or all robots became inactive.";
        }
    }
}