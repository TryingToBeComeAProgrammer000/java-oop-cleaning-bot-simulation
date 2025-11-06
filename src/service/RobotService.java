package service;

import data.model.Cell;
import data.model.Robot;
import data.model.Room;
import data.model.Room.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for robot movement and decision making
 */
public class RobotService {
    
    /**
     * Check if robot can move to a specific position
     * @param room The room
     * @param x Target X
     * @param y Target Y
     * @return true if can move
     */
    public boolean canMoveTo(Room room, int x, int y) {
        // Check bounds
        if (!room.isValidPosition(x, y)) {
            return false;
        }
        
        // Check if cell is traversable (not O or T)
        Cell cell = room.getCell(x, y);
        if (!cell.isTraversable()) {
            return false;
        }
        
        // Check if another robot is there
        if (room.isCellOccupiedByRobot(x, y)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Move robot to new position and consume battery
     * @param room The room
     * @param robot The robot
     * @param newX New X position
     * @param newY New Y position
     * @return true if move was successful
     */
    public boolean moveRobot(Room room, Robot robot, int newX, int newY) {
        if (!canMoveTo(room, newX, newY)) {
            return false;
        }
        
        robot.moveTo(newX, newY);
        robot.consumeBattery();
        
        return true;
    }
    
    /**
     * Clean the cell where robot is standing if it's dirty
     * @param room The room
     * @param robot The robot
     * @return true if cell was cleaned
     */
    public boolean cleanCell(Room room, Robot robot) {
        Cell cell = room.getCell(robot.getX(), robot.getY());
        
        if (cell != null && cell.isDirty()) {
            cell.clean();
            robot.incrementCellsCleaned();
            return true;
        }
        
        return false;
    }
    
    /**
     * Get adjacent cells (up, down, left, right)
     * @param room The room
     * @param robot The robot
     * @return List of adjacent points
     */
    public List<Point> getAdjacentCells(Room room, Robot robot) {
        List<Point> adjacent = new ArrayList<>();
        int x = robot.getX();
        int y = robot.getY();
        
        // Up
        if (room.isValidPosition(x - 1, y)) {
            adjacent.add(new Point(x - 1, y));
        }
        
        // Down
        if (room.isValidPosition(x + 1, y)) {
            adjacent.add(new Point(x + 1, y));
        }
        
        // Left
        if (room.isValidPosition(x, y - 1)) {
            adjacent.add(new Point(x, y - 1));
        }
        
        // Right
        if (room.isValidPosition(x, y + 1)) {
            adjacent.add(new Point(x, y + 1));
        }
        
        return adjacent;
    }
    
    /**
     * Calculate Manhattan distance between two points
     * @param x1 First point X
     * @param y1 First point Y
     * @param x2 Second point X
     * @param y2 Second point Y
     * @return Manhattan distance
     */
    public int calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * Find closest dirty cell to robot
     * @param room The room
     * @param robot The robot
     * @return Point of closest dirty cell or null if none found
     */
    public Point getClosestDirtyCell(Room room, Robot robot) {
        Point closest = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                Cell cell = room.getCell(i, j);
                if (cell.isDirty() && !room.isCellOccupiedByRobot(i, j)) {
                    int distance = calculateDistance(robot.getX(), robot.getY(), i, j);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closest = new Point(i, j);
                    }
                }
            }
        }
        
