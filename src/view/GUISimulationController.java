package view;

import data.dao.MovementLogger;
import data.model.Robot;
import data.model.Room;
import service.*;
import javax.swing.*;
import java.util.List;

/**
 * Controller that manages simulation with GUI updates
 */
public class GUISimulationController {
    private Room room;
    private List<Robot> robots;
    private MovementLogger logger;
    private MultiRobotManager multiRobotManager;
    private RobotController robotController;
    
    private RoomPanel roomPanel;
    private SimulationPanel simulationPanel;
    
    private Timer simulationTimer;
    private int stepCount;
    private int maxSteps;
    private boolean isRunning;
    private boolean isPaused;
    private String missionStatus;
    
    private static final double COMPLETION_THRESHOLD = 80.0; // Show COMPLETED at ≥80%
    private static final double ACCEPTABLE_THRESHOLD = 50.0;
    
    public GUISimulationController(Room room, List<Robot> robots, 
                                   MovementLogger logger,
                                   MultiRobotManager multiRobotManager,
                                   RobotController robotController,
                                   RoomPanel roomPanel,
                                   SimulationPanel simulationPanel,
                                   int maxSteps) {
        this.room = room;
        this.robots = robots;
        this.logger = logger;
        this.multiRobotManager = multiRobotManager;
        this.robotController = robotController;
        this.roomPanel = roomPanel;
        this.simulationPanel = simulationPanel;
        this.maxSteps = maxSteps;
        this.stepCount = 0;
        this.isRunning = false;
        this.isPaused = false;
        this.missionStatus = "IN PROGRESS";
    }
    
    public void start() {
        if (isRunning) return;
        
        isRunning = true;
        isPaused = false;
        
        logger.log("\n=== SIMULATION START ===");
        logger.log("Initial dirty cells: " + room.getTotalDirtyCells());
        logger.log("Robots deployed: " + robots.size());
        logger.log("========================\n");
        
        simulationPanel.updateStatus("Running...");
        
        // Calculate delay based on speed (1=slow, 10=fast)
        int speed = simulationPanel.getSpeed();
        int delay = 1100 - (speed * 100); // 1000ms to 100ms
        
        simulationTimer = new Timer(delay, e -> executeSimulationStep());
        simulationTimer.start();
    }
    
    public void pause() {
        if (!isRunning || isPaused) return;
        
        isPaused = true;
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        simulationPanel.updateStatus("Paused");
        logger.log("--- SIMULATION PAUSED at step " + stepCount + " ---");
    }
    
    public void resume() {
        if (!isRunning || !isPaused) return;
        
        isPaused = false;
        simulationPanel.updateStatus("Running...");
        logger.log("--- SIMULATION RESUMED at step " + stepCount + " ---");
        
        if (simulationTimer != null) {
            simulationTimer.start();
        }
    }
    
    public void stop() {
        if (!isRunning) return;
        
        isRunning = false;
        isPaused = false;
        
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        
        // Clear robots from room
        if (room != null && robots != null) {
            for (Robot robot : robots) {
                room.removeRobot(robot);
            }
        }
        
        logger.log("\n--- SIMULATION STOPPED at step " + stepCount + " ---");
        finishSimulation();
    }
    
    public void updateSpeed(int speed) {
        if (simulationTimer != null && isRunning && !isPaused) {
            int delay = 1100 - (speed * 100);
            simulationTimer.setDelay(delay);
        }
    }
    
