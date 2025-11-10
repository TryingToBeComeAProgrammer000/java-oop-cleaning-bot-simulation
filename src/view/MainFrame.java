package view;

import data.dao.MovementLogger;
import data.dao.RoomDAO;
import data.model.Robot;
import data.model.Room;
import service.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Main application window with modern dark theme
 */
public class MainFrame extends JFrame {
    // Paleta de colores moderna
    private static final Color COLOR_1 = new Color(20, 15, 7);      // #140f07 - Fondo muy oscuro
    private static final Color COLOR_2 = new Color(16, 29, 65);     // #101d41 - Azul oscuro
    private static final Color COLOR_3 = new Color(14, 72, 150);    // #0e4896 - Azul medio
    private static final Color COLOR_4 = new Color(44, 116, 243);   // #2c74f3 - Azul brillante
    private static final Color COLOR_5 = new Color(93, 173, 255);   // #5dadff - Azul claro
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(200, 200, 200);
    
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
    private TemporaryObstacleManager tempObstacleManager;
    
    public MainFrame() {
        this.roomDAO = new RoomDAO();
        this.roomService = new RoomService();
        this.logger = new MovementLogger();
        this.tempObstacleManager = new TemporaryObstacleManager();
        
        initializeFrame();
        createComponents();
        loadRoomsFromFile();
    }
    
    private void initializeFrame() {
        setTitle("Robot Cleaning Simulator");
        setSize(1900, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(COLOR_1);
    }
    
    private void createComponents() {
        // Main panel with dark theme
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(COLOR_1);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        
        // Title panel with gradient effect
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Room visualization panel
        roomPanel = new RoomPanel();
        roomPanel.setBackground(COLOR_2);
        JScrollPane scrollPane = new JScrollPane(roomPanel);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            "Room Visualization",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            COLOR_5
        ));
        scrollPane.setBackground(COLOR_2);
        scrollPane.getViewport().setBackground(COLOR_2);
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
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(COLOR_1);
        bottomPanel.add(simulationPanel, BorderLayout.CENTER);
        
