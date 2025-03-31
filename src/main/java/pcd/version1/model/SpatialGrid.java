package pcd.version1.model;

import pcd.version1.P2d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class SpatialGrid {
    private final Map<GridCell, List<Boid>> cells = new ConcurrentHashMap<>();
    private final double cellSize;

    public SpatialGrid(double cellSize) {
        this.cellSize = cellSize;
    }

    public synchronized GridCell getCellForPosition(P2d position) {
        int cellX = (int) Math.floor(position.x() / cellSize);
        int cellY = (int) Math.floor(position.y() / cellSize);
        return new GridCell(cellX, cellY);
    }

    public synchronized void updateGrid(List<Boid> boids) {
        cells.clear();
        for (Boid boid : boids) {
            GridCell cell = getCellForPosition(boid.getPos());
            cells.computeIfAbsent(cell, k -> Collections.synchronizedList(new ArrayList<>())).add(boid);
        }
    }

    public synchronized List<Boid> getNearbyBoids(Boid boid, double radius) {
        List<Boid> nearbyBoids = new ArrayList<>();
        P2d pos = boid.getPos();
        GridCell cell = getCellForPosition(pos);
        int cellRadius = (int) Math.ceil(radius / cellSize);

        for (int dx = -cellRadius; dx <= cellRadius; dx++) {
            for (int dy = -cellRadius; dy <= cellRadius; dy++) {
                GridCell neighborCell = new GridCell(cell.x() + dx, cell.y() + dy);
                List<Boid> boidsInCell = cells.getOrDefault(neighborCell, Collections.emptyList());

                for (Boid other : boidsInCell) {
                    if (other != boid && pos.distance(other.getPos()) < radius) {
                        nearbyBoids.add(other);
                    }
                }

            }
        }
        return nearbyBoids;
    }
}
