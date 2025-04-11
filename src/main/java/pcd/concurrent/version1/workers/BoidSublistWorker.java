package pcd.concurrent.version1.workers;

import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.monitors.StopFlag;
import pcd.concurrent.shared.monitors.CyclicBarrierImpl;

import java.util.List;

public class BoidSublistWorker implements Runnable {

    private final String name;
    private final BoidsModel model;
    private final List<Boid> boidsSubset;
    private final CyclicBarrierImpl velocityBarrier;
    private final CyclicBarrierImpl positionBarrier;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    public BoidSublistWorker(
            String name,
            BoidsModel model,
            List<Boid> boidsSubset,
            CyclicBarrierImpl velocityBarrier,
            CyclicBarrierImpl positionBarrier,
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

                boidsSubset.forEach(boid -> Boid.updateVelocity(model, boid));
                velocityBarrier.await();

                boidsSubset.forEach(boid -> Boid.updatePos(model, boid));
                positionBarrier.await();

            } catch (Exception e) {
                System.out.println("Worker " + name + " has been interrupted.");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}