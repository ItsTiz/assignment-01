package pcd.version2.tasks;

import pcd.version2.monitors.PauseFlag;
import pcd.version2.model.Boid;
import pcd.version2.model.BoidsModel;
import pcd.version2.model.P2d;
import pcd.version2.model.V2d;
import pcd.version2.monitors.StopFlag;

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
            pauseFlag.awaitUnpause();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (stopFlag.isSet()) return null;

        boidsSubset.forEach(this::updatePos);
        return null;
    }

    public void updatePos(Boid boid) {
        boid.setPos(boid.getPos().sum(boid.getVel()));

        P2d pos = boid.getPos();

        if (pos.x() < model.getMinX()) boid.setPos(pos.sum(new V2d(model.getWidth(), 0)));
        if (pos.x() >= model.getMaxX()) boid.setPos(pos.sum(new V2d(-model.getWidth(), 0)));
        if (pos.y() < model.getMinY()) boid.setPos(pos.sum(new V2d(0, model.getHeight())));
        if (pos.y() >= model.getMaxY())boid.setPos(pos.sum(new V2d(0, -model.getHeight())));
    }
}
