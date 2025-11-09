package data.model;

/**
 * Represents a cleaning robot
 */
public class Robot {

    private int id;
    private int x;
    private int y;
    private int battery;
    private boolean isActive;
    private int movementCount;
    private int cellsCleaned;
    private static final int INITIAL_BATTERY = 10;
    private static final int MAX_BATTERY = 20;
    private static final int BATTERY_CONSUMPTION = 1;
    private static final int RECHARGE_AMOUNT = 10;
    private boolean isRecharging;

    /**
     * Constructor with position
     *
     * @param id Robot identifier
     * @param x Initial x position
     * @param y Initial y position
     */
    public Robot(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.battery = INITIAL_BATTERY;
        this.isActive = true;
        this.movementCount = 0;
        this.cellsCleaned = 0;
    }

    /**
     * Constructor with default position (will be set later)
     *
     * @param id Robot identifier
     */
    public Robot(int id) {
        this(id, 0, 0);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = Math.min(battery, MAX_BATTERY); // Cap at max
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getMovementCount() {
        return movementCount;
    }

    public int getCellsCleaned() {
        return cellsCleaned;
    }

    public void incrementCellsCleaned() {
        this.cellsCleaned++;
    }

    /**
     * Move robot to new position
     *
     * @param newX New x position
     * @param newY New y position
     */
    public void moveTo(int newX, int newY) {
        this.x = newX;
        this.y = newY;
        this.movementCount++;
    }

    /**
     * Consume battery for one movement Deactivates robot if battery reaches 0
     */
    public void consumeBattery() {
        this.battery -= BATTERY_CONSUMPTION;
        if (this.battery <= 0) {
            this.battery = 0;
            this.isActive = false;
        }
    }

    /**
     * Recharge the robot battery Adds RECHARGE_AMOUNT to current battery
     * (cumulative, max 20)
     */
    public void recharge() {
        this.battery = Math.min(this.battery + RECHARGE_AMOUNT, MAX_BATTERY);
    }

    /**
     * Check if battery is low (less than 3)
     *
     * @return true if battery is low
     */
    public boolean isLowBattery() {
        return this.battery < 3;
    }

    /**
     * Check if robot has enough battery to reach a distance
     *
     * @param distance Distance to check
     * @return true if has enough battery
     */
    public boolean hasEnergyToReach(int distance) {
        return this.battery >= distance;
    }

    /**
     * Deactivate the robot
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Get robot status as string
     *
     * @return Status string
     */
    public String getStatus() {
        if (!isActive) {
            return "INACTIVE";
        } else if (isLowBattery()) {
            return "LOW_BATTERY";
        } else {
            return "ACTIVE";
        }
    }

    public boolean isRecharging() {
        return isRecharging;
    }

    public void setRecharging(boolean recharging) {
        isRecharging = recharging;
    }

    @Override
    public String toString() {
        return "Robot #" + id + " [(" + x + "," + y + ") Battery:" + battery
                + " Status:" + getStatus() + " Cleaned:" + cellsCleaned + "]";
    }
}
