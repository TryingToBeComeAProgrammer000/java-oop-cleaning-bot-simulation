package view;

import data.model.Cell;
import data.model.Room;
import javax.swing.*;
import java.awt.*;

/**
 * Panel for visualizing the room matrix
 */
public class RoomPanel extends JPanel {
    // Colors for each cell state
    public static final Color COLOR_CLEAN = new Color(240, 248, 255);        // Alice Blue
    public static final Color COLOR_DIRTY = new Color(139, 69, 19);          // Saddle Brown
    public static final Color COLOR_OBSTACLE = new Color(64, 64, 64);        // Dark Gray
    public static final Color COLOR_TEMP_OBSTACLE = new Color(169, 169, 169); // Light Gray
    public static final Color COLOR_RECHARGE = new Color(34, 139, 34);       // Forest Green
    public static final Color COLOR_GRID = new Color(200, 200, 200);
    
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
    }
    
    private void drawCell(Graphics2D g2d, Cell cell, int x, int y) {
        // Fill cell with color based on state
        Color cellColor = getCellColor(cell);
        g2d.setColor(cellColor);
        g2d.fillRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
        
        // Draw state letter
        g2d.setColor(getTextColor(cell));
        g2d.setFont(new Font("Arial", Font.BOLD, cellSize / 3));
        FontMetrics fm = g2d.getFontMetrics();
        String text = String.valueOf(cell.getState());
        int textX = x + (cellSize - fm.stringWidth(text)) / 2;
        int textY = y + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, textX, textY);
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
        // Use white text for dark backgrounds
        if (state == 'S' || state == 'O' || state == 'R') {
            return Color.WHITE;
        }
        return Color.BLACK;
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