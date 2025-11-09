package service;

import data.model.Cell;
import data.model.Robot;
import data.model.Room;
import data.model.Room.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for robot movement and decision making Optimized for exploration
 * without loops
 */
public class RobotService {

    public boolean canMoveTo(Room room, int x, int y) {
        if (!room.isValidPosition(x, y)) {
            return false;
        }

        Cell cell = room.getCell(x, y);
        if (!cell.isTraversable()) {
            return false;
        }

        if (room.isCellOccupiedByRobot(x, y)) {
            return false;
        }

        return true;
    }

    public boolean moveRobot(Room room, Robot robot, int newX, int newY) {
        if (!canMoveTo(room, newX, newY)) {
            return false;
        }

        robot.moveTo(newX, newY);
        robot.consumeBattery();

        return true;
    }

    public boolean cleanCell(Room room, Robot robot) {
        Cell cell = room.getCell(robot.getX(), robot.getY());

        if (cell != null && cell.isDirty()) {
            cell.clean();
            robot.incrementCellsCleaned();
            return true;
        }

        return false;
    }

    public List<Point> getAdjacentCells(Room room, Robot robot) {
        List<Point> adjacent = new ArrayList<>();
        int x = robot.getX();
        int y = robot.getY();

        if (room.isValidPosition(x - 1, y)) {
            adjacent.add(new Point(x - 1, y));
        }
        if (room.isValidPosition(x + 1, y)) {
            adjacent.add(new Point(x + 1, y));
        }
        if (room.isValidPosition(x, y - 1)) {
            adjacent.add(new Point(x, y - 1));
        }
        if (room.isValidPosition(x, y + 1)) {
            adjacent.add(new Point(x, y + 1));
        }

        return adjacent;
    }

    public int calculateDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public Point getClosestDirtyCell(Room room, Robot robot) {
        Point closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                Cell cell = room.getCell(i, j);
                if (cell.isDirty() && !room.isCellOccupiedByRobot(i, j)) {
                    int distance = calculateDistance(robot.getX(), robot.getY(), i, j);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closest = new Point(i, j);
                    }
                }
            }
        }

        return closest;
    }

    public List<Point> getRechargePoints(Room room) {
        List<Point> rechargePoints = new ArrayList<>();

        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                Cell cell = room.getCell(i, j);
                if (cell.isRechargePoint()) {
                    rechargePoints.add(new Point(i, j));
                }
            }
        }

        return rechargePoints;
    }

    public Point getClosestRechargePoint(Room room, Robot robot) {
        List<Point> rechargePoints = getRechargePoints(room);

        if (rechargePoints.isEmpty()) {
            return null;
        }

        Point closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Point point : rechargePoints) {
            int distance = calculateDistance(robot.getX(), robot.getY(), point.x, point.y);
            if (distance < minDistance) {
                minDistance = distance;
                closest = point;
            }
        }

        return closest;
    }

    public boolean isOnRechargePoint(Room room, Robot robot) {
        Cell cell = room.getCell(robot.getX(), robot.getY());
        return cell != null && cell.isRechargePoint();
    }

    /**
     * Optimized recharge logic Only returns true when battery is critically low
     * or cannot reach recharge safely
     */
    // En RobotService.java
    public boolean needsRecharge(Room room, Robot robot) {
        // Batería crítica - debe recargar AHORA
        if (robot.getBattery() <= 5) {
            return true;
        }

        // No recargar si la batería está bien (18+)
        if (robot.getBattery() >= 18) {
            return false;
        }

        // Batería media (6-17): verificar si puede llegar al punto de recarga
        Point closestRecharge = getClosestRechargePoint(room, robot);
        if (closestRecharge == null) {
            return false;
        }

        int distanceToRecharge = calculateDistance(robot.getX(), robot.getY(),
                closestRecharge.x, closestRecharge.y);

        // Necesita recargar si no puede hacer el viaje de regreso
        // Estimación conservadora: distancia Manhattan * 2 + margen de seguridad
        return robot.getBattery() <= (distanceToRecharge * 2 + 3);
    }

    /**
     * Find best target avoiding other robots and prioritizing exploration
     */
    public Point findBestTargetAvoidingOthers(Room room, Robot robot, int avoidRadius) {
        List<Point> dirtyCells = getAllDirtyCells(room);

        if (dirtyCells.isEmpty()) {
            return null;
        }

        Point bestTarget = null;
        int bestScore = Integer.MIN_VALUE;

        for (Point dirtyCell : dirtyCells) {
            boolean tooCloseToOtherRobot = false;

            for (Robot other : room.getRobots()) {
                if (other.getId() != robot.getId() && other.isActive()) {
                    int distToOther = calculateDistance(dirtyCell.x, dirtyCell.y,
                            other.getX(), other.getY());

                    if (distToOther <= avoidRadius) {
                        if (other.getBattery() >= robot.getBattery()) {
                            tooCloseToOtherRobot = true;
                            break;
                        }
                    }
                }
            }

            if (tooCloseToOtherRobot) {
                continue;
            }

            int distanceToRobot = calculateDistance(robot.getX(), robot.getY(),
                    dirtyCell.x, dirtyCell.y);

            int score = -distanceToRobot;

            // Bonus for being far from other robots
            int minDistToOther = Integer.MAX_VALUE;
            for (Robot other : room.getRobots()) {
                if (other.getId() != robot.getId() && other.isActive()) {
                    int dist = calculateDistance(dirtyCell.x, dirtyCell.y,
                            other.getX(), other.getY());
                    minDistToOther = Math.min(minDistToOther, dist);
                }
            }

            if (minDistToOther != Integer.MAX_VALUE) {
                score += minDistToOther * 2;
            }

            if (score > bestScore) {
                bestScore = score;
                bestTarget = dirtyCell;
            }
        }

        return bestTarget != null ? bestTarget : getClosestDirtyCell(room, robot);
    }

    public List<Point> getAllDirtyCells(Room room) {
        List<Point> dirtyCells = new ArrayList<>();

        for (int i = 0; i < room.getRows(); i++) {
            for (int j = 0; j < room.getCols(); j++) {
                Cell cell = room.getCell(i, j);
                if (cell.isDirty()) {
                    dirtyCells.add(new Point(i, j));
                }
            }
        }

        return dirtyCells;
    }
}
