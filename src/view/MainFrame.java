package view;

import data.dao.MovementLogger;
import data.dao.RoomDAO;
import data.model.Robot;
import data.model.Room;
import service.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main application window with simulation
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
    private SimulationPanel simulationPanel;
    private JLabel statusLabel;
    private JComboBox<String> roomSelector;
    
    // Simulation
    private GUISimulationController simulationController;
    private MovementLogger logger;
    private List<Robot> currentRobots;
    
    public MainFrame() {
        this.roomDAO = new RoomDAO();
        this.roomService = new RoomService();
        this.logger = new MovementLogger();
        
        initializeFrame();
        createComponents();
        loadRoomsFromFile();
    }
    
    private void initializeFrame() {
        setTitle("Robot Cleaning Simulator");
        setSize(1900, 1000);
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
        
        // Simulation panel at the bottom
        simulationPanel = new SimulationPanel();
        simulationPanel.setSimulationListener(new SimulationPanel.SimulationListener() {
            @Override
            public void onStart() {
                startSimulation();
            }
            
            @Override
            public void onPause() {
                if (simulationController != null) {
                    if (simulationController.isPaused()) {
                        simulationController.resume();
                        simulationPanel.updateStatus("Running...");
                    } else {
                        simulationController.pause();
                    }
                }
            }
            
            @Override
            public void onStop() {
                if (simulationController != null) {
                    simulationController.stop();
                }
            }
            
            @Override
            public void onSpeedChange(int speed) {
                if (simulationController != null) {
                    simulationController.updateSpeed(speed);
                }
            }
        });
        mainPanel.add(simulationPanel, BorderLayout.SOUTH);
        
        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        statusLabel = new JLabel("Ready. Load or create a room to begin.");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        mainPanel.add(statusPanel, BorderLayout.PAGE_END);
        
        add(mainPanel);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLabel = new JLabel("Robot Cleaning Simulator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
;
        
        panel.add(textPanel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Room Controls"));
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
        
        JButton createButton = createStyledButton("➕ Create Room", new Color(46, 204, 113));
        createButton.addActionListener(e -> openCreateRoomDialog());
        panel.add(createButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton randomButton = createStyledButton("🎲 Generate Random", new Color(155, 89, 182));
        randomButton.addActionListener(e -> openRandomRoomDialog());
        panel.add(randomButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JButton saveButton = createStyledButton("💾 Save Room", new Color(230, 126, 34));
        saveButton.addActionListener(e -> saveCurrentRoom());
        panel.add(saveButton);
        
        // Spacer
        panel.add(Box.createVerticalGlue());
        
        return panel;
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
            simulationPanel.reset();
            statusLabel.setText("Room loaded: " + currentRoom.getRows() + "x" + 
                              currentRoom.getCols() + " - Ready to simulate");
        }
    }
    
    private void openCreateRoomDialog() {
        CreateRoomDialog dialog = new CreateRoomDialog(this, roomService);
        dialog.setVisible(true);
        
        Room newRoom = dialog.getCreatedRoom();
        if (newRoom != null) {
            currentRoom = newRoom;
            roomPanel.setRoom(currentRoom);
            simulationPanel.reset();
            statusLabel.setText("New room created: " + currentRoom.getRows() + "x" + 
                              currentRoom.getCols());
        }
    }
    
    private void openRandomRoomDialog() {
        RandomRoomDialog dialog = new RandomRoomDialog(this, roomService);
        dialog.setVisible(true);
        
        Room newRoom = dialog.getCreatedRoom();
        if (newRoom != null) {
            currentRoom = newRoom;
            roomPanel.setRoom(currentRoom);
            simulationPanel.reset();
            statusLabel.setText("Random room generated: " + currentRoom.getRows() + "x" + 
                              currentRoom.getCols());
        }
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
            loadRoomsFromFile();
        }
    }
    
    private void startSimulation() {
        if (currentRoom == null) {
            JOptionPane.showMessageDialog(this,
                "Please load or create a room first.",
                "No Room", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (simulationController != null && simulationController.isRunning()) {
            return;
        }
        
        // Initialize simulation
        logger.initialize();
        
        // Create services
        RobotService robotService = new RobotService();
        PathfindingService pathfindingService = new PathfindingService();
        MultiRobotManager multiManager = new MultiRobotManager(logger);
        RobotController robotController = new RobotController(robotService, pathfindingService, logger);
        
        // Calculate and initialize robots
        int recommendedRobots = multiManager.calculateRecommendedRobots(currentRoom);
        currentRobots = multiManager.initializeRobots(currentRoom, recommendedRobots);
        
        if (currentRobots.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Could not initialize robots!",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create simulation controller
        simulationController = new GUISimulationController(
            currentRoom,
            currentRobots,
            logger,
            multiManager,
            robotController,
            roomPanel,
            simulationPanel,
            1000 // Max steps
        );
        
        // Update panel and start
        simulationPanel.setSimulationRunning(true);
        roomPanel.setRoom(currentRoom); // Refresh to show robots
        statusLabel.setText("Simulation running with " + currentRobots.size() + " robot(s)...");
        
        simulationController.start();
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