        // Status bar
        statusLabel = new JLabel(" Ready");
        statusLabel.setForeground(TEXT_PRIMARY);
        statusLabel.setBackground(COLOR_2);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, COLOR_4),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_2);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, COLOR_4),
            BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));
        
        // Title only
        JLabel titleLabel = new JLabel("Robot Cleaning Simulator");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        panel.add(titleLabel, BorderLayout.WEST);
        
        return panel;
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_2);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 2, 0, 0, COLOR_4),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(280, 0));
        
        // Section title - centered
        JLabel sectionTitle = new JLabel("Room Controls");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sectionTitle.setForeground(COLOR_5);
        sectionTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(sectionTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Room selector - centered
        JPanel selectorPanel = new JPanel();
        selectorPanel.setLayout(new BoxLayout(selectorPanel, BoxLayout.Y_AXIS));
        selectorPanel.setOpaque(false);
        selectorPanel.setMaximumSize(new Dimension(250, 80));
        
        JLabel selectLabel = new JLabel("Select Room");
        selectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        selectLabel.setForeground(TEXT_PRIMARY);
        selectLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectorPanel.add(selectLabel);
        selectorPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        
        roomSelector = new JComboBox<>();
        roomSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roomSelector.setBackground(COLOR_1);
        roomSelector.setForeground(Color.BLACK);
        roomSelector.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        roomSelector.setMaximumSize(new Dimension(250, 40));
        roomSelector.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomSelector.addActionListener(e -> onRoomSelected());
        selectorPanel.add(roomSelector);
        
        panel.add(selectorPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        
        
        // Buttons - all centered
        panel.add(createStyledButton("Load Rooms", COLOR_3, e -> loadRoomsFromFile()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createStyledButton("Create Room", COLOR_4, e -> openCreateRoomDialog()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createStyledButton("Generate Random", COLOR_3, e -> openRandomRoomDialog()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createStyledButton("Save Room", new Color(14, 72, 150), e -> saveCurrentRoom()));
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panel.add(createStyledButton("Reset Room", new Color(200, 50, 50), e -> resetRoom()));
        
        // Spacer
        panel.add(Box.createVerticalGlue());
        
        // Info panel at bottom
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(COLOR_3);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_4, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        JLabel infoTitle = new JLabel("Instructions");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoTitle.setForeground(COLOR_5);
        infoTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel infoText = new JLabel("<html><div style='text-align: center;'>First create the room, then save it, and then you can use it. If you don't do this, the room will be temporary and you won't be able to reset the room.</div></html>");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoText.setForeground(TEXT_SECONDARY);
        infoText.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        infoPanel.add(infoTitle);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(infoText);
        
        panel.add(infoPanel);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(250, 45));
        button.setBackground(bgColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.brighter(), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        button.addActionListener(listener);
        
        return button;
    }
    
    private Border createModernBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, COLOR_4),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13),
            COLOR_5
        );
    }
    
    private void loadRoomsFromFile() {
        loadedRooms = roomDAO.readRooms();
        updateRoomSelector();
        
        if (loadedRooms.isEmpty()) {
            statusLabel.setText(" No rooms found in salon.txt");
            JOptionPane.showMessageDialog(this, 
                "No rooms found in salon.txt.\nCreate a new room to get started.",
                "No Rooms", JOptionPane.INFORMATION_MESSAGE);
        } else {
            statusLabel.setText(" Ready");
        }
    }
    
    private void updateRoomSelector() {
        roomSelector.removeAllItems();
        
        if (loadedRooms != null && !loadedRooms.isEmpty()) {
            for (int i = 0; i < loadedRooms.size(); i++) {
                Room room = loadedRooms.get(i);
                roomSelector.addItem("Room " + (i + 1) + " (" + 
                                   room.getRows() + "×" + room.getCols() + ")");
            }
            roomSelector.setSelectedIndex(0);
        }
    }
    
    private void onRoomSelected() {
        int index = roomSelector.getSelectedIndex();
        if (index >= 0 && loadedRooms != null && index < loadedRooms.size()) {
            currentRoom = loadedRooms.get(index);
            roomPanel.setRoom(currentRoom);
            if (simulationPanel != null) {
                simulationPanel.reset();
            }
            if (statusLabel != null) {
                statusLabel.setText(" Room loaded: " + currentRoom.getRows() + "×" + 
                                  currentRoom.getCols() + " - Ready to simulate");
            }
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
            statusLabel.setText(" New room created: " + currentRoom.getRows() + "×" + 
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
            statusLabel.setText(" Random room generated: " + currentRoom.getRows() + "×" + 
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
            statusLabel.setText(" Room saved successfully!");
            JOptionPane.showMessageDialog(this,
                "Room saved successfully to salon.txt",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            loadRoomsFromFile();
        }
    }
    
    private void resetRoom() {
        if (currentRoom == null) {
            JOptionPane.showMessageDialog(this,
                "Please select or create a room first.",
                "No Room Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (simulationController != null && simulationController.isRunning()) {
            simulationController.stop();
        }
        
        tempObstacleManager.stop();
        currentRoom.reset();
        currentRobots = null;
        simulationController = null;
        
        roomPanel.setRoom(currentRoom);
        simulationPanel.reset();
        statusLabel.setText(" Room reset to original state - Ready for new simulation");
        
        JOptionPane.showMessageDialog(this,
            "Room has been reset to its original state.\n" +
            "All robots and simulation data have been cleared.\n" +
            "Press START to begin a new simulation.",
            "Room Reset", JOptionPane.INFORMATION_MESSAGE);
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
        
        tempObstacleManager.stop();
        logger.initialize();
        tempObstacleManager.startManaging(currentRoom);
        
        RobotService robotService = new RobotService();
        PathfindingService pathfindingService = new PathfindingService();
        MultiRobotManager multiManager = new MultiRobotManager(logger);
        RobotController robotController = new RobotController(robotService, pathfindingService, logger);
        
        int recommendedRobots = multiManager.calculateRecommendedRobots(currentRoom);
        currentRobots = multiManager.initializeRobots(currentRoom, recommendedRobots);
        
        if (currentRobots.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Could not initialize robots!",
                "Error", JOptionPane.ERROR_MESSAGE);
            tempObstacleManager.stop();
            return;
        }
        
        simulationController = new GUISimulationController(
            currentRoom,
            currentRobots,
            logger,
            multiManager,
            robotController,
            roomPanel,
            simulationPanel,
            1000
        );
        
        simulationPanel.setSimulationRunning(true);
        roomPanel.setRoom(currentRoom);
        statusLabel.setText(" Simulation running with " + currentRobots.size() + " robot(s)...");
        
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