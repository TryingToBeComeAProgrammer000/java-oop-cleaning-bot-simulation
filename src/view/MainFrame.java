package view;

import data.dao.RoomDAO;
import data.model.Room;
import service.RoomService;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * Main application window
 */
public class MainFrame extends JFrame {
    private RoomDAO roomDAO;
    private RoomService roomService;
    private List<Room> loadedRooms;
    private Room currentRoom;
    
    // Components
    private JPanel mainPanel;
    private RoomPanel roomPanel;
    private JPanel controlPanel;
    private JPanel infoPanel;
    private JLabel statusLabel;
    private JComboBox<String> roomSelector;
    
    public MainFrame() {
        this.roomDAO = new RoomDAO();
        this.roomService = new RoomService();
        
        initializeFrame();
        createComponents();
        loadRoomsFromFile();
    }
    
    private void initializeFrame() {
        setTitle("Robot Cleaning Simulator - Stage 1");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
    }
    
    private void createComponents() {
        // Main panel with padding
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Title panel
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Room visualization panel
        roomPanel = new RoomPanel();
        JScrollPane scrollPane = new JScrollPane(roomPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Room Visualization"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Control panel on the right
        controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.EAST);
        
        // Info panel at the bottom
        infoPanel = createInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("🤖 Robot Cleaning Simulator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Stage 1: Room Management");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.WHITE);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subtitleLabel);
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Controls"));
        panel.setPreferredSize(new Dimension(250, 0));
        
        // Room selector
        JPanel selectorPanel = new JPanel(new BorderLayout(5, 5));
        selectorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        selectorPanel.add(new JLabel("Select Room:"), BorderLayout.NORTH);
        
        roomSelector = new JComboBox<>();
        roomSelector.addActionListener(e -> onRoomSelected());
        selectorPanel.add(roomSelector, BorderLayout.CENTER);
        
        panel.add(selectorPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Buttons
        JButton loadButton = createStyledButton("📂 Load Rooms", new Color(52, 152, 219));
        loadButton.addActionListener(e -> loadRoomsFromFile());
        panel.add(loadButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton createButton = createStyledButton("➕ Create New Room", new Color(46, 204, 113));
        createButton.addActionListener(e -> openCreateRoomDialog());
        panel.add(createButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton randomButton = createStyledButton("🎲 Generate Random", new Color(155, 89, 182));
        randomButton.addActionListener(e -> openRandomRoomDialog());
        panel.add(randomButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton statsButton = createStyledButton("📊 Show Statistics", new Color(241, 196, 15));
        statsButton.addActionListener(e -> showStatistics());
        panel.add(statsButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton saveButton = createStyledButton("💾 Save Current Room", new Color(230, 126, 34));
        saveButton.addActionListener(e -> saveCurrentRoom());
        panel.add(saveButton);
        
        // Legend
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(createLegendPanel());
        
        // Spacer
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createLegendPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Legend"));
        
        panel.add(createLegendItem("L - Clean", RoomPanel.COLOR_CLEAN));
        panel.add(createLegendItem("S - Dirty", RoomPanel.COLOR_DIRTY));
        panel.add(createLegendItem("O - Permanent Obstacle", RoomPanel.COLOR_OBSTACLE));
        panel.add(createLegendItem("T - Temporary Obstacle", RoomPanel.COLOR_TEMP_OBSTACLE));
        panel.add(createLegendItem("R - Recharge Point", RoomPanel.COLOR_RECHARGE));
        
        return panel;
    }
    
    private JPanel createLegendItem(String text, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JPanel colorBox = new JPanel();
        colorBox.setPreferredSize(new Dimension(20, 20));
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        
        item.add(colorBox);
        item.add(label);
        
        return item;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        statusLabel = new JLabel("Ready. Load or create a room to begin.");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private void loadRoomsFromFile() {
        loadedRooms = roomDAO.readRooms();
        updateRoomSelector();
        
        if (loadedRooms.isEmpty()) {
            statusLabel.setText("No rooms found in salon.txt");
            JOptionPane.showMessageDialog(this, 
                "No rooms found in salon.txt.\nCreate a new room to get started.",
                "No Rooms", JOptionPane.INFORMATION_MESSAGE);
        } else {
            statusLabel.setText("Loaded " + loadedRooms.size() + " room(s) from salon.txt");
        }
    }
    
    private void updateRoomSelector() {
        roomSelector.removeAllItems();
        
        if (loadedRooms != null && !loadedRooms.isEmpty()) {
            for (int i = 0; i < loadedRooms.size(); i++) {
                Room room = loadedRooms.get(i);
                roomSelector.addItem("Room " + (i + 1) + " (" + 
                                   room.getRows() + "x" + room.getCols() + ")");
            }
            roomSelector.setSelectedIndex(0);
        }
    }
    
    private void onRoomSelected() {
        int index = roomSelector.getSelectedIndex();
        if (index >= 0 && loadedRooms != null && index < loadedRooms.size()) {
            currentRoom = loadedRooms.get(index);
            roomPanel.setRoom(currentRoom);
            statusLabel.setText("Displaying Room " + (index + 1) + " - " + 
                              currentRoom.getRows() + "x" + currentRoom.getCols());
        }
    }
    
    private void openCreateRoomDialog() {
        CreateRoomDialog dialog = new CreateRoomDialog(this, roomService);
        dialog.setVisible(true);
        
        Room newRoom = dialog.getCreatedRoom();
        if (newRoom != null) {
            currentRoom = newRoom;
            roomPanel.setRoom(currentRoom);
            statusLabel.setText("Created new room: " + 
                              currentRoom.getRows() + "x" + currentRoom.getCols());
        }
    }
    
    private void openRandomRoomDialog() {
        RandomRoomDialog dialog = new RandomRoomDialog(this, roomService);
        dialog.setVisible(true);
        
        Room newRoom = dialog.getCreatedRoom();
        if (newRoom != null) {
            currentRoom = newRoom;
            roomPanel.setRoom(currentRoom);
            statusLabel.setText("Generated random room: " + 
                              currentRoom.getRows() + "x" + currentRoom.getCols());
        }
    }
    
    private void showStatistics() {
        if (currentRoom == null) {
            JOptionPane.showMessageDialog(this,
                "Please select or create a room first.",
                "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        StatisticsDialog dialog = new StatisticsDialog(this, currentRoom, roomService);
        dialog.setVisible(true);
    }
    
    private void saveCurrentRoom() {
        if (currentRoom == null) {
            JOptionPane.showMessageDialog(this,
                "Please select or create a room first.",
                "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Save current room to salon.txt?",
            "Confirm Save", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            roomDAO.saveRoom(currentRoom);
            statusLabel.setText("Room saved successfully!");
            JOptionPane.showMessageDialog(this,
                "Room saved successfully to salon.txt",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            loadRoomsFromFile(); // Reload to update selector
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}