package data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a room (salon) as a matrix of cells
 */
public class Room {
    private Cell[][] matrix;
    private int rows;
    private int cols;
    private int totalDirtyCells;
    private List<Robot> robots;
    
    /**
     * Constructor with dimensions
     * @param rows Number of rows
     * @param cols Number of columns
     */
    public Room(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.matrix = new Cell[rows][cols];
        this.totalDirtyCells = 0;
        this.robots = new ArrayList<>();
        
        // Initialize all cells as clean
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = new Cell('L');
            }
        }
    }
    
    /**
     * Constructor with existing matrix
     * @param matrix The cell matrix
     */
    public Room(Cell[][] matrix) {
        this.matrix = matrix;
        this.rows = matrix.length;
        this.cols = matrix[0].length;
        this.totalDirtyCells = countDirtyCells();
        this.robots = new ArrayList<>();
    }

    // Getters and Setters
    public Cell[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(Cell[][] matrix) {
        this.matrix = matrix;
        this.rows = matrix.length;
        this.cols = matrix[0].length;
        this.totalDirtyCells = countDirtyCells();
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getTotalDirtyCells() {
        return totalDirtyCells;
    }

    public void setTotalDirtyCells(int totalDirtyCells) {
        this.totalDirtyCells = totalDirtyCells;
    }
    
    public List<Robot> getRobots() {
        return robots;
    }
    
    /**
     * Add a robot to the room
     * @param robot Robot to add
     */
    public void addRobot(Robot robot) {
        if (!robots.contains(robot)) {
            robots.add(robot);
        }
    }
    
    /**
     * Remove a robot from the room
     * @param robot Robot to remove
     */
    public void removeRobot(Robot robot) {
        robots.remove(robot);
    }
    
    /**
     * Get robot at specific position
     * @param x X coordinate
     * @param y Y coordinate
     * @return Robot at position or null if none
     */
    public Robot getRobotAt(int x, int y) {
        for (Robot robot : robots) {
            if (robot.getX() == x && robot.getY() == y) {
                return robot;
            }
        }
        return null;
    }
    
    /**
     * Check if a cell is occupied by a robot
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if occupied by robot
     */
    public boolean isCellOccupiedByRobot(int x, int y) {
        return getRobotAt(x, y) != null;
    }
    
    /**
     * Find an empty position in the room for robot initialization
     * An empty position is one that is clean (L) or dirty (S) and not occupied by another robot
     * @return Point with x,y coordinates or null if no empty position found
     */
    public Point findEmptyPosition() {
        Random random = new Random();
        List<Point> validPositions = new ArrayList<>();
        
        // Collect all valid positions
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = matrix[i][j];
                // Valid if it's L or S and no robot there
                if ((cell.isClean() || cell.isDirty()) && !isCellOccupiedByRobot(i, j)) {
                    validPositions.add(new Point(i, j));
                }
            }
        }
        
        if (validPositions.isEmpty()) {
            return null;
        }
        
        // Return random valid position
        return validPositions.get(random.nextInt(validPositions.size()));
    }
    
    /**
     * Simple Point class for coordinates
     */
    public static class Point {
        public int x;
        public int y;
        
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) return false;
            Point other = (Point) obj;
            return this.x == other.x && this.y == other.y;
        }
        
        @Override
        public int hashCode() {
            return x * 1000 + y;
        }
    }
    
    /**
     * Get a specific cell
     * @param row Row index
     * @param col Column index
     * @return The cell at the specified position
     */
    public Cell getCell(int row, int col) {
        if (isValidPosition(row, col)) {
            return matrix[row][col];
        }
        return null;
    }
    
    /**
     * Set a specific cell
     * @param row Row index
     * @param col Column index
     * @param cell The cell to set
     */
    public void setCell(int row, int col, Cell cell) {
        if (isValidPosition(row, col)) {
            matrix[row][col] = cell;
        }
    }
    
    /**
     * Check if a position is valid within the matrix bounds
     * @param row Row index
     * @param col Column index
     * @return true if position is valid
     */
    public boolean isValidPosition(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }
    
    /**
     * Count the total number of dirty cells in the room
     * @return Number of dirty cells
     */
    public int countDirtyCells() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j].isDirty()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Count the total number of cleaned cells
     * @return Number of cells that were dirty and are now clean
     */
    public int countCleanedCells() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j].isCleaned()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Count recharge points in the room
     * @return Number of recharge points
     */
    public int countRechargePoints() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j].isRechargePoint()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Count permanent obstacles in the room
     * @return Number of permanent obstacles
     */
    public int countPermanentObstacles() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j].isPermanentObstacle()) {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Calculate cleaning percentage
     * @return Percentage of dirty cells that have been cleaned
     */
    public double getCleaningPercentage() {
        if (totalDirtyCells == 0) {
            return 100.0;
        }
        return (countCleanedCells() * 100.0) / totalDirtyCells;
    }
    
    /**
     * Convert room to string representation
     * @return String representation of the room matrix
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                sb.append(matrix[i][j].getState());
                if (j < cols - 1) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}