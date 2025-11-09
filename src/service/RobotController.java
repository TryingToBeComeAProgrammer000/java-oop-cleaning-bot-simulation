package service;

import data.dao.MovementLogger;
import data.model.Robot;
import data.model.Room;
import data.model.Room.Point;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for robot decision making and actions
 * Uses pathfinding to make intelligent decisions
 * FIXED: Eliminates loops and erratic behavior
 */
public class RobotController {
    private RobotService robotService;
    private PathfindingService pathfindingService;
    private MovementLogger logger;
    
    // Track robot states to avoid loops
    private Map<Integer, Point> lastCleanedCell; // Last cell cleaned by each robot
    private Map<Integer, Integer> stepsAtRecharge; // Steps spent at recharge point
    private Map<Integer, Boolean> isRecharging; // Is robot currently recharging
    private Map<Integer, Point> lastRechargePoint; // Last recharge point used
    private Map<Integer, Integer> stepsAwayFromRecharge; // Steps since leaving recharge
    
    private static final int BATTERY_LOW_THRESHOLD = 8; // Go to recharge at 8 or less
    private static final int BATTERY_FULL_THRESHOLD = 18; // Leave recharge at 18 or more
    private static final int MAX_RECHARGE_STEPS = 3; // Max steps at recharge point
    private static final int MIN_STEPS_AWAY_FROM_RECHARGE = 5; // Must move 5 steps away before returning
    
    public RobotController(RobotService robotService, PathfindingService pathfindingService, MovementLogger logger) {
        this.robotService = robotService;
        this.pathfindingService = pathfindingService;
        this.logger = logger;
        this.lastCleanedCell = new HashMap<>();
        this.stepsAtRecharge = new HashMap<>();
        this.isRecharging = new HashMap<>();
        this.lastRechargePoint = new HashMap<>();
        this.stepsAwayFromRecharge = new HashMap<>();
    }
    
    /**
     * Execute one action for a robot
     * FIXED: Clear priority system to avoid loops
     * 
     * @param room The room
     * @param robot The robot
     * @return true if robot did something, false if stuck/inactive
     */
    public boolean executeRobotAction(Room room, Robot robot) {
        if (!robot.isActive()) {
            return false;
        }
        
        int robotId = robot.getId();
        Point currentPos = new Point(robot.getX(), robot.getY());
        
        // Track steps away from recharge point
        Point lastRecharge = lastRechargePoint.get(robotId);
        if (lastRecharge != null && !currentPos.equals(lastRecharge)) {
            int stepsAway = stepsAwayFromRecharge.getOrDefault(robotId, 0);
            stepsAwayFromRecharge.put(robotId, stepsAway + 1);
        }
        
        // Priority 1: Clean current cell if dirty (always do this first)
        if (robotService.cleanCell(room, robot)) {
            logger.logCleaning(robot);
            lastCleanedCell.put(robotId, new Point(robot.getX(), robot.getY()));
        }
        
        // Priority 2: Handle recharge point logic
        if (robotService.isOnRechargePoint(room, robot)) {
            return handleRechargePoint(room, robot);
        }
        
        // Priority 3: Check if needs emergency recharge (battery critical)
        if (needsEmergencyRecharge(robot)) {
            return goToRecharge(room, robot);
        }
        
        // Priority 4: Check if should go recharge (battery low but not critical)
        if (shouldGoRecharge(room, robot)) {
            return goToRecharge(room, robot);
        }
        
        // Priority 5: Continue cleaning
        return goToClean(room, robot);
    }
    
    /**
     * Handle robot at recharge point
     * FIXED: Clear logic to avoid getting stuck
     */
    private boolean handleRechargePoint(Room room, Robot robot) {
        int robotId = robot.getId();
        Point currentPos = new Point(robot.getX(), robot.getY());
        
        // Track steps at recharge
        stepsAtRecharge.putIfAbsent(robotId, 0);
        int steps = stepsAtRecharge.get(robotId);
        
        // Check if battery is full or reached max recharge steps
        if (robot.getBattery() >= BATTERY_FULL_THRESHOLD || steps >= MAX_RECHARGE_STEPS) {
            // Battery is good, LEAVE recharge point
            isRecharging.put(robotId, false);
            stepsAtRecharge.put(robotId, 0);
            lastRechargePoint.put(robotId, currentPos); // Remember this recharge point
            stepsAwayFromRecharge.put(robotId, 0); // Reset counter
            logger.log("Robot #" + robotId + " - Fully recharged, resuming cleaning");
            
            // Find dirty cells to move toward (not emergency move)
            return moveAwayFromRecharge(room, robot);
        }
        
        // Battery still low, continue recharging
        isRecharging.put(robotId, true);
        stepsAtRecharge.put(robotId, steps + 1);
        robot.recharge();
        logger.logRecharge(robot);
        
        // Ensure robot is active after recharging
        if (!robot.isActive() && robot.getBattery() > 0) {
            robot.setActive(true);
        }
        
        return true;
    }
    
