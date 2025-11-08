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
        
        // Initialize all temporary obstacles
        initializeTemporaryObstacles();
        
        // Start new timer
        timer = new Timer("TempObstacleManager", true);
    }
    
    /**
     * Initialize all temporary obstacles with random timers
     */
    private void initializeTemporaryObstacles() {
        if (room == null) return;
        
        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                Cell cell = room.getCell(i, j);
                if (cell != null && cell.isTemporaryObstacle()) {
                    scheduleTemporaryObstacle(i, j, cell);
                }
            }
        }
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
                        // No reappear task - obstacle is permanently removed
                    }
                }
            }
        };
        
        if (timer != null) {
            timer.schedule(removalTask, initialDelay + duration);
            synchronized (activeTasks) {
                activeTasks.add(removalTask);
            }
        }
    }
    
    /**
     * Stop managing temporary obstacles
     */
    public void stop() {
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