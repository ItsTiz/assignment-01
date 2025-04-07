package pcd.concurrent.version1.workers;

import pcd.concurrent.shared.model.P2d;
import pcd.concurrent.shared.model.V2d;
import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.monitors.StopFlag;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class BoidSublistWorker implements Runnable {

    private final String name;
    private final BoidsModel model;
    private final List<Boid> boidsSubset;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    public BoidSublistWorker(
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
                pauseFlag.awaitUnpause();

                boidsSubset.forEach(this::updateVelocity);
                velocityBarrier.await();

                boidsSubset.forEach(this::updatePos);
                positionBarrier.await();

            } catch (Exception e) {
                System.out.println("Worker " + name + " interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public void updateVelocity(Boid boid) {
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

    public void updatePos(Boid boid) {
        boid.setPos(boid.getPos().sum(boid.getVel()));

        P2d pos = boid.getPos();
        if (pos.x() < model.getMinX()) boid.setPos(pos.sum(new V2d(model.getWidth(), 0)));
        if (pos.x() >= model.getMaxX()) boid.setPos(pos.sum(new V2d(-model.getWidth(), 0)));
        if (pos.y() < model.getMinY()) boid.setPos(pos.sum(new V2d(0, model.getHeight())));
        if (pos.y() >= model.getMaxY()) boid.setPos(pos.sum(new V2d(0, -model.getHeight())));
    }

}