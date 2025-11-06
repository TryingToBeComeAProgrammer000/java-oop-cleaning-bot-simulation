package view;

import data.model.Room;
import service.RoomService;
import javax.swing.*;
import java.awt.*;

/**
 * Dialog for creating a new room with custom percentages
 */
public class CreateRoomDialog extends JDialog {
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
    }
    
    private void createComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("Create Custom Room");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Dimensions section
        JPanel dimensionsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        dimensionsPanel.setBorder(BorderFactory.createTitledBorder("Dimensions"));
        
        dimensionsPanel.add(new JLabel("Rows:"));
        rowsField = new JTextField("6");
        dimensionsPanel.add(rowsField);
        
        dimensionsPanel.add(new JLabel("Columns:"));
        colsField = new JTextField("8");
        dimensionsPanel.add(colsField);
        
        mainPanel.add(dimensionsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Percentages section
        JPanel percentagesPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        percentagesPanel.setBorder(BorderFactory.createTitledBorder("State Percentages (%)"));
        
        percentagesPanel.add(new JLabel("Clean (L):"));
        cleanPctField = new JTextField("40.0");
        cleanPctField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateTotalPercentage();
            }
        });
        percentagesPanel.add(cleanPctField);
        
        percentagesPanel.add(new JLabel("Dirty (S):"));
        dirtyPctField = new JTextField("30.0");
        dirtyPctField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateTotalPercentage();
            }
        });
        percentagesPanel.add(dirtyPctField);
        
        percentagesPanel.add(new JLabel("Permanent Obstacle (O):"));
        permObsPctField = new JTextField("15.0");
        permObsPctField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateTotalPercentage();
            }
        });
        percentagesPanel.add(permObsPctField);
        
        percentagesPanel.add(new JLabel("Temporary Obstacle (T):"));
        tempObsPctField = new JTextField("15.0");
        tempObsPctField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                updateTotalPercentage();
            }
        });
        percentagesPanel.add(tempObsPctField);
        
        percentagesPanel.add(new JLabel("Total:"));
        totalPctLabel = new JLabel("100.0%");
        totalPctLabel.setFont(new Font("Arial", Font.BOLD, 12));
        percentagesPanel.add(totalPctLabel);
        
        mainPanel.add(percentagesPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Info label
        JLabel infoLabel = new JLabel("<html><i>Note: Recharge points (R) will be 1-4 randomly assigned</i></html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(infoLabel);
        
        mainPanel.add(Box.createVerticalGlue());
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton createButton = new JButton("Create Room");
        createButton.setBackground(new Color(46, 204, 113));
        createButton.setForeground(Color.WHITE);
        createButton.setFont(new Font("Arial", Font.BOLD, 12));
        createButton.addActionListener(e -> createRoom());
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateTotalPercentage();
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
                totalPctLabel.setForeground(Color.RED);
            } else {
                totalPctLabel.setForeground(new Color(46, 204, 113));
            }
        } catch (NumberFormatException e) {
            totalPctLabel.setText("Invalid");
            totalPctLabel.setForeground(Color.RED);
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
            // Parse dimensions
            int rows = Integer.parseInt(rowsField.getText().trim());
            int cols = Integer.parseInt(colsField.getText().trim());
            
            if (rows <= 0 || cols <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Dimensions must be positive numbers.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Parse percentages
            double cleanPct = parseDouble(cleanPctField.getText());
            double dirtyPct = parseDouble(dirtyPctField.getText());
            double permObsPct = parseDouble(permObsPctField.getText());
            double tempObsPct = parseDouble(tempObsPctField.getText());
            
            // Validate percentages
            if (!roomService.validatePercentages(cleanPct, dirtyPct, permObsPct, tempObsPct)) {
                JOptionPane.showMessageDialog(this,
                    "Invalid percentages.\nTotal cannot exceed 100% and all values must be non-negative.",
                    "Invalid Percentages", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Generate room
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