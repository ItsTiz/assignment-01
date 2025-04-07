package pcd.version1.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoidsModel {

    private final List<Boid> boids;
    private final SpatialGrid spatialGrid;
    private int boidNumber;
    private double separationWeight;
    private double alignmentWeight;
    private double cohesionWeight;
    private final double width;
    private final double height;
    private final double maxSpeed;
    private final double perceptionRadius;
    private final double avoidRadius;

    public BoidsModel(
            int boidNumber,
            double initialSeparationWeight,
            double initialAlignmentWeight,
            double initialCohesionWeight,
            double width,
            double height,
            double maxSpeed,
            double perceptionRadius,
            double avoidRadius) {
        this.boidNumber = boidNumber;
        this.boids = new ArrayList<>();
        this.separationWeight = initialSeparationWeight;
        this.alignmentWeight = initialAlignmentWeight;
        this.cohesionWeight = initialCohesionWeight;
        this.width = width;
        this.height = height;
        this.maxSpeed = maxSpeed;
        this.perceptionRadius = perceptionRadius;
        this.avoidRadius = avoidRadius;
        this.spatialGrid = new SpatialGrid(perceptionRadius);
    }

    public int getBoidNumber() {
        return boidNumber;
    }

    public void setBoidNumber(int boidNumber) {
        this.boidNumber = boidNumber;
    }

    private List<Boid> generateRandomBoids(int nboids) {
        final List<Boid> boids;
        boids = new ArrayList<>();
        for (int i = 0; i < nboids; i++) {
            P2d pos = new P2d(-width / 2 + Math.random() * width, -height / 2 + Math.random() * height);
            V2d vel = new V2d(Math.random() * maxSpeed / 2 - maxSpeed / 4, Math.random() * maxSpeed / 2 - maxSpeed / 4);
            boids.add(new Boid(pos, vel));
        }
        return boids;
    }

    public void newBoids() {
        boids.clear();
        boids.addAll(generateRandomBoids(boidNumber));
        spatialGrid.updateGrid(boids);
    }

    public List<Boid> getBoids() {
        return Collections.unmodifiableList(boids);
    }

    public List<Boid> getBoidsSubset(int from, int to) {

        return boids.subList(from, to);
    }

    public List<Boid> getNearbyBoids(Boid boid) {
        return spatialGrid.getNearbyBoids(boid, getPerceptionRadius());
    }

    public void updateSpatialGrid() {
        spatialGrid.updateGrid(getBoids());
    }

    public double getMinX() {
        return -width / 2;
    }

    public double getMaxX() {
        return width / 2;
    }

    public double getMinY() {
        return -height / 2;
    }

    public double getMaxY() {
        return height / 2;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public void setSeparationWeight(double value) {
        this.separationWeight = value;
    }

    public void setAlignmentWeight(double value) {
        this.alignmentWeight = value;
    }

    public void setCohesionWeight(double value) {
        this.cohesionWeight = value;
    }

    public double getSeparationWeight() {
        return separationWeight;
    }

    public double getCohesionWeight() {
        return cohesionWeight;
    }

    public double getAlignmentWeight() {
        return alignmentWeight;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getAvoidRadius() {
        return avoidRadius;
    }

    public double getPerceptionRadius() {
        return perceptionRadius;
    }
}
