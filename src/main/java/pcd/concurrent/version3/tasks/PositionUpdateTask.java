package pcd.concurrent.version3.tasks;

import pcd.concurrent.shared.model.Boid;
import pcd.concurrent.shared.model.BoidsModel;
import pcd.concurrent.shared.model.P2d;
import pcd.concurrent.shared.model.V2d;
import pcd.concurrent.shared.monitors.PauseFlag;
import pcd.concurrent.shared.monitors.StopFlag;


public class PositionUpdateTask implements Runnable {

    private final Boid boid;
    private final BoidsModel model;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    public PositionUpdateTask(Boid boid, BoidsModel model, PauseFlag pauseFlag, StopFlag stopFlag) {
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

            updatePos(boid);
        } catch (InterruptedException e) {
            System.out.println("Task interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }

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
