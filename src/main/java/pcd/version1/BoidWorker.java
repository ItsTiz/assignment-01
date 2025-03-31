package pcd.version1;

import pcd.version1.model.Boid;
import pcd.version1.model.BoidsModel;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class BoidWorker implements Runnable {

    private final String name;
    private final BoidsModel model;
    private List<Boid> boidsSubset;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;

    public BoidWorker(String name, BoidsModel model, List<Boid> boidsSubset, CyclicBarrier velocityBarrier, CyclicBarrier positionBarrier) {
        this.name = name;
        this.model = model;
        this.boidsSubset = boidsSubset;
        this.velocityBarrier = velocityBarrier;
        this.positionBarrier = positionBarrier;
    }

    public void setBoidsSubset(List<Boid> boidsSubset) {
        this.boidsSubset = boidsSubset;
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (Boid boid : boidsSubset) {
                    updateVelocity(boid);
                }

                velocityBarrier.await();

                /*
                 * ..then update positions
                 */

                for (Boid boid : boidsSubset) {
                    updatePos(boid);
                }

                positionBarrier.await();

            } catch (Exception e) {
                System.err.println("Error in worker " + name + ": " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void updateVelocity(Boid boid) {

        /* change velocity vector according to separation, alignment, cohesion */

        List<Boid> nearbyBoids = model.getNearbyBoids(boid);

        V2d separation = boid.calculateSeparation(nearbyBoids, model);
        V2d alignment = boid.calculateAlignment(nearbyBoids);
        V2d cohesion = boid.calculateCohesion(nearbyBoids);

        boid.setVel(boid.getVel().sum(alignment.mul(model.getAlignmentWeight()))
                .sum(separation.mul(model.getSeparationWeight()))
                .sum(cohesion.mul(model.getCohesionWeight())));

        /* Limit speed to MAX_SPEED */

        double speed = boid.getVel().abs();

        if (speed > model.getMaxSpeed()) {
            boid.setVel(boid.getVel().getNormalized().mul(model.getMaxSpeed()));
        }
    }

    public void updatePos(Boid boid) {

        /* Update position */

        boid.setPos(boid.getPos().sum(boid.getVel()));

        /* environment wrap-around */

        P2d pos = boid.getPos();

        if (pos.x() < model.getMinX()) pos = pos.sum(new V2d(model.getWidth(), 0));
        if (pos.x() >= model.getMaxX()) pos = pos.sum(new V2d(-model.getWidth(), 0));
        if (pos.y() < model.getMinY()) pos = pos.sum(new V2d(0, model.getHeight()));
        if (pos.y() >= model.getMaxY()) pos = pos.sum(new V2d(0, -model.getHeight()));
    }

}