        return closest;
    }
    
    /**
     * Find all recharge points in the room
     * @param room The room
     * @return List of recharge points
     */
    public List<Point> getRechargePoints(Room room) {
        List<Point> rechargePoints = new ArrayList<>();
        
        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                Cell cell = room.getCell(i, j);
                if (cell.isRechargePoint()) {
                    rechargePoints.add(new Point(i, j));
                }
            }
        }
        
        return rechargePoints;
    }
    
    /**
     * Find closest recharge point to robot
     * @param room The room
     * @param robot The robot
     * @return Point of closest recharge or null if none found
     */
    public Point getClosestRechargePoint(Room room, Robot robot) {
        List<Point> rechargePoints = getRechargePoints(room);
        
        if (rechargePoints.isEmpty()) {
            return null;
        }
        
        Point closest = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Point point : rechargePoints) {
            int distance = calculateDistance(robot.getX(), robot.getY(), point.x, point.y);
            if (distance < minDistance) {
                minDistance = distance;
                closest = point;
            }
        }
        
        return closest;
    }
    
    /**
     * Check if there's another robot nearby (within radius)
     * @param room The room
     * @param robot The robot to check around
     * @param radius Search radius
     * @return true if another robot is nearby
     */
    public boolean isAnotherRobotNearby(Room room, Robot robot, int radius) {
        for (Robot other : room.getRobots()) {
            if (other.getId() != robot.getId() && other.isActive()) {
                int distance = calculateDistance(robot.getX(), robot.getY(), 
                                                other.getX(), other.getY());
                if (distance <= radius) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Get next move towards a target using simple greedy approach
     * Moves one step closer to target on X or Y axis
     * @param room The room
     * @param robot The robot
     * @param target Target point
     * @return Next position to move to, or null if can't move
     */
    public Point getNextMoveTowards(Room room, Robot robot, Point target) {
        int currentX = robot.getX();
        int currentY = robot.getY();
        
        // Calculate differences
        int dx = target.x - currentX;
        int dy = target.y - currentY;
        
        // Try to move on X axis first if needed
        if (dx != 0) {
            int nextX = currentX + (dx > 0 ? 1 : -1);
            if (canMoveTo(room, nextX, currentY)) {
                return new Point(nextX, currentY);
            }
        }
        
        // Try to move on Y axis if X didn't work or not needed
        if (dy != 0) {
            int nextY = currentY + (dy > 0 ? 1 : -1);
            if (canMoveTo(room, currentX, nextY)) {
                return new Point(currentX, nextY);
            }
        }
        
        // If direct path blocked, try adjacent cells
        List<Point> adjacent = getAdjacentCells(room, robot);
        for (Point adj : adjacent) {
            if (canMoveTo(room, adj.x, adj.y)) {
                // Choose the one that gets us closer to target
                int newDist = calculateDistance(adj.x, adj.y, target.x, target.y);
                int currentDist = calculateDistance(currentX, currentY, target.x, target.y);
                if (newDist < currentDist) {
                    return adj;
                }
            }
        }
        
        // If no good move, just try any adjacent cell
        for (Point adj : adjacent) {
            if (canMoveTo(room, adj.x, adj.y)) {
                return adj;
            }
        }
        
        return null; // Stuck
    }
    
    /**
     * Check if robot is on a recharge point
     * @param room The room
     * @param robot The robot
     * @return true if on recharge point
     */
    public boolean isOnRechargePoint(Room room, Robot robot) {
        Cell cell = room.getCell(robot.getX(), robot.getY());
        return cell != null && cell.isRechargePoint();
    }
    
    /**
     * Check if robot needs to go recharge
     * Considers battery level and distance to nearest recharge point
     * @param room The room
     * @param robot The robot
     * @return true if should go recharge
     */
    public boolean needsRecharge(Room room, Robot robot) {
        // Always recharge if battery is very low
        if (robot.getBattery() <= 3) {
            return true;
        }
        
        // Don't need recharge if battery is good
        if (robot.getBattery() >= 12) {
            return false;
        }
        
        Point closestRecharge = getClosestRechargePoint(room, robot);
        if (closestRecharge == null) {
            return false; // No recharge point available
        }
        
        int distanceToRecharge = calculateDistance(robot.getX(), robot.getY(), 
                                                   closestRecharge.x, closestRecharge.y);
        
        // Need recharge if battery is getting close to distance needed
        // Use Manhattan distance as approximation (actual path may be longer)
        return robot.getBattery() <= (distanceToRecharge * 1.5 + 3); // 1.5x for detours, +3 safety
    }
    
    /**
     * Find the best target for a robot considering other robots
     * Avoids targets that are too close to other robots
     * Prioritizes targets that are further from other robots
     * 
     * @param room The room
     * @param robot The robot
     * @param avoidRadius Radius to avoid other robots
     * @return Best target point or null if none available
     */
    public Point findBestTargetAvoidingOthers(Room room, Robot robot, int avoidRadius) {
        List<Point> dirtyCells = getAllDirtyCells(room);
        
        if (dirtyCells.isEmpty()) {
            return null;
        }
        
        Point bestTarget = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Point dirtyCell : dirtyCells) {
            // Check if another robot is heading here or nearby
            boolean tooCloseToOtherRobot = false;
            
            for (Robot other : room.getRobots()) {
                if (other.getId() != robot.getId() && other.isActive()) {
                    int distToOther = calculateDistance(dirtyCell.x, dirtyCell.y, 
                                                       other.getX(), other.getY());
                    
                    if (distToOther <= avoidRadius) {
                        // Another robot is close to this target
                        // Only avoid if the other robot has more battery (priority)
                        if (other.getBattery() >= robot.getBattery()) {
                            tooCloseToOtherRobot = true;
                            break;
                        }
                    }
                }
            }
            
            if (tooCloseToOtherRobot) {
                continue; // Skip this target
            }
            
            // Calculate score: closer is better, further from others is better
            int distanceToRobot = calculateDistance(robot.getX(), robot.getY(), 
                                                   dirtyCell.x, dirtyCell.y);
            
            // Score: negative distance (closer = higher score)
            int score = -distanceToRobot;
            
            // Add bonus for being far from other robots
            int minDistToOther = Integer.MAX_VALUE;
            for (Robot other : room.getRobots()) {
                if (other.getId() != robot.getId() && other.isActive()) {
                    int dist = calculateDistance(dirtyCell.x, dirtyCell.y, 
                                               other.getX(), other.getY());
                    minDistToOther = Math.min(minDistToOther, dist);
                }
            }
            
            if (minDistToOther != Integer.MAX_VALUE) {
                score += minDistToOther * 2; // Bonus for being far from others
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestTarget = dirtyCell;
            }
        }
        
        return bestTarget != null ? bestTarget : getClosestDirtyCell(room, robot);
    }
    
    /**
     * Get all dirty cells in the room
     * @param room The room
     * @return List of all dirty cell positions
     */
    public List<Point> getAllDirtyCells(Room room) {
        List<Point> dirtyCells = new ArrayList<>();
        
        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                Cell cell = room.getCell(i, j);
                if (cell.isDirty()) {
                    dirtyCells.add(new Point(i, j));
                }
            }
        }
        
        return dirtyCells;
    }
    
    /**
     * Determine which robot should handle a specific target
     * Based on distance and battery level
     * 
     * @param room The room
     * @param target Target position
     * @param robots List of robots to consider
     * @return Best robot for the job or null
     */
    public Robot selectBestRobotForTarget(Room room, Point target, List<Robot> robots) {
        Robot bestRobot = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Robot robot : robots) {
            if (!robot.isActive()) {
                continue;
            }
            
            int distance = calculateDistance(robot.getX(), robot.getY(), target.x, target.y);
            
            // Skip if doesn't have energy to reach
            if (!robot.hasEnergyToReach(distance + 3)) { // +3 safety margin
                continue;
            }
            
            // Score based on distance (closer is better) and battery (more is better)
            int score = -distance + (robot.getBattery() / 2);
            
            if (score > bestScore) {
                bestScore = score;
                bestRobot = robot;
            }
        }
        
        return bestRobot;
    }
}