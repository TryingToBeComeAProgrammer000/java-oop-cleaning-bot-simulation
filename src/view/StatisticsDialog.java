package view;

import data.model.Room;
import service.RoomService;
import javax.swing.*;
import java.awt.*;

/**
 * Dialog for displaying room statistics
 */
public class StatisticsDialog extends JDialog {
    private Room room;
    private RoomService roomService;
    
    public StatisticsDialog(JFrame parent, Room room, RoomService roomService) {
        super(parent, "Room Statistics", true);
        this.room = room;
        this.roomService = roomService;
        
        initializeDialog();
        createComponents();
    }
    
    private void initializeDialog() {
        setSize(500, 500);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
    }
    
    private void createComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("📊 Room Statistics");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Calculate statistics
        int totalCells = room.getRows() * room.getCols();
        int dirtyCells = room.getTotalDirtyCells();
        int cleanCells = countCells('L');
        int permanentObstacles = room.countPermanentObstacles();
        int temporaryObstacles = countCells('T');
        int rechargePoints = room.countRechargePoints();
        int recommendedRobots = roomService.calculateRecommendedRobots(room);
        
        // Dimensions section
        JPanel dimensionsPanel = createStatPanel("Room Dimensions");
        dimensionsPanel.add(createStatRow("Rows:", String.valueOf(room.getRows())));
        dimensionsPanel.add(createStatRow("Columns:", String.valueOf(room.getCols())));
        dimensionsPanel.add(createStatRow("Total Cells:", String.valueOf(totalCells)));
        mainPanel.add(dimensionsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Cell distribution section
        JPanel distributionPanel = createStatPanel("Cell Distribution");
        distributionPanel.add(createStatRow("Clean (L):", 
            cleanCells + " (" + formatPercentage(cleanCells, totalCells) + ")"));
        distributionPanel.add(createStatRow("Dirty (S):", 
            dirtyCells + " (" + formatPercentage(dirtyCells, totalCells) + ")"));
        distributionPanel.add(createStatRow("Permanent Obstacles (O):", 
            permanentObstacles + " (" + formatPercentage(permanentObstacles, totalCells) + ")"));
        distributionPanel.add(createStatRow("Temporary Obstacles (T):", 
            temporaryObstacles + " (" + formatPercentage(temporaryObstacles, totalCells) + ")"));
        distributionPanel.add(createStatRow("Recharge Points (R):", 
            rechargePoints + " (" + formatPercentage(rechargePoints, totalCells) + ")"));
        mainPanel.add(distributionPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Analysis section
        JPanel analysisPanel = createStatPanel("Analysis");
        analysisPanel.add(createStatRow("Recommended Robots:", String.valueOf(recommendedRobots)));
        
        String difficulty = getDifficultyLevel(dirtyCells, permanentObstacles, totalCells);
        JLabel difficultyLabel = createStatRow("Difficulty Level:", difficulty);
        difficultyLabel.setForeground(getDifficultyColor(difficulty));
        analysisPanel.add(difficultyLabel);
        
        analysisPanel.add(createStatRow("Traversable Cells:", 
            (totalCells - permanentObstacles - temporaryObstacles) + " cells"));
        
        mainPanel.add(analysisPanel);
        mainPanel.add(Box.createVerticalGlue());
        
        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("Close");
        closeButton.setPreferredSize(new Dimension(100, 30));
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createStatPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(41, 128, 185), 2),
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(41, 128, 185)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        return panel;
    }
    
    private JLabel createStatRow(String label, String value) {
        JLabel statLabel = new JLabel(label + " " + value);
        statLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return statLabel;
    }
    
    private String formatPercentage(int count, int total) {
        if (total == 0) return "0.0%";
        double percentage = (count * 100.0) / total;
        return String.format("%.1f%%", percentage);
    }
    
    private int countCells(char state) {
        int count = 0;
        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                if (room.getCell(i, j).getState() == state) {
                    count++;
                }
            }
        }
        return count;
    }
    
    private String getDifficultyLevel(int dirty, int obstacles, int total) {
        double dirtyPercentage = (dirty * 100.0) / total;
        double obstaclePercentage = (obstacles * 100.0) / total;
        
        double difficultyScore = dirtyPercentage * 0.6 + obstaclePercentage * 0.4;
        
        if (difficultyScore < 20) {
            return "Easy";
        } else if (difficultyScore < 35) {
            return "Medium";
        } else if (difficultyScore < 50) {
            return "Hard";
        } else {
            return "Very Hard";
        }
    }
    
    private Color getDifficultyColor(String difficulty) {
        switch (difficulty) {
            case "Easy":
                return new Color(46, 204, 113); // Green
            case "Medium":
                return new Color(241, 196, 15); // Yellow
            case "Hard":
                return new Color(230, 126, 34); // Orange
            case "Very Hard":
                return new Color(231, 76, 60);  // Red
            default:
                return Color.BLACK;
        }
    }
}