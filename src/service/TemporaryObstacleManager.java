package service;

import data.model.Cell;
import data.model.Room;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages temporary obstacles in the room
 * Makes them disappear permanently after their duration expires
 */
public class TemporaryObstacleManager {
    private Timer timer;
    private Room room;
    private Random random;
    private List<TimerTask> activeTasks;
    
    public TemporaryObstacleManager() {
        this.random = new Random();
        this.activeTasks = new ArrayList<>();
    }
    
    /**
     * Start managing temporary obstacles for a room
     * @param room The room to manage
     */
    public void startManaging(Room room) {
        this.room = room;
        
        // Stop any previous timer
        stop();
        
        // Start new timer FIRST
        timer = new Timer("TempObstacleManager", true);
        
        // Then initialize all temporary obstacles
        initializeTemporaryObstacles();
        
        System.out.println("[TemporaryObstacleManager] Started managing temporary obstacles");
    }
    
    /**
     * Initialize all temporary obstacles with random timers
     */
    private void initializeTemporaryObstacles() {
        if (room == null) return;
        
        int obstacleCount = 0;
        
        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                Cell cell = room.getCell(i, j);
                if (cell != null && cell.isTemporaryObstacle()) {
                    scheduleTemporaryObstacle(i, j, cell);
                    obstacleCount++;
                }
            }
        }
        
        System.out.println("[TemporaryObstacleManager] Scheduled " + obstacleCount + " temporary obstacles");
    }
    
    /**
     * Schedule a temporary obstacle to disappear permanently
     * @param row Row index
     * @param col Column index
     * @param cell The cell
     */
    private void scheduleTemporaryObstacle(int row, int col, Cell cell) {
        // Random duration between 1-7 seconds (1000-7000 ms)
        long duration = (random.nextInt(7) + 1) * 1000;
        
        // Random initial delay between 0-5 seconds
        long initialDelay = random.nextInt(5000);
        
        // Set the obstacle time and duration
        long startTime = System.currentTimeMillis() + initialDelay;
        cell.setTemporaryObstacleTime(startTime);
        cell.setTemporaryObstacleDuration(duration);
        
        long totalWait = initialDelay + duration;
        
        System.out.println(String.format("[TemporaryObstacleManager] Obstacle at (%d,%d) will disappear in %.1f seconds", 
                                         row, col, totalWait / 1000.0));
        
        // Schedule removal (PERMANENT - does not reappear)
        TimerTask removalTask = new TimerTask() {
            @Override
            public void run() {
                synchronized (room) {
                    Cell c = room.getCell(row, col);
                    if (c != null && c.isTemporaryObstacle()) {
                        // Convert to clean PERMANENTLY
                        c.setState('L');
                        c.setTemporaryObstacleTime(0);
                        c.setTemporaryObstacleDuration(0);
                        System.out.println(String.format("[TemporaryObstacleManager] Obstacle at (%d,%d) removed permanently", row, col));
                    }
                }
            }
        };
        
        if (timer != null) {
            try {
                timer.schedule(removalTask, totalWait);
                synchronized (activeTasks) {
                    activeTasks.add(removalTask);
                }
            } catch (Exception e) {
                System.err.println("[TemporaryObstacleManager] Error scheduling task: " + e.getMessage());
            }
        } else {
            System.err.println("[TemporaryObstacleManager] Timer is null, cannot schedule obstacle removal");
        }
    }
    
    /**
     * Stop managing temporary obstacles
     */
    public void stop() {
        System.out.println("[TemporaryObstacleManager] Stopping... (testing)");
        
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        
        synchronized (activeTasks) {
            for (TimerTask task : activeTasks) {
                task.cancel();
            }
            activeTasks.clear();
        }
    }
}