    /**
     * Move away from recharge point toward cleaning targets
     * FIXED: Intelligent departure from recharge point
     */
    private boolean moveAwayFromRecharge(Room room, Robot robot) {
        Point currentPos = new Point(robot.getX(), robot.getY());
        
        // Find the closest dirty cell
        List<Point> allDirtyCells = robotService.getAllDirtyCells(room);
        
        if (allDirtyCells.isEmpty()) {
            // No dirty cells, just move away from recharge point
            return makeEmergencyMove(room, robot);
        }
        
        // Find path to closest dirty cell
        List<Point> path = pathfindingService.findPathToClosest(room, currentPos, allDirtyCells);
        
        if (path.isEmpty() || path.size() < 2) {
            // Can't find path, move randomly away
            return makeEmergencyMove(room, robot);
        }
        
        // Move toward the dirty cell
        Point nextMove = pathfindingService.getNextMoveFromPath(path);
        
        if (nextMove == null) {
            return makeEmergencyMove(room, robot);
        }
        
        int oldX = robot.getX();
        int oldY = robot.getY();
        
        if (robotService.moveRobot(room, robot, nextMove.x, nextMove.y)) {
            logger.log("Robot #" + robot.getId() + " - Leaving recharge point toward target");
            logger.logMovement(robot, oldX, oldY, robot.getX(), robot.getY());
            return true;
        }
        
        return makeEmergencyMove(room, robot);
    }
    
    /**
     * Check if robot needs EMERGENCY recharge (battery critical)
     * FIXED: More aggressive threshold for safety
     */
    private boolean needsEmergencyRecharge(Robot robot) {
        // Critical battery - must go NOW
        return robot.getBattery() <= 3;
    }
    
    /**
     * Check if robot should go recharge (battery low but can plan)
     * FIXED: Consider distance to recharge point AND avoid returning too soon
     */
    private boolean shouldGoRecharge(Room room, Robot robot) {
        int robotId = robot.getId();
        
        // Don't go if battery is still good
        if (robot.getBattery() > BATTERY_LOW_THRESHOLD) {
            return false;
        }
        
        // Don't return to recharge if we just left recently
        Point lastRecharge = lastRechargePoint.get(robotId);
        if (lastRecharge != null) {
            int stepsAway = stepsAwayFromRecharge.getOrDefault(robotId, 0);
            if (stepsAway < MIN_STEPS_AWAY_FROM_RECHARGE) {
                // Just left recharge, don't go back yet unless critical
                if (robot.getBattery() > 5) {
                    return false;
                }
            }
        }
        
        // Check if we can reach recharge point
        Point closestRecharge = robotService.getClosestRechargePoint(room, robot);
        if (closestRecharge == null) {
            return false; // No recharge available
        }
        
        Point currentPos = new Point(robot.getX(), robot.getY());
        int distanceToRecharge = pathfindingService.getPathLength(room, currentPos, closestRecharge);
        
        if (distanceToRecharge < 0) {
            return false; // Can't reach
        }
        
        // Go to recharge if we might not make it back safely
        // Add safety margin of 2
        return robot.getBattery() <= (distanceToRecharge + 2);
    }
    
