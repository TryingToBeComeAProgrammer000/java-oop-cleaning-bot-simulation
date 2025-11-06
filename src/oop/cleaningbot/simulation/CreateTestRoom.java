package oop.cleaningbot.simulation;

import data.dao.RoomDAO;
import data.model.Room;
import service.RoomService;

/**
 * Creates a larger test room to see multiple robots in action
 */
public class CreateTestRoom {
    
    public static void main(String[] args) {
        System.out.println("Creating test room for multiple robots...\n");
        
        RoomService roomService = new RoomService();
        RoomDAO roomDAO = new RoomDAO();
        
        // Create a large room: 10x10 with high dirt percentage
        Room testRoom = roomService.generateRandomRoom(
            10,  // rows
            10,  // cols
            20.0, // clean %
            50.0, // dirty % (HIGH)
            15.0, // permanent obstacles %
            10.0  // temporary obstacles %
        );
        
        System.out.println("Generated test room:");
        System.out.println(testRoom.toString());
        
        System.out.println("\nRoom statistics:");
        roomService.printRoomStatistics(testRoom);
        
        // Save to file
        roomDAO.saveRoom(testRoom);
        
        System.out.println("\n✅ Test room saved to salon.txt");
        System.out.println("Run TestMultiRobot again to see multiple robots!");
    }
}