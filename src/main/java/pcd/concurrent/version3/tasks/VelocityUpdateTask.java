package pcd.concurrent.version3.tasks;

import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.monitors.StopFlag;
import pcd.concurrent.shared.utils.Utils;

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

    @Override
    public void run() {
        try {
            pauseFlag.awaitUnpause();

            if (stopFlag.isSet()) return;

            Boid.updateVelocity(model, boid);

        } catch (InterruptedException e) {
            Utils.log("Velocity task interrupted.", Thread.currentThread().getName());
            Thread.currentThread().interrupt();
        }
    }
}
