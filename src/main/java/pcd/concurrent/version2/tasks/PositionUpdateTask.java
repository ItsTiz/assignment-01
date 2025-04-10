package pcd.concurrent.version2.tasks;

import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.model.P2d;
import pcd.concurrent.shared.model.V2d;
import pcd.concurrent.shared.monitors.StopFlag;
import pcd.concurrent.shared.utils.Utils;

import java.util.List;
import java.util.concurrent.Callable;

public class PositionUpdateTask implements Callable<Void> {

    private final List<Boid> boidsSubset;
    private final BoidsModel model;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    public PositionUpdateTask(List<Boid> boidsSubset, BoidsModel model, PauseFlag pauseFlag, StopFlag stopFlag) {
        this.boidsSubset = boidsSubset;
        this.model = model;
        this.pauseFlag = pauseFlag;
        this.stopFlag = stopFlag;
    }

    @Override
    public Void call() {
        try {
            if (stopFlag.isSet()) return null;

            pauseFlag.awaitUnpause();

            boidsSubset.forEach(boid -> Boid.updatePos(model, boid));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Utils.log("Task interrupted.",Thread.currentThread().getName());
        }

        return null;
    }
}
