package pcd.version1.model;

import pcd.version1.P2d;
import pcd.version1.V2d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BoidsModel {
    
    private final List<Boid> boids;
    private final SpatialGrid spatialGrid;
    private double separationWeight; 
    private double alignmentWeight; 
    private double cohesionWeight; 
    private final double width;
    private final double height;
    private final double maxSpeed;
    private final double perceptionRadius;
    private final double avoidRadius;

    public BoidsModel(int nboids,
    						double initialSeparationWeight,
    						double initialAlignmentWeight,
    						double initialCohesionWeight,
    						double width,
    						double height,
    						double maxSpeed,
    						double perceptionRadius,
    						double avoidRadius){
        separationWeight = initialSeparationWeight;
        alignmentWeight = initialAlignmentWeight;
        cohesionWeight = initialCohesionWeight;
        this.width = width;
        this.height = height;
        this.maxSpeed = maxSpeed;
        this.perceptionRadius = perceptionRadius;
        this.avoidRadius = avoidRadius;

        boids = generateRandomBoids(nboids, width, height, maxSpeed);

        this.spatialGrid = new SpatialGrid(perceptionRadius);
        spatialGrid.updateGrid(boids);
    }

    private List<Boid> generateRandomBoids(int nboids, double width, double height, double maxSpeed) {
        final List<Boid> boids;
        boids = new ArrayList<>();
        for (int i = 0; i < nboids; i++) {
        	P2d pos = new P2d(-width /2 + Math.random() * width, -height /2 + Math.random() * height);
        	V2d vel = new V2d(Math.random() * maxSpeed /2 - maxSpeed /4, Math.random() * maxSpeed /2 - maxSpeed /4);
        	boids.add(new Boid(pos, vel));
        }
        return boids;
    }

    public List<Boid> getBoids(){
    	return Collections.unmodifiableList(boids);
    }

    public List<Boid> getBoidsSubset(int from, int to){

        return boids.subList(from, to);
    }

    public List<Boid> getNearbyBoids(Boid boid) {
        return spatialGrid.getNearbyBoids(boid, getPerceptionRadius());
    }

    public void updateSpatialGrid() {
        spatialGrid.updateGrid(getBoids());
    }

    public synchronized double getMinX() {
    	return -width/2;
    }

    public synchronized double getMaxX() {
    	return width/2;
    }

    public synchronized double getMinY() {
    	return -height/2;
    }

    public synchronized double getMaxY() {
    	return height/2;
    }
    
    public synchronized double getWidth() {
    	return width;
    }
 
    public synchronized double getHeight() {
    	return height;
    }

    public synchronized void setSeparationWeight(double value) {
    	this.separationWeight = value;
    }

    public synchronized void setAlignmentWeight(double value) {
    	this.alignmentWeight = value;
    }

    public synchronized void setCohesionWeight(double value) {
    	this.cohesionWeight = value;
    }

    public synchronized double getSeparationWeight() {
    	return separationWeight;
    }

    public synchronized double getCohesionWeight() {
    	return cohesionWeight;
    }

    public synchronized double getAlignmentWeight() {
    	return alignmentWeight;
    }
    
    public synchronized double getMaxSpeed() {
    	return maxSpeed;
    }

    public synchronized double getAvoidRadius() {
    	return avoidRadius;
    }

    public synchronized double getPerceptionRadius() {
    	return perceptionRadius;
    }
}
