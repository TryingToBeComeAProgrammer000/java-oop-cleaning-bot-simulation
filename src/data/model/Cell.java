package data.model;

/**
 * Represents a single cell in the room matrix
 * Each cell has a state: L (Clean), S (Dirty), O (Permanent Obstacle), 
 * T (Temporary Obstacle), R (Recharge Point)
 */
public class Cell {
    private char state;
    private boolean cleaned; // Track if a dirty cell has been cleaned
    private long temporaryObstacleTime; // Time when temporary obstacle was placed (in seconds)
    
    /**
     * Constructor with state
     * @param state The initial state of the cell
     */
    public Cell(char state) {
        this.state = state;
        this.cleaned = false;
        this.temporaryObstacleTime = 0;
    }
    
    /**
     * Default constructor - creates a clean cell
     */
    public Cell() {
        this('L');
    }

    // Getters and Setters
    public char getState() {
        return state;
    }

    public void setState(char state) {
        this.state = state;
    }

    public boolean isCleaned() {
        return cleaned;
    }

    public void setCleaned(boolean cleaned) {
        this.cleaned = cleaned;
    }

    public long getTemporaryObstacleTime() {
        return temporaryObstacleTime;
    }

    public void setTemporaryObstacleTime(long temporaryObstacleTime) {
        this.temporaryObstacleTime = temporaryObstacleTime;
    }
    
    /**
     * Check if the cell is a dirty space
     * @return true if state is 'S'
     */
    public boolean isDirty() {
        return state == 'S';
    }
    
    /**
     * Check if the cell is a permanent obstacle
     * @return true if state is 'O'
     */
    public boolean isPermanentObstacle() {
        return state == 'O';
    }
    
    /**
     * Check if the cell is a temporary obstacle
     * @return true if state is 'T'
     */
    public boolean isTemporaryObstacle() {
        return state == 'T';
    }
    
    /**
     * Check if the cell is a recharge point
     * @return true if state is 'R'
     */
    public boolean isRechargePoint() {
        return state == 'R';
    }
    
    /**
     * Check if the cell is clean
     * @return true if state is 'L'
     */
    public boolean isClean() {
        return state == 'L';
    }
    
    /**
     * Check if cell can be traversed (not an obstacle)
     * @return true if cell is not O or T
     */
    public boolean isTraversable() {
        return state != 'O' && state != 'T';
    }
    
    /**
     * Clean the cell - changes dirty cell to clean
     */
    public void clean() {
        if (isDirty()) {
            this.state = 'L';
            this.cleaned = true;
        }
    }
    
    @Override
    public String toString() {
        return String.valueOf(state);
    }
}