    /**
     * Move robot towards recharge point
     * FIXED: Better error handling and fallback
     */
    private boolean goToRecharge(Room room, Robot robot) {
        Point closestRecharge = robotService.getClosestRechargePoint(room, robot);
        
        if (closestRecharge == null) {
            logger.log("Robot #" + robot.getId() + " - No recharge point available!");
            // Try to clean what we can
            return goToClean(room, robot);
        }
        
        Point currentPos = new Point(robot.getX(), robot.getY());
        
        // Check if already at recharge
        if (currentPos.equals(closestRecharge)) {
            return handleRechargePoint(room, robot);
        }
        
        // Find path to recharge
        List<Point> path = pathfindingService.findPath(room, currentPos, closestRecharge);
        
        if (path.isEmpty()) {
            // No path to recharge
            if (robot.getBattery() <= 1) {
                logger.logDeactivation(robot, "No path to recharge point");
                robot.deactivate();
                return false;
            }
            // Try to clean or move somewhere
            return makeEmergencyMove(room, robot);
        }
        
        // Check if we have enough battery
        if (path.size() > robot.getBattery() && robot.getBattery() <= 1) {
            logger.logDeactivation(robot, "Insufficient battery to reach recharge");
            robot.deactivate();
            return false;
        }
        
        // Move one step towards recharge
        Point nextMove = pathfindingService.getNextMoveFromPath(path);
        
        if (nextMove == null) {
            return false;
        }
        
        logger.logGoingToRecharge(robot, closestRecharge.x, closestRecharge.y);
        
        int oldX = robot.getX();
        int oldY = robot.getY();
        
        if (robotService.moveRobot(room, robot, nextMove.x, nextMove.y)) {
            logger.logMovement(robot, oldX, oldY, robot.getX(), robot.getY());
            return true;
        }
        
        return false;
    }
    
    /**
     * Move robot towards dirty cell to clean
     * FIXED: Avoid recently cleaned cells and better target selection
     */
    private boolean goToClean(Room room, Robot robot) {
        int robotId = robot.getId();
        
        // Find best target avoiding other robots and recently cleaned cells
        Point target = findBestCleaningTarget(room, robot);
        
        if (target == null) {
            // No dirty cells available - mission might be complete
            return false;
        }
        
        Point currentPos = new Point(robot.getX(), robot.getY());
        
        // Find path to target
        List<Point> path = pathfindingService.findPath(room, currentPos, target);
        
        if (path.isEmpty()) {
            // No path to this target, try any reachable dirty cell
            return moveToAnyDirtyCell(room, robot);
        }
        
        // Check if we have enough battery to reach target and return to recharge
        if (shouldCheckBatteryForTarget(room, robot, path)) {
            return goToRecharge(room, robot);
        }
        
        // Move one step towards target
        Point nextMove = pathfindingService.getNextMoveFromPath(path);
        
        if (nextMove == null) {
            return false;
        }
        
        int oldX = robot.getX();
        int oldY = robot.getY();
        
        if (robotService.moveRobot(room, robot, nextMove.x, nextMove.y)) {
            logger.logMovement(robot, oldX, oldY, robot.getX(), robot.getY());
            return true;
        }
        
        return false;
    }
    
    /**
     * Find best cleaning target avoiding recently cleaned cells
     * FIXED: Smart target selection
     */
    private Point findBestCleaningTarget(Room room, Robot robot) {
        int robotId = robot.getId();
        Point lastCleaned = lastCleanedCell.get(robotId);
        
        // Get all dirty cells
        List<Point> allDirtyCells = robotService.getAllDirtyCells(room);
        
        if (allDirtyCells.isEmpty()) {
            return null;
        }
        
        Point currentPos = new Point(robot.getX(), robot.getY());
        Point bestTarget = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Point dirtyCell : allDirtyCells) {
            // Skip recently cleaned cell
            if (lastCleaned != null && dirtyCell.equals(lastCleaned)) {
                continue;
            }
            
            // Check if another robot is too close
            boolean tooCloseToOther = false;
            for (Robot other : room.getRobots()) {
                if (other.getId() != robotId && other.isActive()) {
                    int distToOther = robotService.calculateDistance(
                        dirtyCell.x, dirtyCell.y, other.getX(), other.getY()
                    );
                    
                    // Avoid if other robot is closer and has more battery
                    if (distToOther <= 2 && other.getBattery() >= robot.getBattery()) {
                        tooCloseToOther = true;
                        break;
                    }
                }
            }
            
            if (tooCloseToOther) {
                continue;
            }
            
            // Calculate score: closer is better
            int distance = robotService.calculateDistance(
                currentPos.x, currentPos.y, dirtyCell.x, dirtyCell.y
            );
            
            int score = -distance; // Negative because closer = higher score
            
            // Bonus for being far from other robots (reduce clustering)
            int minDistToOther = Integer.MAX_VALUE;
            for (Robot other : room.getRobots()) {
                if (other.getId() != robotId && other.isActive()) {
                    int dist = robotService.calculateDistance(
                        dirtyCell.x, dirtyCell.y, other.getX(), other.getY()
                    );
                    minDistToOther = Math.min(minDistToOther, dist);
                }
            }
            
            if (minDistToOther != Integer.MAX_VALUE && minDistToOther > 2) {
                score += minDistToOther; // Bonus for spreading out
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestTarget = dirtyCell;
            }
        }
        
