package service;

import data.model.Cell;
import data.model.Room;
import data.model.Room.Point;
import java.util.*;

/**
 * Service for pathfinding algorithms
 * Uses BFS (Breadth-First Search) to find shortest paths
 */
public class PathfindingService {
    
    /**
     * Node class for pathfinding
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
     * Find the shortest path from start to goal using BFS
     * Avoids permanent and temporary obstacles
     * 
     * @param room The room
     * @param start Starting point
     * @param goal Goal point
     * @return List of points representing the path (including start and goal), or empty list if no path
     */
    public List<Point> findPath(Room room, Point start, Point goal) {
        if (start.equals(goal)) {
            List<Point> path = new ArrayList<>();
            path.add(start);
            return path;
        }
        
        Queue<Node> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();
        
        queue.add(new Node(start, null, 0));
        visited.add(start);
        
        // Directions: up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            
            // Check if we reached the goal
            if (current.point.equals(goal)) {
                return reconstructPath(current);
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
                
                // Skip if already visited
                if (visited.contains(neighbor)) {
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
                
                // Add to queue
                visited.add(neighbor);
                queue.add(new Node(neighbor, current, current.cost + 1));
            }
        }
        
        // No path found
        return new ArrayList<>();
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
     * Find path to closest target from a list
     * @param room The room
     * @param start Starting point
     * @param targets List of target points
     * @return Path to closest reachable target, or empty list
     */
    public List<Point> findPathToClosest(Room room, Point start, List<Point> targets) {
        List<Point> bestPath = new ArrayList<>();
        int shortestDistance = Integer.MAX_VALUE;
        
        for (Point target : targets) {
            List<Point> path = findPath(room, start, target);
            
            if (!path.isEmpty() && path.size() < shortestDistance) {
                shortestDistance = path.size();
                bestPath = path;
            }
        }
        
        return bestPath;
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
     * Calculate path length (number of steps)
     * @param room The room
     * @param start Starting point
     * @param goal Goal point
     * @return Number of steps, or -1 if no path
     */
    public int getPathLength(Room room, Point start, Point goal) {
        List<Point> path = findPath(room, start, goal);
        if (path.isEmpty()) {
            return -1;
        }
        return path.size() - 1; // Subtract 1 because path includes start position
    }
}