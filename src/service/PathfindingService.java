package service;

import data.model.Cell;
import data.model.Room;
import data.model.Room.Point;
import java.util.*;

/**
 * Service for pathfinding algorithms with robust BFS
 * Prevents loops and optimizes exploration
 */
public class PathfindingService {
    
    // Cache for visited positions to prevent loops
    private Map<Integer, Set<Point>> recentVisits;
    private static final int VISIT_MEMORY_SIZE = 10; // Remember last 10 positions per robot
    
    public PathfindingService() {
        this.recentVisits = new HashMap<>();
    }
    
    /**
     * Node class for pathfinding with cost tracking
     */
    private static class Node {
        Point point;
        Node parent;
        int cost;
        
        Node(Point point, Node parent, int cost) {
            this.point = point;
            this.parent = parent;
            this.cost = cost;
        }
    }
    
    /**
     * Record a visit to prevent immediate loops
     * @param robotId Robot identifier
     * @param point Position visited
     */
    public void recordVisit(int robotId, Point point) {
        recentVisits.putIfAbsent(robotId, new LinkedHashSet<>());
        Set<Point> visits = recentVisits.get(robotId);
        
        // Keep only last N visits
        if (visits.size() >= VISIT_MEMORY_SIZE) {
            Point oldest = visits.iterator().next();
            visits.remove(oldest);
        }
        
        visits.add(point);
    }
    
    /**
     * Check if position was recently visited
     * @param robotId Robot identifier
     * @param point Position to check
     * @return true if recently visited
     */
    public boolean wasRecentlyVisited(int robotId, Point point) {
        Set<Point> visits = recentVisits.get(robotId);
        return visits != null && visits.contains(point);
    }
    
    /**
     * Clear visit history for a robot
     * @param robotId Robot identifier
     */
    public void clearVisitHistory(int robotId) {
        recentVisits.remove(robotId);
    }
    
    /**
     * Find the shortest path from start to goal using improved BFS
     * Avoids recently visited positions and considers path quality
     * 
     * @param room The room
     * @param start Starting point
     * @param goal Goal point
     * @param robotId Robot identifier for visit tracking
     * @return List of points representing the path (including start and goal), or empty list if no path
     */
    public List<Point> findPath(Room room, Point start, Point goal, int robotId) {
        if (start.equals(goal)) {
            List<Point> path = new ArrayList<>();
            path.add(start);
            return path;
        }
        
        Queue<Node> queue = new LinkedList<>();
        Map<Point, Integer> visited = new HashMap<>(); // Point -> cost
        
        queue.add(new Node(start, null, 0));
        visited.put(start, 0);
        
        // Directions: up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        Node bestGoalNode = null;
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            // Check if we reached the goal
            if (current.point.equals(goal)) {
                if (bestGoalNode == null || current.cost < bestGoalNode.cost) {
                    bestGoalNode = current;
                }
                continue; // Keep searching for better path
            }
            
            // Explore neighbors
            for (int[] dir : directions) {
                int newX = current.point.x + dir[0];
                int newY = current.point.y + dir[1];
                Point neighbor = new Point(newX, newY);
                
                // Check if valid position
                if (!room.isValidPosition(newX, newY)) {
                    continue;
                }
                
                // Check if traversable
                Cell cell = room.getCell(newX, newY);
                if (!cell.isTraversable()) {
                    continue;
                }
                
                // Check if occupied by robot (unless it's the goal)
                if (room.isCellOccupiedByRobot(newX, newY) && !neighbor.equals(goal)) {
                    continue;
                }
                
                int newCost = current.cost + 1;
                
                // Apply penalty for recently visited positions (unless it's the goal)
                if (!neighbor.equals(goal) && wasRecentlyVisited(robotId, neighbor)) {
                    newCost += 5; // Heavy penalty for revisiting
                }
                
                // Skip if we found a better path to this point
                if (visited.containsKey(neighbor) && visited.get(neighbor) <= newCost) {
                    continue;
                }
                
                // Add to queue
                visited.put(neighbor, newCost);
                queue.add(new Node(neighbor, current, newCost));
            }
        }
        
        // Reconstruct best path
        if (bestGoalNode != null) {
            return reconstructPath(bestGoalNode);
        }
        
