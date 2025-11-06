package view;

import data.model.Cell;
import data.model.Robot;
import data.model.Room;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Panel for visualizing the room matrix with robots
 */
public class RoomPanel extends JPanel {
    // Colors for each cell state
    public static final Color COLOR_CLEAN = new Color(240, 248, 255);        // Alice Blue
    public static final Color COLOR_DIRTY = new Color(139, 69, 19);          // Saddle Brown
    public static final Color COLOR_OBSTACLE = new Color(64, 64, 64);        // Dark Gray
    public static final Color COLOR_TEMP_OBSTACLE = new Color(169, 169, 169); // Light Gray
    public static final Color COLOR_RECHARGE = new Color(34, 139, 34);       // Forest Green
    public static final Color COLOR_GRID = new Color(200, 200, 200);
    
    // Robot colors (different color for each robot)
    private static final Color[] ROBOT_COLORS = {
        new Color(255, 69, 0),    // Red-Orange
        new Color(30, 144, 255),  // Dodger Blue
        new Color(255, 215, 0),   // Gold
        new Color(138, 43, 226)   // Blue Violet
    };
    
    private Room room;
    private int cellSize = 50;
    private static final int MIN_CELL_SIZE = 30;
    private static final int MAX_CELL_SIZE = 80;
    
    public RoomPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(600, 600));
    }
    
    public void setRoom(Room room) {
        this.room = room;
        if (room != null) {
            adjustCellSize();
            updatePreferredSize();
        }
        repaint();
    }
    
    private void adjustCellSize() {
        if (room == null) return;
        
        int maxRows = room.getRows();
        int maxCols = room.getCols();
        
        // Calculate optimal cell size to fit the room
        int availableWidth = getWidth() > 0 ? getWidth() : 600;
        int availableHeight = getHeight() > 0 ? getHeight() : 600;
        
        int cellSizeByWidth = (availableWidth - 40) / maxCols;
        int cellSizeByHeight = (availableHeight - 40) / maxRows;
        
        cellSize = Math.min(cellSizeByWidth, cellSizeByHeight);
        cellSize = Math.max(MIN_CELL_SIZE, Math.min(MAX_CELL_SIZE, cellSize));
    }
    
    private void updatePreferredSize() {
        if (room != null) {
            int width = room.getCols() * cellSize + 40;
            int height = room.getRows() * cellSize + 40;
            setPreferredSize(new Dimension(width, height));
            revalidate();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (room == null) {
            drawEmptyMessage(g);
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int offsetX = 20;
        int offsetY = 20;
        
        // Draw cells
        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                int x = offsetX + j * cellSize;
                int y = offsetY + i * cellSize;
                
                Cell cell = room.getCell(i, j);
                drawCell(g2d, cell, x, y);
            }
        }
        
        // Draw grid lines
        g2d.setColor(COLOR_GRID);
        for (int i = 0; i <= room.getRows(); i++) {
            int y = offsetY + i * cellSize;
            g2d.drawLine(offsetX, y, offsetX + room.getCols() * cellSize, y);
        }
        for (int j = 0; j <= room.getCols(); j++) {
            int x = offsetX + j * cellSize;
            g2d.drawLine(x, offsetY, x, offsetY + room.getRows() * cellSize);
        }
        
        // Draw robots on top
        if (room.getRobots() != null) {
            for (Robot robot : room.getRobots()) {
                int x = offsetX + robot.getY() * cellSize;
                int y = offsetY + robot.getX() * cellSize;
                drawRobot(g2d, robot, x, y);
            }
        }
    }
    
    private void drawCell(Graphics2D g2d, Cell cell, int x, int y) {
        // Fill cell with color based on state
        Color cellColor = getCellColor(cell);
        g2d.setColor(cellColor);
        g2d.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // Draw state letter (smaller and lighter for background)
        g2d.setColor(getTextColor(cell));
        g2d.setFont(new Font("Arial", Font.PLAIN, cellSize / 4));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(cell.getState());
        int textX = x + (cellSize - fm.stringWidth(text)) / 2;
        int textY = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, textX, textY);
    }
    
    private void drawRobot(Graphics2D g2d, Robot robot, int x, int y) {
        if (!robot.isActive()) {
            return; // Don't draw inactive robots
        }
        
        // Get robot color based on ID
        Color robotColor = ROBOT_COLORS[(robot.getId() - 1) % ROBOT_COLORS.length];
        
        int robotSize = (int)(cellSize * 0.7);
        int robotX = x + (cellSize - robotSize) / 2;
        int robotY = y + (cellSize - robotSize) / 2;
        
        // Draw robot as circle with border
        g2d.setColor(robotColor);
        g2d.fillOval(robotX, robotY, robotSize, robotSize);
        
        // Draw border
        g2d.setColor(robotColor.darker());
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(robotX, robotY, robotSize, robotSize);
        
        // Draw robot ID
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, cellSize / 3));
        FontMetrics fm = g2d.getFontMetrics();
        String id = String.valueOf(robot.getId());
        int textX = x + (cellSize - fm.stringWidth(id)) / 2;
        int textY = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(id, textX, textY);
        
        // Draw battery indicator (small bar below robot)
        int barWidth = robotSize - 4;
        int barHeight = 4;
        int barX = robotX + 2;
        int barY = robotY + robotSize - barHeight - 2;
        
        // Background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        // Battery level
        int batteryWidth = (int)((robot.getBattery() / 20.0) * barWidth);
        Color batteryColor;
        if (robot.getBattery() <= 3) {
            batteryColor = Color.RED;
        } else if (robot.getBattery() <= 7) {
            batteryColor = Color.YELLOW;
        } else {
            batteryColor = Color.GREEN;
        }
        g2d.setColor(batteryColor);
        g2d.fillRect(barX, barY, batteryWidth, barHeight);
    }
    
    private Color getCellColor(Cell cell) {
        char state = cell.getState();
        switch (state) {
            case 'L': return COLOR_CLEAN;
            case 'S': return COLOR_DIRTY;
            case 'O': return COLOR_OBSTACLE;
            case 'T': return COLOR_TEMP_OBSTACLE;
            case 'R': return COLOR_RECHARGE;
            default: return Color.WHITE;
        }
    }
    
    private Color getTextColor(Cell cell) {
        char state = cell.getState();
        // Use white text for dark backgrounds, but make it semi-transparent
        if (state == 'S' || state == 'O' || state == 'R') {
            return new Color(255, 255, 255, 100); // Semi-transparent white
        }
        return new Color(0, 0, 0, 100); // Semi-transparent black
    }
    
    private void drawEmptyMessage(Graphics g) {
        String message = "No room loaded. Please load or create a room.";
        g.setColor(Color.GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(message)) / 2;
        int y = getHeight() / 2;
        g.drawString(message, x, y);
    }
}