package data.dao;

import data.model.Cell;
import data.model.Room;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Room operations
 * Handles reading and writing rooms to/from salon.txt file
 */
public class RoomDAO {
    private static final String FILE_NAME = "salon.txt";
    
    /**
     * Read all rooms from the salon.txt file
     * @return List of rooms found in the file
     */
    public List<Room> readRooms() {
        return readRooms(FILE_NAME);
    }
    
    /**
     * Read all rooms from a specified file
     * @param fileName The name of the file to read
     * @return List of rooms found in the file
     */
    public List<Room> readRooms(String fileName) {
        List<Room> rooms = new ArrayList<>();
        File file = new File(fileName);
        
        if (!file.exists()) {
            System.out.println("File " + fileName + " does not exist. Creating empty file.");
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating file: " + e.getMessage());
            }
            return rooms;
        }
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            List<String> currentRoomLines = new ArrayList<>();
            String line;
            
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    // Empty line indicates end of current room
                    if (!currentRoomLines.isEmpty()) {
                        Room room = parseRoom(currentRoomLines);
                        if (room != null) {
                            rooms.add(room);
                        }
                        currentRoomLines.clear();
                    }
                } else {
                    currentRoomLines.add(line);
                }
            }
            
            // Add last room if file doesn't end with empty line
            if (!currentRoomLines.isEmpty()) {
                Room room = parseRoom(currentRoomLines);
                if (room != null) {
                    rooms.add(room);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.err.println("Error closing reader: " + e.getMessage());
                }
            }
        }
        
        return rooms;
    }
    
    /**
     * Parse a room from a list of lines
     * @param lines Lines representing the room matrix
     * @return Room object or null if parsing fails
     */
    private Room parseRoom(List<String> lines) {
        if (lines.isEmpty()) {
            return null;
        }
        
        int rows = lines.size();
        String[] firstRowCells = lines.get(0).trim().split("\\s+");
        int cols = firstRowCells.length;
        
        Cell[][] matrix = new Cell[rows][cols];
        
        for (int i = 0; i < rows; i++) {
            String[] cells = lines.get(i).trim().split("\\s+");
            
            if (cells.length != cols) {
                System.err.println("Error: Inconsistent number of columns in row " + i);
                return null;
            }
            
            for (int j = 0; j < cols; j++) {
                char state = cells[j].charAt(0);
                
                // Validate state
                if (state != 'L' && state != 'S' && state != 'O' && 
                    state != 'T' && state != 'R') {
                    System.err.println("Error: Invalid state '" + state + "' at position [" + i + "][" + j + "]");
                    state = 'L'; // Default to clean
                }
                
                matrix[i][j] = new Cell(state);
            }
        }
        
        Room room = new Room(matrix);
        room.setTotalDirtyCells(room.countDirtyCells());
        return room;
    }
    
    /**
     * Save a single room to the file (appends to existing rooms)
     * @param room The room to save
     */
    public void saveRoom(Room room) {
        saveRoom(room, FILE_NAME);
    }
    
    /**
     * Save a single room to a specified file (appends to existing rooms)
     * @param room The room to save
     * @param fileName The name of the file to write
     */
    public void saveRoom(Room room, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName, true));
            
            // Add empty line before new room if file is not empty
            File file = new File(fileName);
            if (file.length() > 0) {
                writer.newLine();
            }
            
            // Write room matrix
            for (int i = 0; i < room.getRows(); i++) {
                for (int j = 0; j < room.getCols(); j++) {
                    writer.write(room.getCell(i, j).getState());
                    if (j < room.getCols() - 1) {
                        writer.write(" ");
                    }
                }
                writer.newLine();
            }
            
        } catch (IOException e) {
            System.err.println("Error saving room: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error closing writer: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Save all rooms to the file (overwrites existing file)
     * @param rooms List of rooms to save
     */
    public void saveAllRooms(List<Room> rooms) {
        saveAllRooms(rooms, FILE_NAME);
    }
    
    /**
     * Save all rooms to a specified file (overwrites existing file)
     * @param rooms List of rooms to save
     * @param fileName The name of the file to write
     */
    public void saveAllRooms(List<Room> rooms, String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName, false));
            
            for (int r = 0; r < rooms.size(); r++) {
                Room room = rooms.get(r);
                
                // Write room matrix
                for (int i = 0; i < room.getRows(); i++) {
                    for (int j = 0; j < room.getCols(); j++) {
                        writer.write(room.getCell(i, j).getState());
                        if (j < room.getCols() - 1) {
                            writer.write(" ");
                        }
                    }
                    writer.newLine();
                }
                
                // Add empty line between rooms (except after last room)
                if (r < rooms.size() - 1) {
                    writer.newLine();
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error saving rooms: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error closing writer: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Clear the file (delete all rooms)
     */
    public void clearFile() {
        clearFile(FILE_NAME);
    }
    
    /**
     * Clear a specified file (delete all rooms)
     * @param fileName The name of the file to clear
     */
    public void clearFile(String fileName) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(fileName, false));
        } catch (IOException e) {
            System.err.println("Error clearing file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error closing writer: " + e.getMessage());
                }
            }
        }
    }
}