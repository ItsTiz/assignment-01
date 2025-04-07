package pcd.version2.tasks;

import pcd.version2.monitors.PauseFlag;
import pcd.version2.monitors.StopFlag;
import pcd.version2.model.Boid;
import pcd.version2.model.BoidsModel;
import pcd.version2.model.V2d;

import java.util.List;
import java.util.concurrent.Callable;

public class VelocityUpdateTask implements Callable<Void> {

    private final List<Boid> boidsSubset;
    private final BoidsModel model;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    public VelocityUpdateTask(List<Boid> boidsSubset, BoidsModel model, PauseFlag pauseFlag, StopFlag stopFlag) {
        this.boidsSubset = boidsSubset;
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
    public Void call() {
        try {
            pauseFlag.awaitUnpause();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (stopFlag.isSet()) return null;

        boidsSubset.forEach(this::updateVelocity);
        return null;
    }
}
