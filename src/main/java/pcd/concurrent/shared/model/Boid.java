package pcd.concurrent.shared.model;

import java.util.List;

public class Boid {

    private P2d pos;
    private V2d vel;
    private String currentHandler;

    public String getCurrentHandler() {
        return currentHandler;
    }

    public void setCurrentHandler(String currentHandler) {
        this.currentHandler = currentHandler;
    }

    public Boid(P2d pos, V2d vel) {
    	this.pos = pos;
    	this.vel = vel;
    }

    public P2d getPos() {
    	return pos;
    }

    public V2d getVel() {
    	return vel;
    }

    public void setPos(P2d pos) {
        this.pos = pos;
    }

    public void setVel(V2d vel) {
        this.vel = vel;
    }

    public V2d calculateAlignment(List<Boid> nearbyBoids) {
        double avgVx = 0;
        double avgVy = 0;
        if (!nearbyBoids.isEmpty()) {
	        for (Boid other : nearbyBoids) {
	        	V2d otherVel = other.getVel();
	            avgVx += otherVel.x();
	            avgVy += otherVel.y();
	        }
	        avgVx /= nearbyBoids.size();
	        avgVy /= nearbyBoids.size();
	        return new V2d(avgVx - vel.x(), avgVy - vel.y()).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }

    public V2d calculateCohesion(List<Boid> nearbyBoids) {
        double centerX = 0;
        double centerY = 0;
        if (nearbyBoids.size() > 0) {
	        for (Boid other: nearbyBoids) {
	        	P2d otherPos = other.getPos();
	            centerX += otherPos.x();
	            centerY += otherPos.y();
	        }
            centerX /= nearbyBoids.size();
            centerY /= nearbyBoids.size();
            return new V2d(centerX - pos.x(), centerY - pos.y()).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }
    
    public V2d calculateSeparation(List<Boid> nearbyBoids, BoidsModel model) {
        double dx = 0;
        double dy = 0;
        int count = 0;
        for (Boid other: nearbyBoids) {
        	P2d otherPos = other.getPos();
    	    double distance = pos.distance(otherPos);
    	    if (distance < model.getAvoidRadius()) {
    	    	dx += pos.x() - otherPos.x();
    	    	dy += pos.y() - otherPos.y();
    	    	count++;
    	    }
    	}
        if (count > 0) {
            dx /= count;
            dy /= count;
            return new V2d(dx, dy).getNormalized();
        } else {
        	return new V2d(0, 0);
        }
    }

    public static void updateVelocity(BoidsModel model, Boid boid) {
        List<Boid> nearbyBoids = model.getNearbyBoids(boid);

        V2d separation = boid.calculateSeparation(nearbyBoids, model);
        V2d alignment = boid.calculateAlignment(nearbyBoids);
        V2d cohesion = boid.calculateCohesion(nearbyBoids);

        boid.setVel(boid.getVel().sum(alignment.mul(model.getAlignmentWeight()))
                .sum(separation.mul(model.getSeparationWeight()))
                .sum(cohesion.mul(model.getCohesionWeight())));
        double speed = boid.getVel().abs();
        if (speed > model.getMaxSpeed()) {
            boid.setVel(boid.getVel().getNormalized().mul(model.getMaxSpeed()));
        }
    }

    public static void updatePos(BoidsModel model, Boid boid) {
        boid.setPos(boid.getPos().sum(boid.getVel()));

        P2d pos = boid.getPos();
        if (pos.x() < model.getMinX()) boid.setPos(pos.sum(new V2d(model.getWidth(), 0)));
        if (pos.x() >= model.getMaxX()) boid.setPos(pos.sum(new V2d(-model.getWidth(), 0)));
        if (pos.y() < model.getMinY()) boid.setPos(pos.sum(new V2d(0, model.getHeight())));
        if (pos.y() >= model.getMaxY()) boid.setPos(pos.sum(new V2d(0, -model.getHeight())));
    }
}
