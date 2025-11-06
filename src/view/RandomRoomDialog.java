package view;

import data.model.Room;
import service.RoomService;
import javax.swing.*;
import java.awt.*;

/**
 * Dialog for generating a random room with default percentages
 */
public class RandomRoomDialog extends JDialog {
    private RoomService roomService;
    private Room createdRoom;
    
    private JTextField rowsField;
    private JTextField colsField;
    
    public RandomRoomDialog(JFrame parent, RoomService roomService) {
        super(parent, "Generate Random Room", true);
        this.roomService = roomService;
        this.createdRoom = null;
        
        initializeDialog();
        createComponents();
    }
    
    private void initializeDialog() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
    }
    
    private void createComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Generate Random Room");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("With Default Percentages");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Dimensions panel
        JPanel dimensionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        dimensionsPanel.setBorder(BorderFactory.createTitledBorder("Room Dimensions"));
        dimensionsPanel.setMaximumSize(new Dimension(350, 100));
        
        dimensionsPanel.add(new JLabel("Rows:"));
        rowsField = new JTextField("8");
        dimensionsPanel.add(rowsField);
        
        dimensionsPanel.add(new JLabel("Columns:"));
        colsField = new JTextField("10");
        dimensionsPanel.add(colsField);
        
        mainPanel.add(dimensionsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Default percentages info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Default Distribution"));
        infoPanel.setMaximumSize(new Dimension(350, 150));
        
        infoPanel.add(createInfoLabel("• Clean (L): 40%"));
        infoPanel.add(createInfoLabel("• Dirty (S): 30%"));
        infoPanel.add(createInfoLabel("• Permanent Obstacles (O): 15%"));
        infoPanel.add(createInfoLabel("• Temporary Obstacles (T): 15%"));
        infoPanel.add(createInfoLabel("• Recharge Points (R): 1-4 (random)"));
        
        mainPanel.add(infoPanel);
        mainPanel.add(Box.createVerticalGlue());
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton generateButton = new JButton("Generate");
        generateButton.setBackground(new Color(155, 89, 182));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFont(new Font("Arial", Font.BOLD, 12));
        generateButton.addActionListener(e -> generateRoom());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private void generateRoom() {
        try {
            // Parse dimensions
            int rows = Integer.parseInt(rowsField.getText().trim());
            int cols = Integer.parseInt(colsField.getText().trim());
            
            if (rows <= 0 || cols <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Dimensions must be positive numbers.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (rows > 50 || cols > 50) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Large dimensions may affect performance.\nContinue anyway?",
                    "Large Room Warning", JOptionPane.YES_NO_OPTION);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Generate room with default percentages
            createdRoom = roomService.generateRandomRoom(rows, cols);
            
            JOptionPane.showMessageDialog(this,
                "Room generated successfully!\n" +
                "Size: " + rows + "x" + cols + "\n" +
                "Dirty cells: " + createdRoom.getTotalDirtyCells() + "\n" +
                "Recharge points: " + createdRoom.countRechargePoints() + "\n" +
                "Recommended robots: " + roomService.calculateRecommendedRobots(createdRoom),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid input. Please enter valid numbers.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error generating room: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public Room getCreatedRoom() {
        return createdRoom;
    }
}