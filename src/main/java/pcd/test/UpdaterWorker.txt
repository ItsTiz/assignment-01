package pcd.ass01;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UpdaterWorker implements Runnable {

    private final String name;
    private CountDownLatch doneSignal;
    private List<Boid> boidsSubset;
    private final BoidsModel model;
    public UpdaterWorker(String name, CountDownLatch doneSignal, List<Boid> boidsSubset, BoidsModel model) {
        this.name = name;
        this.doneSignal = doneSignal;
        this.boidsSubset = boidsSubset;
        this.model = model;
    }

    public void setDoneSignal(CountDownLatch doneSignal) {
        this.doneSignal = doneSignal;
    }

    public void setBoidsSubset(List<Boid> boidsSubset) {
        this.boidsSubset = boidsSubset;
    }

    @Override
    public void run() {
        while(true) {
            for (Boid boid : boidsSubset) {
                boid.updateVelocity(model);
            }
            doneSignal.countDown();
        }
    }
}