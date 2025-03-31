package pcd.version1.controller;

import pcd.version1.Utils;
import pcd.version1.model.Boid;
import pcd.version1.model.BoidsModel;
import pcd.version1.monitors.PauseFlag;
import pcd.version1.monitors.StopFlag;
import pcd.version1.view.BoidsView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class BoidsSimulator implements InputListener {

    private static final int FRAMERATE = 60;

    private final BoidsModel model;
    private Optional<BoidsView> view;
    private int framerate;

    private int nWorkers;
    private List<Thread> workerThreads;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;

    public BoidsSimulator(BoidsModel model) {
        this.model = model;
        this.view = Optional.empty();
        this.workerThreads = new ArrayList<>();

        nWorkers = Runtime.getRuntime().availableProcessors() + 1;
        this.velocityBarrier = new CyclicBarrier(nWorkers + 1);
        this.positionBarrier = new CyclicBarrier(nWorkers + 1, model::updateSpatialGrid);
        this.pauseFlag = new PauseFlag();
        this.stopFlag = new StopFlag();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void setWorkerThreads(List<Thread> workerThreads) {
        this.workerThreads = workerThreads;
    }

    private List<Thread> divideWorkLoad() {
        int boidCount =  model.getBoidNumber();
        int boidsPerThread = boidCount / nWorkers;
        final List<Thread> workers = new ArrayList<>();

        for (int i = 0; i < nWorkers; i++) {
            int from = i * boidsPerThread;
            int to = (i == nWorkers - 1) ? boidCount : (i + 1) * boidsPerThread;
            List<Boid> boidSubset = model.getBoidsSubset(from, to);

            int r = new Random().nextInt(0xFFFFFF + 1);
            for (Boid boid : boidSubset) {
                boid.setColorHex(String.format("#%06X", r));
            }

            BoidWorker updater = new BoidWorker(
                    "Worker-" + (i + 1),
                    model,
                    boidSubset,
                    velocityBarrier,
                    positionBarrier,
                    pauseFlag,
                    stopFlag
            );
            Thread worker = new Thread(updater);
            worker.start();
            workers.add(worker);
        }
        return workers;
    }

    public void startSimulation() {
        model.newBoids();

        setWorkerThreads(divideWorkLoad());
        Thread mainLoopThread = new Thread(() -> {
            try {
                runSimulation();
            } catch (InterruptedException | BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        });
        mainLoopThread.start();
    }

    private void stopSimulation() {
        model.setBoidNumber(0);
        workerThreads.forEach(Thread::interrupt);
        pauseFlag.reset();
        stopFlag.reset();
        velocityBarrier.reset();
        positionBarrier.reset();
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
                    } catch (Exception ignored) {
                    }
                    framerate = FRAMERATE;
                } else {
                    framerate = (int) (1000 / dtElapsed);
                }
            }
        }
    }

    @Override
    public synchronized void started() {
        startSimulation();
    }

    @Override
    public synchronized void paused() {
        pauseFlag.set();
    }

    @Override
    public void resumed() {
        synchronized (pauseFlag) {
            pauseFlag.reset();
            pauseFlag.notifyAll();
        }
    }

    @Override
    public synchronized void stopped() {
        stopFlag.set();
        stopSimulation();
    }

    @Override
    public synchronized void boidsNumberChanged(int nBoids) {
        model.setBoidNumber(nBoids);
    }

    @Override
    public synchronized void separationWeightChanged(int sWeight) {
        model.setSeparationWeight(0.1 * sWeight);
    }

    @Override
    public synchronized void alignmentWeightChanged(int hWeight) {
        model.setAlignmentWeight(0.1 * hWeight);
    }

    @Override
    public synchronized void cohesionWeightChanged(int cWeight) {
        model.setCohesionWeight(0.1 * cWeight);

    }
}
