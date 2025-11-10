package view;

import data.model.Room;
import service.RoomService;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class CreateRoomDialog extends JDialog {
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
    private JTextField cleanPctField;
    private JTextField dirtyPctField;
    private JTextField permObsPctField;
    private JTextField tempObsPctField;
    private JLabel totalPctLabel;
    
    public CreateRoomDialog(JFrame parent, RoomService roomService) {
        super(parent, "Create New Room", true);
        this.roomService = roomService;
        this.createdRoom = null;
        
        initializeDialog();
        createComponents();
    }
    
    private void initializeDialog() {
        setSize(450, 500);
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
        JLabel titleLabel = new JLabel("Create Custom Room");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Dimensions section
        JPanel dimensionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        dimensionsPanel.setBackground(COLOR_3);
        dimensionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                "Dimensions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                COLOR_5
            )
        ));
        
        JLabel rowsLabel = new JLabel("Rows:");
        rowsLabel.setForeground(TEXT_PRIMARY);
        dimensionsPanel.add(rowsLabel);
        rowsField = createStyledTextField("6");
        dimensionsPanel.add(rowsField);
        
        JLabel colsLabel = new JLabel("Columns:");
        colsLabel.setForeground(TEXT_PRIMARY);
        dimensionsPanel.add(colsLabel);
        colsField = createStyledTextField("8");
        dimensionsPanel.add(colsField);
        
        mainPanel.add(dimensionsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Percentages section
        JPanel percentagesPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        percentagesPanel.setBackground(COLOR_3);
        percentagesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(5, 10, 5, 10),
                "State Percentages (%)",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                COLOR_5
            )
        ));
        
        JLabel cleanLabel = new JLabel("Clean (L):");
        cleanLabel.setForeground(TEXT_PRIMARY);
        percentagesPanel.add(cleanLabel);
        cleanPctField = createStyledTextField("40.0");
        cleanPctField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateTotalPercentage();
            }
        });
        percentagesPanel.add(cleanPctField);
        
        JLabel dirtyLabel = new JLabel("Dirty (S):");
        dirtyLabel.setForeground(TEXT_PRIMARY);
        percentagesPanel.add(dirtyLabel);
        dirtyPctField = createStyledTextField("30.0");
        dirtyPctField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateTotalPercentage();
            }
        });
        percentagesPanel.add(dirtyPctField);
        
        JLabel permObsLabel = new JLabel("Permanent Obstacle (O):");
        permObsLabel.setForeground(TEXT_PRIMARY);
        percentagesPanel.add(permObsLabel);
        permObsPctField = createStyledTextField("15.0");
        permObsPctField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateTotalPercentage();
            }
        });
        percentagesPanel.add(permObsPctField);
        
        JLabel tempObsLabel = new JLabel("Temporary Obstacle (T):");
        tempObsLabel.setForeground(TEXT_PRIMARY);
        percentagesPanel.add(tempObsLabel);
        tempObsPctField = createStyledTextField("15.0");
        tempObsPctField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateTotalPercentage();
            }
        });
        percentagesPanel.add(tempObsPctField);
        
        JLabel totalLabel = new JLabel("Total:");
        totalLabel.setForeground(TEXT_PRIMARY);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        percentagesPanel.add(totalLabel);
        totalPctLabel = new JLabel("100.0%");
        totalPctLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        totalPctLabel.setForeground(COLOR_5);
        percentagesPanel.add(totalPctLabel);
        
        mainPanel.add(percentagesPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Info label
        JLabel infoLabel = new JLabel("<html><i>Note: Recharge points (R) will be 1-4 randomly assigned</i></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setForeground(TEXT_SECONDARY);
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(infoLabel);
        
        mainPanel.add(Box.createVerticalGlue());
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(COLOR_2);
        
        JButton createButton = new JButton("Create Room");
        createButton.setBackground(new Color(46, 204, 113));
        createButton.setForeground(Color.BLACK);
        createButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        createButton.setFocusPainted(false);
        createButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113).brighter(), 1),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        createButton.addActionListener(e -> createRoom());
        
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
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateTotalPercentage();
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
    
    private void updateTotalPercentage() {
        try {
            double clean = parseDouble(cleanPctField.getText());
            double dirty = parseDouble(dirtyPctField.getText());
            double permObs = parseDouble(permObsPctField.getText());
            double tempObs = parseDouble(tempObsPctField.getText());
            
            double total = clean + dirty + permObs + tempObs;
            totalPctLabel.setText(String.format("%.1f%%", total));
            
            if (total > 100.0) {
                totalPctLabel.setForeground(new Color(231, 76, 60));
            } else {
                totalPctLabel.setForeground(COLOR_5);
            }
        } catch (NumberFormatException e) {
            totalPctLabel.setText("Invalid");
            totalPctLabel.setForeground(new Color(231, 76, 60));
        }
    }
    
    private double parseDouble(String text) throws NumberFormatException {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }
        return Double.parseDouble(text.trim());
    }
    
    private void createRoom() {
        try {
            int rows = Integer.parseInt(rowsField.getText().trim());
            int cols = Integer.parseInt(colsField.getText().trim());
            
            if (rows <= 0 || cols <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Dimensions must be positive numbers.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double cleanPct = parseDouble(cleanPctField.getText());
            double dirtyPct = parseDouble(dirtyPctField.getText());
            double permObsPct = parseDouble(permObsPctField.getText());
            double tempObsPct = parseDouble(tempObsPctField.getText());
            
            if (!roomService.validatePercentages(cleanPct, dirtyPct, permObsPct, tempObsPct)) {
                JOptionPane.showMessageDialog(this,
                    "Invalid percentages.\nTotal cannot exceed 100% and all values must be non-negative.",
                    "Invalid Percentages", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            createdRoom = roomService.generateRandomRoom(rows, cols, cleanPct, 
                                                        dirtyPct, permObsPct, tempObsPct);
            
            JOptionPane.showMessageDialog(this,
                "Room created successfully!\n" +
                "Size: " + rows + "x" + cols + "\n" +
                "Dirty cells: " + createdRoom.getTotalDirtyCells() + "\n" +
                "Recharge points: " + createdRoom.countRechargePoints(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Invalid input. Please enter valid numbers.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error creating room: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public Room getCreatedRoom() {
        return createdRoom;
    }
}