        return bestTarget;
    }
    
    /**
     * Check if robot should go recharge before pursuing target
     * FIXED: Better battery planning
     */
    private boolean shouldCheckBatteryForTarget(Room room, Robot robot, List<Point> pathToTarget) {
        // If battery is very low, don't bother checking
        if (robot.getBattery() <= 5) {
            return true;
        }
        
        // If battery is high, no need to check
        if (robot.getBattery() >= 12) {
            return false;
        }
        
        Point closestRecharge = robotService.getClosestRechargePoint(room, robot);
        if (closestRecharge == null) {
            return false;
        }
        
        Point targetPos = pathToTarget.get(pathToTarget.size() - 1);
        int distanceToTarget = pathToTarget.size() - 1;
        int distanceToRecharge = pathfindingService.getPathLength(room, targetPos, closestRecharge);
        
        if (distanceToRecharge < 0) {
            return false;
        }
        
        // Need battery for: target + recharge + safety margin
        int requiredBattery = distanceToTarget + distanceToRecharge + 3;
        
        return robot.getBattery() < requiredBattery;
    }
    
    /**
     * Try to move to any reachable dirty cell
     * FIXED: Better fallback strategy
     */
    private boolean moveToAnyDirtyCell(Room room, Robot robot) {
        List<Point> allDirtyCells = robotService.getAllDirtyCells(room);
        
        if (allDirtyCells.isEmpty()) {
            return false;
        }
        
        Point currentPos = new Point(robot.getX(), robot.getY());
        
        // Find closest reachable dirty cell
        List<Point> path = pathfindingService.findPathToClosest(room, currentPos, allDirtyCells);
        
        if (path.isEmpty()) {
            return false;
        }
        
        Point nextMove = pathfindingService.getNextMoveFromPath(path);
        
        if (nextMove == null) {
            return false;
        }
        
        int oldX = robot.getX();
        int oldY = robot.getY();
        
        if (robotService.moveRobot(room, robot, nextMove.x, nextMove.y)) {
            logger.logMovement(robot, oldX, oldY, robot.getX(), robot.getY());
            return true;
        }
        
        return false;
    }
    
    /**
     * Emergency move to avoid robot getting stuck
     * FIXED: Avoid returning to recharge point immediately
     */
    private boolean makeEmergencyMove(Room room, Robot robot) {
        int robotId = robot.getId();
        Point lastRecharge = lastRechargePoint.get(robotId);
        int stepsAway = stepsAwayFromRecharge.getOrDefault(robotId, Integer.MAX_VALUE);
        
        List<Point> adjacentCells = robotService.getAdjacentCells(room, robot);
        
        // Try to move to an adjacent cell, avoiding recharge points we just left
        for (Point cell : adjacentCells) {
            if (robotService.canMoveTo(room, cell.x, cell.y)) {
                boolean isRecentRecharge = lastRecharge != null && 
                                          cell.equals(lastRecharge) && 
                                          stepsAway < MIN_STEPS_AWAY_FROM_RECHARGE;
                
                // Skip if it's the recharge point we just left
                if (isRecentRecharge) {
                    continue;
                }
                
                // Prefer cells that are not recharge points
                if (!room.getCell(cell.x, cell.y).isRechargePoint()) {
                    int oldX = robot.getX();
                    int oldY = robot.getY();
                    
                    if (robotService.moveRobot(room, robot, cell.x, cell.y)) {
                        logger.log("Robot #" + robotId + " - Making emergency move to unexplored area");
                        logger.logMovement(robot, oldX, oldY, robot.getX(), robot.getY());
                        return true;
                    }
                }
            }
        }
        
        // If all else fails, move anywhere possible (except recent recharge)
        for (Point cell : adjacentCells) {
            if (robotService.canMoveTo(room, cell.x, cell.y)) {
                boolean isRecentRecharge = lastRecharge != null && 
                                          cell.equals(lastRecharge) && 
                                          stepsAway < MIN_STEPS_AWAY_FROM_RECHARGE;
                
                if (isRecentRecharge) {
                    continue;
                }
                
                int oldX = robot.getX();
                int oldY = robot.getY();
                
                if (robotService.moveRobot(room, robot, cell.x, cell.y)) {
                    logger.log("Robot #" + robotId + " - Critical emergency move");
                    logger.logMovement(robot, oldX, oldY, robot.getX(), robot.getY());
                    return true;
                }
            }
        }
        
        return false;
    }
}