    private void executeSimulationStep() {
        if (isPaused) return;
        
        stepCount++;
        
        // Execute one step for all robots
        boolean anyRobotMoved = false;
        
        for (Robot robot : robots) {
            if (robot.isActive()) {
                boolean moved = robotController.executeRobotAction(room, robot);
                if (moved) {
                    anyRobotMoved = true;
                }
                
                // Check if robot became inactive
                if (!robot.isActive()) {
                    logger.logDeactivation(robot, "Battery depleted");
                }
            }
        }
        
        // Update GUI
        updateGUI();
        
        // Check if all dirty cells are cleaned (100%)
        if (room.countDirtyCells() == 0) {
            logger.log("\nALL CELLS CLEANED (100%) - MISSION ENDING");
            finishSimulation();
            return;
        }
        
        // If no robot can move or all inactive, end simulation
        if (!anyRobotMoved || multiRobotManager.areAllRobotsInactive(robots)) {
            logger.log("\nNO MORE PROGRESS POSSIBLE - MISSION ENDING");
            finishSimulation();
            return;
        }
        
        // Check if reached max steps
        if (stepCount >= maxSteps) {
            logger.log("\nMAX STEPS REACHED - ENDING SIMULATION");
            finishSimulation();
            return;
        }
    }
    
    private void updateGUI() {
        // Update room panel (robots will be drawn)
        roomPanel.repaint();
        
        // Update statistics
        int cleaned = multiRobotManager.getTotalCellsCleaned(robots);
        int total = room.getTotalDirtyCells();
        double percentage = getCleaningPercentage();
        int activeRobots = multiRobotManager.getActiveRobotCount(robots);
        
        simulationPanel.updateStats(stepCount, cleaned, total, percentage, activeRobots, robots.size());
    }
    
    private void finishSimulation() {
        isRunning = false;
        isPaused = false;
        
        if (simulationTimer != null) {
            simulationTimer.stop();
        }
        
        // Calculate final percentage to determine mission status
        double finalPercentage = getCleaningPercentage();
        
        // Determine mission status based on final percentage
        if (finalPercentage >= COMPLETION_THRESHOLD) {
            missionStatus = "COMPLETED"; // ≥80% is COMPLETED
        } else if (finalPercentage >= ACCEPTABLE_THRESHOLD) {
            missionStatus = "ACCEPTABLE"; // 50-79% is ACCEPTABLE
        } else {
            missionStatus = "FAILED"; // <50% is FAILED
        }
        
        // Final update
        updateGUI();
        
        // Update status
        simulationPanel.updateStatus(missionStatus);
        simulationPanel.setSimulationRunning(false);
        
        // Log final results
        int totalCleaned = multiRobotManager.getTotalCellsCleaned(robots);
        int activeRobots = multiRobotManager.getActiveRobotCount(robots);
        int totalMovements = multiRobotManager.getTotalMovements(robots);
        
        logger.log("\n" + multiRobotManager.getRobotsStatistics(robots));
        logger.log("=== SIMULATION COMPLETE ===");
        logger.log("Total steps: " + stepCount);
        logger.log("Total movements: " + totalMovements);
        logger.log("Initial dirty cells: " + room.getTotalDirtyCells());
        logger.log("Cells cleaned: " + totalCleaned);
        logger.log("Cleaning percentage: " + String.format("%.2f%%", finalPercentage));
        logger.log("Active robots at end: " + activeRobots + "/" + robots.size());
        logger.log("Mission status: " + missionStatus);
        logger.log("===========================\n");
        
        logger.logFinalResults(room.getTotalDirtyCells(), totalCleaned, 
                             finalPercentage, missionStatus);
        
        // Show results dialog
        showResultsDialog();
    }
    
    private void showResultsDialog() {
        SwingUtilities.invokeLater(() -> {
            ResultsDialog dialog = new ResultsDialog(
                (JFrame) SwingUtilities.getWindowAncestor(roomPanel),
                room,
                robots,
                multiRobotManager,
                stepCount,
                missionStatus
            );
            dialog.setVisible(true);
        });
    }
    
    private double getCleaningPercentage() {
        if (room.getTotalDirtyCells() == 0) {
            return 100.0;
        }
        
        int cleaned = multiRobotManager.getTotalCellsCleaned(robots);
        return (cleaned * 100.0) / room.getTotalDirtyCells();
    }
    
    public boolean isRunning() {
        return isRunning;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public int getStepCount() {
        return stepCount;
    }
    
    public String getMissionStatus() {
        return missionStatus;
    }
}