package pcd.version1.controller;

import pcd.common.ExecutionManager;
import pcd.version1.model.Boid;
import pcd.version1.model.BoidsModel;
import pcd.version1.monitors.PauseFlag;
import pcd.version1.monitors.StopFlag;
import pcd.version1.workers.BoidWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Consumer;

public class ThreadExecutionManager implements ExecutionManager {

    private final static int nWorkers = Runtime.getRuntime().availableProcessors() + 1;

    private final List<Thread> workerThreads;
    private final PauseFlag pauseFlag;
    private final StopFlag stopFlag;
    private final CyclicBarrier velocityBarrier;
    private final CyclicBarrier positionBarrier;

    private final BoidsModel model;

    public ThreadExecutionManager(BoidsModel model) {
        this.model = model;
        this.workerThreads = new ArrayList<>();
        this.velocityBarrier = new CyclicBarrier(nWorkers + 1); //+1 for main simulation loop thread
        this.positionBarrier = new CyclicBarrier(nWorkers + 1, model::updateSpatialGrid);
        this.pauseFlag = new PauseFlag();
        this.stopFlag = new StopFlag();
    }

    private void doForEachThread(Consumer<Thread> action) {
        for (Thread worker : workerThreads) {
            action.accept(worker);
        }
    }

    void resetSynchronizers(){
        stopFlag.reset();
        pauseFlag.reset();
        velocityBarrier.reset();
        positionBarrier.reset();
    }

    @Override
    public void startWorkers() {
        int boidCount = model.getBoidNumber();
        int boidsPerThread = boidCount / nWorkers;

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
            this.workerThreads.add(worker);
        }
    }

    @Override
    public void stopWorkers() {
        stopFlag.set();

        doForEachThread(Thread::interrupt);

        doForEachThread((thread) -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        workerThreads.clear();
    }

    @Override
    public void pauseWorkers() {
        pauseFlag.set();
    }

    @Override
    public void resumeWorkers() {
        synchronized (pauseFlag) {
            pauseFlag.reset();
        }
    }

    @Override
    public void awaitStepCompletion() {
        try {
            velocityBarrier.await();

            positionBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }
}
