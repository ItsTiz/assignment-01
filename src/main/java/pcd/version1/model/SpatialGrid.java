package pcd.version1.model;

import pcd.version1.P2d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

class SpatialGrid {
    private final Map<GridCell, List<Boid>> cells = new ConcurrentHashMap<>();
    private final double cellSize;

    public SpatialGrid(double cellSize) {
        this.cellSize = cellSize;
    }

    public GridCell getCellForPosition(P2d position) {
        int cellX = (int) Math.floor(position.x() / cellSize);
        int cellY = (int) Math.floor(position.y() / cellSize);
        return new GridCell(cellX, cellY);
    }

    public void updateGrid(List<Boid> boids) {
        Map<GridCell, List<Boid>> newCells = new ConcurrentHashMap<>();
        for (Boid boid : boids) {
            GridCell cell = getCellForPosition(boid.getPos());
            newCells.computeIfAbsent(cell, k -> new CopyOnWriteArrayList<>()).add(boid);
        }
        cells.clear();
        cells.putAll(newCells);
    }

    public List<Boid> getNearbyBoids(Boid boid, double radius) {
        P2d pos = boid.getPos();
        GridCell cell = getCellForPosition(pos);
        int cellRadius = (int) Math.ceil(radius / cellSize);

        List<Boid> nearbyBoids = null;

        for (int dx = -cellRadius; dx <= cellRadius; dx++) {
            for (int dy = -cellRadius; dy <= cellRadius; dy++) {
                GridCell neighborCell = new GridCell(cell.x() + dx, cell.y() + dy);
                List<Boid> boidsInCell = cells.get(neighborCell);

                if (boidsInCell != null) {
                    for (Boid other : boidsInCell) {
                        if (other != boid && pos.distance(other.getPos()) < radius) {
                            if (nearbyBoids == null) {
                                nearbyBoids = new ArrayList<>();
                            }
                            nearbyBoids.add(other);
                        }
                    }
                }
            }
        }

        return (nearbyBoids == null) ? Collections.emptyList() : nearbyBoids;
    }
}

