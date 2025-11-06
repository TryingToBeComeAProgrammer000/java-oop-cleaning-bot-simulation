package service;

import data.dao.MovementLogger;
import data.model.Robot;
import data.model.Room;
import data.model.Room.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager for multiple robots
 * Handles robot creation, initialization and coordination
 */
public class MultiRobotManager {
    private MovementLogger logger;
    
    public MultiRobotManager(MovementLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Calculate recommended number of robots based on room characteristics
     * Formula considers:
     * - Room size (larger rooms need more robots)
     * - Dirt percentage (more dirt needs more robots)
     * - Obstacle percentage (more obstacles need more robots)
     * - Distance between areas (spread out dirt needs more robots)
     * 
     * @param room The room to analyze
     * @return Recommended number of robots (1-4)
     */
    public int calculateRecommendedRobots(Room room) {
        int totalCells = room.getRows() * room.getCols();
        int dirtyCells = room.getTotalDirtyCells();
        int obstacles = room.countPermanentObstacles();
        int rechargePoints = room.countRechargePoints();
        
        double dirtyPercentage = (dirtyCells * 100.0) / totalCells;
        double obstaclePercentage = (obstacles * 100.0) / totalCells;
        
        // Base calculation: 1 robot per 30 cells
        int baseRobots = Math.max(1, totalCells / 30);
        
        // Adjust for dirt percentage
        if (dirtyPercentage > 40) {
            baseRobots++; // High dirt needs extra robot
        } else if (dirtyPercentage > 60) {
            baseRobots += 2; // Very high dirt needs 2 extra
        }
        
        // Adjust for obstacles
        if (obstaclePercentage > 20) {
            baseRobots++; // Many obstacles slow down robots
        }
        
        // Adjust for recharge point availability
        if (rechargePoints < 2 && totalCells > 50) {
            baseRobots--; // Few recharge points = robots spend more time traveling
        }
        
        // Ensure we have at least 1 robot per recharge point (up to 4)
        baseRobots = Math.max(baseRobots, Math.min(rechargePoints, 2));
        
        // Cap between 1 and 4 robots
        return Math.max(1, Math.min(baseRobots, 4));
    }
    
    /**
     * Initialize multiple robots in the room
     * Places them in empty positions distributed across the room
     * 
     * @param room The room
     * @param count Number of robots to create
     * @return List of created robots
     */
    public List<Robot> initializeRobots(Room room, int count) {
        List<Robot> robots = new ArrayList<>();
        
        logger.log("=== INITIALIZING " + count + " ROBOT(S) ===");
        
        for (int i = 1; i <= count; i++) {
            Point position = room.findEmptyPosition();
            
            if (position == null) {
                logger.log("! WARNING: Could not find empty position for Robot #" + i);
                System.err.println("Warning: Could not place robot #" + i);
                break;
            }
            
            Robot robot = new Robot(i, position.x, position.y);
            room.addRobot(robot);
            robots.add(robot);
            
            logger.logInitialization(robot);
            System.out.println("Robot #" + i + " initialized at " + position);
        }
        
        logger.log("=== " + robots.size() + " ROBOT(S) READY ===\n");
        
        return robots;
    }
    
    /**
     * Get count of active robots
     * @param robots List of robots
     * @return Number of active robots
     */
    public int getActiveRobotCount(List<Robot> robots) {
        int count = 0;
        for (Robot robot : robots) {
            if (robot.isActive()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Check if all robots are inactive
     * @param robots List of robots
     * @return true if all inactive
     */
    public boolean areAllRobotsInactive(List<Robot> robots) {
        for (Robot robot : robots) {
            if (robot.isActive()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get total cells cleaned by all robots
     * @param robots List of robots
     * @return Total cells cleaned
     */
    public int getTotalCellsCleaned(List<Robot> robots) {
        int total = 0;
        for (Robot robot : robots) {
            total += robot.getCellsCleaned();
        }
        return total;
    }
    
    /**
     * Get total movements by all robots
     * @param robots List of robots
     * @return Total movements
     */
    public int getTotalMovements(List<Robot> robots) {
        int total = 0;
        for (Robot robot : robots) {
            total += robot.getMovementCount();
        }
        return total;
    }
    
    /**
     * Get robot with highest battery
     * @param robots List of robots
     * @return Robot with most battery, or null if all inactive
     */
    public Robot getRobotWithHighestBattery(List<Robot> robots) {
        Robot best = null;
        int maxBattery = -1;
        
        for (Robot robot : robots) {
            if (robot.isActive() && robot.getBattery() > maxBattery) {
                maxBattery = robot.getBattery();
                best = robot;
            }
        }
        
        return best;
    }
    
    /**
     * Get statistics summary for all robots
     * @param robots List of robots
     * @return String with statistics
     */
    public String getRobotsStatistics(List<Robot> robots) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== ROBOTS STATISTICS ===\n");
        
        for (Robot robot : robots) {
            sb.append(String.format("Robot #%d: %s | Battery: %d | Cleaned: %d | Moves: %d\n",
                robot.getId(),
                robot.getStatus(),
                robot.getBattery(),
                robot.getCellsCleaned(),
                robot.getMovementCount()));
        }
        
        sb.append("\nTotals:\n");
        sb.append("Active robots: " + getActiveRobotCount(robots) + "/" + robots.size() + "\n");
        sb.append("Total cleaned: " + getTotalCellsCleaned(robots) + "\n");
        sb.append("Total movements: " + getTotalMovements(robots) + "\n");
        sb.append("========================\n");
        
        return sb.toString();
    }
}