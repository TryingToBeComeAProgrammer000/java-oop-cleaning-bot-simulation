package service;

import data.dao.MovementLogger;
import data.model.Robot;
import data.model.Room;
import data.model.Room.Point;
import java.util.List;

/**
 * Controller for robot decision making and actions
 * Uses pathfinding to make intelligent decisions
 */
public class RobotController {
    private RobotService robotService;
    private PathfindingService pathfindingService;
    private MovementLogger logger;
    
    public RobotController(RobotService robotService, PathfindingService pathfindingService, MovementLogger logger) {
        this.robotService = robotService;
        this.pathfindingService = pathfindingService;
        this.logger = logger;
    }
    
    /**
     * Execute one action for a robot
     * Decides what to do and executes it
     * 
     * @param room The room
     * @param robot The robot
     * @return true if robot did something, false if stuck/inactive
     */
    public boolean executeRobotAction(Room room, Robot robot) {
        if (!robot.isActive()) {
            return false;
        }
        
        // 1. Check if standing on dirty cell and clean it
        if (robotService.cleanCell(room, robot)) {
            logger.logCleaning(robot);
        }
        
        // 2. Check if on recharge point
        if (robotService.isOnRechargePoint(room, robot)) {
            // Always recharge when on recharge point (up to max 20)
            if (robot.getBattery() < 20) {
                robot.recharge();
                logger.logRecharge(robot);
                
                // Ensure robot is active after recharging
                if (!robot.isActive() && robot.getBattery() > 0) {
                    robot.setActive(true);
                }
                
                return true;
            }
            // Fully charged, continue to next action
        }
        
        // 3. Check if needs to go recharge
        if (robotService.needsRecharge(room, robot)) {
            return goToRecharge(room, robot);
        }
        
        // 4. Find and go to dirty cell
        return goToClean(room, robot);
    }
    
    /**
     * Move robot towards recharge point
     * @param room The room
     * @param robot The robot
     * @return true if moved, false if stuck
     */
    private boolean goToRecharge(Room room, Robot robot) {
        Point closestRecharge = robotService.getClosestRechargePoint(room, robot);
        
        if (closestRecharge == null) {
            // No recharge point available - robot will die eventually
            logger.log("Robot #" + robot.getId() + " - No recharge point available!");
            return moveToAnyDirtyCell(room, robot); // Try to clean while we can
        }
        
        // Check if we have enough battery to reach
        Point currentPos = new Point(robot.getX(), robot.getY());
        int pathLength = pathfindingService.getPathLength(room, currentPos, closestRecharge);
        
        // Allow robot to try reaching recharge even with low battery
        // Only give up if battery is 0 and not on recharge point
        if (pathLength > robot.getBattery() && robot.getBattery() == 0 && !robotService.isOnRechargePoint(room, robot)) {
            // Can't reach recharge - will die
            logger.logDeactivation(robot, "Cannot reach recharge point - battery depleted");
            robot.deactivate();
            return false;
        }
        
        // Find path to recharge
        List<Point> path = pathfindingService.findPath(room, currentPos, closestRecharge);
        
        if (path.isEmpty()) {
            // No path to recharge
            if (robot.getBattery() == 0) {
                logger.logDeactivation(robot, "No path to recharge point");
                robot.deactivate();
                return false;
            }
            // Still has battery, try to find dirty cells
            return moveToAnyDirtyCell(room, robot);
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
            
            // Check if reached recharge and recharge
            if (robotService.isOnRechargePoint(room, robot)) {
                robot.recharge();
                logger.logRecharge(robot);
                
                // Make sure robot doesn't die after recharging
                if (!robot.isActive()) {
                    robot.setActive(true);
                }
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Move robot towards dirty cell to clean
     * @param room The room
     * @param robot The robot
     * @return true if moved, false if stuck or no targets
     */
    private boolean goToClean(Room room, Robot robot) {
        // Find best target avoiding other robots
        Point target = robotService.findBestTargetAvoidingOthers(room, robot, 2);
        
        if (target == null) {
            // No dirty cells available
            return false;
        }
        
        Point currentPos = new Point(robot.getX(), robot.getY());
        
        // Check if we have enough battery to reach target and return to recharge
        Point closestRecharge = robotService.getClosestRechargePoint(room, robot);
        
        if (closestRecharge != null) {
            int distanceToTarget = pathfindingService.getPathLength(room, currentPos, target);
            int distanceToRecharge = pathfindingService.getPathLength(room, target, closestRecharge);
            
            if (distanceToTarget > 0 && distanceToRecharge > 0) {
                int totalDistance = distanceToTarget + distanceToRecharge;
                
                // Need to have enough battery to go to target and back to recharge
                if (totalDistance + 2 > robot.getBattery()) {
                    // Not enough battery for this target, go recharge first
                    return goToRecharge(room, robot);
                }
            }
        }
        
        // Find path to target
        List<Point> path = pathfindingService.findPath(room, currentPos, target);
        
        if (path.isEmpty()) {
            // No path to this target, try any dirty cell
            return moveToAnyDirtyCell(room, robot);
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
     * Try to move to any reachable dirty cell
     * Fallback when preferred target is not reachable
     * 
     * @param room The room
     * @param robot The robot
     * @return true if found and moved, false otherwise
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
            // No reachable dirty cells
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
}