package oop.cleaningbot.simulation;

import data.dao.MovementLogger;
import data.dao.RoomDAO;
import data.model.Robot;
import data.model.Room;
import service.*;
import java.util.List;

/**
 * Test for Day 3 - Full simulation with pathfinding
 */
public class TestFullSimulation {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   DAY 3 TEST - Full Simulation");
        System.out.println("========================================\n");
        
        // Load room
        RoomDAO roomDAO = new RoomDAO();
        List<Room> rooms = roomDAO.readRooms();
        
        if (rooms.isEmpty()) {
            System.out.println("No rooms found in salon.txt!");
            return;
        }
        
        // Use the last room (should be the 10x10 we created)
        Room room = rooms.get(rooms.size() - 1);
        
        System.out.println("Loaded room: " + room.getRows() + "x" + room.getCols());
        System.out.println("Total cells: " + (room.getRows() * room.getCols()));
        System.out.println("Dirty cells: " + room.getTotalDirtyCells());
        System.out.println("Recharge points: " + room.countRechargePoints());
        System.out.println("Permanent obstacles: " + room.countPermanentObstacles());
        
        double dirtyPercentage = (room.getTotalDirtyCells() * 100.0) / (room.getRows() * room.getCols());
        System.out.println("Dirt percentage: " + String.format("%.1f%%", dirtyPercentage));
        
        System.out.println("\nInitial room state:");
        System.out.println(room.toString());
        
        // Initialize services
        MovementLogger logger = new MovementLogger();
        logger.initialize();
        
        RobotService robotService = new RobotService();
        PathfindingService pathfindingService = new PathfindingService();
        MultiRobotManager multiManager = new MultiRobotManager(logger);
        RobotController robotController = new RobotController(robotService, pathfindingService, logger);
        
        // Calculate and initialize robots
        int recommendedRobots = multiManager.calculateRecommendedRobots(room);
        System.out.println("\n--- ROBOT CALCULATION ---");
        System.out.println("Recommended robots: " + recommendedRobots);
        
        System.out.println("\n--- INITIALIZING ROBOTS ---");
        List<Robot> robots = multiManager.initializeRobots(room, recommendedRobots);
        
        if (robots.isEmpty()) {
            System.out.println("Failed to initialize robots!");
            logger.close();
            return;
        }
        
        System.out.println("Robots initialized successfully!");
        System.out.println(multiManager.getRobotsStatistics(robots));
        
        // Create simulation controller
        int maxSteps = 1000; // Safety limit
        SimulationController simulation = new SimulationController(
            room, robots, logger, multiManager, robotController, maxSteps
        );
        
        // Run simulation
        System.out.println("\n========================================");
        System.out.println("   STARTING SIMULATION");
        System.out.println("========================================\n");
        
        long startTime = System.currentTimeMillis();
        double finalPercentage = simulation.runSimulation();
        long endTime = System.currentTimeMillis();
        
        // Results
        System.out.println("\n========================================");
        System.out.println("   SIMULATION RESULTS");
        System.out.println("========================================\n");
        
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
        System.out.println("Total steps: " + simulation.getStepCount());
        System.out.println("Mission status: " + simulation.getMissionStatus());
        System.out.println();
        
        System.out.println("Initial dirty cells: " + room.getTotalDirtyCells());
        System.out.println("Cells cleaned: " + simulation.getTotalCellsCleaned());
        System.out.println("Cleaning percentage: " + String.format("%.2f%%", finalPercentage));
        System.out.println("Active robots at end: " + simulation.getActiveRobotCount() + "/" + robots.size());
        
        System.out.println("\n" + multiManager.getRobotsStatistics(robots));
        
        System.out.println("\n--- FINAL ROOM STATE ---");
        System.out.println(room.toString());
        
        // Mission evaluation
        System.out.println("\n--- MISSION EVALUATION ---");
        if (simulation.getMissionStatus().equals("COMPLETED")) {
            System.out.println("MISSION COMPLETED!");
            System.out.println("The room has been cleaned successfully (≥80%)");
        } else if (simulation.getMissionStatus().equals("ACCEPTABLE")) {
            System.out.println("MISSION ACCEPTABLE");
            System.out.println("The room is partially cleaned (≥50% but <80%)");
        } else {
            System.out.println("MISSION FAILED");
            System.out.println("Could not achieve minimum 50% cleaning");
        }
        
        logger.close();
        System.out.println("");
        System.out.println("\n Day 3 test completed!");
        System.out.println("Check registro.txt for complete simulation log.");
    }
}