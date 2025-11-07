package view;

import data.model.Robot;
import data.model.Room;
import service.MultiRobotManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Dialog showing final simulation results
 */
public class ResultsDialog extends JDialog {
    
    public ResultsDialog(JFrame parent, Room room, List<Robot> robots, 
                        MultiRobotManager multiRobotManager, int steps, String status) {
        super(parent, "Simulation Results", true);
        
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        // Title panel
        JPanel titlePanel = createTitlePanel(status);
        add(titlePanel, BorderLayout.NORTH);
        
        // Results panel
        JPanel resultsPanel = createResultsPanel(room, robots, multiRobotManager, steps, status);
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 30));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTitlePanel(String status) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        Color bgColor;
        String icon;
        
        switch (status) {
            case "COMPLETED":
                bgColor = new Color(46, 204, 113); // Green
                icon = "✅";
                break;
            case "ACCEPTABLE":
                bgColor = new Color(241, 196, 15); // Yellow
                icon = "⚠️";
                break;
            default:
                bgColor = new Color(231, 76, 60); // Red
                icon = "❌";
                break;
        }
        
        panel.setBackground(bgColor);
        
        JLabel titleLabel = new JLabel(icon + " Mission " + status);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(titleLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createResultsPanel(Room room, List<Robot> robots, 
                                     MultiRobotManager multiRobotManager, int steps, String status) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
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
        messageArea.setFont(new Font("Arial", Font.PLAIN, 13));
        messageArea.setBackground(panel.getBackground());
        messageArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        messageArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(messageArea);
        
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Robot statistics
        panel.add(createSectionTitle("Robot Statistics"));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        for (Robot robot : robots) {
            panel.add(createRobotStatPanel(robot));
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JLabel createSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JPanel createResultRow(String label, String value) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.BOLD, 14));
        
        JLabel valueComponent = new JLabel(value);
        valueComponent.setFont(new Font("Arial", Font.PLAIN, 14));
        
        panel.add(labelComponent);
        panel.add(valueComponent);
        
        return panel;
    }
    
    private JPanel createRobotStatPanel(Robot robot) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Robot indicator (colored circle)
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
        
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        panel.add(colorPanel);
        panel.add(Box.createRigidArea(new Dimension(10, 0)));
        
        // Robot info
        String info = String.format("Robot #%d: %s | Battery: %d | Cleaned: %d | Moves: %d",
            robot.getId(), robot.getStatus(), robot.getBattery(), 
            robot.getCellsCleaned(), robot.getMovementCount());
        
        JLabel infoLabel = new JLabel(info);
        infoLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(infoLabel);
        panel.add(Box.createHorizontalGlue());
        
        return panel;
    }
    
    private String getStatusMessage(String status, double percentage) {
        switch (status) {
            case "COMPLETED":
                return "Excellent! The cleaning mission was completed successfully. " +
                       "The room has been cleaned to satisfaction (≥80%).";
            case "ACCEPTABLE":
                return "The mission was partially successful. The room is cleaner but " +
                       "did not reach the optimal goal (≥50% but <80%).";
            default:
                return "The mission failed. The robots were unable to clean the minimum " +
                       "required amount (<50%) or all robots became inactive.";
        }
    }
}