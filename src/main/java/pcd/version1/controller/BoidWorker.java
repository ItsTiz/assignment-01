package pcd.version1.controller;

import pcd.version1.P2d;
import pcd.version1.V2d;
import pcd.version1.model.Boid;
import pcd.version1.model.BoidsModel;
import pcd.version1.monitors.PauseFlag;
import pcd.version1.monitors.StopFlag;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class BoidWorker implements Runnable {

    private final String name;
    private final BoidsModel model;
    private final List<Boid> boidsSubset;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    public BoidWorker(
            String name,
            BoidsModel model,
            List<Boid> boidsSubset,
            CyclicBarrier velocityBarrier,
            CyclicBarrier positionBarrier,
            PauseFlag pauseFlag,
            StopFlag stopFlag
    ) {
        this.name = name;
        this.model = model;
        this.boidsSubset = boidsSubset;
        this.velocityBarrier = velocityBarrier;
        this.positionBarrier = positionBarrier;
        this.pauseFlag = pauseFlag;
        this.stopFlag = stopFlag;
    }

    @Override
    public void run() {
        while (!stopFlag.isSet()) {
            try {
                synchronized (pauseFlag) {
                    while (pauseFlag.isSet()) {
                        try {
                            pauseFlag.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                boidsSubset.forEach(this::updateVelocity);
                velocityBarrier.await();

                boidsSubset.forEach(this::updatePos);
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