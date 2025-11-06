package data.dao;

import data.model.Robot;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger for robot movements and actions
 * Writes to registro.txt file
 */
public class MovementLogger {
    private static final String LOG_FILE = "registro.txt";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private BufferedWriter writer;
    private boolean isInitialized;
    
    public MovementLogger() {
        this.isInitialized = false;
    }
    
    /**
     * Initialize the logger (clears previous log)
     */
    public void initialize() {
        try {
            writer = new BufferedWriter(new FileWriter(LOG_FILE, false));
            isInitialized = true;
            
            // Write header
            writer.write("========================================\n");
            writer.write("  ROBOT CLEANING SIMULATION LOG\n");
            writer.write("  Started at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("========================================\n\n");
            writer.flush();
            
        } catch (IOException e) {
            System.err.println("Error initializing logger: " + e.getMessage());
        }
    }
    
    /**
     * Log a general message
     * @param message Message to log
     */
    public void log(String message) {
        if (!isInitialized) {
            initialize();
        }
        
        try {
            String timestamp = LocalDateTime.now().format(TIME_FORMAT);
            writer.write("[" + timestamp + "] " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing to log: " + e.getMessage());
        }
    }
    
    /**
     * Log robot movement
     * @param robot The robot
     * @param fromX Previous X position
     * @param fromY Previous Y position
     * @param toX New X position
     * @param toY New Y position
     */
    public void logMovement(Robot robot, int fromX, int fromY, int toX, int toY) {
        String message = String.format("Robot #%d - Moved from (%d,%d) to (%d,%d) | Battery: %d",
            robot.getId(), fromX, fromY, toX, toY, robot.getBattery());
        log(message);
    }
    
    /**
     * Log robot cleaning action
     * @param robot The robot
     */
    public void logCleaning(Robot robot) {
        String message = String.format("Robot #%d - Cleaned cell at (%d,%d) | Battery: %d | Total cleaned: %d",
            robot.getId(), robot.getX(), robot.getY(), robot.getBattery(), robot.getCellsCleaned());
        log(message);
    }
    
    /**
     * Log robot recharging
     * @param robot The robot
     */
    public void logRecharge(Robot robot) {
        String message = String.format("Robot #%d - Recharging at (%d,%d) | Battery: %d → %d",
            robot.getId(), robot.getX(), robot.getY(), 
            robot.getBattery() - 10, robot.getBattery());
        log(message);
    }
    
    /**
     * Log robot going to recharge point
     * @param robot The robot
     * @param targetX Target X position
     * @param targetY Target Y position
     */
    public void logGoingToRecharge(Robot robot, int targetX, int targetY) {
        String message = String.format("Robot #%d - LOW BATTERY! Going to recharge point at (%d,%d) | Battery: %d",
            robot.getId(), targetX, targetY, robot.getBattery());
        log(message);
    }
    
    /**
     * Log robot deactivation
     * @param robot The robot
     * @param reason Reason for deactivation
     */
    public void logDeactivation(Robot robot, String reason) {
        String message = String.format("Robot #%d - DEACTIVATED at (%d,%d) | Reason: %s | Total cleaned: %d",
            robot.getId(), robot.getX(), robot.getY(), reason, robot.getCellsCleaned());
        log(message);
    }
    
    /**
     * Log robot initialization
     * @param robot The robot
     */
    public void logInitialization(Robot robot) {
        String message = String.format("Robot #%d - INITIALIZED at position (%d,%d) | Battery: %d",
            robot.getId(), robot.getX(), robot.getY(), robot.getBattery());
        log(message);
    }
    
    /**
     * Log mission status
     * @param status Status message
     */
    public void logMissionStatus(String status) {
        log("\n--- MISSION STATUS ---");
        log(status);
        log("----------------------\n");
    }
    
    /**
     * Log final results
     * @param totalDirty Total dirty cells
     * @param cleaned Cells cleaned
     * @param percentage Cleaning percentage
     * @param status Final status
     */
    public void logFinalResults(int totalDirty, int cleaned, double percentage, String status) {
        try {
            writer.write("\n========================================\n");
            writer.write("  SIMULATION COMPLETED\n");
            writer.write("========================================\n");
            writer.write("Total dirty cells: " + totalDirty + "\n");
            writer.write("Cells cleaned: " + cleaned + "\n");
            writer.write("Cleaning percentage: " + String.format("%.2f%%", percentage) + "\n");
            writer.write("Mission status: " + status + "\n");
            writer.write("Ended at: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("========================================\n");
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error writing final results: " + e.getMessage());
        }
    }
    
    /**
     * Close the logger
     */
    public void close() {
        if (writer != null) {
            try {
                writer.close();
                isInitialized = false;
            } catch (IOException e) {
                System.err.println("Error closing logger: " + e.getMessage());
            }
        }
    }
    
    /**
     * Clear the log file
     */
    public void clearLog() {
        try {
            BufferedWriter clearWriter = new BufferedWriter(new FileWriter(LOG_FILE, false));
            clearWriter.close();
        } catch (IOException e) {
            System.err.println("Error clearing log: " + e.getMessage());
        }
    }
}