package service;

import data.dao.MovementLogger;
import data.model.Robot;
import data.model.Room;
import java.util.List;

/**
 * Controller for the complete simulation
 * Manages robot actions, mission completion, and results
 */
public class SimulationController {
    private Room room;
    private List<Robot> robots;
    private MovementLogger logger;
    private MultiRobotManager multiRobotManager;
    private RobotController robotController;
    
    private int stepCount;
    private int maxSteps;
    private boolean isCompleted;
    private String missionStatus;
    
    private static final double COMPLETION_THRESHOLD = 80.0;
    private static final double ACCEPTABLE_THRESHOLD = 50.0;
    
    /**
     * Constructor
     * @param room The room to simulate
     * @param robots List of robots
     * @param logger Movement logger
     * @param multiRobotManager Multi-robot manager
     * @param robotController Robot controller
     * @param maxSteps Maximum simulation steps (safety limit)
     */
    public SimulationController(Room room, List<Robot> robots, MovementLogger logger,
                                MultiRobotManager multiRobotManager, RobotController robotController,
                                int maxSteps) {
        this.room = room;
        this.robots = robots;
        this.logger = logger;
        this.multiRobotManager = multiRobotManager;
        this.robotController = robotController;
        this.maxSteps = maxSteps;
        this.stepCount = 0;
        this.isCompleted = false;
        this.missionStatus = "IN PROGRESS";
    }
    
    /**
     * Run the complete simulation until completion or failure
     * @return Final cleaning percentage
     */
    public double runSimulation() {
        logger.log("\n=== SIMULATION START ===");
        logger.log("Initial dirty cells: " + room.getTotalDirtyCells());
        logger.log("Robots deployed: " + robots.size());
        logger.log("========================\n");
        
        while (!isCompleted && stepCount < maxSteps) {
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
            
            // Check completion conditions
            checkMissionStatus();
            
            // If no robot can move, end simulation
            if (!anyRobotMoved && multiRobotManager.areAllRobotsInactive(robots)) {
                logger.log("\n⚠ ALL ROBOTS INACTIVE - MISSION ENDING");
                isCompleted = true;
            }
            
            // Log progress every 50 steps
            if (stepCount % 50 == 0) {
                logProgress();
            }
        }
        
        // Final results
        double finalPercentage = getCleaningPercentage();
        logFinalResults(finalPercentage);
        
        return finalPercentage;
    }
    
    /**
     * Check if mission is completed or failed
     */
    private void checkMissionStatus() {
        double percentage = getCleaningPercentage();
        
        if (percentage >= COMPLETION_THRESHOLD) {
            // Mission completed!
            isCompleted = true;
            missionStatus = "COMPLETED";
            logger.log("\n🎉 MISSION COMPLETED! " + String.format("%.2f%%", percentage) + " cleaned");
        } else if (multiRobotManager.areAllRobotsInactive(robots)) {
            // All robots inactive
            isCompleted = true;
            
            if (percentage >= ACCEPTABLE_THRESHOLD) {
                missionStatus = "ACCEPTABLE";
            } else {
                missionStatus = "FAILED";
            }
            
            logger.log("\n⚠ ALL ROBOTS INACTIVE - Mission status: " + missionStatus);
        }
    }
    
    /**
     * Calculate cleaning percentage
     * @return Percentage of dirty cells cleaned
     */
    public double getCleaningPercentage() {
        if (room.getTotalDirtyCells() == 0) {
            return 100.0;
        }
        
        int cleaned = multiRobotManager.getTotalCellsCleaned(robots);
        return (cleaned * 100.0) / room.getTotalDirtyCells();
    }
    
    /**
     * Log current progress
     */
    private void logProgress() {
        int activeRobots = multiRobotManager.getActiveRobotCount(robots);
        int cleaned = multiRobotManager.getTotalCellsCleaned(robots);
        double percentage = getCleaningPercentage();
        
        String progress = String.format("Step %d: %d/%d cells cleaned (%.2f%%) | Active robots: %d/%d",
            stepCount, cleaned, room.getTotalDirtyCells(), percentage, activeRobots, robots.size());
        
        logger.log(progress);
    }
    
    /**
     * Log final results
     * @param finalPercentage Final cleaning percentage
     */
    private void logFinalResults(double finalPercentage) {
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
    }
    
    // Getters
    public int getStepCount() {
        return stepCount;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public String getMissionStatus() {
        return missionStatus;
    }
    
    public int getActiveRobotCount() {
        return multiRobotManager.getActiveRobotCount(robots);
    }
    
    public int getTotalCellsCleaned() {
        return multiRobotManager.getTotalCellsCleaned(robots);
    }
}