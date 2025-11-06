package service;

import data.model.Cell;
import data.model.Room;
import java.util.Random;

/**
 * Service class for Room business logic
 * Handles room generation with random state assignment
 */
public class RoomService {
    private Random random;
    
    public RoomService() {
        this.random = new Random();
    }
    
    /**
     * Generate a random room with specified dimensions and percentages
     * @param rows Number of rows
     * @param cols Number of columns
     * @param cleanPercentage Percentage of clean cells (L)
     * @param dirtyPercentage Percentage of dirty cells (S)
     * @param permanentObstaclePercentage Percentage of permanent obstacles (O)
     * @param temporaryObstaclePercentage Percentage of temporary obstacles (T)
     * @return A new Room with randomly assigned states
     */
    public Room generateRandomRoom(int rows, int cols, 
                                   double cleanPercentage, 
                                   double dirtyPercentage,
                                   double permanentObstaclePercentage,
                                   double temporaryObstaclePercentage) {
        
        // Validate percentages sum to 100 or less (remaining for R)
        double total = cleanPercentage + dirtyPercentage + 
                      permanentObstaclePercentage + temporaryObstaclePercentage;
        
        if (total > 100.0) {
            throw new IllegalArgumentException("Total percentages cannot exceed 100%");
        }
        
        Room room = new Room(rows, cols);
        int totalCells = rows * cols;
        
        // Calculate number of cells for each state
        int numClean = (int) Math.round((cleanPercentage / 100.0) * totalCells);
        int numDirty = (int) Math.round((dirtyPercentage / 100.0) * totalCells);
        int numPermanentObstacle = (int) Math.round((permanentObstaclePercentage / 100.0) * totalCells);
        int numTemporaryObstacle = (int) Math.round((temporaryObstaclePercentage / 100.0) * totalCells);
        
        // Random number of recharge points (1 to 4)
        int numRecharge = random.nextInt(4) + 1;
        
        // Create array with all states
        char[] states = new char[totalCells];
        int index = 0;
        
        // Fill with clean cells
        for (int i = 0; i < numClean && index < totalCells; i++) {
            states[index++] = 'L';
        }
        
        // Fill with dirty cells
        for (int i = 0; i < numDirty && index < totalCells; i++) {
            states[index++] = 'S';
        }
        
        // Fill with permanent obstacles
        for (int i = 0; i < numPermanentObstacle && index < totalCells; i++) {
            states[index++] = 'O';
        }
        
        // Fill with temporary obstacles
        for (int i = 0; i < numTemporaryObstacle && index < totalCells; i++) {
            states[index++] = 'T';
        }
        
        // Fill with recharge points
        for (int i = 0; i < numRecharge && index < totalCells; i++) {
            states[index++] = 'R';
        }
        
        // Fill remaining cells with clean state
        while (index < totalCells) {
            states[index++] = 'L';
        }
        
        // Shuffle the states array using Fisher-Yates algorithm
        shuffleArray(states);
        
        // Assign shuffled states to the room matrix
        index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                room.setCell(i, j, new Cell(states[index++]));
            }
        }
        
        room.setTotalDirtyCells(room.countDirtyCells());
        return room;
    }
    
    /**
     * Shuffle an array using Fisher-Yates algorithm
     * @param array The array to shuffle
     */
    private void shuffleArray(char[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            // Swap
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
    
    /**
     * Generate a random room with default balanced percentages
     * @param rows Number of rows
     * @param cols Number of columns
     * @return A new Room with balanced random distribution
     */
    public Room generateRandomRoom(int rows, int cols) {
        // Default percentages: 40% clean, 30% dirty, 15% obstacles each
        return generateRandomRoom(rows, cols, 40.0, 30.0, 15.0, 15.0);
    }
    
    /**
     * Validate if percentages are valid for room generation
     * @param cleanPercentage Percentage of clean cells
     * @param dirtyPercentage Percentage of dirty cells
     * @param permanentObstaclePercentage Percentage of permanent obstacles
     * @param temporaryObstaclePercentage Percentage of temporary obstacles
     * @return true if percentages are valid
     */
    public boolean validatePercentages(double cleanPercentage, 
                                       double dirtyPercentage,
                                       double permanentObstaclePercentage,
                                       double temporaryObstaclePercentage) {
        
        // Check all percentages are non-negative
        if (cleanPercentage < 0 || dirtyPercentage < 0 || 
            permanentObstaclePercentage < 0 || temporaryObstaclePercentage < 0) {
            return false;
        }
        
        // Check total doesn't exceed 100%
        double total = cleanPercentage + dirtyPercentage + 
                      permanentObstaclePercentage + temporaryObstaclePercentage;
        
        return total <= 100.0;
    }
    
    /**
     * Calculate recommended number of robots based on room characteristics
     * This is a basic implementation - you should refine this logic
     * @param room The room to analyze
     * @return Recommended number of robots
     */
    public int calculateRecommendedRobots(Room room) {
        int totalCells = room.getRows() * room.getCols();
        int dirtyCells = room.countDirtyCells();
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
     * Print room statistics
     * @param room The room to analyze
     */
    public void printRoomStatistics(Room room) {
        int totalCells = room.getRows() * room.getCols();
        int dirtyCells = room.countDirtyCells();
        int rechargePoints = room.countRechargePoints();
        int obstacles = room.countPermanentObstacles();
        
        System.out.println("=== Room Statistics ===");
        System.out.println("Dimensions: " + room.getRows() + " x " + room.getCols());
        System.out.println("Total cells: " + totalCells);
        System.out.println("Dirty cells (S): " + dirtyCells + 
                         " (" + String.format("%.1f", (dirtyCells * 100.0 / totalCells)) + "%)");
        System.out.println("Permanent obstacles (O): " + obstacles + 
                         " (" + String.format("%.1f", (obstacles * 100.0 / totalCells)) + "%)");
        System.out.println("Recharge points (R): " + rechargePoints);
        System.out.println("Recommended robots: " + calculateRecommendedRobots(room));
        System.out.println("======================");
    }
}