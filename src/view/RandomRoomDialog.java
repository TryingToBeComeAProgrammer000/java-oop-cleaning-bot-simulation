package view;

import data.model.Room;
import service.RoomService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class RandomRoomDialog extends JDialog {
    private static final Color COLOR_1 = new Color(20, 15, 7);
    private static final Color COLOR_2 = new Color(16, 29, 65);
    private static final Color COLOR_3 = new Color(14, 72, 150);
    private static final Color COLOR_4 = new Color(44, 116, 243);
    private static final Color COLOR_5 = new Color(93, 173, 255);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(200, 200, 200);
    
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
        setSize(400, 280);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(COLOR_2);
    }
    
    private void createComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(COLOR_2);
        
        // Title
        JLabel titleLabel = new JLabel("Generate Random Room");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Dimensions panel
        JPanel dimensionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        dimensionsPanel.setBackground(COLOR_3);
        dimensionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                "Room Dimensions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                COLOR_5
            )
        ));
        dimensionsPanel.setMaximumSize(new Dimension(350, 100));
        
        JLabel rowsLabel = new JLabel("Rows:");
        rowsLabel.setForeground(TEXT_PRIMARY);
        dimensionsPanel.add(rowsLabel);
        rowsField = createStyledTextField("8");
        dimensionsPanel.add(rowsField);
        
        JLabel colsLabel = new JLabel("Columns:");
        colsLabel.setForeground(TEXT_PRIMARY);
        dimensionsPanel.add(colsLabel);
        colsField = createStyledTextField("10");
        dimensionsPanel.add(colsField);
        
        mainPanel.add(dimensionsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(COLOR_2);
        
        JButton generateButton = new JButton("Generate");
        generateButton.setBackground(new Color(155, 89, 182));
        generateButton.setForeground(Color.BLACK);
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        generateButton.setFocusPainted(false);
        generateButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(155, 89, 182).brighter(), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        generateButton.addActionListener(e -> generateRoom());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(COLOR_3);
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JTextField createStyledTextField(String defaultValue) {
        JTextField field = new JTextField(defaultValue);
        field.setBackground(COLOR_1);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return field;
    }
    
    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        return label;
    }
    
    private void generateRoom() {
        try {
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