        // No path found
        return new ArrayList<>();
    }
    
    /**
     * Overloaded version for backward compatibility
     */
    public List<Point> findPath(Room room, Point start, Point goal) {
        return findPath(room, start, goal, -1); // No robot tracking
    }
    
    /**
     * Reconstruct path from goal node back to start
     * @param goalNode The goal node
     * @return Path from start to goal
     */
    private List<Point> reconstructPath(Node goalNode) {
        List<Point> path = new ArrayList<>();
        Node current = goalNode;
        
        while (current != null) {
            path.add(current.point);
            current = current.parent;
        }
        
        // Reverse to get start -> goal order
        Collections.reverse(path);
        return path;
    }
    
    /**
     * Get the next move from a path
     * Returns the second point in the path (first is current position)
     * 
     * @param path The complete path
     * @return Next point to move to, or null if path is empty/invalid
     */
    public Point getNextMoveFromPath(List<Point> path) {
        if (path == null || path.size() < 2) {
            return null;
        }
        return path.get(1); // Index 0 is current position, 1 is next move
    }
    
    /**
     * Find path to closest target from a list with robot tracking
     * @param room The room
     * @param start Starting point
     * @param targets List of target points
     * @param robotId Robot identifier for visit tracking
     * @return Path to closest reachable target, or empty list
     */
    public List<Point> findPathToClosest(Room room, Point start, List<Point> targets, int robotId) {
        List<Point> bestPath = new ArrayList<>();
        int shortestDistance = Integer.MAX_VALUE;
        
        for (Point target : targets) {
            List<Point> path = findPath(room, start, target, robotId);
            
            if (!path.isEmpty() && path.size() < shortestDistance) {
                shortestDistance = path.size();
                bestPath = path;
            }
        }
        
        return bestPath;
    }
    
    /**
     * Overloaded version for backward compatibility
     */
    public List<Point> findPathToClosest(Room room, Point start, List<Point> targets) {
        return findPathToClosest(room, start, targets, -1);
    }
    
    /**
     * Check if a path exists between two points
     * @param room The room
     * @param start Starting point
     * @param goal Goal point
     * @return true if path exists
     */
    public boolean hasPath(Room room, Point start, Point goal) {
        List<Point> path = findPath(room, start, goal);
        return !path.isEmpty();
    }
    
    /**
     * Calculate path length (number of steps) with robot tracking
     * @param room The room
     * @param start Starting point
     * @param goal Goal point
     * @param robotId Robot identifier
     * @return Number of steps, or -1 if no path
     */
    public int getPathLength(Room room, Point start, Point goal, int robotId) {
        List<Point> path = findPath(room, start, goal, robotId);
        if (path.isEmpty()) {
            return -1;
        }
        return path.size() - 1; // Subtract 1 because path includes start position
    }
    
    /**
     * Overloaded version for backward compatibility
     */
    public int getPathLength(Room room, Point start, Point goal) {
        return getPathLength(room, start, goal, -1);
    }
    
    /**
     * Find best exploration target away from current position
     * Prioritizes targets that are far from recently visited areas
     * 
     * @param room The room
     * @param currentPos Current position
     * @param targets Available targets
     * @param robotId Robot identifier
     * @return Best exploration target or null
     */
    public Point findBestExplorationTarget(Room room, Point currentPos, List<Point> targets, int robotId) {
        if (targets.isEmpty()) {
            return null;
        }
        
        Set<Point> recentlyVisited = recentVisits.get(robotId);
        Point bestTarget = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Point target : targets) {
            // Calculate base distance (negative because closer is better for cost)
            int distance = Math.abs(target.x - currentPos.x) + Math.abs(target.y - currentPos.y);
            
            // Start with negative distance as base score
            double score = -distance;
            
            // Add bonus for being far from recently visited positions
            if (recentlyVisited != null && !recentlyVisited.isEmpty()) {
                int minDistFromVisited = Integer.MAX_VALUE;
                for (Point visited : recentlyVisited) {
                    int dist = Math.abs(target.x - visited.x) + Math.abs(target.y - visited.y);
                    minDistFromVisited = Math.min(minDistFromVisited, dist);
                }
                // Big bonus for being far from visited areas
                score += minDistFromVisited * 3;
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestTarget = target;
            }
        }
        
        return bestTarget;
    }
}