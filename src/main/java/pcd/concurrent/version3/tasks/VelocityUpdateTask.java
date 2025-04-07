package pcd.concurrent.version3.tasks;

import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.model.V2d;
import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.monitors.StopFlag;

import java.util.List;
import java.util.concurrent.Callable;

public class VelocityUpdateTask implements Runnable {

    private final Boid boid;
    private final BoidsModel model;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    public VelocityUpdateTask(Boid boid, BoidsModel model, PauseFlag pauseFlag, StopFlag stopFlag) {
        this.boid = boid;
        this.model = model;
        this.pauseFlag = pauseFlag;
        this.stopFlag = stopFlag;
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

    @Override
    public void run() {
        try {
            pauseFlag.awaitUnpause();

            if (stopFlag.isSet()) return;

            updateVelocity(boid);

        } catch (InterruptedException e) {
            System.out.println("Task interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
