package view;

import data.model.Cell;
import data.model.Robot;
import data.model.Room;
import javax.swing.*;
import java.awt.*;

/**
 * Panel for visualizing the room matrix with robots - Dark theme
 */
public class RoomPanel extends JPanel {
    
    public static final Color COLOR_CLEAN = new Color(30, 40, 60);           
    public static final Color COLOR_DIRTY = new Color(115, 64, 13);          
    public static final Color COLOR_OBSTACLE = new Color(50, 50, 50);        
    public static final Color COLOR_TEMP_OBSTACLE = new Color(100, 100, 100); 
    public static final Color COLOR_RECHARGE = new Color(0, 255, 127);       
    public static final Color COLOR_GRID = new Color(44, 116, 243);          
    public static final Color BACKGROUND = new Color(16, 29, 65);            
    
    // Robot colors
    private static final Color[] ROBOT_COLORS = {
        new Color(255, 223, 0),    // Red-Orange
        new Color(30, 144, 255),  // Dodger Blue
        new Color(255, 215, 0),   // Gold
        new Color(138, 43, 226)   // Blue Violet
    };
    
    private Room room;
    private int cellSize = 50;
    private static final int MIN_CELL_SIZE = 30;
    private static final int MAX_CELL_SIZE = 80;
    
    public RoomPanel() {
        setBackground(BACKGROUND);
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
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
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
        
        // Draw grid lines (subtle)
        g2d.setColor(new Color(COLOR_GRID.getRed(), COLOR_GRID.getGreen(), COLOR_GRID.getBlue(), 100));
        g2d.setStroke(new BasicStroke(1.5f));
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
        
        // Add subtle gradient for depth
        GradientPaint gradient = new GradientPaint(
            x, y, cellColor.brighter(),
            x, y + cellSize, cellColor
        );
        g2d.setPaint(gradient);
        g2d.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // Draw state letter (smaller and semi-transparent)
        g2d.setColor(getTextColor(cell));
        g2d.setFont(new Font("Segoe UI", Font.BOLD, cellSize / 4));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(cell.getState());
        int textX = x + (cellSize - fm.stringWidth(text)) / 2;
        int textY = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, textX, textY);
    }
    
    private void drawRobot(Graphics2D g2d, Robot robot, int x, int y) {
        if (!robot.isActive()) {
            return;
        }
        
        Color robotColor = ROBOT_COLORS[(robot.getId() - 1) % ROBOT_COLORS.length];
        
        int robotSize = (int)(cellSize * 0.7);
        int robotX = x + (cellSize - robotSize) / 2;
        int robotY = y + (cellSize - robotSize) / 2;
        
        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillOval(robotX + 3, robotY + 3, robotSize, robotSize);
        
        // Draw robot circle with gradient
        GradientPaint gradient = new GradientPaint(
            robotX, robotY, robotColor.brighter(),
            robotX, robotY + robotSize, robotColor.darker()
        );
        g2d.setPaint(gradient);
        g2d.fillOval(robotX, robotY, robotSize, robotSize);
        
        // Draw border (glow effect)
        g2d.setColor(robotColor.brighter().brighter());
        g2d.setStroke(new BasicStroke(2.5f));
        g2d.drawOval(robotX, robotY, robotSize, robotSize);
        
        // Draw robot ID
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Segoe UI", Font.BOLD, cellSize / 3));
        FontMetrics fm = g2d.getFontMetrics();
        String id = String.valueOf(robot.getId());
        int textX = x + (cellSize - fm.stringWidth(id)) / 2;
        int textY = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
        
        // Text shadow
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawString(id, textX + 1, textY + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(id, textX, textY);
        
        // Draw battery indicator (modern bar)
        int barWidth = robotSize - 6;
        int barHeight = 5;
        int barX = robotX + 3;
        int barY = robotY + robotSize - barHeight - 3;
        
        // Background
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 3, 3);
        
        // Battery level
        int batteryWidth = (int)((robot.getBattery() / 20.0) * barWidth);
        Color batteryColor;
        if (robot.getBattery() <= 3) {
            batteryColor = new Color(231, 76, 60);
        } else if (robot.getBattery() <= 7) {
            batteryColor = new Color(241, 196, 15);
        } else {
            batteryColor = new Color(46, 204, 113);
        }
        g2d.setColor(batteryColor);
        g2d.fillRoundRect(barX, barY, batteryWidth, barHeight, 3, 3);
    }
    
    private Color getCellColor(Cell cell) {
        char state = cell.getState();
        switch (state) {
            case 'L': return COLOR_CLEAN;
            case 'S': return COLOR_DIRTY;
            case 'O': return COLOR_OBSTACLE;
            case 'T': return COLOR_TEMP_OBSTACLE;
            case 'R': return COLOR_RECHARGE;
            default: return BACKGROUND;
        }
    }
    
    private Color getTextColor(Cell cell) {
        char state = cell.getState();
        // Semi-transparent white for all states
        return new Color(255, 255, 255, 80);
    }
    
    private void drawEmptyMessage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        String message = "No room loaded";
        String submessage = "Please load or create a room to begin";
        
        g2d.setColor(new Color(93, 173, 255, 150)); // COLOR_5 semi-transparent
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
        FontMetrics fm1 = g2d.getFontMetrics();
        int x1 = (getWidth() - fm1.stringWidth(message)) / 2;
        int y1 = getHeight() / 2 - 15;
        g2d.drawString(message, x1, y1);
        
        g2d.setColor(new Color(200, 200, 200, 150));
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        FontMetrics fm2 = g2d.getFontMetrics();
        int x2 = (getWidth() - fm2.stringWidth(submessage)) / 2;
        int y2 = getHeight() / 2 + 10;
        g2d.drawString(submessage, x2, y2);
    }
}