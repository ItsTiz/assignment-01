package pcd.version1.controller;

import pcd.version1.BoidWorker;
import pcd.version1.Utils;
import pcd.version1.model.Boid;
import pcd.version1.model.BoidsModel;
import pcd.version1.monitors.PauseFlag;
import pcd.version1.view.BoidsView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BoidsSimulator {

    private static final int FRAMERATE = 60;

    private BoidsModel model;
    private Optional<BoidsView> view;
    private int framerate;

    private int nWorkers;
    private List<BoidWorker> boidsWorkers;
    private List<Thread> workerThreads;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;
    private final PauseFlag pauseFlag;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        view = Optional.empty();
        workerThreads = new ArrayList<>();

        velocityBarrier = new CyclicBarrier(nWorkers + 1);
        positionBarrier = new CyclicBarrier(nWorkers + 1, model::updateSpatialGrid);
        pauseFlag = new PauseFlag();
        pauseFlag.reset();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void setWorkerThreads(List<Thread> workerThreads) {
        this.workerThreads = workerThreads;
    }

    public void setBoidsWorkers(List<BoidWorker> boidsWorkers) {
        this.boidsWorkers = boidsWorkers;
    }

    private List<Thread> divideWorkLoad(int nWorkers, CyclicBarrier velocityBarrier, CyclicBarrier positionBarrier, PauseFlag pauseFlag) {
        int boidsPerThread = BoidsSimulation.N_BOIDS / nWorkers;
        final List<Thread> workers = new ArrayList<>();
        final List<BoidWorker> updaters = new ArrayList<>();

        for (int i = 0; i < nWorkers; i++) {
            int from = i * boidsPerThread;
            int to = (i == nWorkers - 1) ? BoidsSimulation.N_BOIDS : (i + 1) * boidsPerThread;
            List<Boid> boidSubSet = model.getBoidsSubset(from, to);

            int r = new Random().nextInt(0xFFFFFF + 1);
            for (Boid boid : boidSubSet) {
                boid.setColorHex(String.format("#%06X", r));
                Utils.log(boid.getColorHex(), "test");
            }

            BoidWorker updater = new BoidWorker(
                    "Worker-" + (i + 1),
                    model,
                    boidSubSet,
                    velocityBarrier,
                    positionBarrier,
                    pauseFlag
            );
            Thread worker = new Thread(updater);
            worker.start();
            workers.add(worker);
            updaters.add(updater);
        }
        setBoidsWorkers(updaters);
        return workers;
    }

    public void startSimulation() throws BrokenBarrierException, InterruptedException {
        nWorkers = Runtime.getRuntime().availableProcessors() + 1;
        Utils.log("" + nWorkers, Thread.currentThread().getName());
        setWorkerThreads(divideWorkLoad(nWorkers, velocityBarrier, positionBarrier, pauseFlag));
        runSimulation();
    }


    public void runSimulation() throws InterruptedException, BrokenBarrierException {
        while (true) {
            var t0 = System.currentTimeMillis();

            velocityBarrier.await();

            positionBarrier.await();

            if (view.isPresent()) {
                view.get().update(framerate);
                var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var frameratePeriod = 1000 / FRAMERATE;

                if (dtElapsed < frameratePeriod) {
                    try {
                        Thread.sleep(frameratePeriod - dtElapsed);
                    } catch (Exception ex) {
                    }
                    framerate = FRAMERATE;
                } else {
                    framerate = (int) (1000 / dtElapsed);
                }
            }
        }
    }

    public void pauseSimulation() {

    }

    public void resumeSimulation() {

    }

    public void stopSimulation() {


    }
}
